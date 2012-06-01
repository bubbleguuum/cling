package org.fourthline.cling.binding.xml;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;

public class RecoverUDA10DeviceDescriptorBinderSAXImpl extends UDA10DeviceDescriptorBinderSAXImpl {

	private static Logger log = Logger.getLogger(RecoverUDA10DeviceDescriptorBinderSAXImpl.class.getName());
	
	@Override
    public <D extends Device> D describe(D undescribedDevice, String descriptorXml) throws DescriptorBindingException, ValidationException {
		
		DescriptorBindingException firstException = null;

		try {

			for(int retryCount = 0 ; retryCount < 5 ; retryCount++) {

				try {
					return super.describe(undescribedDevice, descriptorXml);
				} catch(DescriptorBindingException e) {
					
					// Windows: org.fourthline.cling.binding.xml.DescriptorBindingException: Could not parse device descriptor: org.seamless.xml.ParserException: org.xml.sax.SAXParseException: The prefix "dlna" for element "dlna:X_DLNADOC" is not bound.
					// Android: org.xmlpull.v1.XmlPullParserException: undefined prefix: dlna (position:START_TAG <{null}dlna:X_DLNADOC>@19:17 in java.io.StringReader@406dff48)
					
					if(firstException == null) {
						firstException = e;
					}

					Throwable cause = e.getCause();

					if(!(e.getCause() instanceof  org.seamless.xml.ParserException)) throw e;
					
					String message = cause.getMessage();
					if(message == null) throw e;
					
					Pattern pattern = Pattern.compile("The prefix \"(.*)\" for element"); // on Windows
					Matcher matcher = pattern.matcher(message);
					if (!matcher.find() || matcher.groupCount() != 1) {
						pattern = Pattern.compile("undefined prefix: ([^ ]*)"); // on Android
						matcher = pattern.matcher(message);
						if (!matcher.find() || matcher.groupCount() != 1) throw e;
					}
					
					String missingNS = matcher.group(1);
					log.warning("detected missing namespace declaration: " + missingNS);
					
					// extract <root> attrbiutes
					pattern = Pattern.compile("<root([^>]*)");
					matcher = pattern.matcher(descriptorXml);
					if (!matcher.find() || matcher.groupCount() != 1) throw e;
					
					String rootAttributes =  matcher.group(1);

					// extract <root> body
					pattern = Pattern.compile("<root[^>]*>(.*)</root>", Pattern.DOTALL);
					matcher = pattern.matcher(descriptorXml);
					if (!matcher.find() || matcher.groupCount() != 1) throw e;

					String rootBody = matcher.group(1);
					
					// add missing ns. It only matters that it is defined, not that it is correct
					descriptorXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
							+ "<root " + String.format("xmlns:%s=\"urn:schemas-dlna-org:device-1-0\"", missingNS) +	rootAttributes + ">" +
							rootBody + 
							"</root>" ;

				}
			}

			throw firstException;

		} catch(DescriptorBindingException e) {
			onInvalidXML(descriptorXml, firstException);
			throw e;
		} catch(ValidationException e) {
			onInvalidXML(descriptorXml, e);
			throw e;
		}
	}
	
	// override in subclass if you want to log non-parsing XML
	protected void onInvalidXML(String xml, Exception e) {
	}
	
}
