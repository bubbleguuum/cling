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

package org.fourthline.cling.bridge.link.proxy;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import org.fourthline.cling.bridge.BridgeNamespace;
import org.fourthline.cling.bridge.BridgeUpnpService;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.seamless.util.Exceptions;
import org.seamless.util.MimeType;

/**
 * @author Christian Bauer
 */
public class ProxyDiscovery extends DefaultRegistryListener {

    final private static Logger log = Logger.getLogger(ProxyDiscovery.class.getName());

    final private BridgeUpnpService upnpService;

    public ProxyDiscovery(BridgeUpnpService upnpService) {
        this.upnpService = upnpService;
    }

    public BridgeUpnpService getUpnpService() {
        return upnpService;
    }


    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
    	// FIXME: handle this when we do C2DM
    	/*
        for (EndpointResource resource : registry.getResources(EndpointResource.class)) {
            log.fine("Remote device added, sending to endpoint: " + resource.getModel());
            putRemoteDevice(resource.getModel(), device);
        }
        */
    	
    	
    	
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
    	// FIXME: handle this when we do C2DM
    	/*
        if (device instanceof ProxyLocalDevice) {
            log.fine("Proxy added, not announcing to any endpoints: " + device);
            return;
        }
        for (EndpointResource resource : registry.getResources(EndpointResource.class)) {
            log.fine("Local device added, sending to endpoint: " + resource.getModel());
            putLocalDevice(resource.getModel(), device);
        }
        */
    }

    @Override
    public void deviceRemoved(Registry registry, Device device) {
    	// FIXME: handle this when we do C2DM
    	/*
        if (device instanceof ProxyLocalDevice) {
            log.fine("Proxy removed, not announcing to any endpoints: " + device);
            return;
        }
        for (EndpointResource resource : registry.getResources(EndpointResource.class)) {
            log.fine("Device removed, removing from endpoint: " + resource.getModel());
            deleteDevice(resource.getModel(), device);
        }
        */
    }
    
    
    public String getProxyDeviceDescriptor(String udn) {
    	return getProxyDeviceDescriptor(udn, null);
    }
    
    // BBMOD
    public String getProxyDeviceDescriptor(String udn,  String friendlyNameSuffix) {
    	
    	UDN Udn = new UDN(udn);
    	
    	Device device = getUpnpService().getRegistry().getDevice(Udn, true);
    	
    	if(device instanceof LocalDevice) {
    		log.warning("not managing LocalDevice");
    		return null; // FIXME
    	} else if(device instanceof RemoteDevice) {
    	    RemoteDevice preparedDevice;
            try {
                // Rewrite the URIs of all services to URIs reachable through the HTTP gateway, etc.
                log.fine("Preparing remote device for proxying with a modified copy of the device metamodel graph");
                preparedDevice = prepareRemoteDevice((RemoteDevice)device, friendlyNameSuffix);
            } catch (ValidationException ex) {
                // This should never happen, the graph was already OK and our transformation is bug-free
                log.warning("Could not validate transformed device model: " + device);
                for (ValidationError validationError : ex.getErrors()) {
                    log.warning(validationError.toString());
                }
                return null;
            }
            
            try {
                return getUpnpService().getConfiguration().getCombinedDescriptorBinder().write(preparedDevice);
            } catch (IOException ex) {
                log.warning("Could not create combined descriptor: " + Exceptions.unwrap(ex));
            }
            
    	} else {
    		log.warning("device not found: " + udn);
    	}
    	return null;
    }

    /*
    public void putCurrentDevices(Endpoint endpoint) {
        log.fine("Sending current devices to: " + endpoint);
        
        boolean success = true;

        for (RemoteDevice remoteDevice : getUpnpService().getRegistry().getRemoteDevices()) {
            if (!putRemoteDevice(endpoint, remoteDevice)) {
                success = false;
                break;
            }
        }

        if (success) {
            for (LocalDevice localDevice : getUpnpService().getRegistry().getLocalDevices()) {
                if (localDevice instanceof ProxyLocalDevice) {
                    log.fine("Skipping proxy, not announcing to any endpoints: " + localDevice);
                    continue;
                }
                if (!putLocalDevice(endpoint, localDevice)) {
                    success = false;
                    break;
                }
            }
        }
        if (!success) {
            log.warning("Sending notification of current devices to remote '" + endpoint + "' failed");
        }
    }

    
    public boolean putRemoteDevice(Endpoint endpoint, RemoteDevice device) {

        RemoteDevice preparedDevice;
        try {
            // Rewrite the URIs of all services to URIs reachable through the HTTP gateway, etc.
            log.fine("Preparing remote device for proxying with a modified copy of the device metamodel graph");
            preparedDevice = prepareRemoteDevice(device);
        } catch (ValidationException ex) {
            // This should never happen, the graph was already OK and our transformation is bug-free
            log.warning("Could not validate transformed device model: " + device);
            for (ValidationError validationError : ex.getErrors()) {
                log.warning(validationError.toString());
            }
            return false;
        }

        return putProxy(
                getRemoteProxyURL(endpoint, device),
                preparedDevice
        );
    }

    public boolean putLocalDevice(final Endpoint endpoint, LocalDevice device) {
        return putProxy(
                getRemoteProxyURL(endpoint, device),
                device
        );
    }


    protected boolean putProxy(String remoteURL, Device device) {
        String descriptor;
        try {
            descriptor = getUpnpService().getConfiguration().getCombinedDescriptorBinder().write(device);
        } catch (IOException ex) {
            log.warning("Could not create combined descriptor: " + Exceptions.unwrap(ex));
            return false;
        }

        boolean failed = false;
        try {
            log.info("Sending device proxy to: " + remoteURL);
            ClientRequest request = new ClientRequest(remoteURL);
            request.body(MediaType.TEXT_XML, descriptor);
            //getAuthManager().write(credentials, request);
            Response response = request.put();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                failed = true;
                log.warning("Sending notification of device addition to '" + remoteURL + "' failed: " + response.getStatus());
            }

        } catch (Exception ex) {
            log.warning("Sending notification of device addition to remote '" + remoteURL + "' failed: " + Exceptions.unwrap(ex));
            failed = true;
        }
        return !failed;
    }

    /*
    public boolean deleteDevice(Endpoint endpoint, Device device) {
        return deleteProxy(
                //getRemoteProxyURL(endpoint, device),
                device
        );
    }

    
    protected boolean deleteProxy(Device device) {
        boolean failed = false;
        try {
            log.info("Sending deletion of device proxy: " + remoteURL);
            ClientRequest request = new ClientRequest(remoteURL);
            //getAuthManager().write(credentials, request);
            Response response = request.delete();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                failed = true;
                log.warning("Deleting remote proxy '" + remoteURL + "' failed: " + response.getStatus());
            } else {
                log.fine("Deleted remote proxy: " + remoteURL);
            }

        } catch (Exception ex) {
            log.warning("Deleting remote proxy '" + remoteURL + "' failed: " + Exceptions.unwrap(ex));
            failed = true;
        }
        return !failed;
    }
    */

    protected RemoteDevice prepareRemoteDevice(RemoteDevice currentDevice, String friendlyNameSuffix) throws ValidationException {

        List<RemoteService> services = new ArrayList();
        if (currentDevice.hasServices()) {
            for (RemoteService service : currentDevice.getServices()) {
                services.add(prepareRemoteService(service));
            }
        }

        List<RemoteDevice> embeddedDevices = new ArrayList();
        if (currentDevice.hasEmbeddedDevices()) {
            for (RemoteDevice embeddedDevice : currentDevice.getEmbeddedDevices()) {
                embeddedDevices.add(prepareRemoteDevice(embeddedDevice, friendlyNameSuffix));
            }
        }

        // We have to retrieve all icon data, so we can later put it into the combined descriptor base64 encoded
        List<Icon> icons = new ArrayList();
        if (currentDevice.hasIcons()) {
            for (int i = 0; i < currentDevice.getIcons().length; i++) {

                Icon icon = currentDevice.getIcons()[i];
                byte[] iconData = retrieveIconData(icon);
                if (iconData == null || iconData.length == 0) continue;

                // The URI is really not important, it just has to be unique when we later proxy this remote device
                // and of course, it has to match the rules for a local device icon URI (see LocalDevice.java#validate)
                icons.add(
                        new Icon(
                                icon.getMimeType(),
                                icon.getWidth(),
                                icon.getHeight(),
                                icon.getDepth(),
                                URI.create(BridgeNamespace.getIconId(currentDevice, i)),
                                iconData
                        )
                );
            }
        }

        UDN newUDN = UDN.uniqueSystemIdentifier(currentDevice.getIdentity().getUdn().getIdentifierString() + 
        		(friendlyNameSuffix == null ? "" : friendlyNameSuffix));
        //log.info(String.format("proxy device udn: %s, real device udn: %s", newUDN, currentDevice.getIdentity().getUdn()));
        
        DeviceDetails newDetails;
        if(friendlyNameSuffix == null) {
        	newDetails = currentDevice.getDetails(); 
        } else {
//        	public DeviceDetails(URL baseURL, String friendlyName,
//                    ManufacturerDetails manufacturerDetails, ModelDetails modelDetails,
//                    String serialNumber, String upc,
//                    URI presentationURI, DLNADoc[] dlnaDocs, DLNACaps dlnaCaps) {
        	DeviceDetails curDetails = currentDevice.getDetails();
        	newDetails = new DeviceDetails(curDetails.getBaseURL(), 
        			String.format("%s [%s]", curDetails.getFriendlyName(), friendlyNameSuffix),
        			curDetails.getManufacturerDetails(),
        			curDetails.getModelDetails(),
        			curDetails.getSerialNumber(),
        			curDetails.getUpc(),
        			curDetails.getPresentationURI(),
        			curDetails.getDlnaDocs(),
        			curDetails.getDlnaCaps(),
        			curDetails.getSecProductCaps());
        					
        }

        return currentDevice.newInstance(
        		newUDN,
                currentDevice.getVersion(),
                currentDevice.getType(),
                newDetails,
                icons.toArray(new Icon[icons.size()]),
                currentDevice.toServiceArray(services),
                embeddedDevices
        );
    }

    protected RemoteService prepareRemoteService(RemoteService service) throws ValidationException {
        BridgeNamespace namespace = getUpnpService().getConfiguration().getNamespace();

        Action[] actionDupes = new Action[service.getActions().length];
        for (int i = 0; i < service.getActions().length; i++) {
            Action<RemoteService> action = service.getActions()[i];
            actionDupes[i] = action.deepCopy();
        }

        StateVariable[] stateVariableDupes = new StateVariable[service.getStateVariables().length];
        for (int i = 0; i < service.getStateVariables().length; i++) {
            StateVariable stateVariable = service.getStateVariables()[i];
            stateVariableDupes[i] = stateVariable.deepCopy();
        }

        return service.getDevice().newInstance(
                service.getServiceType(),
                service.getServiceId(),
                namespace.getDescriptorPath(service),
                namespace.getControlPath(service),
                namespace.getEventSubscriptionPath(service),
                actionDupes,
                stateVariableDupes
        );
    }

    public String getMimeTypeFromFileExt(String ext) {
		if(ext.equals("gif")) return "image/gif";
		if(ext.equals("png")) return "image/png";
		if(ext.equals("bmp")) return "image/bmp";
		if(ext.equals("jpg")) return "image/jpeg";
		return null;
	}
    
    public static String getFileExtension(String f) {
        String ext = "";
        int i = f.lastIndexOf('.');
        if (i > 0 &&  i < f.length() - 1) {
            ext = f.substring(i+1).toLowerCase();
        }
        return ext;
    }

    protected byte[] retrieveIconData(Icon icon) {
        if (icon.getData() != null) return icon.getData(); // This should cover LocalDevice

        if (!(icon.getDevice() instanceof RemoteDevice)) {
            log.warning("Can't retrieve icon data of: " + icon.getDevice());
            return new byte[0];
        }

        RemoteDevice remoteDevice = (RemoteDevice) icon.getDevice();
        String remoteURL = remoteDevice.normalizeURI(icon.getUri()).toString();
        try {
            ClientRequest request = new ClientRequest(remoteURL);
            log.fine("Retrieving icon data: " + remoteURL);
            ClientResponse<byte[]> response = request.get(byte[].class);

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String contentType = response.getHeaders().getFirst("Content-Type");
                if (contentType != null && !MimeType.valueOf(contentType).getType().equals("image")) {
                	contentType = null;
                }
                
                if(contentType == null) {
                	String ext = getFileExtension(remoteURL);
                	if(ext != null) {
                		contentType = getMimeTypeFromFileExt(ext);
                		if(contentType != null) {
                			log.warning("Inferred icon content-type from URL extension: " + remoteURL);
                		}
                	}
                }

                if(contentType == null) {
                    log.warning("Retrieving icon data of '" + remoteURL + "' failed, no image content type: " + contentType);
                    return new byte[0];
                }

                
                return response.getEntity();
            }
            log.warning("Retrieving icon data of '" + remoteURL + "' failed: " + response.getStatus());
        } catch (Exception ex) {
            log.warning("Retrieving icon data of '" + remoteURL + "' failed: " + Exceptions.unwrap(ex));
        }
        return new byte[0];
    }

    /*
    protected String getRemoteProxyURL(Endpoint endpoint, Device device) {
        return endpoint.getCallbackString() + new BridgeNamespace().getProxyPath(endpoint.getId(), device);
    }
    */

}
