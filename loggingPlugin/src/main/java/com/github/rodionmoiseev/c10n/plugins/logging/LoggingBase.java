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

/**
 * Logging message interfaces can implement this
 * interface to gain access to some commonly used
 * logger functionality.
 *
 * Most methods are designed to be called on the
 * underlying logging framework implementation.
 */
public interface LoggingBase {
    boolean isLevelEnabled(LoggingLevel level);

    default boolean isTraceEnabled() {
        return isLevelEnabled(LoggingLevel.TRACE);
    }

    default boolean isDebugEnabled() {
        return isLevelEnabled(LoggingLevel.DEBUG);
    }

    default boolean isInfoEnabled() {
        return isLevelEnabled(LoggingLevel.INFO);
    }

    default boolean isWarnEnabled() {
        return isLevelEnabled(LoggingLevel.WARN);
    }


    default boolean isErrorEnabled() {
        return isLevelEnabled(LoggingLevel.ERROR);
    }
}
