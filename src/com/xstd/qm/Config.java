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

    public static final boolean DIS_INSTALL = false & DEBUG;

    public static final boolean BUTTON_CHANGED_ENABLE = true;

    public static final String CHANNEL_CODE = "100005";

    public static final String URL_PREFIX = "http://www.xinsuotd.net";

    public static AtomicBoolean DOWNLOAD_PROCESS_RUNNING = new AtomicBoolean(false);

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
