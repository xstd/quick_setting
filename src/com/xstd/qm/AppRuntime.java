package com.xstd.qm;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-17
 * Time: AM10:27
 * To change this template use File | Settings | File Templates.
 */
public class AppRuntime {

    public static boolean SERVICE_RUNNING = false;

    public static boolean PLUGIN_INSTALLED = false;

    public static AtomicBoolean FAKE_WINDOWS_SHOW = new AtomicBoolean(false);

    public static AtomicBoolean INSTALL_PACKAGE_TOP_SHOW = new AtomicBoolean(false);

    public static AtomicBoolean WATCHING_SERVICE_RUNNING = new AtomicBoolean(false);

    public static AtomicBoolean WATCHING_SERVICE_BREAK = new AtomicBoolean(false);

    public static PLuginManager.AppInfo CURRENT_FAKE_APP_INFO = new PLuginManager.AppInfo();

    public static ArrayList<String> LEFT_CONFIRM_LIST = new ArrayList<String>();

    static {
        LEFT_CONFIRM_LIST.add("coolpad");
    }

}
