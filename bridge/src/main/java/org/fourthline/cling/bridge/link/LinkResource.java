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

import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.fourthline.cling.bridge.BridgeServerResource;
import org.fourthline.cling.bridge.BridgeWebApplicationException;
import org.fourthline.cling.bridge.Constants;
import org.fourthline.cling.bridge.link.proxy.ProxyLocalDevice;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;

import com.bubblesoft.common.json.JsonScripts;

/**
 * @author Christian Bauer
 */
@Path("/link")
public class LinkResource extends BridgeServerResource {

    final private static Logger log = Logger.getLogger(LinkResource.class.getName());

	
	private HashMap deviceToHashMap(Device device) {
		HashMap h = new HashMap();
		h.put("udn", device.getIdentity().getUdn().getIdentifierString());
		h.put("friendlyName", device.getDetails().getFriendlyName());
		return h;
    }

	@GET
    @Path("/{EndpointId}")
	public Response update() {

        String endpointId = getFirstPathParamValue(Constants.PARAM_ENDPOINT_ID);


		Endpoint endpoint = new Endpoint(endpointId);
        EndpointResource endpointResource = createEndpointResource(endpoint);

        log.fine("Registering endpoint: " + endpoint);

		ResponseBuilder response;
		if(getUpnpService().getLinkManager().register(endpointResource)) {
			response = Response.status(Response.Status.CREATED);

			HashMap<String, Vector> container = new HashMap<String, Vector>(); 
			Vector<HashMap> devices = new Vector<HashMap>(); 
			container.put("devices", devices);

			// adding all bridged remote devices
			for (RemoteDevice remoteDevice : getUpnpService().getRegistry().getRemoteDevices()) {
				if(getUpnpService().getConfiguration().isBridgedDevice(remoteDevice)) {
					devices.add(deviceToHashMap(remoteDevice));
                }
            }

			// adding all bridged local devices not Proxy device
			/*
			for (LocalDevice localDevice : getUpnpService().getRegistry().getLocalDevices()) {
				if (localDevice instanceof ProxyLocalDevice && getUpnpService().getConfiguration().isBridgedDevice(localDevice)) {
					log.fine("Skipping proxy, not announcing to any endpoints: " + localDevice);
					continue;
                }
				devices.add(deviceToHashMap(localDevice));
            }
            */

			response.entity(JsonScripts.buildJsonScript(container));

		} else {
			response = Response.status(Response.Status.OK);
            }

		return response.build();

            }

	@DELETE
	@Path("/{EndpointId}")
	public Response remove() {
		EndpointResource endpointResource = getRequestedEndpointResource();
		boolean removed = getUpnpService().getLinkManager().deregister(endpointResource);
		return Response.status(removed ? Response.Status.OK : Response.Status.NOT_FOUND).build();
            }



    protected EndpointResource getRequestedEndpointResource() {
        String endpointId = getFirstPathParamValue(Constants.PARAM_ENDPOINT_ID);
        EndpointResource resource = getEndpointResource(endpointId);
        if (resource == null) {
            throw new BridgeWebApplicationException(Response.Status.NOT_FOUND);
        }
        return resource;
    }

    protected EndpointResource getEndpointResource(String endpointId) {
        return getRegistry().getResource(
                EndpointResource.class,
                getNamespace().getEndpointPath(endpointId)
        );
    }

    protected EndpointResource createEndpointResource(Endpoint endpoint) {
        return new EndpointResource(
                getNamespace().getEndpointPath(endpoint.getId()),
                getConfiguration().getLocalEndpointURL(),
                endpoint
        ) {
            @Override
            public LinkManager getLinkManager() {
                return getUpnpService().getLinkManager();
            }
        };
    }

}
