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

package com.github.rodionmoiseev.c10n.plugin;

import com.github.rodionmoiseev.c10n.InvocationDetails;

/**
 * C10N extension plugin.
 *
 * Plugins should be installed at configuration time using
 * the {@link com.github.rodionmoiseev.c10n.C10NConfigBase#installPlugin(C10NPlugin)}
 * method.
 *
 * Multiple plugin may be installed, in which case they will be executed
 * the the installation order. The result of the previous plugin will
 * be passed to the next plugin, so that plugin execution may be chained.
 */
public interface C10NPlugin {
    /**
     * <p>
     * Apply additional formatting or behaviour to the
     * string formatted by the main c10n message factory.
     *
     * <p>
     * Execution of this method should return a {@link PluginResult}
     * object with the {@link PluginResult#value} field set to
     * the object the method execution should return.
     * Setting the value to {@code null} will also cause the
     * method to return the {@code null} value.
     *
     * <p>
     * In case multiple plugins are installed, the value will
     * be passed to the next plugin. If you wish to prevent
     * any further plugins from being executed and return the
     * current value immediately, set the {@link PluginResult#interrupt}
     * flag to {@code true} by using the {@link PluginResult#last(Object)}
     * constructor.
     *
     * <p>
     * Returning a {@code null} value will skip the processing of
     * this plugin. The behaviour would be equivalent to one if this
     * plugin was not installed.
     *
     * @param resolvedMessage     message string formatted by c10n message factory
     * @param resolvedReturnValue the return value of the method, resolved by c10n message factory.
     *                            Returns {@code null} for {@code void} methods and other unknown
     *                            return types.
     * @param invocationDetails   c10n proxy invocation data (not null)
     * @return a plugin result object, or {@code null}
     */
    PluginResult format(String resolvedMessage,
                        Object resolvedReturnValue,
                        InvocationDetails invocationDetails);
}
