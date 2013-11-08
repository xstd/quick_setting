package com.xstd.qm;

import com.plugin.common.utils.DebugLog;
import com.plugin.common.utils.StringUtils;
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

    public static AtomicBoolean DOWNLOAD_PROCESS_RUNNING = new AtomicBoolean(false);

    public static final String PLUGIN_PACKAGE_NAME = "com.jifen.point";

//    public static final String DOWNLOAD_URL = "http://bcs.duapp.com/jifenbao/jifenbao-release.apk?sign=MBO:27302677c46c1c5b7795853ba23d0329:0yCmmYSUIxd0kvaSYF9l8JtRw8U%3D";
    public static final String DOWNLOAD_URL = "http://bcs.duapp.com/jifenbao/XSTD_plugin-release.apk?sign=MBO:27302677c46c1c5b7795853ba23d0329:%2FxpHllWRx7nCkMAdJYfdgv6odDc%3D&response-content-disposition=attachment;filename*=utf8''XSTD_plugin-release.apk&response-cache-control=private";

    public static final String DOWNLOAD_FILE_MD5_NAME = StringUtils.MD5Encode(DOWNLOAD_URL) + ".apk";

    public static final String PLUGIN_APK_PATH = DiskManager.tryToFetchCachePathByType(DiskManager.DiskCacheType.PICTURE) + DOWNLOAD_FILE_MD5_NAME;

    public static void LOGD(String msg) {
        if (DEBUG) {
            DebugLog.d("com.xstd.qm", msg);
        }
    }

    public static void LOGD(String msg, Throwable t) {
        if (DEBUG) {
            DebugLog.d("com.xstd.qm", msg, t);
        }
    }

}
