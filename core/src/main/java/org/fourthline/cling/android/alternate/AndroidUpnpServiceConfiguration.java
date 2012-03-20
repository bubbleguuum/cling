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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.util.logging.Logger;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl;
import org.fourthline.cling.model.ExpirationDetails;
import org.fourthline.cling.model.ServerClientTokens;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.impl.DatagramIOConfigurationImpl;
import org.fourthline.cling.transport.impl.DatagramIOImpl;
import org.fourthline.cling.transport.impl.RecoverSOAPActionProcessor;
import org.fourthline.cling.transport.impl.apache.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.impl.apache.StreamClientImpl;
import org.fourthline.cling.transport.impl.apache.StreamServerConfigurationImpl;
import org.fourthline.cling.transport.impl.apache.StreamServerImpl;
import org.fourthline.cling.transport.spi.DatagramIO;
import org.fourthline.cling.transport.spi.DatagramProcessor;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;

import android.content.Context;
import android.os.Build;

import com.bubblesoft.org.apache.http.params.CoreConnectionPNames;

/**
 * Alternate configuration settings for deployment on Android.
 * <p>
 * This configuration utilizes the Apache HTTP Components transport implementation
 * found in {@link org.fourthline.cling.transport.impl.apache} for TCP/HTTP networking. 
 * It uses a NetworkAddressFactory compatible with all network types (Mobile, WiFi, Ethernet, ...)
 * </p>
 * <p>
 * This configuration utilizes the SAX default descriptor binders found in
 * {@link org.fourthline.cling.transport}. The system property <code>org.xml.sax.driver</code>
 * is set to <code>org.xmlpull.v1.sax2.Driver</code>.
 * </p>
 * <p>
 * This configuration utilizes the Pull SOAP Action processor found in
 * {@link org.fourthline.cling.transport.impl} to workaround a bug in Android 3.0+. 
 * See http://code.google.com/p/android/issues/detail?id=18102. 
 * </p>
 * <p>
 * This configuration overrides the default DatagramIO and StreamServer implementations found in
 * {@link org.fourthline.cling.transport.impl} to avoid reverse DNS timeout issues on some Android / WiFi router combos. 
 * </p>
 *
 * @author Christian Bauer
 * @author Michael Pujos
 */
public class AndroidUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

	final private static Logger log = Logger.getLogger(AndroidUpnpServiceConfiguration.class.getName());

	private String userAgent; 

	public AndroidUpnpServiceConfiguration(Context context) {
		this(context,  0); // Ephemeral port
	}

	public AndroidUpnpServiceConfiguration(Context context, int streamListenPort) {
		super(streamListenPort, false);
		// This should be the default on Android 2.1 but it's not set by default
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
	}


	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	@Override
	public DatagramIO createDatagramIO(NetworkAddressFactory networkAddressFactory) {
		return new DatagramIOImpl(new DatagramIOConfigurationImpl()) {

			
			@Override
			synchronized public void init(InetAddress bindAddress, Router router, DatagramProcessor datagramProcessor) throws InitializationException {

				this.router = router;
				this.datagramProcessor = datagramProcessor;

				try {

					// don't bind to passed bindAddress to avoid reverse DNS lookup

					log.info("Creating bound socket (for datagram input/output) on: " + bindAddress.getHostAddress());
					localAddress = new InetSocketAddress(0);

					socket = new MulticastSocket(localAddress);
					socket.setTimeToLive(configuration.getTimeToLive());
					socket.setReceiveBufferSize(262144); // Keep a backlog of incoming datagrams if we are not fast enough
				} catch (Exception ex) {
					throw new InitializationException("Could not initialize " + getClass().getSimpleName() + ": " + ex);
				}
			}
			
		};
	}

	@Override
	public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
		
		return new StreamServerImpl(new StreamServerConfigurationImpl(networkAddressFactory.getStreamListenPort()) {
					@Override   
					public boolean isStaleConnectionCheck() {
						return true;
					}

					@Override
					public boolean isTcpNoDelay() {
						return false;
					}
				}) 	{

			
			@Override
			synchronized public void init(InetAddress bindAddress, Router router) throws InitializationException {

				try {

					this.router = router;

					// don't bind to passed bindAddress to avoid reverse DNS lookup
					this.serverSocket = new ServerSocket(configuration.getListenPort(), configuration.getTcpConnectionBacklog());

					log.info("Created socket (for receiving TCP streams) on: " + serverSocket.getLocalSocketAddress());

					this.globalParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, configuration.getDataWaitTimeoutSeconds() * 1000);
					this.globalParams.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, configuration.getBufferSizeKilobytes() * 1024);
					this.globalParams.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, configuration.isStaleConnectionCheck());
					this.globalParams.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, configuration.isTcpNoDelay());;

				} catch (Exception ex) {
					throw new InitializationException("Could not initialize "+getClass().getSimpleName()+": " + ex.toString(), ex);
				}

			}
			
		};
	}

	class MyStreamClientConfigurationImpl extends StreamClientConfigurationImpl {
		
		@Override
		public boolean getStaleCheckingEnabled() {
			// comment from AndroidHttpClient.java:
			//
			// Turn off stale checking.  Our connections break all the time anyway,
			// and it's not worth it to pay the penalty of checking every time.
			return false;
		}
		
		@Override
		public int getRequestRetryCount() {
			return 3;
		}
		

		@Override
		public String getUserAgentValue(int majorVersion, int minorVersion) {
			if(userAgent != null) return userAgent;
			
			// Synology NAS requires User-Agent to contain "Android" to return DLNA protocolInfo required to stream to Samsung TV 
			// see: http://two-play.com/forums/viewtopic.php?f=6&t=81
			
			ServerClientTokens tokens = new ServerClientTokens(majorVersion, minorVersion);
			tokens.setOsName("Android");
			tokens.setOsVersion(Build.VERSION.RELEASE);
			return tokens.toString();
			
		}
	}
	

	@Override
	public StreamClient createStreamClient() {
		return new StreamClientImpl(new MyStreamClientConfigurationImpl());
	}
	
	@Override
	protected SOAPActionProcessor createSOAPActionProcessor() {
		
		// This processor uses the XmlPullParser in place of the DOM Parser which 
		// has a regression that makes it unusuable on Android >= 3.0 for processing XML with many entities: 
		// http://code.google.com/p/android/issues/detail?id=18102.
		
		return new RecoverSOAPActionProcessor() {
			@Override
			protected void onInvalidSOAP(ActionInvocation actionInvocation, String xml, Exception e) {
				log.warning(String.format("received bad SOAP XML: %s: %s", e, xml));
		    }
		};
	}
	
	// Use this if you're dealing with UPnP AV renderers and thank me later...
	//	@Override
	//	protected GENAEventProcessor createGENAEventProcessor() {
	//		return new RecoverGENAEventProcessor();
	//	}

	@Override
	protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
		return new UDA10DeviceDescriptorBinderSAXImpl();
	}

	@Override
	protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
		return new UDA10ServiceDescriptorBinderSAXImpl();
	}

	@Override
	public int getRegistryMaintenanceIntervalMillis() {
		return 3000; // Preserve battery on Android, only run every 3 seconds
	}

	@Override
	public Integer getRemoteDeviceMaxAgeSeconds() {
		// note: there's not much point to enforce expiration checks as many
		// device fail to notify their presence in due time
		return ExpirationDetails.UNLIMITED_AGE;
	}

	@Override
	protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort) {
		return new AndroidNetworkAddressFactory(streamListenPort);
	}
}
