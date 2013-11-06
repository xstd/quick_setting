package com.xstd.qm;

import android.content.Context;
import android.content.Intent;

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

}
