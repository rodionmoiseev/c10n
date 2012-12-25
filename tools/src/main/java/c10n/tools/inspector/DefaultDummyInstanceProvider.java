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

package c10n.tools.inspector;

import java.lang.reflect.Method;

/**
 * <p>Default dummy instance provide that can generate dummy values for
 * for all primitive types and strings.</p>
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
        } else if (paramType.equals(byte.class)) {
            return (byte) paramIndex;
        } else if (paramType.equals(short.class)) {
            return (short) paramIndex;
        } else if (paramType.equals(int.class)) {
            return paramIndex;
        } else if (paramType.equals(long.class)) {
            return (long) paramIndex;
        } else if (paramType.equals(float.class)) {
            return (float) paramIndex;
        } else if (paramType.equals(double.class)) {
            return (double) paramIndex;
        } else if (paramType.equals(boolean.class)) {
            return false;
        } else if (paramType.equals(char.class)) {
            return Character.forDigit(paramIndex, 10);
        } else {
            //give up
            return null;
        }
    }
}
