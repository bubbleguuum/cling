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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;

import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.InvalidValueException;
import org.seamless.util.URIUtil;
import org.seamless.xhtml.XHTML.ATTR;
import org.seamless.xhtml.XHTML.ELEMENT;
import org.seamless.xhtml.XHTMLElement;

/**
 * @author Christian Bauer
 */
public class FormActionProcessor {

    public static final String NULL_OUTPUT_ARGUMENT_VALUE = "<<NULL>>";

    // called from ActionResource
    public ActionInvocation createInvocation(MultivaluedMap<String, String> form, Action action, String remoteAddr) throws InvalidValueException {

    	HttpServletRequest request = ActionResource.getRequestMessage();
    	
    	String userAgent = (String)request.getHeader("User-Agent");
    	String avClientInfo = (String)request.getHeader("X-AV-Client-Info");
    	
        ActionInvocation invocation = new ActionInvocation(action, userAgent);
        invocation.setAvClientInfo(avClientInfo);
        if (action.hasInputArguments()) {
            for (ActionArgument arg : action.getInputArguments()) {
                // The first is OK, multiple keys we just ignore
                invocation.setInput(
                        new ActionArgumentValue(arg, form.getFirst(arg.getName()))
                );
            }
        }
        return invocation;
    }

    public void appendOutput(ActionInvocation invocation, MultivaluedMap<String, String> form) {
        for (ActionArgumentValue value : invocation.getOutput()) {
            String stringValue = value.toString();
            form.putSingle(
                    value.getArgument().getName(),
                    stringValue.length() > 0 ? stringValue : NULL_OUTPUT_ARGUMENT_VALUE
            );
        }
    }

    public void appendOutput(ActionInvocation invocation, XHTMLElement output) {
        output.createChild(ELEMENT.span).setContent("SUCCESS");
        if (invocation.getAction().hasOutputArguments()) {
            output.createChild("hr");
            XHTMLElement outputArgs = output.createChild(ELEMENT.dl).setAttribute(ATTR.id, "output-args");
            for (ActionArgumentValue value : invocation.getOutput()) {
                outputArgs.createChild(ELEMENT.dt).setContent(value.getArgument().getName());
                String stringValue = value.toString();
                outputArgs.createChild(ELEMENT.dd).setContent(
                        stringValue.length() > 0 ? stringValue : FormActionProcessor.NULL_OUTPUT_ARGUMENT_VALUE
                );
            }
        }
    }

    public void appendFailure(ActionInvocation invocation, MultivaluedMap<String, String> form) {
        final ActionException exception = invocation.getFailure();
        if (exception != null) {
            form.putSingle("error-code", String.valueOf(exception.getErrorCode()));
            form.putSingle("error-description", exception.getMessage());
        }
    }

    public void appendFailure(ActionInvocation invocation, XHTMLElement output) {
        output.createChild(ELEMENT.span).setContent("FAILURE");
        output.createChild(ELEMENT.span).setAttribute(ATTR.id, "error-code").setContent(
                String.valueOf(invocation.getFailure().getErrorCode())
        );
        output.createChild(ELEMENT.span).setAttribute(ATTR.id, "error-description").setContent(
                invocation.getFailure().getMessage()
        );
    }

    /*
    public void readOutput(MultivaluedMap<String, String> form, ActionInvocation invocation) throws InvalidValueException {
        for (ActionArgument argument : invocation.getAction().getOutputArguments()) {
            String v = form.getFirst(argument.getName());
            invocation.setOutput(new ActionArgumentValue(argument, v));
        }
    }


    public void readFailure(MultivaluedMap<String, String> form, ActionInvocation invocation) {
        String errorCode = URIUtil.percentDecode(form.getFirst("error-code"));
        String errorDescription = URIUtil.percentDecode(form.getFirst("error-description"));
        if (errorCode == null || errorCode.length() == 0) {
            errorCode = String.valueOf(ErrorCode.ACTION_FAILED.getCode());
            errorDescription = "No error description received";
        }
        invocation.setFailure(
                new ActionException(Integer.parseInt(errorCode), errorDescription)
        );
    }
    */
    
    public void readOutput(Map<String, String> form, ActionInvocation invocation) throws InvalidValueException {
        for (ActionArgument argument : invocation.getAction().getOutputArguments()) {
            String v = form.get(argument.getName());
            invocation.setOutput(new ActionArgumentValue(argument, v));
        }
    }
    
    public void readFailure(Map<String, String> form, ActionInvocation invocation) {
        String errorCode = URIUtil.percentDecode(form.get("error-code"));
        String errorDescription = URIUtil.percentDecode(form.get("error-description"));
        if (errorCode == null || errorCode.length() == 0) {
            errorCode = String.valueOf(ErrorCode.ACTION_FAILED.getCode());
            errorDescription = "No error description received";
        }
        invocation.setFailure(
                new ActionException(Integer.parseInt(errorCode), errorDescription)
        );
    }

    public Map<String, String> createForm(ActionInvocation invocation) {
        //MultivaluedMap<String, String> form = new MultivaluedMapImpl();
    	Map<String, String> form = new HashMap<String, String>();
        Action action = invocation.getAction();
        if (action.hasInputArguments()) {
            for (ActionArgumentValue argumentValue : invocation.getInput()) {
                //form.putSingle(argumentValue.getArgument().getName(), argumentValue.toString());
            	form.put(argumentValue.getArgument().getName(), argumentValue.toString());
            }
        }
        return form;
    }

    public String createFormString(ActionInvocation invocation) {
        return toString(createForm(invocation));
    }

    public String toString(Map<String, String> form) {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<String, String> entry : form.entrySet()) {
               s.append(entry.getKey()).append("=").append(URIUtil.percentEncode(entry.getValue()));
               s.append("&");
        }
        if (s.toString().endsWith("&"))
            s.deleteCharAt(s.length() - 1);
        return s.toString();
    }

    /*
    public String toString(MultivaluedMap<String, String> form) {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : form.entrySet()) {
            for (String value : entry.getValue()) {
                s.append(entry.getKey()).append("=").append(URIUtil.percentEncode(value));
                s.append("&");
            }
        }
        if (s.toString().endsWith("&"))
            s.deleteCharAt(s.length() - 1);
        return s.toString();
    }


    public MultivaluedMap<String, String> valueOf(String s) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<String, String>();
        String[] params = s.split("&");
        for (String param : params) {
            if (param.indexOf('=') >= 0) {
                String[] nv = param.split("=");
                String val = nv.length > 1 ? nv[1] : "";
                formData.add(nv[0], URIUtil.percentDecode(val));
            } else {
                formData.add(param, "");
            }
        }
        return formData;
    }
    */
    public Map<String, String> valueOf(String s) {
        Map<String, String> formData = new HashMap<String, String>();
        String[] params = s.split("&");
        for (String param : params) {
            if (param.indexOf('=') >= 0) {
                String[] nv = param.split("=");
                String val = nv.length > 1 ? nv[1] : "";
                formData.put(nv[0], URIUtil.percentDecode(val));
            } else {
                formData.put(param, "");
            }
        }
        return formData;
    }

}
