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
 * <p>Default dummy instance provide that can generate dummy values for
 * for all primitive types and strings.
 *
 * @author rodion
 */
class DefaultDummyInstanceProvider implements DummyInstanceProvider {
    @Override
    public Object getInstance(Class<?> c10nInterface, Method method, Class<?> paramType, int paramIndex) {
        //Replace String and Object type args with their respective index
        if (paramType.isAssignableFrom(String.class) ||
                paramType.isAssignableFrom(CharSequence.class)) {
            return String.format("{%d}", paramIndex);
        } else if (paramType.equals(byte.class) || paramType.equals(Byte.class)) {
            return (byte) paramIndex;
        } else if (paramType.equals(short.class) || paramType.equals(Short.class)) {
            return (short) paramIndex;
        } else if (paramType.equals(int.class) || paramType.equals(Integer.class)) {
            return paramIndex;
        } else if (paramType.equals(long.class) || paramType.equals(Long.class)) {
            return (long) paramIndex;
        } else if (paramType.equals(float.class) || paramType.equals(Float.class)) {
            return (float) paramIndex;
        } else if (paramType.equals(double.class) || paramType.equals(Double.class)) {
            return (double) paramIndex;
        } else if (paramType.equals(boolean.class) || paramType.equals(Boolean.class)) {
            return false;
        } else if (paramType.equals(char.class) || paramType.equals(Character.class)) {
            return Character.forDigit(paramIndex, 10);
        } else {
            //give up
            return null;
        }
    }
}
