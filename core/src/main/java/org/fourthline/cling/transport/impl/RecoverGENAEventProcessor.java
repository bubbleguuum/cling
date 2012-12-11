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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.model.message.UpnpMessage;
import org.fourthline.cling.model.message.gena.IncomingEventRequestMessage;
import org.fourthline.cling.transport.spi.UnsupportedDataException;
import org.seamless.xml.XmlPullParserUtils;


/**
 * implementation based on the <em>Xml Pull Parser</em> XML processing API. 
 * <p>
 * This processor try hard to recover broken XML
 * </p>
 * @author Michael Pujos
 */
public class RecoverGENAEventProcessor extends PullGENAEventProcessor {

	private static Logger log = Logger.getLogger(RecoverGENAEventProcessor.class.getName());

	public void readBody(IncomingEventRequestMessage requestMessage) throws UnsupportedDataException {

		checkRequestBodyValidity(requestMessage);

		try {
			super.readBody(requestMessage);
		} catch(UnsupportedDataException ex) {

			log.warning("bad GENA Event XML found: " + ex);

			// some properties may have been read by the pull parser at this point, so reset the list
			requestMessage.getStateVariableValues().clear();

			String fixedBody = fixXMLEncodedLastChange(XmlPullParserUtils.fixXMLEntities(requestMessage.getBodyString()));
			requestMessage.setBody(UpnpMessage.BodyType.STRING, fixedBody);

			try {
				super.readBody(requestMessage);
				log.info("sucessfully fixed bad GENA XML");
			} catch(UnsupportedDataException ex2) {

				// check if some properties were read
				if(requestMessage.getStateVariableValues().isEmpty()) {
					// throw the initial exception containing unmodified xml
					throw ex;
				}

				log.warning("partial read of GENA event properties (probably due to truncated XML)");

			}
		}
	}

	private String fixXMLEncodedLastChange(String xml) {
		Pattern pattern = Pattern.compile("<LastChange>(.*)</LastChange>", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(xml);

		if (matcher.find() && matcher.groupCount() == 1) {

			String xmlEncodedLastChange = matcher.group(1);

			if(XmlPullParserUtils.isNullOrEmpty(xmlEncodedLastChange)) return xml;

			xmlEncodedLastChange = xmlEncodedLastChange.trim();
			
			String fixedXmlEncodedLastChange;
			
			// first look if LastChange text is XML encoded (some renderers will sent it not XML encoded)
			if(xmlEncodedLastChange.charAt(0) == '<') {
				fixedXmlEncodedLastChange = StringEscapeUtils.escapeXml(xmlEncodedLastChange);
				log.warning("fixed LastChange that was not XML encoded");
			}  else {
				// delete potential funky characters (at least found in the Philips NP2900 that inserts garbage HTML)
				fixedXmlEncodedLastChange = StringUtils.replaceChars(xmlEncodedLastChange, "<>", null);
				if(fixedXmlEncodedLastChange.equals(xmlEncodedLastChange)) {
					// no change
					return xml;
				}

				log.warning("deleted invalid characters in LastChange");
			}

			String fixedXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
			"<e:propertyset xmlns:e=\"urn:schemas-upnp-org:event-1-0\">" +
			"<e:property>" +
			"<LastChange>" +
			fixedXmlEncodedLastChange +
			"</LastChange>" +
			"</e:property>" +
			"</e:propertyset>";

			return fixedXml;

		}

		return xml;
	}

}
