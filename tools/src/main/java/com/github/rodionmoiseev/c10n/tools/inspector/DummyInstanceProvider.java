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

package com.github.rodionmoiseev.c10n.tools.inspector;

import java.lang.reflect.Method;

/**
 * <p>Dummy instace generator, used for supplying arguments to parameterised
 * methods. See {@link #getInstance(Class, java.lang.reflect.Method, Class, int)} for more details.
 *
 * @author rodion
 * @since 1.1
 */
public interface DummyInstanceProvider {
    /**
     * <p>Generate a dummy parameter instance for parameterized c10n methods.
     * <p>Consider the interface below:
     * <pre>{@code
     *     public interface Messages{
     *          &#64;En("Message {0}, {1}")
     *          String msg(String arg1, int arg2);
     *     }
     * }
     * </pre>
     *
     * During inspection, translated value for the <code>msg(String,int)</code>
     * method will be evaluated by invoking it for each locale. Since invokation
     * requires a list of instaces of <code>String</code> and <code>int</code>,
     * dummy instances will have to be generated at runtime.
     *
     * <p>This method must make sure it always returns an instance of type
     * specified by the <code>paramType</code> argument, or null if it cannot
     * be correctly generated. If the generated instance is of other type, or null,
     * the value for {@link C10NTranslations#getValue()} after
     * inspection will also be <code>null</code>.
     *
     * @param c10nInterface c10n interface declaring the corresponding method (not null)
     * @param method        c10n method for which the translation is being provided (not null)
     * @param paramType     type of the parameter for which to generate the dummy instance (not null)
     * @param paramIndex    position of the parameter in the argument list
     * @return instance of given parameter type (<code>paramType</code>) or <code>null</code>
     *         if instance cannot be generated.
     */
    Object getInstance(Class<?> c10nInterface, Method method, Class<?> paramType, int paramIndex);
}
