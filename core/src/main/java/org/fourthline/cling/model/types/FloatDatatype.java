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

/**
 * @author Christian Bauer
 */
public class FloatDatatype extends AbstractDatatype<Float> {

    public FloatDatatype() {
    }

    @Override
    public boolean isHandlingJavaType(Class type) {
        return type == Float.TYPE || Float.class.isAssignableFrom(type);
    }

    public Float valueOf(String s) throws InvalidValueException {
        if (s.equals("")) return null;
        try {
            return Float.parseFloat(s.trim());
        } catch (NumberFormatException ex) {
            throw new InvalidValueException("Can't convert string to number: " + s, ex);
        }
    }

}
