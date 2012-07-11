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

package org.fourthline.cling.test.resources;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.RecoverUDA10DeviceDescriptorBinderSAXImpl;
import org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderImpl;
import org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderSAXImpl;
import org.fourthline.cling.mock.MockUpnpService;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.profile.ControlPointInfo;
import org.fourthline.cling.test.data.SampleData;
import org.fourthline.cling.test.data.SampleDeviceRoot;
import org.seamless.util.io.IO;
import org.testng.annotations.Test;


public class UDA10DeviceDescriptorParsingTest {
	
	
	/*  missing event sub URL
	@Test
	public void readRecoverUDA10DescriptorSAXPMS() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/pms.xml")));
	}
	*/
	
	/* empty deviceType
	@Test
	public void readRecoverUDA10DescriptorSAX_awox() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/awox.xml")));
	}
	*/

	/* invalid and unrecoverable service ID

	@Test
	public void readRecoverUDA10DescriptorSAX_Philips() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/philips.xml")));
	}
	@Test
	public void readRecoverUDA10DescriptorSAX_Kodak() throws Exception {
		
		//<icon></icon> giving NPE

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/kodak.xml")));
	}
	
	@Test
	public void readRecoverUDA10DescriptorSAX_UMS() throws Exception {
		// missing event sub URL
		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/ums.xml")));
	}
	
	*/

	@Test
	public void readRecoverUDA10DescriptorSAX_ReadyDLNA() throws Exception {
		// badly encoded XML entities
		
		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/readydlna.xml")));
	}
	
	
	
	@Test
	public void readRecoverUDA10DescriptorSAX_Plutinosoft() throws Exception {
		
		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/plutinosoft.xml")));
	}

	
	@Test
	public void readRecoverUDA10DescriptorSAX_Escient() throws Exception {
		
		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/escient.xml")));
	}

	
	@Test
	public void readRecoverUDA10DescriptorSAX_EyeCon() throws Exception {
		
		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/eyecon.xml")));
	}
	
	
	@Test
	public void readRecoverUDA10DescriptorSAX_Samsung() throws Exception {
		
		//<icon></icon> giving NPE

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/samsung.xml")));
	}
	
