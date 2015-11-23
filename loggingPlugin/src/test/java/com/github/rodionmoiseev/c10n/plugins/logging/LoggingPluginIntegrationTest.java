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

import com.github.rodionmoiseev.c10n.C10N;
import com.github.rodionmoiseev.c10n.C10NConfigBase;
import com.github.rodionmoiseev.c10n.C10NMessages;
import com.github.rodionmoiseev.c10n.annotations.DefaultC10NAnnotations;
import com.github.rodionmoiseev.c10n.annotations.En;
import com.github.rodionmoiseev.c10n.annotations.Fr;
import com.github.rodionmoiseev.c10n.formatters.ExtendedMessageFormatter;
import com.github.rodionmoiseev.c10n.plugins.logging.LoggingTestUtils.Log;
import com.github.rodionmoiseev.c10n.test.utils.RuleUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by rodexion on 2015/11/13.
 */
public class LoggingPluginIntegrationTest {
    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

    @Rule
    public TestRule tmpLocale = RuleUtils.tmpLocale(Locale.ENGLISH);

    private final String logger = LoggingPluginIntegrationTest.class.getName();

    @Rule
    public LoggingTestUtils.TmpLoggingHandler log = LoggingTestUtils.tmpLoggingHandler(LoggingPluginIntegrationTest.class.getName());

    @C10NMessages
    @Logger(LoggingPluginIntegrationTest.class)
    public interface LoggingForIntegrationTest extends LoggingBase {
        @En("Info message - en")
        @Fr("Info message - fr")
        void info();

        @En("Error message: {user}")
        @Level(LoggingLevel.ERROR)
        void error(String user);
    }

    @Test
    public void messageIsPrintedToTheGivenLogger() throws Exception {
        setupLoggingPlugin();

        LoggingForIntegrationTest lg = C10N.get(LoggingForIntegrationTest.class);
        lg.error("rodion");
        assertThat(log.getLastLog(),
                is(new Log(logger, "Error message: rodion", java.util.logging.Level.SEVERE, null)));
        lg.info();
        assertThat(log.getLastLog(),
                is(new Log(logger, "Info message - en", java.util.logging.Level.INFO, null)));
    }

    @Test
    public void messagesAreInternationalized() throws Exception {
        Locale.setDefault(Locale.FRANCE);
        setupLoggingPlugin();

        LoggingForIntegrationTest lg = C10N.get(LoggingForIntegrationTest.class);
        lg.info();
        assertThat(log.getLastLog(),
                is(new Log(logger, "Info message - fr", java.util.logging.Level.INFO, null)));

    }

    private void setupLoggingPlugin() {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
                installPlugin(new LoggingPlugin(LoggingLevel.INFO, new JavaLoggingUtilLogger()));
                setMessageFormatter(new ExtendedMessageFormatter());
            }
        });
    }
}
