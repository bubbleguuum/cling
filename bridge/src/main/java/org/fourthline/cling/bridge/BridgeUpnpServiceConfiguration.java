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

package org.fourthline.cling.bridge;

import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.bridge.gateway.FormActionProcessor;
import org.fourthline.cling.bridge.link.proxy.CombinedDescriptorBinder;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.impl.apache.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.spi.InitializationException;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.StreamServer;
import org.fourthline.cling.transport.spi.StreamServerConfiguration;

import com.bubblesoft.org.apache.http.client.HttpClient;
import com.bubblesoft.org.apache.http.impl.client.DefaultHttpClient;
import com.bubblesoft.org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import com.bubblesoft.org.apache.http.params.BasicHttpParams;
import com.bubblesoft.org.apache.http.params.HttpConnectionParams;
import com.bubblesoft.org.apache.http.params.HttpParams;
import com.bubblesoft.org.apache.http.params.HttpProtocolParams;

/**
 * @author Christian Bauer
 */
public class BridgeUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

    final private static Logger log = Logger.getLogger(BridgeUpnpServiceConfiguration.class.getName());

    final private URL localBaseURL;
    final private String contextPath;
    final private CombinedDescriptorBinder combinedDescriptorBinder;
    final private FormActionProcessor actionProcessor;
    private HttpClient httpClient;

    // client constructor
    public BridgeUpnpServiceConfiguration(HttpClient httpClient) {
        this(null, "", httpClient);
    }

    public BridgeUpnpServiceConfiguration(URL localBaseURL, HttpClient httpClient) {
        this(localBaseURL, "", httpClient);
    }

    public BridgeUpnpServiceConfiguration(URL localBaseURL, String contextPath, HttpClient httpClient) {
        super(localBaseURL == null ? 0 : localBaseURL.getPort(), false);
        this.localBaseURL = localBaseURL;
        this.contextPath = contextPath;
        this.actionProcessor = createFormActionProcessor();
        this.combinedDescriptorBinder = createCombinedDescriptorBinder();

        if(httpClient == null) {
        	StreamClientConfigurationImpl streamConfiguration = new StreamClientConfigurationImpl();
        	
        	HttpParams params = new BasicHttpParams();
        	
            HttpConnectionParams.setConnectionTimeout(params, streamConfiguration.getConnectionTimeoutSeconds() * 1000);
            HttpConnectionParams.setSoTimeout(params, streamConfiguration.getDataReadTimeoutSeconds() * 1000);
            HttpProtocolParams.setContentCharset(params, streamConfiguration.getContentCharset());
            HttpProtocolParams.setUseExpectContinue(params, false);
            
        	
        	ThreadSafeClientConnManager clientConnectionManager = new ThreadSafeClientConnManager();
        	clientConnectionManager.setMaxTotal(streamConfiguration.getMaxTotalConnections());
            clientConnectionManager.setDefaultMaxPerRoute(100);
        	
            // do not request zipped response as Multicast2Unicast sends buggy data otherwise
            httpClient = new DefaultHttpClient(clientConnectionManager, params);
        }
        
        this.httpClient = httpClient;
    }
    
    public HttpClient getHttpClient() {
    	return httpClient;
    }

    public URL getLocalBaseURL() {
        return localBaseURL;
    }

    public String getContextPath() {
        return contextPath;
    }

    public CombinedDescriptorBinder getCombinedDescriptorBinder() {
        return combinedDescriptorBinder;
    }

    public FormActionProcessor getActionProcessor() {
        return actionProcessor;
    }

    protected CombinedDescriptorBinder createCombinedDescriptorBinder() {
        return new CombinedDescriptorBinder(this);
    }

    protected FormActionProcessor createFormActionProcessor() {
        return new FormActionProcessor();
    }

    public URL getLocalEndpointURL() {
    	if(localBaseURL == null) return null;
        try {
            return new URL(
                    getLocalBaseURL().getProtocol(),
                    getLocalBaseURL().getHost(),
                    getLocalBaseURL().getPort(),
                    getNamespace().getBasePath().toString()
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    // TODO: Make the network interfaces/IPs for binding configurable with servlet context params

    @Override
    public BridgeNamespace getNamespace() {
        return new BridgeNamespace(getContextPath());
    }

    // The job of the StreamServer is now taken care of by the GatewayFilter

    @Override
    public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
        return new StreamServer() {
            public void init(InetAddress bindAddress, Router router) throws InitializationException {
            }

            public int getPort() {
                return getLocalBaseURL().getPort();
            }

            public void stop() {
            }

            public StreamServerConfiguration getConfiguration() {
                return null;
            }

            public void run() {
            }
        };
    }

	public List<DeviceType> getAuthorizedDeviceTypes() {
		return null;
	}

	public boolean isBridgedDevice(Device device) {
		return true;
	}
}
