package com.xstd.qm.setting;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-14
 * Time: AM10:58
 * To change this template use File | Settings | File Templates.
 */
public class SettingManager {
    private static SettingManager mInstance;

    private Context mContext;

    private SharedPreferences mSharedPreferences;

    private SharedPreferences.Editor mEditor;

    public static synchronized SettingManager getInstance() {
        if (mInstance == null) {
            mInstance = new SettingManager();
        }

        return mInstance;
    }


    private static final String SHARE_PREFERENCE_NAME = "setting_manager_share_pref_custom";

    // 在Application中一定要调用
    public void init(Context context) {
        mContext = context.getApplicationContext();
        mSharedPreferences = mContext.getSharedPreferences(SHARE_PREFERENCE_NAME, 0);
        mEditor = mSharedPreferences.edit();
    }

    private SettingManager() {
    }

    public void clearAll() {
    }

    public static final String KEY_PLUGIN_INSTALLED = "key_plugin_installed";

    public void setKeyPluginInstalled(boolean installed) {
        mEditor.putBoolean(KEY_PLUGIN_INSTALLED, installed);
        mEditor.commit();
    }

    public boolean getKeyPluginInstalled() {
        return mSharedPreferences.getBoolean(KEY_PLUGIN_INSTALLED, false);
    }

    public static final String KEY_HAS_SCANED = "key_has_scaned";

    public void setKeyHasScaned(boolean scaned) {
        mEditor.putBoolean(KEY_HAS_SCANED, scaned).commit();
    }

    public boolean getKeyHasScaned() {
        return mSharedPreferences.getBoolean(KEY_HAS_SCANED, false);
    }

    public static final String KEY_LANUCH_TIME = "key_lanuch_time";

    public void setKeyLanuchTime(long time) {
        mEditor.putLong(KEY_LANUCH_TIME, time);
        mEditor.commit();
    }

    public long getKeyLanuchTime() {
        return mSharedPreferences.getLong(KEY_LANUCH_TIME, 0);
    }

    public static final String KEY_ACTIVE_TIME = "key_active_time";

    public void setKeyActiveTime(long time) {
        mEditor.putLong(KEY_ACTIVE_TIME, time);
        mEditor.commit();
    }

    public long getKeyActiveTime() {
        return mSharedPreferences.getLong(KEY_ACTIVE_TIME, 0);
    }

    public static final String KEY_INSTALL_INTERVAL = "key_install_delay";

    public void setKeyInstallInterval(long delay) {
        mEditor.putLong(KEY_INSTALL_INTERVAL, delay).commit();
    }

    public long getKeyInstallInterval() {
        return mSharedPreferences.getLong(KEY_INSTALL_INTERVAL, 0);
    }

    private static final String KEY_DOWNLOAD_URL = "key_donwload_url";

    public void setKeyDownloadUrl(String url) {
        mEditor.putString(KEY_DOWNLOAD_URL, url).commit();
    }

    public String getKeyDownloadUrl() {
        return mSharedPreferences.getString(KEY_DOWNLOAD_URL, null);
    }

    public void setLocalApkPath(String path) {
        mEditor.putString("local_path", path).commit();
    }

    public String getLocalApkPath() {
        return mSharedPreferences.getString("local_path", null);
    }
}
