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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JavaLoggingUtilLoggerTest {
    private static final String TEST_LOGGER = "test";
    private Log lastLog = null;
    private Handler handler = new Handler() {
        @Override
        public void publish(LogRecord log) {
            lastLog = new Log(log.getLoggerName(), log.getMessage(), log.getLevel(), log.getParameters());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    };

    @Before
    public void before() {
        Logger.getLogger(TEST_LOGGER).setLevel(Level.ALL);
        Logger.getLogger(TEST_LOGGER).addHandler(handler);
    }

    @After
    public void after() {
        Logger.getLogger(TEST_LOGGER).removeHandler(handler);
    }

    @Test
    public void levelMappingTest() throws Exception {
        JavaLoggingUtilLogger logger = new JavaLoggingUtilLogger();

        logger.log(TEST_LOGGER, LoggingLevel.TRACE, "trace message", null, null);
        Thread.sleep(100);
        assertThat(lastLog, is(equalTo(new Log(TEST_LOGGER, "trace message", Level.FINER, null))));

        logger.log(TEST_LOGGER, LoggingLevel.DEBUG, "debug message", null, null);
        assertThat(lastLog, is(equalTo(new Log(TEST_LOGGER, "debug message", Level.FINE, null))));

        logger.log(TEST_LOGGER, LoggingLevel.INFO, "info message", null, null);
        assertThat(lastLog, is(equalTo(new Log(TEST_LOGGER, "info message", Level.INFO, null))));

        logger.log(TEST_LOGGER, LoggingLevel.WARN, "warn message", null, null);
        assertThat(lastLog, is(equalTo(new Log(TEST_LOGGER, "warn message", Level.WARNING, null))));

        logger.log(TEST_LOGGER, LoggingLevel.ERROR, "error message", null, null);
        assertThat(lastLog, is(equalTo(new Log(TEST_LOGGER, "error message", Level.SEVERE, null))));
    }

    private static final class Log {
        final String loggerName;
        final String message;
        final Level level;
        final Object[] parameters;

        private Log(String loggerName, String message, Level level, Object[] parameters) {
            this.loggerName = loggerName;
            this.message = message;
            this.level = level;
            this.parameters = parameters;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Log log = (Log) o;

            if (loggerName != null ? !loggerName.equals(log.loggerName) : log.loggerName != null) return false;
            if (message != null ? !message.equals(log.message) : log.message != null) return false;
            if (level != null ? !level.equals(log.level) : log.level != null) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(parameters, log.parameters);

        }

        @Override
        public int hashCode() {
            int result = loggerName != null ? loggerName.hashCode() : 0;
            result = 31 * result + (message != null ? message.hashCode() : 0);
            result = 31 * result + (level != null ? level.hashCode() : 0);
            result = 31 * result + (parameters != null ? Arrays.hashCode(parameters) : 0);
            return result;
        }
    }
}