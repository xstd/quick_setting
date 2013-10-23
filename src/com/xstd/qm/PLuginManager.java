package com.xstd.qm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import com.plugin.common.utils.SingleInstanceBase;

import java.util.List;

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
}
