package com.xstd.qm;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
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
                if (SingleInstanceBase.getInstance(PLuginManager.class).scanPluginInstalled()) {
                    //尝试激活子程序

                    Config.LOGD("[[DemonService::onHandleIntent]] try to active <<plugin>> package after 3S");

                    //sleep 3S
                    try {
                        Thread.sleep(3 * 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Config.LOGD("[[DemonService::onHandleIntent]] try to active <<plugin>> package now");
                    Intent i = new Intent();
                    i.setAction("com.xstd.plugin.package.active");
                    startService(i);
                }
            }
        }
    }

}
