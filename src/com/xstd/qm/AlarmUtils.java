package com.xstd.qm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by michael on 14-3-3.
 */
public class AlarmUtils {

    public static final String ACTION_CLOSE_SCREEN = "com.xstd.screen.close";

    public static void startAlarmForAction(Context context, String action, long delay) {
        cancelAlarmForAction(context, action);
        if (Config.DEBUG) {
            Config.LOGD("[[AlarmUtils::startAlarmForAction]] start for action : " + action + " delay time : " + delay);
        }
        Intent intent = new Intent();
        intent.setAction(action);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        long cur = System.currentTimeMillis();
        long firstTime = cur + delay;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, firstTime, sender);
    }

    public static void cancelAlarmForAction(Context context, String action) {
        if (Config.DEBUG) {
            Config.LOGD("[[DemonService::cancelAlarmForAction]] cancel for action : " + action);
        }
        Intent intent = new Intent();
        intent.setAction(action);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }

}
