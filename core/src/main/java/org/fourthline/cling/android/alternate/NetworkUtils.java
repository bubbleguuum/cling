/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fourthline.cling.android.alternate;

import java.util.logging.Logger;

import org.fourthline.cling.model.ModelUtil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Android network helpers
 * 
 * @author Michael Pujos
 */
public class NetworkUtils {

	private final static int CONNECTIVITY_TYPE_WIMAX = 6; // Since API 8
	private final static int CONNECTIVITY_TYPE_ETHERNET = 9; // Since API 13

	private static Logger log = Logger.getLogger(NetworkUtils.class.getName());

	static public NetworkInfo getConnectedNetworkInfo(Context context) {

		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
			return networkInfo;
		}

		networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) return networkInfo;

		networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) return networkInfo;

		networkInfo = connectivityManager.getNetworkInfo(CONNECTIVITY_TYPE_WIMAX);
		if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) return networkInfo;

		networkInfo = connectivityManager.getNetworkInfo(CONNECTIVITY_TYPE_ETHERNET);
		if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) return networkInfo;

		log.warning("could not find a connected network");

		return null;
	}

	static public boolean isEthNetwork(NetworkInfo networkInfo) {
		return isNetworkType(networkInfo, CONNECTIVITY_TYPE_ETHERNET);
	}

	static public boolean isWiFiNetwork(NetworkInfo networkInfo) {
		return isNetworkType(networkInfo, ConnectivityManager.TYPE_WIFI)  || ModelUtil.ANDROID_EMULATOR;
	}

	static public boolean isMobileNetwork(NetworkInfo networkInfo) {
		return isNetworkType(networkInfo, ConnectivityManager.TYPE_MOBILE) || isNetworkType(networkInfo, CONNECTIVITY_TYPE_WIMAX);
	}

	static public boolean isNetworkType(NetworkInfo networkInfo, int type) {
		return networkInfo != null && networkInfo.getType() == type;
	}
	
	static public boolean isSSDPAwareNetwork(NetworkInfo networkInfo) {
		return isWiFiNetwork(networkInfo) || isEthNetwork(networkInfo);
	}

}
