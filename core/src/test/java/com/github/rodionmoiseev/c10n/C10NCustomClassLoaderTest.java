/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package com.github.rodionmoiseev.c10n;

import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class C10NCustomClassLoaderTest {
    @Test
    public void customProxyClassLoaderIsUsedWhenSpecified() {
        C10NMsgFactory msg = C10N.createMsgFactory(new C10NConfigBase() {
            @Override
            protected void configure() {
                setProxyClassLoader(new MyClassLoader());
            }
        });

        try {
            msg.get(MyMessages.class);
            fail("Expected to throw class loading exception");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("MyMessages is not visible from class loader"));
        }
    }

    private interface MyMessages {
    }

    private static final class MyClassLoader extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            throw new ClassNotFoundException("MyClassLoader: " + name + " not found");
        }
    }
}
