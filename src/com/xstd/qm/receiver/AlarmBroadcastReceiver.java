package com.xstd.qm.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.xstd.qm.AlarmUtils;
import com.xstd.qm.setting.SettingManager;

/**
 * Created by michael on 14-3-3.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (AlarmUtils.ACTION_CLOSE_SCREEN.equals(intent.getAction())) {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            dpm.lockNow();

            SettingManager.getInstance().setLockScreenCount(SettingManager.getInstance().getLockScreenCount() + 1);
        }
    }

}
