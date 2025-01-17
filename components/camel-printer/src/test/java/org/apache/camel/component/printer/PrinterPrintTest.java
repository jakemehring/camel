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
package org.apache.camel.component.printer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.Sides;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.camel.util.IOHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisabledOnOs(OS.AIX)
public class PrinterPrintTest extends CamelTestSupport {
    Class<?> printServiceLookupServicesClass = PrintServiceLookup.class.getDeclaredClasses()[0];
    Object printServiceLookup;

    @BeforeEach
    public void setup() throws Exception {
        setupJavaPrint();
    }

    @AfterEach
    public void tearDown() throws Exception {
        restoreJavaPrint();
    }

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    // Check if there is an awt library
    private boolean isAwtHeadless() {
        return Boolean.getBoolean("java.awt.headless");
    }

    private void sendFile() {
        template.send("direct:start", new Processor() {
            public void process(Exchange exchange) throws Exception {
                // Read from an input stream
                InputStream is = IOHelper.buffered(new FileInputStream("src/test/resources/test.txt"));

                byte buffer[] = new byte[is.available()];
                int n = is.available();
                for (int i = 0; i < n; i++) {
                    buffer[i] = (byte) is.read();
                }

                is.close();
                // Set the property of the charset encoding
                exchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");
                Message in = exchange.getIn();
                in.setBody(buffer);
            }
        });
    }

    private void sendGIF() {
        template.send("direct:start", new Processor() {
            public void process(Exchange exchange) throws Exception {
                // Read from an input stream
                InputStream is = IOHelper.buffered(new FileInputStream("src/test/resources/asf-logo.gif"));

                byte buffer[] = new byte[is.available()];
                int n = is.available();
                for (int i = 0; i < n; i++) {
                    buffer[i] = (byte) is.read();
                }

                is.close();
                // Set the property of the charset encoding
                exchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");
                Message in = exchange.getIn();
                in.setBody(buffer);
            }
        });
    }

    private void sendJPEG() {
        template.send("direct:start", new Processor() {
            public void process(Exchange exchange) throws Exception {
                // Read from an input stream
                InputStream is = IOHelper.buffered(new FileInputStream("src/test/resources/asf-logo.JPG"));

                byte buffer[] = new byte[is.available()];
                int n = is.available();
                for (int i = 0; i < n; i++) {
                    buffer[i] = (byte) is.read();
                }

                is.close();

                // Set the property of the charset encoding
                exchange.setProperty(Exchange.CHARSET_NAME, "UTF-8");
                Message in = exchange.getIn();
                in.setBody(buffer);
            }
        });
    }

