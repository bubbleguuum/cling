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

package org.fourthline.cling.transport.impl;

import java.util.logging.Logger;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpMessage.BodyType;
import org.fourthline.cling.model.message.control.ActionRequestMessage;
import org.fourthline.cling.model.message.control.ActionResponseMessage;
import org.fourthline.cling.transport.spi.UnsupportedDataException;
import org.seamless.xml.XmlPullParserUtils;

/**
 * implementation based on the <em>Xml Pull Parser</em> XML processing API. 
 * <p>
 * This processor try to recover broken XML
 * </p>
 * @author Michael Pujos
 */
public abstract class RecoverSOAPActionProcessor extends PullSOAPActionProcessor {

	private static Logger log = Logger.getLogger(RecoverSOAPActionProcessor.class.getName());

	public void readBody(ActionRequestMessage requestMessage, ActionInvocation actionInvocation) throws UnsupportedDataException {

		
		checkActionMessageBodyValidity(requestMessage);
		
		try {
			super.readBody(requestMessage, actionInvocation);
		} catch(UnsupportedDataException ex) {

			// There's many broken XML out there sent by various software failing to properly encode it
			// This simple fix will detect '&' characters in the body that are not XML entities and will encode it 
			// properly if necessary

			// This fix was done initially to workaround a TwonkyMobile bug:
			// TwonkyMobile sends unencoded URL as the first parameter of SetAVTransportURI, and this gives unparsable XML
			// if the URL has a query with parameters
			//
			// Here's the broken XML sent by TwonkyMobile:
			//
			// <s:Envelope
			// ...
			//	<u:SetAVTransportURI
			//	    ...
			//		<CurrentURI>http://192.168.1.14:56923/content/12a470d854dbc6887e4103e3140783fd.wav?profile_id=0&convert=wav</CurrentURI>
			//			

			log.warning("bad SOAP XML request: " + ex);

			String fixedBody = XmlPullParserUtils.fixXMLEntities(requestMessage.getBodyString().trim());
			requestMessage.setBody(BodyType.STRING, fixedBody);

			try {
				super.readBody(requestMessage, actionInvocation);
			} catch(UnsupportedDataException ex2) {
				// throw the initial exception 
				onInvalidSOAP(actionInvocation, (String)ex2.getData(), ex);
				throw ex;
			}
		}

	}

	public void readBody(ActionResponseMessage responseMsg, ActionInvocation actionInvocation) throws UnsupportedDataException {

		checkActionMessageBodyValidity(responseMsg);

		try {
			super.readBody(responseMsg, actionInvocation);
		} catch(UnsupportedDataException ex) {

			log.warning("bad SOAP XML response: " + ex);
			String fixedBody = XmlPullParserUtils.fixXMLEntities(responseMsg.getBodyString().trim());

			if(fixedBody.endsWith("</s:Envelop")) {
				// YAMAHA NP-S2000 does not terminate XML with </s:Envelope> (at least for SOAP action GetPositionInfo())
				fixedBody += "e>";
			}
			responseMsg.setBody(BodyType.STRING, fixedBody);
			
			try {
				super.readBody(responseMsg, actionInvocation);
			} catch(UnsupportedDataException ex2) {
				// throw the initial exception 
				onInvalidSOAP(actionInvocation, (String)ex2.getData(), ex);
				throw ex;
			}
		}

	}

	abstract protected void onInvalidSOAP(ActionInvocation actionInvocation, String xml, Exception e);


}
