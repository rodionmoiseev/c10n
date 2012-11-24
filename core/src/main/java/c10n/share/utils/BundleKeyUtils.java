/*
 * Licensed to the Apache Software Foundation (ASF) under one
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
 */

package c10n.share.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author rodion
 */
public final class BundleKeyUtils {
    /**
     * <p>Retrieves bundle keys for all methods declared in the given
     * list of c10n interfaces</p>
     *
     * @param c10nInterfaces c10n interfaces to extract bundle keys from
     * @return set of bundle keys (not null)
     */
    public static Set<C10NBundleKey> allBundleKeys(Class<?>... c10nInterfaces) {
        return allBundleKeys("", c10nInterfaces);
    }

    public static Set<C10NBundleKey> allBundleKeys(String globalPrefix, Class<?>... c10nInterfaces) {
        return allBundleKeys(globalPrefix, Arrays.asList(c10nInterfaces));
    }

    public static Set<C10NBundleKey> allBundleKeys(String globalPrefix, Iterable<Class<?>> c10nInterfaces) {
        Set<C10NBundleKey> res = new HashSet<C10NBundleKey>();
        for (Class<?> c10nInterface : c10nInterfaces) {
            for (Method method : c10nInterface.getDeclaredMethods()) {
                String keyAnnotationValue = ReflectionUtils.getKeyAnnotationValue(method);
                String bundleKey = ReflectionUtils.getC10NKey(globalPrefix, method);
                //this evaluation has already been performed in getC10NKey, but since
                //this method is for testing/tooling purposes, performance is not a priority.
                boolean isCustom = ReflectionUtils.getKeyAnnotationBasedKey(method) != null;
                res.add(new C10NBundleKey(c10nInterface, method, isCustom, bundleKey, keyAnnotationValue));
            }
        }
        return res;
    }
}
