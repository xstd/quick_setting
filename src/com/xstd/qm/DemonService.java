package com.xstd.qm;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import com.xstd.qm.receiver.PackageAddBrc;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-17
 * Time: AM10:18
 * To change this template use File | Settings | File Templates.
 */
public class DemonService extends Service {

    public static final String ACTION_STOP_SERVICE = "com.xdtd.service.stop";

    private PackageAddBrc mAddBRC = new PackageAddBrc();

    @Override
    public void onCreate() {
        super.onCreate();
        Config.LOGD("[[DemonService]] onCreate");

        AppRuntime.SERVICE_RUNNING = true;

        //registe
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_PACKAGE_ADDED);
        f.setPriority(0x7fffffff);
        f.addDataScheme("package");
        registerReceiver(mAddBRC, f);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Config.LOGD("[[DemonService]] onCreate, intent = " + (intent != null ? intent.getAction() : "null"));

        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_STOP_SERVICE.equals(action)) {
                stopSelf();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Config.LOGD("[[DemonService]] onCreate");

        AppRuntime.SERVICE_RUNNING = false;

        unregisterReceiver(mAddBRC);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

}
