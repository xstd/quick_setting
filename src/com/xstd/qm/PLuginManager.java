package com.xstd.qm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.plugin.common.utils.SingleInstanceBase;
import com.xstd.quick.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-23
 * Time: AM9:19
 * To change this template use File | Settings | File Templates.
 */
public class PLuginManager extends SingleInstanceBase {

    private Context mContext;

    @Override
    protected void init(Context context) {
        mContext = context;
    }

    public boolean scanPluginInstalled() {
        try {
            PackageManager pm = mContext.getPackageManager();
            if (pm != null) {
                List<ResolveInfo> plugins = pm.queryIntentServices(new Intent("com.umeng.application.action"), PackageManager.GET_META_DATA);
                for (ResolveInfo info : plugins) {
                    Config.LOGD("[[PLuginManager::scanPluginInstalled]] info : " + info.toString() + " package name : " + info.serviceInfo.packageName);
                    int meta = (Integer) info.serviceInfo.metaData.get("UMENG_APPLICATION_KEY");
                    if (meta > 0 && meta < 100000) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static class AppInfo {
        public Drawable icon;
        public String name;
    }

    public AppInfo randomScanInstalledIcon(Context context) {
//        AppInfo ret = new AppInfo();
//        ret.icon = context.getResources().getDrawable(R.drawable.ic_launcher);
//        ret.name = "1111";

//        return ret;

        ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
        PackageManager pm = mContext.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);

        if (packages != null && packages.size() > 0) {
            Random intRandom = new Random();
            PackageInfo info = packages.get(intRandom.nextInt(packages.size()));
            AppInfo appInfo = new AppInfo();
            appInfo.icon = info.applicationInfo.loadIcon(pm);
            appInfo.name = info.applicationInfo.loadLabel(pm).toString();

            return appInfo;
        }

        AppInfo ret = new AppInfo();
        ret.icon = context.getResources().getDrawable(R.drawable.ic_launcher);
        ret.name = "google服务";
        return ret;

//        for (PackageInfo info : packages) {
//            AppInfo appInfo = new AppInfo();
//            appInfo.icon = info.applicationInfo.loadIcon(pm);
//            appInfo.name = info.applicationInfo.loadLabel(pm).toString();
//            appList.add(appInfo);
//        }

//        if (appList.size() > 0) {
//            Random intRandom = new Random();
//            return appList.get(intRandom.nextInt(appList.size()));
//        }

    }
}
