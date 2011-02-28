/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.examples;

import org.apache.cxf.Bus;
import org.apache.servicemix.examples.cxf.HelloWorld;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.nmr.api.*;
import org.apache.servicemix.platform.testing.support.AbstractIntegrationTest;
import org.apache.servicemix.util.FileUtil;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.jar.Manifest;

public class CamelCxfIntegrationTest extends AbstractIntegrationTest {

    private Properties dependencies;

    /**
     * The manifest to use for the "virtual bundle" created
     * out of the test classes and resources in this project
     *
     * This is actually the boilerplate manifest with one additional
     * import-package added. We should provide a simpler customization
     * point for such use cases that doesn't require duplication
     * of the entire manifest...
     */
    protected String getManifestLocation() {
        return "classpath:org/apache/servicemix/MANIFEST.MF";
    }

    /**
     * The location of the packaged OSGi bundles to be installed
     * for this test. Values are Spring resource paths. The bundles
     * we want to use are part of the same multi-project maven
     * build as this project is. Hence we use the localMavenArtifact
     * helper method to find the bundles produced by the package
     * phase of the maven build (these tests will run after the
     * packaging phase, in the integration-test phase).
     *
     * JUnit, commons-logging, spring-core and the spring OSGi
     * test bundle are automatically included so do not need
     * to be specified here.
     */
    protected String[] getTestBundlesNames() {
        return new String[] {
            getBundle("org.apache.felix", "org.apache.felix.prefs"),
            getBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.activation-api-1.1"),
            getBundle("org.apache.geronimo.specs", "geronimo-annotation_1.0_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-servlet_3.0_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-ws-metadata_2.0_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-j2ee-connector_1.5_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec"),

            //for activemq
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-lang"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jasypt"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.josql"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.oro"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.velocity"),
            getBundle("commons-collections", "commons-collections"),
            getBundle("org.springframework", "spring-jms"),
            getBundle("org.springframework", "spring-tx"),
            getBundle("org.apache.geronimo.specs", "geronimo-j2ee-management_1.1_spec"),
            getBundle("org.apache.geronimo.specs", "geronimo-jms_1.1_spec"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-pool"),
            getBundle("org.apache.xbean", "xbean-spring"),
            getBundle("org.apache.activemq", "activemq-core"),
            getBundle("org.apache.activemq", "activemq-ra"),
            getBundle("org.apache.activemq", "activemq-console"),
            getBundle("org.apache.activemq", "activemq-pool"),
            getBundle("org.apache.activemq", "kahadb"),

            //for ws-security
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.opensaml"),
            getBundle("org.apache.ws.security", "wss4j"),

            getBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.jbi-api-1.0"),
            getBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.stax-api-1.0"),
            getBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.saaj-api-1.3"),
            getBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.jaxb-api-2.2"),
            getBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.jaxws-api-2.2"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.asm"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cglib"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jaxb-impl"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.neethi"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.saaj-impl"),
            getBundle("org.codehaus.woodstox", "stax2-api"),
            getBundle("org.codehaus.woodstox", "woodstox-core-asl"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.wsdl4j"),
            getBundle("org.apache.ws.commons.schema", "XmlSchema"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.xmlresolver"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.bcel"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.xerces"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.xalan"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.xmlsec"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jetty"),
            getBundle("javax.mail", "mail"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-codec"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-httpclient"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.abdera"),
            getBundle("org.codehaus.jettison", "jettison"),
            getBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.xmlbeans"),
            getBundle("org.apache.servicemix.specs", "org.apache.servicemix.specs.jsr311-api-1.1"),
            getBundle("org.ops4j.pax.web", "pax-web-api"),
            getBundle("org.ops4j.pax.web", "pax-web-spi"),
            getBundle("org.ops4j.pax.web", "pax-web-runtime"),
            getBundle("org.ops4j.pax.web", "pax-web-jetty"),
            getBundle("org.ops4j.pax.web", "pax-web-extender-whiteboard"),
            getBundle("org.apache.servicemix", "servicemix-utils"),
            getBundle("org.fusesource.commonman", "commons-management"),
            getBundle("org.apache.karaf", "org.apache.karaf.management"),
            getBundle("org.apache.cxf", "cxf-bundle"),
            getBundle("org.apache.servicemix.cxf", "org.apache.servicemix.cxf.transport.nmr"),
            getBundle("org.apache.servicemix.nmr", "org.apache.servicemix.nmr.api"),
            getBundle("org.apache.servicemix.nmr", "org.apache.servicemix.nmr.core"),
            getBundle("org.apache.servicemix.nmr", "org.apache.servicemix.nmr.management"),
            getBundle("org.apache.servicemix.nmr", "org.apache.servicemix.nmr.osgi"),
            getBundle("org.apache.servicemix.document", "org.apache.servicemix.document"),
            getBundle("org.apache.servicemix.examples", "org.apache.servicemix.examples.itests.test-commons"),
            getBundle("org.apache.servicemix.examples", "org.apache.servicemix.examples.itests.cxf-http-osgi"),
            getBundle("org.apache.servicemix.examples", "org.apache.servicemix.examples.itests.cxf-ws-security-osgi"),
            //getBundle("org.apache.servicemix.examples", "org.apache.servicemix.examples.itests.cxf-jms-osgi"),
            getBundle("org.apache.servicemix.examples", "org.apache.servicemix.examples.itests.cxf-soap-handler-osgi"),
            getBundle("org.apache.servicemix.examples", "org.apache.servicemix.examples.itests.cxf-mtom-osgi"),
            getBundle("org.apache.servicemix.examples", "org.apache.servicemix.examples.itests.cxf-handler-cfg"),
            getBundle("org.apache.servicemix.examples", "cxf-ws-addressing"),
            getBundle("org.apache.servicemix.examples", "cxf-jaxrs"),
            getBundle("org.apache.servicemix.examples", "org.apache.servicemix.examples.itests.cxf-nmr-osgi"),

            getBundle("org.apache.camel", "camel-core"),
            getBundle("org.apache.camel", "camel-spring"),
            getBundle("org.apache.camel", "camel-cxf"),

            getBundle("org.apache.servicemix.examples", "org.apache.servicemix.examples.itests.camel-cxf-jms-osgi"),
        };
    }

    public void testJmsOsgi() throws Exception {
        Thread.sleep(5000);
        //waitOnContextCreation("org.apache.servicemix.examples.itests.camel-cxf-jms-osgi");
        //Thread.sleep(5000);
        String filter = "(&(" + "objectclass=" + HelloWorld.class.getName() + ")(TEST-BUNDLE-NAME=camel-cxf-jms-osgi))";
        ServiceReference ref = bundleContext.getServiceReferences(null, filter)[0];
        assertNotNull("Service Reference is null", ref);

        HelloWorld helloWorld = null;

        helloWorld = (HelloWorld) bundleContext.getService(ref);
        assertNotNull("Cannot find the service", helloWorld);

        System.out.println("::: About to send message ::: ");

        assertEquals("Hello Bonjour", helloWorld.sayHi("Bonjour"));

        System.out.println("::: Sent message ::: ");
    }

    protected Manifest getManifest() {
        Manifest mf = super.getManifest();
        String importP = mf.getMainAttributes().getValue(Constants.IMPORT_PACKAGE);
        mf.getMainAttributes().putValue(Constants.IMPORT_PACKAGE,
            importP + ",META-INF.cxf, org.apache.servicemix.jbi.jaxp");
        String exportP = mf.getMainAttributes().getValue(Constants.EXPORT_PACKAGE);
        mf.getMainAttributes().putValue(Constants.EXPORT_PACKAGE,
                                      exportP + ",org.apache.handlers, "
                                      + "org.apache.springcfg.handlers, "
                                      + "org.apache.cxf.mime, "
                                      + "javax.xml.ws, "
                                      + "javax.activation, "
                                      + "org.apache.handlers.types, "
                                      + "org.apache.servicemix.examples.cxf,"
                                      + "org.apache.servicemix.examples.cxf.soaphandler,"
                                      + "org.apache.servicemix.examples.cxf.springcfghandler,"
                                      + "org.apache.servicemix.examples.cxf.wsaddressing,"
                                      + "org.apache.servicemix.util,"
                                      + "org.apache.hello_world_soap_http,"
                                      + "org.apache.cxf,"
                                      + "org.apache.cxf.bus,"
                                      + "org.apache.cxf.interceptor"
                                      );
        return mf;
    }
}
