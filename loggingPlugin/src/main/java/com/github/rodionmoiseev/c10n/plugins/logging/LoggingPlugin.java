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

import com.github.rodionmoiseev.c10n.plugin.C10NPlugin;
import com.github.rodionmoiseev.c10n.plugin.PluginResult;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Created by rodexion on 2015/10/10.
 */
public class LoggingPlugin implements C10NPlugin {
    /**
     * Holds arguments passed from the c10n framework at
     * method invocation time.
     */
    public static final class InvocationDetails {
        private final Class<?> c10nInterface;
        private final Method method;
        private final Object[] methodArguments;

        InvocationDetails(Class<?> c10nInterface, Method method, Object[] methodArguments) {
            this.c10nInterface = c10nInterface;
            this.method = method;
            this.methodArguments = methodArguments;
        }

        /**
         * @return The c10n interface class containing the invoked method
         */
        public Class<?> getC10nInterface() {
            return c10nInterface;
        }

        /**
         * @return The c10n method that was invoked
         */
        public Method getMethod() {
            return method;
        }

        /**
         * @return The actual arguments that were passed to the c10n method invoked
         */
        public Object[] getMethodArguments() {
            return methodArguments;
        }

        @Override
        public String toString() {
            return "InvocationDetails{" +
                    "c10nInterface=" + c10nInterface +
                    ", method=" + method +
                    ", methodArguments=" + Arrays.toString(methodArguments) +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InvocationDetails that = (InvocationDetails) o;

            if (c10nInterface != null ? !c10nInterface.equals(that.c10nInterface) : that.c10nInterface != null)
                return false;
            if (method != null ? !method.equals(that.method) : that.method != null) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(methodArguments, that.methodArguments);

        }

        @Override
        public int hashCode() {
            int result = c10nInterface != null ? c10nInterface.hashCode() : 0;
            result = 31 * result + (method != null ? method.hashCode() : 0);
            result = 31 * result + (methodArguments != null ? Arrays.hashCode(methodArguments) : 0);
            return result;
        }
    }

    private final LoggingLevel defaultLoggingLevel;
    private final LoggerImplementation implementation;

    /**
     * Configures an instance of the logging plugin.
     *
     * The default logging level will be assumed to be {@link LoggingLevel#INFO},
     * and the java logging util based logger implementation will be used.
     *
     * @see JavaLoggingUtilLogger
     */
    public LoggingPlugin() {
        this(LoggingLevel.INFO, new JavaLoggingUtilLogger());
    }

    /**
     * Configures an instance of the logging plugin.
     *
     * @param defaultLoggingLevel the assumed default logging level
     * @param implementation      logger implementation to use for actual logging
     */
    public LoggingPlugin(LoggingLevel defaultLoggingLevel, LoggerImplementation implementation) {
        this.defaultLoggingLevel = defaultLoggingLevel;
        this.implementation = implementation;
    }

    @Override
    public PluginResult format(Class<?> c10nInterface,
                               Method method,
                               Object[] methodArgs,
                               Object resolvedReturnValue) {
        //Determine if the plugin should be applied
        //by checking if the @Logger annotation is present
        if (null == c10nInterface.getAnnotation(Logger.class)) {
            return PluginResult.passOn(resolvedReturnValue);
        }

        LoggingLevel level = get(c10nInterface, method, Level.class, defaultLoggingLevel, Level::value);
        String loggerName = get(c10nInterface, method, Logger.class, c10nInterface.getName(),
                (logger) -> {
                    if (!logger.name().equals(Logger.NO_LOGGER_NAME)) {
                        return logger.name();
                    }
                    if (!logger.value().equals(Logger.class)) {
                        return logger.value().getName();
                    }
                    return c10nInterface.getName();
                });
        implementation.log(loggerName,
                level,
                String.valueOf(resolvedReturnValue),
                getCauseOrNull(methodArgs),
                new InvocationDetails(c10nInterface, method, methodArgs));
        return PluginResult.last(null);
    }

    private Throwable getCauseOrNull(Object[] methodArgs) {
        if (methodArgs != null &&
                methodArgs.length > 0) {
            Object lastArg = methodArgs[methodArgs.length - 1];
            if (lastArg instanceof Throwable) {
                return (Throwable) lastArg;
            }
        }
        return null;
    }

    private static <A extends Annotation, T> T get(Class<?> parentClass,
                                                   Method method,
                                                   Class<A> ann,
                                                   T defaultValue,
                                                   Function<A, T> getValueF) {
        A methodAnnotation = method.getAnnotation(ann);
        if (null != methodAnnotation) {
            return getValueF.apply(methodAnnotation);
        }
        A classAnnotation = parentClass.getAnnotation(ann);
        if (null != classAnnotation) {
            return getValueF.apply(classAnnotation);
        }
        return defaultValue;
    }
}
