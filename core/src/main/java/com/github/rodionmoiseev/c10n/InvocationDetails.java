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
import java.util.Arrays;

/**
 * Holds arguments passed from the c10n framework at
 * method invocation time.
 */
public final class InvocationDetails {
    private final Object proxy;
    private final Class<?> c10nInterface;
    private final Method method;
    private final Object[] methodArguments;

    public InvocationDetails(Object proxy, Class<?> c10nInterface, Method method, Object[] methodArguments) {
        this.proxy = proxy;
        this.c10nInterface = c10nInterface;
        this.method = method;
        this.methodArguments = methodArguments;
    }

    /**
     * @return the proxy object instance on which the method was invoked
     */
    public Object getProxy() {
        return proxy;
    }

    /**
     * @return The c10n interface class containing the invoked method
     */
    public Class<?> getC10nInterface() {
        return c10nInterface;
    }

    /**
     * @return The c10n method that was invoked
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return The actual arguments that were passed to the c10n method invoked
     */
    public Object[] getMethodArguments() {
        return methodArguments;
    }

    @Override
    public String toString() {
        return "InvocationDetails{" +
                "proxy=-" +
                ", c10nInterface=" + c10nInterface +
                ", method=" + method +
                ", methodArguments=" + Arrays.toString(methodArguments) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvocationDetails that = (InvocationDetails) o;

        if (proxy != null ? !proxy.equals(that.proxy) : that.proxy != null) return false;
        if (c10nInterface != null ? !c10nInterface.equals(that.c10nInterface) : that.c10nInterface != null)
            return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(methodArguments, that.methodArguments);

    }

    @Override
    public int hashCode() {
        int result = proxy != null ? proxy.hashCode() : 0;
        result = 31 * result + (c10nInterface != null ? c10nInterface.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(methodArguments);
        return result;
    }
}