	@Test
	public void readRecoverUDA10DescriptorSAX_Aquos_BD() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/perfectwave.xml")));
	}
	
	@Test
	public void readRecoverUDA10DescriptorSAX_PerfectWave() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/perfectwave.xml")));
	}
	
	
	@Test
	public void readRecoverUDA10DescriptorSAX_Ceton_InfiniTV() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/ceton_infinitv.xml")));
	}
	
	@Test
	public void readRecoverUDA10DescriptorSAX_TPG() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/tpg.xml")));
	}
	
	@Test
	public void readRecoverUDA10DescriptorSAX_LG() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/lg.xml")));
	}
	
	@Test
	public void readRecoverUDA10DescriptorSAX_Zyxel_MiViewTV() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/zyxel_miviewtv.xml")));
	}
	
	@Test
	public void readRecoverUDA10DescriptorSAX_ATB_MiViewTV() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/atb_miviewtv.xml")));
	}
	
	@Test
	public void readRecoverUDA10DescriptorSAX_EyeTVNetStreamSat() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/eyetv_netstream_sat.xml")));
	}
	
	@Test
	public void readRecoverUDA10DescriptorSAX_DoubleTwist() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/doubletwist.xml")));
	}
	
	@Test
	public void readRecoverUDA10DescriptorSAX_ushare() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/ushare.xml")));
	}
	
	@Test
	public void readRecoverUDA10DescriptorSAX_MultipleNSMissing() throws Exception {

		DeviceDescriptorBinder binder = new RecoverUDA10DeviceDescriptorBinderSAXImpl();

		RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
		binder.describe(device, IO.readLines(getClass().getResourceAsStream("/brokenxml/devicedesc/multiplensmissing.xml")));
	}


    @Test
    public void readUDA10DescriptorDOM() throws Exception {

        DeviceDescriptorBinder binder = new UDA10DeviceDescriptorBinderImpl();

        RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        device = binder.describe(device, IO.readLines(getClass().getResourceAsStream("/test-dvc-uda10.xml")));

        SampleDeviceRoot.assertLocalResourcesMatch(
                new MockUpnpService().getConfiguration().getNamespace().getResources(device)
        );
        SampleDeviceRoot.assertMatch(device, SampleData.createRemoteDevice());

    }

    @Test
    public void readUDA10DescriptorSAX() throws Exception {

        DeviceDescriptorBinder binder = new UDA10DeviceDescriptorBinderSAXImpl();

        RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        device = binder.describe(device, IO.readLines(getClass().getResourceAsStream("/test-dvc-uda10.xml")));

        SampleDeviceRoot.assertLocalResourcesMatch(
                new MockUpnpService().getConfiguration().getNamespace().getResources(device)
        );
        SampleDeviceRoot.assertMatch(device, SampleData.createRemoteDevice());

    }

    @Test
    public void writeUDA10Descriptor() throws Exception {

        MockUpnpService upnpService = new MockUpnpService();
        DeviceDescriptorBinder binder = new UDA10DeviceDescriptorBinderImpl();
        
        RemoteDevice device = SampleData.createRemoteDevice();
        String descriptorXml = binder.generate(
                device,
                new ControlPointInfo(),
                upnpService.getConfiguration().getNamespace()
        );

/*
        System.out.println("#######################################################################################");
        System.out.println(descriptorXml);
        System.out.println("#######################################################################################");
*/

        RemoteDevice hydratedDevice = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        hydratedDevice = binder.describe(hydratedDevice, descriptorXml);

        SampleDeviceRoot.assertLocalResourcesMatch(
                upnpService.getConfiguration().getNamespace().getResources(hydratedDevice)

        );
        SampleDeviceRoot.assertMatch(hydratedDevice, device);

    }

    @Test
    public void writeUDA10DescriptorWithProvider() throws Exception {

        MockUpnpService upnpService = new MockUpnpService();
        DeviceDescriptorBinder binder = new UDA10DeviceDescriptorBinderImpl();

        LocalDevice device = SampleData.createLocalDevice(true);
        String descriptorXml = binder.generate(
                device,
                new ControlPointInfo(),
                upnpService.getConfiguration().getNamespace()
        );


        //System.out.println("#######################################################################################");
        //System.out.println(descriptorXml);
        //System.out.println("#######################################################################################");


        RemoteDevice hydratedDevice = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        hydratedDevice = binder.describe(hydratedDevice, descriptorXml);

        SampleDeviceRoot.assertLocalResourcesMatch(
                upnpService.getConfiguration().getNamespace().getResources(hydratedDevice)

        );
        //SampleDeviceRoot.assertMatch(hydratedDevice, device, false);

    }

    @Test
    public void readUDA10DescriptorWithURLBase() throws Exception {
        MockUpnpService upnpService = new MockUpnpService();
        DeviceDescriptorBinder binder = upnpService.getConfiguration().getDeviceDescriptorBinderUDA10();

        RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        device = binder.describe(
                device,
                IO.readLines(getClass().getResourceAsStream("/test-dvc-uda10-withbase.xml"))
        );

        assertEquals(
                device.normalizeURI(device.getDetails().getManufacturerDetails().getManufacturerURI()).toString(),
                SampleData.getLocalBaseURL().toString() + "mfc.html"
        );
        assertEquals(
                device.normalizeURI(device.getDetails().getModelDetails().getModelURI()).toString(),
                SampleData.getLocalBaseURL().toString() + "someotherbase/MY-DEVICE-123/model.html"
        );
        assertEquals(
                device.normalizeURI(device.getDetails().getPresentationURI()).toString(),
                "http://www.4thline.org/some_ui"
        );

        assertEquals(
                device.normalizeURI(device.getIcons()[0].getUri()).toString(),
                SampleData.getLocalBaseURL().toString() + "someotherbase/MY-DEVICE-123/icon.png"
        );

        assertEquals(device.normalizeURI(
                device.getServices()[0].getDescriptorURI()).toString(),
                     SampleData.getLocalBaseURL().toString() + "someotherbase/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/desc.xml"
        );
        assertEquals(
                device.normalizeURI(device.getServices()[0].getControlURI()).toString(),
                SampleData.getLocalBaseURL().toString() + "someotherbase/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/control"
        );
        assertEquals(
                device.normalizeURI(device.getServices()[0].getEventSubscriptionURI()).toString(),
                SampleData.getLocalBaseURL().toString() + "someotherbase/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/events"
        );

        assertTrue(device.isRoot());
    }

    @Test
    public void readUDA10DescriptorWithURLBase2() throws Exception {
        MockUpnpService upnpService = new MockUpnpService();
        DeviceDescriptorBinder binder = upnpService.getConfiguration().getDeviceDescriptorBinderUDA10();

        RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        device = binder.describe(
                device,
                IO.readLines(getClass().getResourceAsStream("/test-dvc-uda10-withbase2.xml"))
        );

        assertEquals(
                device.normalizeURI(device.getDetails().getManufacturerDetails().getManufacturerURI()).toString(),
                SampleData.getLocalBaseURL().toString() + "mfc.html"
        );

        assertEquals(
                device.normalizeURI(device.getDetails().getModelDetails().getModelURI()).toString(),
                SampleData.getLocalBaseURL().toString() + "model.html"
        );
        assertEquals(
                device.normalizeURI(device.getDetails().getPresentationURI()).toString(),
                "http://www.4thline.org/some_ui"
        );

        assertEquals(
                device.normalizeURI(device.getIcons()[0].getUri()).toString(),
                SampleData.getLocalBaseURL().toString() + "icon.png"
        );

        assertEquals(device.normalizeURI(
                device.getServices()[0].getDescriptorURI()).toString(),
                     SampleData.getLocalBaseURL().toString() + "svc.xml"
        );
        assertEquals(
                device.normalizeURI(device.getServices()[0].getControlURI()).toString(),
                SampleData.getLocalBaseURL().toString() + "control"
        );
        assertEquals(
                device.normalizeURI(device.getServices()[0].getEventSubscriptionURI()).toString(),
                SampleData.getLocalBaseURL().toString() + "events"
        );

        assertTrue(device.isRoot());
    }

}

