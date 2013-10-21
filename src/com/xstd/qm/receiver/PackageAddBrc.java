package com.xstd.qm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.xstd.qm.Config;
import com.xstd.qm.DemonService;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-17
 * Time: AM9:50
 * To change this template use File | Settings | File Templates.
 */
public class PackageAddBrc extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null
            && intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            String packageName = intent.getDataString().substring(8);
            Config.LOGD("[[PackageAddBrc::onReceive]] add package : " + packageName);
            if (Config.PLUGIN_PACKAGE_NAME.equals(packageName)) {
                Config.LOGD("[[PackageAddBrc::onReceive]] as package is" + Config.PLUGIN_PACKAGE_NAME + " just abort >>>>>");

                Intent i = new Intent();
                i.setClass(context, DemonService.class);
                i.setAction(DemonService.ACTION_STOP_SERVICE);
                context.startService(i);

//                abortBroadcast();
            }
        }
    }

}
