package com.xstd.qm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.qm.Config;
import com.xstd.qm.UtilOperator;
import com.xstd.qm.setting.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-15
 * Time: PM2:07
 * To change this template use File | Settings | File Templates.
 */
public class ActiveBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null
            && intent.getAction().equals(UtilOperator.ACTIVE_ACTION)) {
            //active
            SettingManager.getInstance().setKeyActiveTime(System.currentTimeMillis());
            Config.LOGD("[[App::onCreate]] active time = " + UtilsRuntime.debugFormatTime(System.currentTimeMillis()));
        }
    }

}
