/*
 * Copyright 2012 Rodion Moiseev (https://github.com/rodionmoiseev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rodionmoiseev.c10n.plugins.logging;

import com.github.rodionmoiseev.c10n.InvocationDetails;
import com.github.rodionmoiseev.c10n.plugin.C10NPlugin;
import com.github.rodionmoiseev.c10n.plugin.PluginResult;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Created by rodexion on 2015/10/10.
 */
public class LoggingPlugin implements C10NPlugin {
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
    public PluginResult format(String resolvedMessage,
                               Object resolvedReturnValue,
                               InvocationDetails invocationDetails) {
        Class<?> c10nInterface = invocationDetails.getC10nInterface();
        //Determine if the plugin should be applied
        //by checking if the @Logger annotation is present
        if (null == c10nInterface.getAnnotation(Logger.class)) {
            return PluginResult.passOn(resolvedReturnValue);
        }

        Method method = invocationDetails.getMethod();
        Object[] methodArgs = invocationDetails.getMethodArguments();
        if (method.getDeclaringClass().equals(LoggingBase.class)) {
            try {
                return PluginResult.last(method.invoke(implementation, methodArgs));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to invoke metod '" +
                        method.getName() + "' on current logger implementation instance.", e);
            }
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
                String.valueOf(resolvedMessage),
                getCauseOrNull(methodArgs),
                invocationDetails);
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
