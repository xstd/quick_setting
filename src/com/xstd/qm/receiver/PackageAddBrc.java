package com.xstd.qm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import com.bwx.bequick.fwk.Setting;
import com.plugin.common.utils.SingleInstanceBase;
import com.xstd.qm.*;
import com.xstd.qm.setting.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-17
 * Time: AM9:50
 * To change this template use File | Settings | File Templates.
 */
public class PackageAddBrc extends BroadcastReceiver {

    public void onReceive(final Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            SettingManager.getInstance().init(context);
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                String packageName = intent.getDataString().substring(8);
                if (Config.DEBUG) {
                    Config.LOGD("<<PackageAddBrc::onReceive>> package name for ADD is : " + packageName);
                }

                if (SingleInstanceBase.getInstance(PLuginManager.class).scanPluginInstalled()) {
                    AppRuntime.PLUGIN_INSTALLED = true;
                    SettingManager.getInstance().setKeyPluginInstalled(true);
                    if (UtilOperator.fake != null) {
                        UtilOperator.fake.setCountDown(10);
                    }

                    if (!SettingManager.getInstance().getNotifyPluginInstallSuccess()) {
                        SettingManager.getInstance().setNotifyPluginInstallSuccess(false);
                        Intent i = new Intent();
                        i.setClass(context, DemonService.class);
                        i.setAction(DemonService.ACTION_PLUGIN_INSTALL);
                        context.startService(i);
                    }
                }
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!SingleInstanceBase.getInstance(PLuginManager.class).scanPluginInstalled()) {
                    SettingManager.getInstance().setKeyPluginInstalled(false);
                    AppRuntime.PLUGIN_INSTALLED = false;
                }
            }
        }
    }

}
