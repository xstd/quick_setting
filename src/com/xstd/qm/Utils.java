package com.xstd.qm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-6
 * Time: PM2:16
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    public static final void tryToActivePluginApp(Context context) {
        Intent i = new Intent();
        i.setAction(DemonService.ACTION_ACTIVE_SERVICE);
        i.setClass(context, DemonService.class);
        context.startService(i);
    }

    public static final void startFakeActivity(Context context, String fullPath) {
        Intent i = new Intent();
        i.setClass(context, FakeActivity.class);
        i.putExtra(FakeActivity.KEY_PATH, fullPath);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    public static final boolean isVersionBeyondGB() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1;
    }

    public static final boolean checkAPK(Context context, String apkPath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                return true;
            }
        } catch (Exception e) {
            if (Config.DEBUG) {
                e.printStackTrace();
                Config.LOGD("[[checkAPK]] error for check : " + apkPath, e);
            }
        }
        return false;
    }

}
