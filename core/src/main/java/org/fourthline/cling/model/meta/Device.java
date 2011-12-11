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

import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.profile.ControlPointInfo;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Describes either a root or embedded device.
 *
 * @author Christian Bauer
 */
public abstract class Device<DI extends DeviceIdentity, D extends Device, S extends Service> implements Validatable {

    final private static Logger log = Logger.getLogger(Device.class.getName());

    final private DI identity;

    final private UDAVersion version;
    final private DeviceType type;
    final private DeviceDetails details;
    final private Icon[] icons;
    final protected S[] services;
    final protected D[] embeddedDevices;

    // Package mutable state
    private D parentDevice;
    
    public Device(DI identity) throws ValidationException {
        this(identity, null, null, null, null, null);
    }

    public Device(DI identity, DeviceType type, DeviceDetails details,
                  Icon[] icons, S[] services) throws ValidationException {
        this(identity, null, type, details, icons, services, null);
    }

    public Device(DI identity, DeviceType type, DeviceDetails details,
                  Icon[] icons, S[] services, D[] embeddedDevices) throws ValidationException {
        this(identity, null, type, details, icons, services, embeddedDevices);
    }

    public Device(DI identity, UDAVersion version, DeviceType type, DeviceDetails details,
                  Icon[] icons, S[] services, D[] embeddedDevices) throws ValidationException {

        this.identity = identity;
        this.version = version == null ? new UDAVersion() : version;
        this.type = type;
        this.details = details;

        boolean allNullIcons = true;
        if (icons != null) {
            for (Icon icon : icons) {
                if (icon != null) {
                    allNullIcons = false;
                    icon.setDevice(this);
                }
            }
        }
        this.icons = icons == null || allNullIcons ? new Icon[0] : icons;

        boolean allNullServices = true;
        if (services != null) {
            for (S service : services) {
                if (service != null) {
                    allNullServices = false;
                    service.setDevice(this);
                }
            }
        }
        this.services = services == null || allNullServices ? null : services;

        boolean allNullEmbedded = true;
        if (embeddedDevices != null) {
            for (D embeddedDevice : embeddedDevices) {
                if (embeddedDevice != null) {
                    allNullEmbedded = false;
                    embeddedDevice.setParentDevice(this);
                }
            }
        }
        this.embeddedDevices = embeddedDevices == null || allNullEmbedded  ? null : embeddedDevices;

        List<ValidationError> errors = validate();
        if (errors.size() > 0) {
            if (log.isLoggable(Level.FINEST)) {
                for (ValidationError error : errors) {
                    log.finest(error.toString());
                }
            }
            throw new ValidationException("Validation of device graph failed, call getErrors() on exception", errors);
        }
    }

    public DI getIdentity() {
        return identity;
    }

    public UDAVersion getVersion() {
        return version;
    }

    public DeviceType getType() {
        return type;
    }

    public DeviceDetails getDetails() {
        return details;
    }

    public DeviceDetails getDetails(ControlPointInfo info) {
        return this.getDetails();
    }

    public Icon[] getIcons() {
        return icons;
    }

    public boolean hasIcons() {
        return getIcons() != null && getIcons().length > 0;
    }

    public boolean hasServices() {
        return getServices() != null && getServices().length > 0;
    }


    public boolean hasEmbeddedDevices() {
        return getEmbeddedDevices() != null && getEmbeddedDevices().length > 0;
    }

    public D getParentDevice() {
        return parentDevice;
    }

    void setParentDevice(D parentDevice) {
        if (this.parentDevice != null)
            throw new IllegalStateException("Final value has been set already, model is immutable");
        this.parentDevice = parentDevice;
    }

    public boolean isRoot() {
        return getParentDevice() == null;
    }

    public abstract S[] getServices();

    public abstract D[] getEmbeddedDevices();

    public abstract D getRoot();

    public abstract D findDevice(UDN udn);

    public D[] findEmbeddedDevices() {
        return toDeviceArray(findEmbeddedDevices((D) this));
    }

    public D[] findDevices(DeviceType deviceType) {
        return toDeviceArray(find(deviceType, (D) this));
    }

