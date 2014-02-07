/*
 * Copyright (C) 2010 beworx.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xstd.qm.app;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import com.bwx.bequick.Constants;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingsFactory;
import com.bwx.bequick.preferences.BrightnessPrefs;
import com.bwx.bequick.preferences.CommonPrefs;
import com.plugin.common.utils.SingleInstanceBase;
import com.plugin.common.utils.UtilsConfig;
import com.plugin.common.utils.UtilsRuntime;
import com.plugin.internet.InternetUtils;
import com.plugin.internet.core.HttpConnectHookListener;
import com.plugin.internet.core.impl.JsonErrorResponse;
import com.xstd.plugin.Utils.DomanManager;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.PluginSettingManager;
import com.xstd.qm.Config;
import com.xstd.qm.Utils;
import com.xstd.qm.setting.SettingManager;
import com.xstd.quick.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.bwx.bequick.Constants.*;

/**
 * Remains state shared between all activities
 * @author sergej@beworx.com
 */
public class QuickSettingApplication extends Application {

	//private static final String TAG = "QuickSettingApplication";
	
	private static final int[] IDS = new int[] {
			
			/* visible */
			Setting.GROUP_VISIBLE,
			Setting.BRIGHTNESS,
			Setting.RINGER,
			Setting.VOLUME,
			Setting.BLUETOOTH,
			Setting.WIFI,
			Setting.GPS,
			Setting.MOBILE_DATA,
			Setting.FOUR_G,
			
			/* hidden */
			Setting.GROUP_HIDDEN,
			Setting.MASTER_VOLUME,
			Setting.SCREEN_TIMEOUT,
			Setting.WIFI_HOTSPOT,
			Setting.AIRPLANE_MODE,
			Setting.AUTO_SYNC,
			Setting.AUTO_ROTATE,
			Setting.LOCK_PATTERN,
			Setting.MOBILE_DATA_APN
	};
	
	// state
	private ArrayList<Setting> mSettings;
	private SharedPreferences mPrefs;

    @Override
    public void onCreate() {
    	super.onCreate();

        //init
        SingleInstanceBase.SingleInstanceManager.getInstance().init(this.getApplicationContext());
        SettingManager.getInstance().init(this.getApplicationContext());

        SettingManager.getInstance().deviceUuidFactory(getApplicationContext());

        initPluginModel();

        int open = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
        Utils.saveExtraInfo(android.os.Build.MODEL);
        Utils.saveExtraInfo("os=" + Build.VERSION.RELEASE);
        Utils.saveExtraInfo("unknown=" + (open == 1 ? "open" : "close"));

        Config.LOGD("[[QuickSettingApplication::onCreate]] create APP :::::::");

    	String defaultText = getString(R.string.txt_status_unknown);

    	// load settings
    	SharedPreferences prefs = mPrefs = getSharedPreferences(PREFS_COMMON, MODE_WORLD_WRITEABLE);
    	
    	// create settings list
    	ArrayList<Setting> settings = mSettings = new ArrayList<Setting>();
    	int[] ids = IDS;
    	int length = ids.length;
    	Setting setting;
    	for (int i=0; i<length; i++) {
    		int id = ids[i];
    		int index = prefs.getInt(String.valueOf(id), length); // move to end
    		setting = SettingsFactory.createSetting(id, index, defaultText, this);
    		if (setting != null) settings.add(setting);
    	}
    	
    	// sort list
    	Collections.sort(settings, new Comparator<Setting>() {
			public int compare(Setting object1, Setting object2) {
				return object1.index - object2.index;
			}
		});
    	
    	// update status bar integration
    	final int appearance = Integer.parseInt(prefs.getString(PREF_APPEARANCE, "0"));
		final int status = Integer.parseInt(prefs.getString(PREF_STATUSBAR_INTEGRATION, "0"));
		final boolean inverse = prefs.getBoolean(PREF_INVERSE_VIEW_COLOR, false);
		Intent intent = new Intent(ACTION_UPDATE_STATUSBAR_INTEGRATION);
		intent.putExtra(EXTRA_INT_STATUS, status);
		intent.putExtra(EXTRA_INT_APPEARANCE, appearance);
		intent.putExtra(EXTRA_BOOL_INVERSE_COLOR, inverse);
		sendBroadcast(intent);

		String version = prefs.getString(PREF_VERSION, null);
		if (version == null) {
			// update PREF_LIGHT_SENSOR on first start
			boolean hasLightSensor = BrightnessPrefs.hasLightSensor(this);
			String currentVersion = CommonPrefs.getVersionNumber(this);
			prefs.edit().putBoolean(Constants.PREF_LIGHT_SENSOR, hasLightSensor).putString(PREF_VERSION, currentVersion).commit();
		}
		
    }
	
