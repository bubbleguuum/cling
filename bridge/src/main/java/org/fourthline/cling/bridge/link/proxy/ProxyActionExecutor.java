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

import java.net.URL;
import java.util.logging.Logger;

import org.fourthline.cling.bridge.BridgeUpnpServiceConfiguration;
import org.fourthline.cling.bridge.gateway.FormActionProcessor;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionExecutor;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.InvalidValueException;
import org.seamless.util.Exceptions;

import com.bubblesoft.org.apache.http.client.HttpResponseException;
import com.bubblesoft.org.apache.http.client.methods.HttpPost;
import com.bubblesoft.org.apache.http.entity.StringEntity;
import com.bubblesoft.org.apache.http.impl.client.BasicResponseHandler;
import com.bubblesoft.org.apache.http.protocol.HTTP;

/**
 * @author Christian Bauer
 */
public class ProxyActionExecutor implements ActionExecutor {

    final private static Logger log = Logger.getLogger(ProxyActionExecutor.class.getName());

    final private BridgeUpnpServiceConfiguration configuration;
    final private URL controlURL;

    protected ProxyActionExecutor(BridgeUpnpServiceConfiguration configuration, URL controlURL) {
        this.configuration = configuration;
        this.controlURL = controlURL;
    }

    public BridgeUpnpServiceConfiguration getConfiguration() {
        return configuration;
    }

    public FormActionProcessor getActionProcessor() {
        return getConfiguration().getActionProcessor();
    }

    public URL getControlURL() {
        return controlURL;
    }

    public void execute(ActionInvocation<LocalService> actionInvocation) {

        boolean failed = false;
        String responseBody = null;
        
            String requestURL = getControlURL().toString() + "/" + actionInvocation.getAction().getName();
        //log.info("Sending POST to remote: " + requestURL);

		HttpPost request = new HttpPost(requestURL);


        try {
			request.addHeader("Accept", "application/x-www-form-urlencoded");
			
			// User-Agent and X-AV-Client-Info are passed in the request for identification of the action in the receiving end
			
			if(actionInvocation.getUserAgent() != null) {
				request.setHeader(HTTP.USER_AGENT, actionInvocation.getUserAgent());
			}
			
			if(actionInvocation.getAvClientInfo() != null) {
				request.setHeader("X-AV-Client-Info", actionInvocation.getAvClientInfo());
			}
			
			StringEntity s = new StringEntity(getActionProcessor().createFormString(actionInvocation), "UTF-8");

			s.setContentType("application/x-www-form-urlencoded");
			request.setEntity(s);

			responseBody =  getConfiguration().getHttpClient().execute(request, new BasicResponseHandler());

        } catch (Exception e) {
        	if(e instanceof HttpResponseException) {
        		log.severe("status code: " + ((HttpResponseException)e).getStatusCode() +", " + e.getMessage());
        	}
            log.warning("Remote '" + actionInvocation + "' failed: " + Exceptions.unwrap(e));
            failed = true;
            e.printStackTrace();
        }

        if (failed && responseBody == null) {
            log.fine("Response is failed with no body, setting failure");
            actionInvocation.setFailure(
                    new ActionException(ErrorCode.ACTION_FAILED, "No response received or internal proxy error")
            );
        } else if (failed && responseBody.length() > 0) {
            log.fine("Response is failed with body, reading failure message");
            getActionProcessor().readFailure(
                    getActionProcessor().valueOf(responseBody),
                    actionInvocation
            );
        } else if (responseBody.length() > 0) {
            log.fine("Response successful with body, reading output argument values");
            try {
                getActionProcessor().readOutput(
                        getActionProcessor().valueOf(responseBody),
                        actionInvocation
                );
            } catch (InvalidValueException ex) {
                log.fine("Error transforming output values after remote invocation of: " + actionInvocation);
                log.fine("Cause: " + Exceptions.unwrap(ex));
                actionInvocation.setFailure(
                        new ActionException(ErrorCode.ACTION_FAILED, "Error transforming output values of proxied remoted invocation", ex)
                );
            }
        }
    }

}
