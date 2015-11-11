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

package com.github.rodionmoiseev.c10n.plugins.logging;

import com.github.rodionmoiseev.c10n.C10NMessages;
import com.github.rodionmoiseev.c10n.TestUtil;
import com.github.rodionmoiseev.c10n.annotations.En;
import com.github.rodionmoiseev.c10n.plugin.PluginResult;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@SuppressWarnings("unused")
public class LoggingPluginTest {

    @C10NMessages
    @Logger(LoggingPluginTest.class)
    @Level(LoggingLevel.ERROR)
    public interface LoggingMessagesWithClassDecls {
        void usesClassLogger();

        @Logger(name = "methodLogger")
        @Level(LoggingLevel.TRACE)
        void usesMethodLogger();

        void withArg(String name);

        @Level(LoggingLevel.WARN)
        void withCause(String name, Throwable cause);
    }

    @Logger
    @C10NMessages
    public interface LoggingMessagesWithoutClassDecls {
        void defaultLogger();

        @Logger(name = "methodLogger")
        @Level(LoggingLevel.TRACE)
        void usesMethodLogger();
    }

    @C10NMessages
    public interface UnrelatedC10NMessageBundle {
        @En("noArg")
        String noArg();

        @En("withArg")
        String withArg(String name);
    }

    @Test
    public void loggingRequestsInterruptThePluginPipeline() throws Exception {
        LoggerImplementation logger = Mockito.mock(LoggerImplementation.class);
        LoggingPlugin plugin = new LoggingPlugin(LoggingLevel.INFO, logger);
        PluginResult res = plugin.format(LoggingMessagesWithClassDecls.class, TestUtil.method(LoggingMessagesWithClassDecls.class, "usesClassLogger"), null, "test message");
        assertThat(res.isInterrupt(), is(equalTo(true)));
    }

    @Test
    public void classDeclarationsAreTakenIntoAccountUnlessOveriddenOnMethod() throws Exception {
        LoggerImplementation logger = Mockito.mock(LoggerImplementation.class);
        LoggingPlugin plugin = new LoggingPlugin(LoggingLevel.INFO, logger);
        plugin.format(LoggingMessagesWithClassDecls.class, TestUtil.method(LoggingMessagesWithClassDecls.class, "usesClassLogger"), null, "test message");
        verify(logger).log(
                eq(LoggingPluginTest.class.getName()),
                eq(LoggingLevel.ERROR),
                eq("test message"),
                eq(null),
                any(LoggingPlugin.InvocationDetails.class));

        plugin.format(LoggingMessagesWithClassDecls.class, TestUtil.method(LoggingMessagesWithClassDecls.class, "usesMethodLogger"), null, "test message");
        verify(logger).log(
                eq("methodLogger"),
                eq(LoggingLevel.TRACE),
                eq("test message"),
                eq(null),
                any(LoggingPlugin.InvocationDetails.class));
    }

    @Test
    public void defaultDeclarationsAreUsedUnlessOveriddenOnMethod() throws Exception {
        LoggerImplementation logger = Mockito.mock(LoggerImplementation.class);
        LoggingPlugin plugin = new LoggingPlugin(LoggingLevel.INFO, logger);
        plugin.format(LoggingMessagesWithoutClassDecls.class, TestUtil.method(LoggingMessagesWithoutClassDecls.class, "defaultLogger"), null, "test message");
        verify(logger).log(
                eq(LoggingMessagesWithoutClassDecls.class.getName()),
                eq(LoggingLevel.INFO),
                eq("test message"),
                eq(null),
                any(LoggingPlugin.InvocationDetails.class));

        plugin.format(LoggingMessagesWithoutClassDecls.class, TestUtil.method(LoggingMessagesWithoutClassDecls.class, "usesMethodLogger"), null, "test message");
        verify(logger).log(
                eq("methodLogger"),
                eq(LoggingLevel.TRACE),
                eq("test message"),
                eq(null),
                any(LoggingPlugin.InvocationDetails.class));
    }

    @Test
    public void methodArgumentsArePassedInsideInvocationDetails() throws Exception {
        LoggerImplementation logger = Mockito.mock(LoggerImplementation.class);
        LoggingPlugin plugin = new LoggingPlugin(LoggingLevel.INFO, logger);
        Method withArgMethod = TestUtil.method(LoggingMessagesWithClassDecls.class, "withArg");
        Object[] args = {"rodion"};
        plugin.format(LoggingMessagesWithClassDecls.class, withArgMethod, args, "test message");
        verify(logger).log(
                eq(LoggingPluginTest.class.getName()),
                eq(LoggingLevel.ERROR),
                eq("test message"),
                eq(null),
                eq(new LoggingPlugin.InvocationDetails(LoggingMessagesWithClassDecls.class, withArgMethod, args)));
    }

    @Test
    public void lastThrowableArgumentIsTreatedAsCause() throws Exception {
        LoggerImplementation logger = Mockito.mock(LoggerImplementation.class);
        LoggingPlugin plugin = new LoggingPlugin(LoggingLevel.INFO, logger);
        Object[] args = {"rodion", new AnException("intentional")};
        plugin.format(LoggingMessagesWithClassDecls.class, TestUtil.method(LoggingMessagesWithClassDecls.class, "withCause"), args, "message and stack");
        verify(logger).log(
                eq(LoggingPluginTest.class.getName()),
                eq(LoggingLevel.WARN),
                eq("message and stack"),
                eq(new AnException("intentional")),
                any(LoggingPlugin.InvocationDetails.class));
    }

    @Test
    public void unrelatedC10NMessageClassesAreNotPassedThroughThePlugin() {
        LoggerImplementation logger = Mockito.mock(LoggerImplementation.class);
        LoggingPlugin plugin = new LoggingPlugin(LoggingLevel.INFO, logger);
        PluginResult res = plugin.format(UnrelatedC10NMessageBundle.class, TestUtil.method(UnrelatedC10NMessageBundle.class, "noArg"), null, "noArg");
        assertThat(res.isInterrupt(), is(equalTo(false)));
        assertThat(res.getValue(), is(equalTo("noArg")));
        verifyZeroInteractions(logger);
    }

    private static final class AnException extends Exception {
        AnException(String message) {
            super(message);
        }

        @Override
        public int hashCode() {
            return getMessage().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof AnException) && ((AnException) obj).getMessage().equals(this.getMessage());
        }
    }
}