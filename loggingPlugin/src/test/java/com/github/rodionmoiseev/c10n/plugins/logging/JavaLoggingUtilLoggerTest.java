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

import com.github.rodionmoiseev.c10n.plugins.logging.LoggingTestUtils.Log;
import org.junit.Rule;
import org.junit.Test;

import java.util.logging.Level;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JavaLoggingUtilLoggerTest {
    private static final String TEST_LOGGER = "test";
    @Rule
    public LoggingTestUtils.TmpLoggingHandler log = LoggingTestUtils.tmpLoggingHandler(TEST_LOGGER);

    @Test
    public void levelMappingTest() throws Exception {
        JavaLoggingUtilLogger logger = new JavaLoggingUtilLogger();

        logger.log(TEST_LOGGER, LoggingLevel.TRACE, "trace message", null, null);
        Thread.sleep(100);
        assertThat(log.getLastLog(), is(equalTo(new Log(TEST_LOGGER, "trace message", Level.FINER, null))));

        logger.log(TEST_LOGGER, LoggingLevel.DEBUG, "debug message", null, null);
        assertThat(log.getLastLog(), is(equalTo(new Log(TEST_LOGGER, "debug message", Level.FINE, null))));

        logger.log(TEST_LOGGER, LoggingLevel.INFO, "info message", null, null);
        assertThat(log.getLastLog(), is(equalTo(new Log(TEST_LOGGER, "info message", Level.INFO, null))));

        logger.log(TEST_LOGGER, LoggingLevel.WARN, "warn message", null, null);
        assertThat(log.getLastLog(), is(equalTo(new Log(TEST_LOGGER, "warn message", Level.WARNING, null))));

        logger.log(TEST_LOGGER, LoggingLevel.ERROR, "error message", null, null);
        assertThat(log.getLastLog(), is(equalTo(new Log(TEST_LOGGER, "error message", Level.SEVERE, null))));
    }
}