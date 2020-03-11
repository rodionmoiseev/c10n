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
 * Result object returned by the plugin, controlling
 * the formatted message flow within the system.
 *
 * @see C10NPlugin#format(String, Object, InvocationDetails)
 */
public class PluginResult {
    private final Object value;
    private final boolean interrupt;

    private PluginResult(Object value, boolean interrupt) {
        this.value = value;
        this.interrupt = interrupt;
    }

    public static PluginResult passOn(Object value) {
        return new PluginResult(value, false);
    }

    public static PluginResult last(Object value) {
        return new PluginResult(value, true);
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public Object getValue() {
        return value;
    }
}
