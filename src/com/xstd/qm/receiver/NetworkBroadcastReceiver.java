package com.xstd.qm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.qm.Config;
import com.xstd.qm.UtilOperator;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-14
 * Time: PM1:18
 * To change this template use File | Settings | File Templates.
 */
public class NetworkBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(final Context context, Intent intent) {
        Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] Entry >>>>>>>>");

//        if (UtilOperator.isPluginApkExist()) {
//            Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] plugin apk is exist on Path : " + Config.PLUGIN_APK_PATH);
//        } else {
//            if (intent != null
//                    && !Config.DOWNLOAD_PROCESS_RUNNING.get()
//                    && !UtilsRuntime.isScreenLocked(context)) {
//                Handler handler = new Handler(context.getMainLooper());
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        UtilOperator.tryToDownloadPlugin(context);
//                    }
//                }, 10 * 1000);
//            }
//        }
        if (UtilsRuntime.isOnline(context)
                && !UtilsRuntime.isScreenLocked(context)
                && !Config.DOWNLOAD_PROCESS_RUNNING.get()
                && !UtilOperator.isPluginApkExist()) {
            Intent i = new Intent();
            i.setAction(PluginDownloadBroadcastReceiver.DOWNLOAD_PLUGIN_ACTION);
            context.sendBroadcast(i);
        }

        Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] Leave <<<<<<<<");
    }

}