    public D[] findDevices(ServiceType serviceType) {
        return toDeviceArray(find(serviceType, (D) this));
    }

    public Icon[] findIcons() {
        List<Icon> icons = new ArrayList();
        if (hasIcons()) {
            icons.addAll(Arrays.asList(getIcons()));
        }
        D[] embeddedDevices = findEmbeddedDevices();
        for (D embeddedDevice : embeddedDevices) {
            if (embeddedDevice.hasIcons()) {
                icons.addAll(Arrays.asList(embeddedDevice.getIcons()));
            }
        }
        return icons.toArray(new Icon[icons.size()]);
    }

    public S[] findServices() {
        return toServiceArray(findServices(null, null, (D) this));
    }

    public S[] findServices(ServiceType serviceType) {
        return toServiceArray(findServices(serviceType, null, (D) this));
    }

    protected D find(UDN udn, D current) {
        if (current.getIdentity().getUdn().equals(udn)) return current;
        if (current.hasEmbeddedDevices()) {
            for (D embeddedDevice : (D[]) current.getEmbeddedDevices()) {
                D match;
                if ((match = find(udn, embeddedDevice)) != null) return match;
            }
        }
        return null;
    }

    protected Collection<D> findEmbeddedDevices(D current) {
        Collection<D> devices = new HashSet();
        if (!current.isRoot()) {
            devices.add(current);
        }
        if (current.hasEmbeddedDevices()) {
            for (D embeddedDevice : (D[]) current.getEmbeddedDevices()) {
                devices.addAll(findEmbeddedDevices(embeddedDevice));
            }
        }
        return devices;
    }

    protected Collection<D> find(DeviceType deviceType, D current) {
        Collection<D> devices = new HashSet();
        // Type might be null if we just discovered the device and it hasn't yet been hydrated
        if (current.getType() != null && current.getType().implementsVersion(deviceType)) {
            devices.add(current);
        }
        if (current.hasEmbeddedDevices()) {
            for (D embeddedDevice : (D[]) current.getEmbeddedDevices()) {
                devices.addAll(find(deviceType, embeddedDevice));
            }
        }
        return devices;
    }

    protected Collection<D> find(ServiceType serviceType, D current) {
        Collection<S> services = findServices(serviceType, null, current);
        Collection<D> devices = new HashSet();
        for (Service service : services) {
            devices.add((D) service.getDevice());
        }
        return devices;
    }

    protected Collection<S> findServices(ServiceType serviceType, ServiceId serviceId, D current) {
        Collection services = new HashSet();
        if (current.hasServices()) {
            for (Service service : current.getServices()) {
                if (isMatch(service, serviceType, serviceId))
                    services.add(service);
            }
        }
        Collection<D> embeddedDevices = findEmbeddedDevices(current);
        if (embeddedDevices != null) {
            for (D embeddedDevice : embeddedDevices) {
                if (embeddedDevice.hasServices()) {
                    for (Service service : embeddedDevice.getServices()) {
                        if (isMatch(service, serviceType, serviceId))
                            services.add(service);
                    }
                }
            }
        }
        return services;
    }

    public S findService(ServiceId serviceId) {
        Collection<S> services = findServices(null, serviceId, (D) this);
        return services.size() == 1 ? services.iterator().next() : null;
    }

    public S findService(ServiceType serviceType) {
        Collection<S> services = findServices(serviceType, null, (D) this);
        return services.size() > 0 ? services.iterator().next() : null;
    }

    public ServiceType[] findServiceTypes() {
        Collection<S> services = findServices(null, null, (D) this);
        Collection<ServiceType> col = new HashSet();
        for (S service : services) {
            col.add(service.getServiceType());
        }
        return col.toArray(new ServiceType[col.size()]);
    }

    private boolean isMatch(Service s, ServiceType serviceType, ServiceId serviceId) {
        boolean matchesType = serviceType == null || s.getServiceType().implementsVersion(serviceType);
        boolean matchesId = serviceId == null || s.getServiceId().equals(serviceId);
        return matchesType && matchesId;
    }