    @Test
    @Disabled
    public void testSendingFileToPrinter() throws Exception {
        assumeTrue(isAwtHeadless());

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:start").to(
                        "lpr://localhost/default?copies=1&flavor=DocFlavor.BYTE_ARRAY&mimeType=AUTOSENSE&mediaSize=na-letter&sides=one-sided&sendToPrinter=false");
            }
        });
        context.start();

        sendFile();
    }

    @Test
    @Disabled
    public void testSendingGIFToPrinter() throws Exception {
        assumeTrue(isAwtHeadless());

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:start").to(
                        "lpr://localhost/default?flavor=DocFlavor.INPUT_STREAM&mimeType=GIF&mediaSize=na-letter&sides=one-sided&sendToPrinter=false");
            }
        });
        context.start();

        sendGIF();
    }

    @Test
    @Disabled
    public void testSendingJPEGToPrinter() throws Exception {
        assumeTrue(isAwtHeadless());

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:start").to("lpr://localhost/default?copies=2&flavor=DocFlavor.INPUT_STREAM"
                                        + "&mimeType=JPEG&mediaSize=na-letter&sides=one-sided&sendToPrinter=false");
            }
        });
        context.start();

        sendJPEG();
    }

    @Test
    @Disabled
    public void testSendingJPEGToPrinterWithLandscapePageOrientation() throws Exception {
        assumeTrue(isAwtHeadless());

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:start").to("lpr://localhost/default?flavor=DocFlavor.INPUT_STREAM"
                                        + "&mimeType=JPEG&sendToPrinter=false&orientation=landscape");
            }
        });
        context.start();

        sendJPEG();
    }

    /**
     * Test for resolution of bug CAMEL-3446. Not specifying mediaSize nor sides attributes make it use default values
     * when starting the route.
     */
    @Test
    @Disabled
    public void testDefaultPrinterConfiguration() throws Exception {
        assumeTrue(isAwtHeadless());

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:start").to("lpr://localhost/default?sendToPrinter=false");
            }
        });
        context.start();
    }

    @Test
    public void moreThanOneLprEndpoint() throws Exception {
        assumeTrue(isAwtHeadless());

        int numberOfPrintservicesBefore = PrintServiceLookup.lookupPrintServices(null, null).length;

        // setup javax.print 
        PrintService ps1 = mock(PrintService.class);
        when(ps1.getName()).thenReturn("printer1");
        when(ps1.isDocFlavorSupported(any(DocFlavor.class))).thenReturn(Boolean.TRUE);
        PrintService ps2 = mock(PrintService.class);
        when(ps2.getName()).thenReturn("printer2");
        boolean res1 = PrintServiceLookup.registerService(ps1);
        assertTrue(res1, "PrintService #1 should be registered.");
        boolean res2 = PrintServiceLookup.registerService(ps2);
        assertTrue(res2, "PrintService #2 should be registered.");
        PrintService[] pss = PrintServiceLookup.lookupPrintServices(null, null);
        assertEquals(numberOfPrintservicesBefore + 2, pss.length, "lookup should report two PrintServices.");

        DocPrintJob job1 = mock(DocPrintJob.class);
        when(ps1.createPrintJob()).thenReturn(job1);

        context.addRoutes(new RouteBuilder() {

            public void configure() {
                from("direct:start1").to("lpr://localhost/printer1?sendToPrinter=true");
                from("direct:start2").to("lpr://localhost/printer2?sendToPrinter=false");
            }
        });
        context.start();

        // Are there two different PrintConfigurations?
        Map<String, Endpoint> epm = context().getEndpointMap();
        assertEquals(4, epm.size(), "Four endpoints");
        Endpoint lp1 = null;
        Endpoint lp2 = null;
        for (Map.Entry<String, Endpoint> ep : epm.entrySet()) {
            if (ep.getKey().contains("printer1")) {
                lp1 = ep.getValue();
            }
            if (ep.getKey().contains("printer2")) {
                lp2 = ep.getValue();
            }
        }
        assertNotNull(lp1);
        assertNotNull(lp2);
        assertEquals("printer1", ((PrinterEndpoint) lp1).getConfig().getPrintername());
        assertEquals("printer2", ((PrinterEndpoint) lp2).getConfig().getPrintername());

        template.sendBody("direct:start1", "Hello Printer 1");

        context.stop();

        verify(job1, times(1)).print(any(Doc.class), any(PrintRequestAttributeSet.class));
    }

    @Test
    public void printerNameTest() throws Exception {
        assumeTrue(isAwtHeadless());

        // setup javax.print
        PrintService ps1 = mock(PrintService.class);
        when(ps1.getName()).thenReturn("MyPrinter\\\\remote\\printer1");
        when(ps1.isDocFlavorSupported(any(DocFlavor.class))).thenReturn(Boolean.TRUE);
        boolean res1 = PrintServiceLookup.registerService(ps1);
        assertTrue(res1, "The Remote PrintService #1 should be registered.");
        DocPrintJob job1 = mock(DocPrintJob.class);
        when(ps1.createPrintJob()).thenReturn(job1);

        context.addRoutes(new RouteBuilder() {

            public void configure() {
                from("direct:start1").to("lpr://remote/printer1?sendToPrinter=true");
            }
        });
        context.start();

        template.sendBody("direct:start1", "Hello Printer 1");

        context.stop();

        verify(job1, times(1)).print(any(Doc.class), any(PrintRequestAttributeSet.class));
    }

    /*
     * Test for CAMEL-12890
     * Unable to send to remote printer
     * */
    @Test
    public void testSendingFileToRemotePrinter() throws Exception {
        // setup javax.print 
        PrintService ps1 = mock(PrintService.class);
        when(ps1.getName()).thenReturn("printer1");
        when(ps1.isDocFlavorSupported(any(DocFlavor.class))).thenReturn(Boolean.TRUE);
        boolean res1 = PrintServiceLookup.registerService(ps1);
        assertTrue(res1, "The Remote PrintService #1 should be registered.");
        DocPrintJob job1 = mock(DocPrintJob.class);
        when(ps1.createPrintJob()).thenReturn(job1);

        context.addRoutes(new RouteBuilder() {

            public void configure() {
                from("direct:start1").to("lpr://remote/printer1?sendToPrinter=true");
            }
        });
        context.start();

        template.sendBody("direct:start1", "Hello Printer 1");

        context.stop();

        verify(job1, times(1)).print(any(Doc.class), any(PrintRequestAttributeSet.class));
    }

    @Test
    public void setJobName() throws Exception {
        assumeTrue(isAwtHeadless());

        getMockEndpoint("mock:output").setExpectedMessageCount(1);
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:start").to("lpr://localhost/default").to("mock:output");
            }
        });
        context.start();
        template.sendBodyAndHeader("direct:start", "Hello Printer", PrinterEndpoint.JOB_NAME, "Test-Job-Name");
        context.stop();
        assertMockEndpointsSatisfied();
    }

    @Test
    public void printToMiddleTray() {
        PrinterEndpoint endpoint = new PrinterEndpoint();
        PrinterConfiguration configuration = new PrinterConfiguration();
        configuration.setHostname("localhost");
        configuration.setPort(631);
        configuration.setPrintername("DefaultPrinter");
        configuration.setMediaSizeName(MediaSizeName.ISO_A4);
        configuration.setInternalSides(Sides.ONE_SIDED);
        configuration.setInternalOrientation(OrientationRequested.PORTRAIT);
        configuration.setMediaTray("middle");

        PrinterProducer producer = new PrinterProducer(endpoint, configuration);
        producer.start();
        PrinterOperations printerOperations = producer.getPrinterOperations();
        PrintRequestAttributeSet attributeSet = printerOperations.getPrintRequestAttributeSet();

        Attribute attribute = attributeSet.get(javax.print.attribute.standard.Media.class);
        assertNotNull(attribute);
        assertTrue(attribute instanceof MediaTray);
        MediaTray mediaTray = (MediaTray) attribute;
        assertEquals("middle", mediaTray.toString());
    }

    @Test
    public void printsWithLandscapeOrientation() {
        PrinterEndpoint endpoint = new PrinterEndpoint();
        PrinterConfiguration configuration = new PrinterConfiguration();
        configuration.setHostname("localhost");
        configuration.setPort(631);
        configuration.setPrintername("DefaultPrinter");
        configuration.setMediaSizeName(MediaSizeName.ISO_A4);
        configuration.setInternalSides(Sides.ONE_SIDED);
        configuration.setInternalOrientation(OrientationRequested.REVERSE_LANDSCAPE);
        configuration.setMediaTray("middle");
        configuration.setSendToPrinter(false);

        PrinterProducer producer = new PrinterProducer(endpoint, configuration);
        producer.start();
        PrinterOperations printerOperations = producer.getPrinterOperations();
        PrintRequestAttributeSet attributeSet = printerOperations.getPrintRequestAttributeSet();

        Attribute attribute = attributeSet.get(OrientationRequested.class);
        assertNotNull(attribute);
        assertEquals("reverse-landscape", attribute.toString());
    }

    protected void setupJavaPrint() throws Exception {
        // save the current print services
        Class<?> clazz = context.getClassResolver().resolveClass("sun.awt.AppContext");
        Object ac = clazz.getMethod("getAppContext").invoke(null);
        printServiceLookup = clazz.getMethod("get", Object.class).invoke(ac, printServiceLookupServicesClass);

        // setup a new empty list of printer services
        clazz.getMethod("put", Object.class, Object.class).invoke(ac, printServiceLookupServicesClass, null);

        Method method = PrintServiceLookup.class.getDeclaredMethod("initListOfLookupServices");
        method.setAccessible(true);
        method.invoke(null);

        // "install" another default printer
        PrintService psDefault = mock(PrintService.class);
        when(psDefault.getName()).thenReturn("DefaultPrinter");
        when(psDefault.isDocFlavorSupported(any(DocFlavor.class))).thenReturn(Boolean.TRUE);
        PrintServiceLookup psLookup = mock(PrintServiceLookup.class);
        when(psLookup.getPrintServices()).thenReturn(new PrintService[] { psDefault });
        when(psLookup.getDefaultPrintService()).thenReturn(psDefault);
        DocPrintJob docPrintJob = mock(DocPrintJob.class);
        when(psDefault.createPrintJob()).thenReturn(docPrintJob);
        MediaTray[] trays = new MediaTray[] {
                MediaTray.TOP,
                MediaTray.MIDDLE,
                MediaTray.BOTTOM
        };
        when(psDefault.getSupportedAttributeValues(Media.class, null, null)).thenReturn(trays);
        PrintServiceLookup.registerServiceProvider(psLookup);
    }

    protected void restoreJavaPrint() throws Exception {
        // restore print services
        if (printServiceLookup != null) {
            Class<?> clazz = context.getClassResolver().resolveClass("sun.awt.AppContext");
            Object ac = clazz.getMethod("getAppContext").invoke(null);
            clazz.getMethod("put", Object.class, Object.class).invoke(ac, printServiceLookupServicesClass, printServiceLookup);
        }
    }

}
