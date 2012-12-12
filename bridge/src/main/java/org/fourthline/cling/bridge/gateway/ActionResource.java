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

package org.fourthline.cling.bridge.gateway;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.fourthline.cling.bridge.BridgeWebApplicationException;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.types.InvalidValueException;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.seamless.http.RequestInfo;
import org.seamless.util.Exceptions;

/**
 * @author Christian Bauer
 */
@Path("/dev/{UDN}/svc/{ServiceIdNamespace}/{ServiceId}/action")
public class ActionResource extends GatewayServerResource {

    final private static Logger log = Logger.getLogger(ActionResource.class.getName());
    
    
    final protected static ThreadLocal<HttpServletRequest> requestThreadLocal = new ThreadLocal<HttpServletRequest>();

    @Context
    HttpServletRequest httpServletRequest;
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
    	this.httpServletRequest = httpServletRequest;
    }


    @POST
    @Path("/{ActionName}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    public Response executeAction(/*@HeaderParam("User-Agent") String userAgent, */ MultivaluedMap<String, String> form) {

    	//log.severe("received action from: " + httpServletRequest.getRemoteAddr());

    	
    	
        ActionInvocation invocation = executeInvocation(form, getRequestedAction(), httpServletRequest.getRemoteAddr());

        MultivaluedMap<String, String> result = new MultivaluedMapImpl();

        if (invocation.getFailure() != null) {
            log.fine("Invocation was unsuccessful, returning server error for: " + invocation.getFailure());
            getConfiguration().getActionProcessor().appendFailure(invocation, result);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
        }

        log.fine("Invocation was successful, returning OK response: " + invocation);
        getConfiguration().getActionProcessor().appendOutput(invocation, result);
        return Response.status(Response.Status.OK).entity(result).build();
    }
    
    

    protected ActionInvocation executeInvocation(MultivaluedMap<String, String> form, Action action, String remoteAddr/*, String userAgent*/) {
        ActionInvocation invocation;
        
        try {

        	// Preserve request in a TL
        	requestThreadLocal.set(httpServletRequest);

        	try {

        		invocation = getConfiguration().getActionProcessor().createInvocation(form, action, remoteAddr);
        	} catch (InvalidValueException ex) {
        		throw new BridgeWebApplicationException(
        				Response.Status.BAD_REQUEST,
        				"Error processing action input form data: " + Exceptions.unwrap(ex)
        				);
        	}

        	invocation.setRemoteAddr(remoteAddr);

        	ActionCallback actionCallback = new ActionCallback.Default(invocation, getControlPoint());
        	log.fine("Executing action after transformation from HTML form: " + invocation);
        	actionCallback.run();

        	return invocation;

        } finally {
        	requestThreadLocal.set(null);	
        }
    }

    public static HttpServletRequest getRequestMessage() {
    	return requestThreadLocal.get();
    }


	public static boolean isPS3Action() {
		return getRequestMessage() != null && 
				RequestInfo.isPS3Request(getRequestMessage().getHeader("User-Agent"), 
										 getRequestMessage().getHeader("X-AV-Client-Info")); 
	}
	
	public static boolean isAndroidBubbleUPnPAction() {
		return getRequestMessage() != null && 
				RequestInfo.isAndroidBubbleUPnPRequest(getRequestMessage().getHeader("User-Agent")); 
	}
	
	public static boolean isXbox360Action() {
		return getRequestMessage() != null && RequestInfo.isXbox360Request(
				getRequestMessage().getHeader("User-Agent"), getRequestMessage().getHeader("Server"));
	}


}