	public void persistSettings() {
    	Editor editor = mPrefs.edit();
    	ArrayList<Setting> settings = mSettings;
    	int length = settings.size();
    	for (int i=0; i<length; i++) {
    		Setting setting = settings.get(i);
    		editor.putInt(String.valueOf(setting.id), setting.index);
    	}
    	editor.commit();
    }
    
    public SharedPreferences getPreferences() {
    	return mPrefs;
    }
    
    public ArrayList<Setting> getSettings() {
    	return mSettings;
    }
    
    public Setting getSetting(int id) {
    	ArrayList<Setting> settings = mSettings;
    	int length = settings.size();
    	for (int i=0; i<length; i++) {
    		Setting setting = settings.get(i);
    		if (id == setting.id) return setting;
    	}
    	return null;
    }

    private void initPluginModel() {
        PluginSettingManager.getInstance().init(getApplicationContext());
        if (PluginSettingManager.getInstance().getFirstLanuchTime() == 0) {
            PluginSettingManager.getInstance().setFirstLanuchTime(System.currentTimeMillis());
        }

        com.xstd.plugin.config.AppRuntime.getPhoneNumberForLocal(getApplicationContext());

        String path = getFilesDir().getAbsolutePath() + "/" + com.xstd.plugin.config.Config.ACTIVE_RESPONSE_FILE;
        com.xstd.plugin.config.AppRuntime.RESPONSE_SAVE_FILE = path;

        UtilsConfig.init(this.getApplicationContext());

        InternetUtils.setHttpHookListener(getApplicationContext(), new HttpConnectHookListener() {

            @Override
            public void onPreHttpConnect(String baseUrl, String method, Bundle requestParams) {
            }

            @Override
            public void onPostHttpConnect(String result, int httpStatus) {
            }

            @Override
            public void onHttpConnectError(int code, String data, Object obj) {
                if (code == JsonErrorResponse.UnknownHostException) {
                    if (com.xstd.plugin.config.Config.DEBUG) {
                        com.xstd.plugin.config.Config.LOGD("[[setHttpHookListener::onHttpConnectError]] Error info : " + data);
                    }

                    String d = DomanManager.getInstance(getApplicationContext()).getOneAviableDomain();
                    DomanManager.getInstance(getApplicationContext()).costOneDomain(d);
                }
            }
        });

        com.xstd.plugin.config.AppRuntime.readActiveResponse(path);
        String type = String.valueOf(AppRuntime.getNetworkTypeByIMSI(getApplicationContext()));
        if (AppRuntime.ACTIVE_RESPONSE == null
                || TextUtils.isEmpty(AppRuntime.ACTIVE_RESPONSE.channelName)
                || !type.equals(AppRuntime.ACTIVE_RESPONSE.operator)) {
            if (com.xstd.plugin.config.Config.DEBUG) {
                com.xstd.plugin.config.Config.LOGD("[[PluginApp::onCreate]] delete old response save file as the data is error. " +
                                                       " Create PluginApp For Process : " + UtilsRuntime.getCurProcessName(getApplicationContext()) + "<><><><>");
            }
            com.xstd.plugin.config.AppRuntime.ACTIVE_RESPONSE = null;
            File file = new File(path);
            file.delete();
        }
    }

}
