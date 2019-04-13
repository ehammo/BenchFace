/*******************************************************************************
 * Copyright (C) 2014 Philipp B. Costa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.ufc.mdcc.mpos.util.device;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.persistence.MobileDao;
import br.ufc.mdcc.mpos.util.LocationListenerAdapter;
import br.ufc.mdcc.mpos.util.LocationTracker;
import weka.core.Capabilities;

/**
 * This controller get all informations about mobile
 * 
 * @author Philipp B. Costa
 */
public final class DeviceController {
	private final String clsName = MposFramework.class.getName();

	private Context context;
	private Device device;

	private String appName;
	private String appVersion;

	public DeviceController(Activity activity) throws NameNotFoundException {
	    this.context = activity.getApplicationContext();
        appStatus(activity);
	}

	public String getAppName() {
		return appName;
	}

	public Device getDevice() {
		return device;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public boolean isOnline() {
		ConnectivityManager cm = ((ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return cm != null && cm.getActiveNetwork() != null;
		} else {
			return cm != null && cm.getActiveNetworkInfo().isConnected();
		}
	}

	public int getNetworkDownloadSpeed() {
		ConnectivityManager cm = ((ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE));

		if(cm != null){
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
				return capabilities.getLinkDownstreamBandwidthKbps();
			}
		}
		return 0;
	}

	public int getNetworkUploadSpeed() {
		ConnectivityManager cm = ((ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE));

		if(cm != null){
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
				return capabilities.getLinkUpstreamBandwidthKbps();
			}
		}
		return 0;
	}

	public void collectDeviceConfig() {
		device = new Device();
		device.setMobileId(new MobileDao(context).checkMobileId());
		device.setCarrier(((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName());
		device.setDeviceName(Build.MANUFACTURER + " " + Build.MODEL);
	}

	public void destroy() {
		device = null;
		appName = null;
	}
	
	private void appStatus(Activity activity) throws NameNotFoundException{
	    appName = activity.getString(activity.getApplicationInfo().labelRes).replace(' ', '_');
        appVersion = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
	}

}