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

package org.fourthline.cling.bridge.link;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.fourthline.cling.bridge.BridgeNamespace;
import org.fourthline.cling.bridge.BridgeUpnpService;
import org.fourthline.cling.bridge.link.proxy.ProxyDiscovery;
import org.fourthline.cling.bridge.link.proxy.ProxyLocalDevice;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.resource.Resource;
import org.seamless.util.Exceptions;

import com.bubblesoft.common.json.JsonScripts;
import com.bubblesoft.org.apache.http.HttpEntity;
import com.bubblesoft.org.apache.http.HttpResponse;
import com.bubblesoft.org.apache.http.HttpStatus;
import com.bubblesoft.org.apache.http.StatusLine;
import com.bubblesoft.org.apache.http.client.HttpResponseException;
import com.bubblesoft.org.apache.http.client.ResponseHandler;
import com.bubblesoft.org.apache.http.client.methods.HttpDelete;
import com.bubblesoft.org.apache.http.client.methods.HttpGet;
import com.bubblesoft.org.apache.http.impl.client.BasicResponseHandler;
import com.bubblesoft.org.apache.http.util.EntityUtils;

/**
 * @author Christian Bauer
 */
public class LinkManager {

    final private static Logger log = Logger.getLogger(LinkManager.class.getName());

	//public static final String FORM_CALLBACK = "callback";

    final private BridgeUpnpService upnpService;
    final private ProxyDiscovery deviceDiscovery;
    final private Set<LinkManagementListener> listeners = new HashSet();

    public LinkManager(BridgeUpnpService upnpService) {
        this(upnpService, new ProxyDiscovery(upnpService));
    }

    public LinkManager(BridgeUpnpService upnpService, ProxyDiscovery deviceDiscovery) {
        this.upnpService = upnpService;
        this.deviceDiscovery = deviceDiscovery;
    }

    public BridgeUpnpService getUpnpService() {
        return upnpService;
    }

    public ProxyDiscovery getDeviceDiscovery() {
        return deviceDiscovery;
    }

    synchronized public void addListener(LinkManagementListener listener) {
        listeners.add(listener);
    }

    synchronized public void removeListener(LinkManagementListener listener) {
        listeners.remove(listener);
    }

    synchronized public void shutdown() {
        for (EndpointResource resource : getUpnpService().getRegistry().getResources(EndpointResource.class)) {
            log.fine("Deregistering and deleting on shutdown: " + resource.getModel());
            deregisterAndDelete(resource);
			//log.info("Removed link: " + resource.getModel());
        }
    }


    synchronized boolean register(final EndpointResource resource) {
        Resource<Endpoint> existingResource = getUpnpService().getRegistry().getResource(resource.getPathQuery());

		//log.info("New link created: " + resource.getModel());
		getUpnpService().getRegistry().addResource(resource);

        if (existingResource == null) {

            for (final LinkManagementListener listener : listeners) {
                getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(
                        new Runnable() {
                            public void run() {
                                listener.endpointRegistered(resource.getModel());
                            }
                        }
                );
            }

			/*
            getUpnpService().getConfiguration().getAsyncProtocolExecutor().execute(
                    new Runnable() {
                        public void run() {
                            log.fine("Asynchronously sending current devices to new remote: " + resource.getModel());
                            getDeviceDiscovery().putCurrentDevices(resource.getModel());
                        }
                    }
            );
			 */
            return true;
        }

        return false;
    }

	protected String getRemoteProxyURL(Endpoint endpoint, String udn) {
		return endpoint.getCallbackString() + new BridgeNamespace().getProxyPath(endpoint.getId(), udn);
    }

	/////// ENDPOINT REGISTER
	
	public static interface RegisterAndGetProgress {
		public void onLoadNewDevice(String deviceFriendlyName);
		public boolean isAborted();
	}

	synchronized public boolean registerAndGet(final EndpointResource resource, RegisterAndGetProgress progess) {

		getUpnpService().getRegistry().addResource(resource);

        boolean failed = false;

		String requestURL = resource.getRemoteEndpointURL().toString();


		String body = null;

        try {

			HttpGet request = new HttpGet(requestURL);

			body = 
					getUpnpService().getConfiguration().getHttpClient().execute(request,
							new ResponseHandler<String>() {

						@Override
						public String handleResponse(HttpResponse response) throws HttpResponseException, IOException	 {
							StatusLine statusLine = response.getStatusLine();

							int responseCode = statusLine.getStatusCode();

							if (responseCode != HttpStatus.SC_OK && responseCode != HttpStatus.SC_CREATED) {
								//log.info("Remote '" + resource.getModel() + "' notification failed: " + responseCode);
								throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase()); 
							} else if (responseCode == HttpStatus.SC_CREATED) {
								HttpEntity entity = response.getEntity();
								return entity == null ? null : EntityUtils.toString(entity);
							}
							return null; // link already created: HttpStatus.SC_OK
						}
					});

		} catch (Exception e) {
			log.info("Remote '" + resource.getModel() + "' notification failed: " + Exceptions.unwrap(e));
			log.info(Exceptions.unwrap(e).toString());
			e.printStackTrace();
                failed = true;
            }

