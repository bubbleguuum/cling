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

package org.fourthline.cling.model.meta;

import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.model.Constants;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.types.UDN;

/**
 * Unique device name, received and offered during discovery with SSDP.
 *
 * @author Christian Bauer
 */
public class DeviceIdentity implements Validatable {

    final private UDN udn;
    final private Integer maxAgeSeconds;

    public DeviceIdentity(UDN udn, DeviceIdentity template) {
        this.udn = udn;
        this.maxAgeSeconds = template.getMaxAgeSeconds();
    }

    public DeviceIdentity(UDN udn) {
        this.udn = udn;
        this.maxAgeSeconds = Constants.MIN_ADVERTISEMENT_AGE_SECONDS;
    }

    public DeviceIdentity(UDN udn, Integer maxAgeSeconds) {
        this.udn = udn;
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public UDN getUdn() {
        return udn;
    }

    public Integer getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceIdentity that = (DeviceIdentity) o;

        if (!udn.equals(that.udn)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return udn.hashCode();
    }

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + ") UDN: " + getUdn();
    }

    @Override
    public List<ValidationError> validate() {
    	List<ValidationError> errors = new ArrayList<ValidationError>();

    	if (getUdn() == null) {
    		errors.add(new ValidationError(
    				getClass(),
    				"major",
    				"Device has no UDN"
    				));
    	}

    	return errors;
    }
}