    public boolean isFullyHydrated() {
        S[] services = findServices();
        for (S service : services) {
            if (service.hasStateVariables()) return true;
        }
        return false;
    }

    public String getDisplayString() {

        // The goal is to have a clean string with "<manufacturer> <model name> <model#>"

        String cleanModelName = null;
        String cleanModelNumber = null;

        if (getDetails() != null && getDetails().getModelDetails() != null) {

            // Some vendors end the model name with the model number, let's remove that
            ModelDetails modelDetails = getDetails().getModelDetails();
            if (modelDetails.getModelName() != null) {
                cleanModelName = modelDetails.getModelNumber() != null && modelDetails.getModelName().endsWith(modelDetails.getModelNumber())
                        ? modelDetails.getModelName().substring(0, modelDetails.getModelName().length() - modelDetails.getModelNumber().length())
                        : modelDetails.getModelName();
            }

            // Some vendors repeat the model name as the model number, no good
            if (cleanModelName != null) {
                cleanModelNumber = modelDetails.getModelNumber() != null && !cleanModelName.startsWith(modelDetails.getModelNumber())
                        ? modelDetails.getModelNumber()
                        : "";
            } else {
                cleanModelNumber = modelDetails.getModelNumber();
            }
        }

        StringBuilder sb = new StringBuilder();

        if (getDetails() != null && getDetails().getManufacturerDetails() != null) {

            // Some vendors repeat the manufacturer in model name, let's remove that too
            if (cleanModelName != null && getDetails().getManufacturerDetails().getManufacturer() != null) {
                cleanModelName = cleanModelName.startsWith(getDetails().getManufacturerDetails().getManufacturer())
                        ? cleanModelName.substring(getDetails().getManufacturerDetails().getManufacturer().length()).trim()
                        : cleanModelName.trim();
            }

            if (getDetails().getManufacturerDetails().getManufacturer() != null) {
                sb.append(getDetails().getManufacturerDetails().getManufacturer());
            }
        }

        sb.append((cleanModelName != null && cleanModelName.length() > 0 ? " " + cleanModelName : ""));
        sb.append((cleanModelNumber != null && cleanModelNumber.length() > 0 ? " " + cleanModelNumber.trim() : ""));
        return sb.toString();
    }

    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList();

        if (getType() != null) {

            // Only validate the graph if we have a device type - that means we validate only if there
            // actually is a fully hydrated graph, not just a discovered device of which we haven't even
            // retrieved the descriptor yet. This assumes that the descriptor will ALWAYS contain a device
            // type. Now that is a risky assumption...

            errors.addAll(getVersion().validate());

            if (getDetails() != null) {
                errors.addAll(getDetails().validate());
            }

            if (hasIcons()) {
                for (Icon icon : getIcons()) {
                    if (icon != null)
                        errors.addAll(icon.validate());
                }
            }

            if (hasServices()) {
                for (Service service : getServices()) {
                    if (service != null)
                        errors.addAll(service.validate());
                }
            }

            if (hasEmbeddedDevices()) {
                for (Device embeddedDevice : getEmbeddedDevices()) {
                    if (embeddedDevice != null)
                        errors.addAll(embeddedDevice.validate());
                }
            }
        }

        return errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (!identity.equals(device.identity)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return identity.hashCode();
    }

    public abstract D newInstance(UDN udn, UDAVersion version, DeviceType type, DeviceDetails details,
                                  Icon[] icons, S[] services, List<D> embeddedDevices) throws ValidationException;

    public abstract S newInstance(ServiceType serviceType, ServiceId serviceId,
                                  URI descriptorURI, URI controlURI, URI eventSubscriptionURI,
                                  Action<S>[] actions, StateVariable<S>[] stateVariables) throws ValidationException;

    public abstract D[] toDeviceArray(Collection<D> col);

    public abstract S[] newServiceArray(int size);

    public abstract S[] toServiceArray(Collection<S> col);

    public abstract Resource[] discoverResources(Namespace namespace);

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + ") Identity: " + getIdentity().toString() + ", Root: " + isRoot();
    }
}