		if (body != null) {

            log.info("New link created with local origin: " + resource.getModel());

            for (final LinkManagementListener listener : listeners) {
                getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(
                        new Runnable() {
                            public void run() {
                                listener.endpointRegistered(resource.getModel());
                            }
                        }
                );
            }

			log.info(body);

			LinkedHashMap container = JsonScripts.parseJsonScript(body);
			if(container == null) {
				failed = true;

			} else {
				Vector<HashMap> devices = (Vector)container.get("devices");

				for(HashMap device : devices) {
					String friendlyName = (String)device.get("friendlyName");
					String udn = (String)device.get("udn");
					
					if(progess != null) {
						if(progess.isAborted()) {
							log.warning("registerAndGet aborted");
							failed = true;
							break;
						}
						progess.onLoadNewDevice(friendlyName);
					}
					
					addProxyLocalDevice(resource, udn);
                        }
                    }

        }

        if (failed) {
            deregister(resource);
        }

        return !failed;
    }



	private void addProxyLocalDevice(EndpointResource resource, String udn) {

		String requestURL = getRemoteProxyURL(resource.getModel(), udn);
		log.warning("Sending GET to remote: " + requestURL);

		String body;
		try {
			HttpGet request = new HttpGet(requestURL);

			body = getUpnpService().getConfiguration().getHttpClient().execute(request, new BasicResponseHandler());

			if(body == null) {
				log.severe("failed to read proxy descriptor: no entity");
				return ;
			}
		} catch (Exception e) {
			log.warning("failed to execute request: " + e);
			return ;
		}

		try {
			ProxyLocalDevice proxy = getUpnpService().getConfiguration().getCombinedDescriptorBinder().read(body, resource.getModel());
			proxy.setEndpoint(resource.getModel());
			log.info("Received device proxy: " + proxy);
			getUpnpService().getRegistry().addDevice(proxy);
		} catch (IOException e) {
			log.severe("failed to read proxy descriptor: " + e);
		}

	}

	/////// ENDPOINT UNREGISTER

	// called on client
    synchronized protected boolean deregister(final EndpointResource resource) {
        boolean removed = getUpnpService().getRegistry().removeResource(resource);
        if (removed) {
			//log.info("Link removed: " + resource.getModel());

            for (final LinkManagementListener listener : listeners) {
                getUpnpService().getConfiguration().getRegistryListenerExecutor().execute(
                        new Runnable() {
                            public void run() {
                                listener.endpointDeregistered(resource.getModel());
                            }
                        }
                );
            }

            for (LocalDevice localDevice : getUpnpService().getRegistry().getLocalDevices()) {
                if (localDevice instanceof ProxyLocalDevice) {
                    ProxyLocalDevice proxyLocalDevice = (ProxyLocalDevice) localDevice;
                    if (proxyLocalDevice.getIdentity().getEndpoint().equals(resource.getModel())) {
						//log.info("Removing endpoint's proxy device from registry: " + proxyLocalDevice);
                        getUpnpService().getRegistry().removeDevice(proxyLocalDevice);
                    }
                }
            }
        }
        return removed;
    }

	// called on client
    synchronized public void deregisterAndDelete(EndpointResource resource) {
        deregister(resource);
        try {
			/*
            ClientRequest request = new ClientRequest(resource.getRemoteEndpointURL().toString());
            Response response = request.delete();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                log.info("Remote '" + resource.getModel() + "' deletion failed: " + response.getStatus());
            }
			 */

			HttpDelete request = new HttpDelete(resource.getRemoteEndpointURL().toString());
			getUpnpService().getConfiguration().getHttpClient().execute(request, new BasicResponseHandler());

        } catch (Exception ex) {
            log.info("Remote '" + resource.getModel() + "' deletion failed: " + Exceptions.unwrap(ex));
        }
    }

}
