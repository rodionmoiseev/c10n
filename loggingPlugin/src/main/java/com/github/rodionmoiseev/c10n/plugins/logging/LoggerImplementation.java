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

/**
 * The actual implementation of logging using a
 * logging framework of choice, or a custom implementation.
 */
public interface LoggerImplementation extends LoggingBase {
    /**
     * Log an event. This method will be invoked every
     * time a c10n method invocation is made.
     *
     * @param logger  the logger name to be used, derived from the {@link Logger} annotation (not null)
     * @param level   the logging level to be used, derived from the {@link Level} annotation (not null)
     * @param message the message to be logged with all the argument placeholders resolved, except the stack trace (if present) (not null)
     * @param cause   the exception associated with the logging event (maybe null)
     * @param details c10n method invocation details
     */
    void log(String logger, LoggingLevel level, String message, Throwable cause, InvocationDetails details);
}
