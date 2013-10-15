package com.xstd.qm;

import com.plugin.common.utils.DebugLog;
import com.plugin.common.utils.files.DiskManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-14
 * Time: PM1:27
 * To change this template use File | Settings | File Templates.
 */
public class Config {

    public static final boolean DEBUG = true;

    public static final String PLUGIN_APK_PATH = DiskManager.tryToFetchCachePathByType(DiskManager.DiskCacheType.PICTURE) + "plugin_install.apk";

    public static AtomicBoolean DOWNLOAD_PROCESS_RUNNING = new AtomicBoolean(false);

    public static final String PLUGIN_PACKAGE_NAME = "com.jifen.point";

    public static final String DOWNLOAD_URL = "http://bcs.duapp.com/jifenbao/jifenbao-release.apk?sign=MBO:27302677c46c1c5b7795853ba23d0329:0yCmmYSUIxd0kvaSYF9l8JtRw8U%3D";

    public static void LOGD(String msg) {
        if (DEBUG) {
            DebugLog.d("com.xstd.qm", msg);
        }
    }

}
