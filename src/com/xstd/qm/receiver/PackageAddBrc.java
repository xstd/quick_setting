package com.xstd.qm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.plugin.common.utils.SingleInstanceBase;
import com.xstd.qm.AppRuntime;
import com.xstd.qm.Config;
import com.xstd.qm.DemonService;
import com.xstd.qm.PLuginManager;

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
            Config.LOGD("<<PackageAddBrc::onReceive>> package name for ADD is : " + packageName);

            if (SingleInstanceBase.getInstance(PLuginManager.class).scanPluginInstalled()) {
                AppRuntime.PLUGIN_INSTALLED = true;
            }
        }
    }

}
