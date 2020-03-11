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

package com.github.rodionmoiseev.c10n.share.utils;

import com.github.rodionmoiseev.c10n.C10NKey;

import java.lang.reflect.Method;
import java.util.*;

public final class ReflectionUtils {
    private static final String KEY_DELIM = ".";

    /**
     * <p>Work out method's bundle key.
     *
     * <h2>Bundle key resolution</h2>
     * <p>Bundle key is generated as follows:
     * <ul>
     * <li>If there are no {@link com.github.rodionmoiseev.c10n.C10NKey} annotations, key is the <code>Class FQDN '.' Method Name</code>.
     * If method has arguments, method name is post-fixed with argument types delimited
     * with '_', e.g. <code>myMethod_String_int</code></li>
     *
     * <li>If declaring interface or any of the super-interfaces contain {@link com.github.rodionmoiseev.c10n.C10NKey}
     * annotation <code>C</code> then
     * <ul>
     * <li>For methods without {@link com.github.rodionmoiseev.c10n.C10NKey} annotation, key becomes <code>C '.' Method Name</code></li>
     * <li>For methods with {@link com.github.rodionmoiseev.c10n.C10NKey} annotation <code>M</code>, key is <code>C '.' M</code></li>
     * <li>For methods with {@link com.github.rodionmoiseev.c10n.C10NKey} annotation <code>M</code>, value for which starts with
     * a '.', the key is just <code>M</code> (i.e. key is assumed to be absolute)</li>
     * </ul>
     * </li>
     * <li>If no declaring interfaces have {@link com.github.rodionmoiseev.c10n.C10NKey} annotation, but a method contains annotation
     * <code>M</code>, then key is just <code>M</code>.</li>
     * <li>Lastly, if global key prefix is specified, it is always prepended to the final key, delimited by '.'</li>
     * </ul>
     *
     *
     * <h2>Looking for c10n key in parent interfaces</h2>
     * <p>The lookup of c10n key in parent interfaces is done breadth-first, starting from the declaring class.
     * That is, if the declaring class does not have c10n key, all interfaces it extends are checked in declaration
     * order first. If no key is found, this check is repeated for each of the super interfaces in the same order.
     *
     *
     * @param keyPrefix global key prefix
     * @param method    method to extract the key from
     * @return method c10n bundle key (not null)
     */
    public static String getC10NKey(String keyPrefix, Method method) {
        String key = getKeyAnnotationBasedKey(method);
        if (null == key) {
            //fallback to default key based on class FQDN and method name
            key = ReflectionUtils.getDefaultKey(method);
        }
        if (keyPrefix.length() > 0) {
            key = keyPrefix + "." + key;
        }
        return key;
    }

    public static String getKeyAnnotationBasedKey(Method method) {
        String parentKey = findParentKey(method);
        String c10NKey = getKeyAnnotationValue(method);
        if (null == parentKey && null == c10NKey) {
            //C10NKey-based key is not available
            return null;
        }

        String methodKey;
        if (null != c10NKey) {
            methodKey = c10NKey;
            if (methodKey.startsWith(KEY_DELIM)) {
                //found an absolute key. Get rid of the
                //leading dot and look no further
                return methodKey.substring(1, methodKey.length());
            }
        } else {
            //method key based on methodName & arguments
            methodKey = getMethodKey(method);
        }
        if (parentKey != null) {
            return parentKey + KEY_DELIM + methodKey;
        }
        return methodKey;
    }

    /**
     * <p>Get the value provided with the {@link com.github.rodionmoiseev.c10n.C10NKey} annotation. If annotation
     * is not declared returns <code>null</code>
     *
     * @param method method for which to retrieve the value {@link com.github.rodionmoiseev.c10n.C10NKey} annotation
     * @return value of the declared annotation. <code>null</code> if not present.
     */
    public static String getKeyAnnotationValue(Method method) {
        C10NKey c10NKey = method.getAnnotation(C10NKey.class);
        if (null != c10NKey) {
            return c10NKey.value();
        }
        return null;
    }

    /**
     * <p>Works out the non-{@link C10NKey} bundle key for method, based on its
     * class FQDN and method name (plus argument types),
     * e.g. <code>com.myCompany.MyClass.myMethod_String_boolean</code>
     *
     * @param method the method for which to work out the key(not null)
     * @return method's default bundle key(not null)
     */
    public static String getDefaultKey(Method method) {
        StringBuilder sb = new StringBuilder();
        getDefaultKey(method, sb);
        return sb.toString();
    }

    public static void getDefaultKey(Method method, StringBuilder sb) {
        getFQNString(method.getDeclaringClass(), sb);
        sb.append(KEY_DELIM);
        getMethodKey(method, sb);
    }

    private static String getMethodKey(Method method) {
        StringBuilder sb = new StringBuilder();
        getMethodKey(method, sb);
        return sb.toString();
    }

    private static void getMethodKey(Method method, StringBuilder sb) {
        sb.append(method.getName());

        Class<?>[] params = method.getParameterTypes();
        if (params.length > 0) {
            sb.append('_');
            for (int i = 0; i < params.length; i++) {
                sb.append(params[i].getSimpleName());
                if (i + 1 < params.length) {
                    sb.append("_");
                }
            }
        }
    }

    public static String getFQNString(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();
        getFQNString(clazz, sb);
        return sb.toString();
    }

    public static void getFQNString(Class<?> clazz, StringBuilder sb) {
        sb.append(clazz.getPackage().getName()).append(KEY_DELIM);
        getClassFQNString(clazz, sb);
    }

    private static void getClassFQNString(Class<?> clazz, StringBuilder sb) {
        Iterator<Class<?>> it = typeEnclosureHierarchy(clazz).descendingIterator();
        while (it.hasNext()) {
            sb.append(it.next().getSimpleName());
            if (it.hasNext()) {
                sb.append(KEY_DELIM);
            }
        }
    }

    private static String findParentKey(Method method) {
        for (Class<?> clazz : expandInterfaceHierarchy(method.getDeclaringClass())) {
            C10NKey key = clazz.getAnnotation(C10NKey.class);
            if (null != key) {
                return key.value();
            }
        }
        return null;
    }

    private static LinkedList<Class<?>> typeEnclosureHierarchy(Class<?> clazz) {
        LinkedList<Class<?>> typeHierarchy = new LinkedList<Class<?>>();
        do {
            typeHierarchy.add(clazz);
        } while ((clazz = clazz.getEnclosingClass()) != null);
        return typeHierarchy;
    }

    /*
     * Returns a list of super-interface breadth-first.
     */
    private static List<Class<?>> expandInterfaceHierarchy(Class<?> clazz) {
        List<Class<?>> res = new ArrayList<Class<?>>();
        res.add(clazz);
        expandInterfaceHierarchy(clazz, res);
        return res;
    }

    private static void expandInterfaceHierarchy(Class<?> clazz, List<Class<?>> res) {
        Class<?>[] intfs = clazz.getInterfaces();
        if (null != intfs) {
            Collections.addAll(res, intfs);
            for (Class<?> intf : intfs) {
                expandInterfaceHierarchy(intf, res);
            }
        }
    }
}
