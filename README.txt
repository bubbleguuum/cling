The apache transport implementation found in org.fourthline.cling.transport.impl.apache
uses repackaged httpclient/httpcore 4.1.1 (otherwise it conflicts with Android Apache classes which 
are from an earlier and old version)

To install the repackaged httpclient/httpcore jars found in lib/ into your local repository, issue the following maven commands: 

mvn install:install-file -DgroupId=com.bubblesoft.org.apache.httpcomponents -DartifactId=httpclient-repackaged -Dversion=4.1.1 -Dpackaging=jar -Dfile=lib/httpclient-repackaged-4.1.1.jar
mvn install:install-file -DgroupId=com.bubblesoft.org.apache.httpcomponents -DartifactId=httpcore-repackaged -Dversion=4.1.1 -Dpackaging=jar -Dfile=lib/httpcore-repackaged-4.1.1.jar

Moreover, in your Android project you will need to add repackaged-commons-logging-1.1.1.jar as a dependency, as it is referenced by both jars above.

In this version of Cling, local devices *do not* advertise themselves on the LAN by default (like they do in regular Cling).
To have regular Cling behaviour, you must call setAdvertising(true) on the local device:

LocalDevice localDevice = ...
localDevice.setAdvertising(true);


 