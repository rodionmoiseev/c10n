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

package com.github.rodionmoiseev.c10n;

import java.lang.reflect.Method;

/**
 * @author rodion
 */
class DefaultUntranslatedMessageHandler implements UntranslatedMessageHandler {
    private static final int MAX_ARG_VALUE_LENGTH = 10;

    @Override
    public String render(Class<?> c10nInterface, Method method, Object[] methodArgs) {
        StringBuilder sb = new StringBuilder();
        sb.append(c10nInterface.getSimpleName()).append('.');
        sb.append(method.getName());
        if (methodArgs != null && methodArgs.length > 0) {
            sb.append('(');
            for (int i = 0; i < methodArgs.length; i++) {
                String argValue = truncate(String.valueOf(methodArgs[i]), MAX_ARG_VALUE_LENGTH);
                if (methodArgs[i] instanceof String) {
                    argValue = "\"" + argValue + "\"";
                }
                sb.append(argValue);
                if (i + 1 < methodArgs.length) {
                    sb.append(", ");
                }
            }
            sb.append(')');
        }
        return sb.toString();
    }

    private static String truncate(String value, int maxChars) {
        int l = Math.min(value.length(), maxChars);
        if (l < value.length()) {
            return value.substring(0, l) + "...";
        }
        return value;
    }
}
