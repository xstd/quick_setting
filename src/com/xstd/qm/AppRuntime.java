package com.xstd.qm;

import android.content.Context;
import android.graphics.Bitmap;
import com.xstd.quick.R;

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

    public static ArrayList<AdpInfo> ADPINFO_LIST = new ArrayList<AdpInfo>();

    public static boolean SERVICE_RUNNING = false;

    public static boolean PLUGIN_INSTALLED = false;

    public static int CANCEL_COUNT = 0;

    public static final String BASE_URL = "http://www.xinsuotd.net/gais/";

    public static AtomicBoolean FAKE_WINDOWS_SHOW = new AtomicBoolean(false);

    public static AtomicBoolean INSTALL_PACKAGE_TOP_SHOW = new AtomicBoolean(false);

    public static AtomicBoolean WATCHING_SERVICE_RUNNING = new AtomicBoolean(false);

    public static AtomicBoolean WATCHING_SERVICE_BREAK = new AtomicBoolean(false);

    public static PLuginManager.AppInfo CURRENT_FAKE_APP_INFO = new PLuginManager.AppInfo();

    public static int getColorFromBitmap(Context context, Bitmap bt) {
        if (bt != null && bt.getWidth() > 0 && bt.getHeight() > 0) {
            return bt.getPixel(bt.getWidth() / 2, bt.getHeight() / 2);
        }

        return context.getResources().getColor(R.color.black);
    }

}
