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
package org.apache.camel.component.netty;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NettyTextlineInOutSynchronousTest extends BaseNettyTest {

    private static String beforeThreadName;
    private static String afterThreadName;

    @Test
    public void testSynchronous() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("Bye World");

        String reply = template.requestBody("direct:start", "Hello World", String.class);
        assertEquals("Bye World", reply);

        assertMockEndpointsSatisfied();

        assertTrue(beforeThreadName.equalsIgnoreCase(afterThreadName), "Should use same threads");
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                        .to("log:before")
                        .process(new Processor() {
                            public void process(Exchange exchange) {
                                beforeThreadName = Thread.currentThread().getName();
                            }
                        })
                        .to("netty:tcp://localhost:{{port}}?textline=true&sync=true&synchronous=true")
                        .process(new Processor() {
                            public void process(Exchange exchange) {
                                afterThreadName = Thread.currentThread().getName();
                            }
                        })
                        .to("log:after")
                        .to("mock:result");

                from("netty:tcp://localhost:{{port}}?textline=true&sync=true&synchronous=true")
                        // body should be a String when using textline codec
                        .validate(body().isInstanceOf(String.class))
                        .transform(body().regexReplaceAll("Hello", "Bye"));
            }
        };
    }
}
