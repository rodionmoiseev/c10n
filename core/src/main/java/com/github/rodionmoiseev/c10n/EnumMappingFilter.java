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

package com.github.rodionmoiseev.c10n;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rodion
 */
final class EnumMappingFilter<E extends Enum<?>> implements C10NFilter<E> {
    private final Object enumC10NInterfaceInstance;
    private final Map<Enum<?>, Method> c10nInfMethodMapping;

    EnumMappingFilter(Class<E> enumClass, Class<?> c10nInterface) {
        this.enumC10NInterfaceInstance = C10N.get(c10nInterface);
        this.c10nInfMethodMapping = genMapping(enumClass, c10nInterface);
    }

    private static <E extends Enum<?>> Map<Enum<?>, Method> genMapping(Class<E> enumClass, Class<?> enumC10NInterface) {
        Map<String, Method> allMethods = new HashMap<String, Method>();
        for (Method m : enumC10NInterface.getMethods()) {
            allMethods.put(m.getName().toLowerCase(), m);
        }

        Map<Enum<?>, Method> res = new HashMap<Enum<?>, Method>();

        for (Enum<?> enumValue : enumClass.getEnumConstants()) {
            //1. Check of methods for pattern: ClassName_EnumValue()
            Method m = allMethods.get(enumClass.getSimpleName().toLowerCase() + "_" + enumValue.name().toLowerCase());
            if (null == m || hasOneOrMoreParams(m) || returnsNonObjectType(m)) {
                //no good ...
                //2. Check for methods for pattern: EnumValue()
                m = allMethods.get(enumValue.name().toLowerCase());
                if (null == m || hasOneOrMoreParams(m) || returnsNonObjectType(m)) {
                    throw new IllegalStateException("method mapping for " +
                            enumClass.getSimpleName() + "." + enumValue.name() + " was not found!!");
                }
            }
            res.put(enumValue, m);
        }
        return res;
    }

    private static boolean returnsNonObjectType(Method m) {
        return m.getReturnType().equals(Void.TYPE);
    }

    private static boolean hasOneOrMoreParams(Method m) {
        Class[] paramTypes = m.getParameterTypes();
        return paramTypes != null && paramTypes.length != 0;
    }

    @Override
    public Object apply(E arg) {
        Method m = c10nInfMethodMapping.get(arg);
        try {
            return m.invoke(enumC10NInterfaceInstance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to dispatch invocation to " +
                    m.getDeclaringClass().getSimpleName() + "." + m.getName() + "() method.", e);
        }
    }
}
