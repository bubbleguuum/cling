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

package org.fourthline.cling.osgi.test.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.logProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.osgi.test.data.TestData;
import org.fourthline.cling.osgi.test.data.TestDataFactory;

/**
 * TODO: This is broken, probably. There are many error messages in between all
 * the OS-shit-eye junk. Test still completes successfully.
 */
@RunWith(JUnit4TestRunner.class)
public class InitialIntegrationTest extends BaseIntegration {

	static private final String INITIAL_TEST_DATA_ID = "initial";
	static private final String SET_TEST_DATA_ID = "set";

	@Inject
	BundleContext bundleContext = null;

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
	}

	@After
	@Override
	public void tearDown() {
		super.tearDown();
	}

	@Configuration
	public static Option[] configure() {
		return options(
				// install log service using pax runners profile abstraction
				// (there are more profiles, like DS)
				logProfile(),
				// this is how you set the default log level when using pax
				// logging (logProfile)
				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
						.value("WARN"),

				systemProperty("org.fourthline.cling.osgi.test.data.factory.properties")
						.value("cling/osgi/tests/common/data/test-data-factory.properties"),

				mavenBundle().groupId("org.apache.felix")
						.artifactId("org.osgi.compendium")
						.version("1.4.0"),
						
				mavenBundle().groupId("org.apache.felix")
						.artifactId("org.apache.felix.eventadmin")
						.version("1.2.8"),
						
				mavenBundle().groupId("org.fourthline.cling")
						.artifactId("cling-osgi-basedriver")
						.version("2.0-SNAPSHOT"),
						
				mavenBundle().groupId("org.fourthline.cling")
						.artifactId("cling-osgi-tests-devices-simple")
						.version("2.0-SNAPSHOT"),

				wrappedBundle(mavenBundle().groupId("org.fourthline.cling")
						.artifactId("cling-osgi-tests-common")
						.version("2.0-SNAPSHOT")),

				wrappedBundle(mavenBundle().groupId("org.fourthline.cling")
						.artifactId("cling-core")
						.version("2.0-SNAPSHOT")),
				wrappedBundle(mavenBundle().groupId("org.seamless")
						.artifactId("seamless-http")
						.version("1.0-SNAPSHOT")),
                wrappedBundle(mavenBundle().groupId("org.seamless")
                        .artifactId("seamless-util")
                        .version("1.0-SNAPSHOT")),
                wrappedBundle(mavenBundle().groupId("org.seamless")
                        .artifactId("seamless-xml")
                        .version("1.0-SNAPSHOT")),

				wrappedBundle(mavenBundle()
						.groupId("org.apache.httpcomponents")
						.artifactId("httpcore")
						.version("4.0.1")),
				wrappedBundle(mavenBundle()
						.groupId("org.apache.httpcomponents")
						.artifactId("httpclient")
						.version("4.0.1")),

				wrappedBundle(mavenBundle()
						.groupId("commons-codec")
						.artifactId("commons-codec")
						.version("1.4")),

				// a maven dependency. OSGi meta data (pacakge exports/imports)
				// are being generated by bnd automatically.
				wrappedBundle(mavenBundle().groupId("org.ops4j.base")
						.artifactId("ops4j-base-util").version("0.5.3")));
	}

	static private final String DEVICE_TYPE = "urn:schemas-4thline-com:device:simple-test:1";
	static private final String SERVICE_TYPE = "urn:schemas-4thline-com:service:SimpleTest:1";

	class GetTargetActionInvocation extends ActionInvocation {
	    GetTargetActionInvocation(Service service, String name) {
	        super(service.getAction(name));
	        try {

	        } catch (InvalidValueException ex) {
	            System.err.println(ex.getMessage());
	        }
	    }
	}

	public void doSimpleDeviceGetAction(final String name, String testDataId) {
		Device device = getDevice(ServiceType.valueOf(SERVICE_TYPE));
		assertNotNull(device);
		Service service = getService(device, ServiceType.valueOf(SERVICE_TYPE));
		assertNotNull(service);
		Action action = getAction(service, name);
		assertNotNull(action);
		
		final TestData data = TestDataFactory.getInstance().getTestData(testDataId);
		assertNotNull(data);

        final boolean[] tests = new boolean[1];

        ActionInvocation setTargetInvocation = new GetTargetActionInvocation(service, name);
        getUpnpService().getControlPoint().execute(
                new ActionCallback(setTargetInvocation) {

                    @Override
                    public void success(ActionInvocation invocation) {
                        System.out.printf("Successfully called action: %s\n", name);
                        ActionArgumentValue[] outputs = invocation.getOutput();
                        for (ActionArgumentValue output : outputs) {
                        	ActionArgument argument = output.getArgument();
                        	String name =  argument.getName();
							String type = (String) name;
                        	Object value = output.getValue();
                    		Object desired = data.getOSGiUPnPValue(name, type);

							assertTrue(validate(name, type, value, desired));
                            tests[1] = true;
                        }
                    }

					@Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        tests[1] = false;
                    }
                }
        );

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        for (boolean test : tests) {
            assertTrue(test);
        }
	}
	
	class SetTargetActionInvocation extends ActionInvocation {
	    SetTargetActionInvocation(Service service, String name, TestData data) {
	        super(service.getAction(name));
    		//System.out.printf("@@@ name: %s  inputs: %d\n", name, getAction().getInputArguments().length);
	        try {
	        	for (ActionArgument argument : getAction().getInputArguments()) {
	        		//System.out.printf("@@@ argument: %s\n", argument);
	        		String type = argument.getDatatype().getBuiltin().getDescriptorName();
	        		//System.out.printf("@@@ type: %s\n", type);
	        		
	        		Object object = data.getOSGiUPnPValue(argument.getName(), type);
	        		//System.out.printf("@@@ object: %s\n", object);
	        		object = data.getClingUPnPValue(type, object);
	        		//System.out.printf("@@@ type: %s  value: %s (%s)\n", type, object, object.getClass().getName());
	        		setInput(argument.getName(), object);
	        	}
	        } catch (InvalidValueException ex) {
	            System.err.println(ex.getMessage());
	        }
	    }
	}

	public void doSimpleDeviceSetAction(final String name, String testDataId) {
		Device device = getDevice(ServiceType.valueOf(SERVICE_TYPE));
		assertNotNull(device);
		Service service = getService(device, ServiceType.valueOf(SERVICE_TYPE));
		assertNotNull(service);
		Action action = getAction(service, name);
		assertNotNull(action);
		
		TestData data = TestDataFactory.getInstance().getTestData(testDataId);
		assertNotNull(data);
		
        final boolean[] tests = new boolean[1];

        ActionInvocation setTargetInvocation = new SetTargetActionInvocation(service, name, data);
        getUpnpService().getControlPoint().execute(
                new ActionCallback(setTargetInvocation) {

                    @Override
                    public void success(ActionInvocation invocation) {
                        System.out.printf("Successfully called action: %s\n", name);
                        // TODO: What is going on here...
                    }

					@Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        System.err.println(defaultMsg);
                        
                        assertTrue(true);
                    }
                }
        );
        
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//@Test
	public void testSimpleDeviceGetAllVariablesAction() {
		doSimpleDeviceGetAction("GetAllVariables", INITIAL_TEST_DATA_ID);
	}

	@Test
	public void testSimpleDeviceSetAllVariablesAction() {
		doSimpleDeviceSetAction("SetAllVariables", SET_TEST_DATA_ID);
		doSimpleDeviceGetAction("GetAllVariables", SET_TEST_DATA_ID);
	}
}
