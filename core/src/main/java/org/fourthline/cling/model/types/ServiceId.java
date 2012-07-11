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

package org.fourthline.cling.model.types;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fourthline.cling.model.Constants;

/**
 * Represents a service identifer, for example <code>urn:my-domain-namespace:serviceId:MyService123</code>
 *
 * @author Christian Bauer
 */
public class ServiceId {

	final private static Logger log = Logger.getLogger(ServiceId.class.getName());

	public static final Pattern PATTERN =
			Pattern.compile("urn:(" + Constants.REGEX_NAMESPACE + "):serviceId:(" + Constants.REGEX_ID+ ")");
	
	
	public static final Pattern BROKEN_PATTERN =
            Pattern.compile("urn:(" + Constants.REGEX_NAMESPACE + "):service:(" + Constants.REGEX_ID+ ")"); // Note: 'service' vs. 'serviceId'
	
	public static final Pattern BROKEN_PATTERN_EYECON = 
			Pattern.compile("urn:upnp-orgerviceId:urnchemas-upnp-orgervice:(" + Constants.REGEX_ID+ ")"); // talk about being broken...
	
	private String namespace;
	private String id;

	public ServiceId(String namespace, String id) {
		if (namespace != null && !namespace.matches(Constants.REGEX_NAMESPACE)) {
			throw new IllegalArgumentException("Service ID namespace contains illegal characters");
		}
		this.namespace = namespace;

		if (id != null && !id.matches(Constants.REGEX_ID)) {
			throw new IllegalArgumentException("Service ID suffix too long (64) or contains illegal characters");
		}
		this.id = id;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getId() {
		return id;
	}

	public static ServiceId valueOf(String s) throws InvalidValueException {

		ServiceId serviceId = null;

		// First try UDAServiceId parse
		try {
			serviceId = UDAServiceId.valueOf(s);
		} catch (Exception ex) {
			// Ignore
		}

		// Now try a generic ServiceId parse
		if (serviceId == null) {
			Matcher matcher = ServiceId.PATTERN.matcher(s);
			if (matcher.matches()) {
				return new ServiceId(matcher.group(1), matcher.group(2));
			} else {
				matcher = ServiceId.BROKEN_PATTERN.matcher(s);
				if (matcher.matches()) {
					return new ServiceId(matcher.group(1), matcher.group(2));
				} else {
					matcher = ServiceId.BROKEN_PATTERN_EYECON.matcher(s);
					if (matcher.matches()) {
						return new ServiceId(UDAServiceId.DEFAULT_NAMESPACE, matcher.group(1));
					} else {
						throw new InvalidValueException("Can't parse Service ID string (namespace/id): " + s);
					}
				}
			}
		}
		return serviceId;
	}

	@Override
	public String toString() {
		return "urn:" + getNamespace() + ":serviceId:" + getId();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof ServiceId)) return false;

		ServiceId serviceId = (ServiceId) o;

		if (!id.equals(serviceId.id)) return false;
		if (!namespace.equals(serviceId.namespace)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = namespace.hashCode();
		result = 31 * result + id.hashCode();
		return result;
	}
}
