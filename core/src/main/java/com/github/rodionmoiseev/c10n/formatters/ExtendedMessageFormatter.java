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

package com.github.rodionmoiseev.c10n.formatters;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * A replacement for the default {@link java.text.MessageFormat}-based
 * message formatting scheme.
 *
 * <p>
 * This formatter allows to use the following types of argument replacement,
 * assuming a method with 2 arguments:
 *
 * <pre>
 * greet(String name, String surname)
 * </pre>
 *
 * <p>
 * <em>All examples below evaluate to the same result</em>
 * <ul>
 * <li>{@code "Hello {} {}"} - Argument-less placeholders.
 * First argument-less placeholder is replaced by the first parameter,
 * second one by the second parameter, and so on.</li>
 * <li>{@code "Hello {0} {1}"} - Indexed placeholders. Replaced with the argument
 * value with the same index (0-based).</li>
 * <li>{@code "Hello {name} {surname}"} - Named placeholders. Replaced with the
 * argument with the same name (*1). </li>
 * </ul>
 *
 * <p>
 * <b>*1</b> Named placeholders only work if source was
 * compiled with {@code -parameters} javac flag.
 * Alternatively you can use one of {@link NamedArg}
 * annotations to manually specify parameter names in the source.
 * Manually specified parameter names do not have to match real parameter names.
 * For clashing parameter names, behaviour is undefined.
 */
public class ExtendedMessageFormatter implements MessageFormatter {
    private final Map<String, String> customReplacements;

    @SuppressWarnings("unused")
    public ExtendedMessageFormatter() {
        this(Collections.<String, String>emptyMap());
    }

    public ExtendedMessageFormatter(Map<String, String> customReplacements) {
        this.customReplacements = customReplacements;
    }

    @Override
    public String format(Method method, String message, Locale locale, Object... args) {
        Map<String,String> replacements = new HashMap<String, String>(customReplacements);
        Parameter[] params = method.getParameters();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < params.length; i++) {
            message = message.replaceFirst("\\{\\}", String.format("{%d}", i));
            Parameter parameter = params[i];
            assert args != null;
            String value = String.valueOf(args[i]);
            replacements.put(String.valueOf(i), value);
            if (parameter.isNamePresent()) {
                replacements.put(parameter.getName(), value);
            }
            Annotation[] annotations = paramAnnotations[i];
            for (Annotation annotation : annotations) {
                if (annotation instanceof NamedArg) {
                    replacements.put(((NamedArg) annotation).value(), value);
                }
            }
        }
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue());
        }
        return message;
    }
}
