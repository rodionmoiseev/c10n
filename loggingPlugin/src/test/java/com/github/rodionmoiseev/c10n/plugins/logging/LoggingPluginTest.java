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
import com.github.rodionmoiseev.c10n.InvocationDetails;
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
import static org.mockito.Mockito.*;

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

    @Logger
    @C10NMessages
    public interface WithUtils extends LoggingBase {
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
        PluginResult res = plugin.format("test message", "test message",
                new InvocationDetails(null,
                        LoggingMessagesWithClassDecls.class,
                        TestUtil.method(LoggingMessagesWithClassDecls.class, "usesClassLogger"),
                        null));
        assertThat(res.isInterrupt(), is(equalTo(true)));
    }

    @Test
    public void classDeclarationsAreTakenIntoAccountUnlessOveriddenOnMethod() throws Exception {
        LoggerImplementation logger = Mockito.mock(LoggerImplementation.class);
        LoggingPlugin plugin = new LoggingPlugin(LoggingLevel.INFO, logger);

        check(plugin, "test message", LoggingMessagesWithClassDecls.class, "usesClassLogger", null,
                logger, LoggingPluginTest.class.getName(), LoggingLevel.ERROR);

        check(plugin, "test message", LoggingMessagesWithClassDecls.class, "usesMethodLogger", null,
                logger, "methodLogger", LoggingLevel.TRACE);
    }

    @Test
    public void defaultDeclarationsAreUsedUnlessOveriddenOnMethod() throws Exception {
        LoggerImplementation logger = Mockito.mock(LoggerImplementation.class);
        LoggingPlugin plugin = new LoggingPlugin(LoggingLevel.INFO, logger);

        check(plugin, "test message", LoggingMessagesWithoutClassDecls.class, "defaultLogger", null,
                logger, LoggingMessagesWithoutClassDecls.class.getName(), LoggingLevel.INFO);

        check(plugin, "test message", LoggingMessagesWithoutClassDecls.class, "usesMethodLogger", null,
                logger, "methodLogger", LoggingLevel.TRACE);
    }

    @Test
    public void methodArgumentsArePassedInsideInvocationDetails() throws Exception {
        LoggerImplementation logger = Mockito.mock(LoggerImplementation.class);
        LoggingPlugin plugin = new LoggingPlugin(LoggingLevel.INFO, logger);
        Method withArgMethod = TestUtil.method(LoggingMessagesWithClassDecls.class, "withArg");
        Object[] args = {"rodion"};

        check(plugin, "test message", LoggingMessagesWithClassDecls.class, "withArg", args,
                logger, LoggingPluginTest.class.getName(), LoggingLevel.ERROR);
    }

    @Test
    public void lastThrowableArgumentIsTreatedAsCause() throws Exception {
        LoggerImplementation logger = Mockito.mock(LoggerImplementation.class);
        LoggingPlugin plugin = new LoggingPlugin(LoggingLevel.INFO, logger);
        Object[] args = {"rodion", new AnException("intentional")};

        check(plugin, "message and stack", LoggingMessagesWithClassDecls.class, "withCause", args, new AnException("intentional"),
                logger, LoggingPluginTest.class.getName(), LoggingLevel.WARN);
    }

    @Test
    public void unrelatedC10NMessageClassesAreNotPassedThroughThePlugin() {
        LoggerImplementation logger = Mockito.mock(LoggerImplementation.class);
        LoggingPlugin plugin = new LoggingPlugin(LoggingLevel.INFO, logger);
        PluginResult res = plugin.format("noArg", "noArg",
                new InvocationDetails(null,
                        UnrelatedC10NMessageBundle.class,
                        TestUtil.method(UnrelatedC10NMessageBundle.class, "noArg"),
                        null));
        assertThat(res.isInterrupt(), is(equalTo(false)));
        assertThat(res.getValue(), is(equalTo("noArg")));
        verifyZeroInteractions(logger);
    }

    @Test
    public void extendingLoggerBaseGainsAccessToUnderlyingLoggerFunctionality() throws Exception {
        LoggerImplementation logger = Mockito.mock(LoggerImplementation.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isLevelEnabled(any(LoggingLevel.class))).thenReturn(false);
        LoggingPlugin plugin = new LoggingPlugin(LoggingLevel.INFO, logger);
        PluginResult res = plugin.format(null, null,
                new InvocationDetails(null,
                        WithUtils.class,
                        TestUtil.method(LoggingBase.class, "isDebugEnabled"),
                        null));
        assertThat(res.isInterrupt(), is(equalTo(true)));
        assertThat(res.getValue(), is(equalTo(true)));

        PluginResult res2 = plugin.format(null, null,
                new InvocationDetails(null,
                        WithUtils.class,
                        TestUtil.method(LoggingBase.class, "isLevelEnabled"),
                        new Object[]{LoggingLevel.ERROR}));
        assertThat(res2.isInterrupt(), is(equalTo(true)));
        assertThat(res2.getValue(), is(equalTo(false)));

        verify(logger).isLevelEnabled(LoggingLevel.ERROR);
    }

    private void check(LoggingPlugin plugin, String message, Class<?> c10nClass, String methodName, Object[] args,
                       LoggerImplementation logger, String loggerName, LoggingLevel level) {
        check(plugin, message, c10nClass, methodName, args, null, logger, loggerName, level);
    }

    private void check(LoggingPlugin plugin, String message, Class<?> c10nClass, String methodName, Object[] args, Throwable cause,
                       LoggerImplementation logger, String loggerName, LoggingLevel level) {
        Method method = TestUtil.method(c10nClass, methodName);
        plugin.format(message, message, new InvocationDetails(null,
                c10nClass,
                method,
                args
        ));
        verify(logger).log(
                eq(loggerName),
                eq(level),
                eq(message),
                eq(cause),
                eq(new InvocationDetails(null, c10nClass, method, args))
        );
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
