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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.logging.Logger;

import org.fourthline.cling.transport.impl.NetworkAddressFactoryImpl;
import org.fourthline.cling.transport.spi.InitializationException;

public class AndroidNetworkAddressFactory extends NetworkAddressFactoryImpl {

	final private static Logger log = Logger.getLogger(AndroidUpnpServiceConfiguration.class.getName());

	final private static int VERSION_CODE_GINGERBREAD = 9;

	public AndroidNetworkAddressFactory(int streamListenPort) {
		super(streamListenPort);
	}

	@Override
	protected boolean requiresNetworkInterface() {
    	return false;
    }
	
	@Override
	protected boolean isUsableNetworkInterface(NetworkInterface iface) throws Exception {

		if(android.os.Build.VERSION.SDK_INT >= VERSION_CODE_GINGERBREAD && !iface.isUp()) {
			log.finer("Skipping network interface (down): " + iface.getDisplayName());
			return false;
		}


		if (getInetAddresses(iface).size() == 0) {
			log.finer("Skipping network interface without bound IP addresses: " + iface.getDisplayName());
			return false;
		}

		if (android.os.Build.VERSION.SDK_INT >= VERSION_CODE_GINGERBREAD && iface.isLoopback()) {
			log.finer("Skipping network interface (ignoring loopback): " + iface.getDisplayName());
			return false;
		}

		return true;
	}

	@Override
	protected void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
		if(android.os.Build.VERSION.SDK_INT >= VERSION_CODE_GINGERBREAD) {
			super.displayInterfaceInformation(netint);
		}
	}

	@Override
	public byte[] getHardwareAddress(InetAddress inetAddress) {
		if(android.os.Build.VERSION.SDK_INT >= VERSION_CODE_GINGERBREAD) {
			try {
				NetworkInterface iface = NetworkInterface.getByInetAddress(inetAddress);
				return iface != null ? iface.getHardwareAddress() : null;
			} catch (Exception ex) {
				// seen NullPointerException on Android 4.0.3 with inetAddress != null
				// on Android we sometimes get "java.net.SocketException: No such device or address" when switching networks (mobile -> WiFi)
				log.warning("cannot get hardware address for inet address: " + ex);
			}
		}
		return null;
	}


	@Override
	public InetAddress getLocalAddress(NetworkInterface networkInterface, boolean isIPv6, InetAddress remoteAddress) {

		/*
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD ) {
			// First try to find a local IP that is in the same subnet as the remote IP
			InetAddress localIPInSubnet = getBindAddressInSubnetOf(remoteAddress);
			if (localIPInSubnet != null) return localIPInSubnet;
		}
		 */

		// TODO: This is totally random because we can't access low level InterfaceAddress on Android!
		for (InetAddress localAddress : getInetAddresses(networkInterface)) {
			if (isIPv6 && localAddress instanceof Inet6Address)
				return localAddress;
			if (!isIPv6 && localAddress instanceof Inet4Address)
				return localAddress;
		}
		throw new IllegalStateException("Can't find any IPv4 or IPv6 address on interface: " + networkInterface.getDisplayName());        	
	}
	
	@Override
	  protected void discoverNetworkInterfaces() throws InitializationException {
		try {
			super.discoverNetworkInterfaces();
		} catch (Exception ex) {
			// try to workaround ICS bug on some model with network interface disappearing while enumerated
			// http://code.google.com/p/android/issues/detail?id=33661
			log.warning("Exception while enumerating network interfaces, trying once more: " + ex);
			super.discoverNetworkInterfaces();
		}
	}

}
