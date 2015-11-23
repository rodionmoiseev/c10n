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

import org.junit.rules.ExternalResource;

import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LoggingTestUtils {
    public static TmpLoggingHandler tmpLoggingHandler() {
        return tmpLoggingHandler("test");
    }

    public static TmpLoggingHandler tmpLoggingHandler(String loggerName) {
        return new TmpLoggingHandler(loggerName);
    }

    public static final class TmpLoggingHandler extends ExternalResource {
        private final String loggerName;
        private Log lastLog = null;
        private List<Log> logs = new ArrayList<>();
        private Set<String> loggers = new HashSet<>();

        private TmpLoggingHandler(String loggerName) {
            this.loggerName = loggerName;
        }

        private Handler handler = new Handler() {
            @Override
            public void publish(LogRecord log) {
                lastLog = new Log(log.getLoggerName(), log.getMessage(), log.getLevel(), log.getParameters());
                logs.add(lastLog);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };

        public void addLoggerHandler(String loggerName) {
            java.util.logging.Logger.getLogger(loggerName).setLevel(java.util.logging.Level.ALL);
            java.util.logging.Logger.getLogger(loggerName).addHandler(handler);
            loggers.add(loggerName);
        }

        public List<Log> getLogs() {
            return logs;
        }

        public Log getLastLog() {
            return lastLog;
        }

        @Override
        protected void before() throws Throwable {
            addLoggerHandler(loggerName);
        }

        @Override
        protected void after() {
            for (String logger : loggers) {
                java.util.logging.Logger.getLogger(logger).removeHandler(handler);
            }
        }
    }

    public static final class Log {
        final String loggerName;
        final String message;
        final java.util.logging.Level level;
        final Object[] parameters;

        public Log(String loggerName, String message, java.util.logging.Level level, Object[] parameters) {
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

        @Override
        public String toString() {
            return "Log{" +
                    "logger='" + loggerName + '\'' +
                    ", msg='" + message + '\'' +
                    ", level=" + level +
                    ", args=" + Arrays.toString(parameters) +
                    '}';
        }
    }
}
