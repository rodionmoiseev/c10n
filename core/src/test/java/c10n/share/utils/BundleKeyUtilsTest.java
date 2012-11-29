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

import c10n.C10NKey;
import c10n.annotations.En;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 */
public class BundleKeyUtilsTest {
    @Test
    public void customGeneratedAndAbsoluteKeysOnSingleInterface() {
        assertThat(BundleKeyUtils.allBundleKeys("", Collections.<Class<?>>singleton(Msg1.class)),
                is(set(
                        customKey(Msg1.class, "customKey", "custom.key"),
                        customKey(Msg1.class, "absoluteKey", "absolute.key", ".absolute.key"),
                        genKey(Msg1.class, "generatedKey", "c10n.share.utils.BundleKeyUtilsTest.Msg1.generatedKey"),
                        genKey(Msg1.class, "annotated", "c10n.share.utils.BundleKeyUtilsTest.Msg1.annotated",
                                Collections.<Class<? extends Annotation>>singleton(En.class))
                )));
    }

    @Test
    public void customGeneratedAndAbsoluteKeyOnInterfaceHierarchy() {
        assertThat(BundleKeyUtils.allBundleKeys(Msg1.class, Msg2.class), is(set(
                customKey(Msg1.class, "customKey", "custom.key"),
                customKey(Msg1.class, "absoluteKey", "absolute.key", ".absolute.key"),
                genKey(Msg1.class, "generatedKey", "c10n.share.utils.BundleKeyUtilsTest.Msg1.generatedKey"),
                genKey(Msg1.class, "annotated", "c10n.share.utils.BundleKeyUtilsTest.Msg1.annotated",
                        Collections.<Class<? extends Annotation>>singleton(En.class)),
                customKey(Msg2.class, "customKey2", "msg2.custom.key2", "custom.key2"),
                customKey(Msg2.class, "absoluteKey2", "absolute.key2", ".absolute.key2"),
                customKey(Msg2.class, "semiGeneratedKey", "msg2.semiGeneratedKey", null)
        )));
    }

    @Test
    public void globalPrefix() {
        assertThat(BundleKeyUtils.allBundleKeys("prefix", Msg1.class), is(set(
                customKey(Msg1.class, "customKey", "prefix.custom.key", "custom.key"),
                customKey(Msg1.class, "absoluteKey", "prefix.absolute.key", ".absolute.key"),
                genKey(Msg1.class, "generatedKey", "prefix.c10n.share.utils.BundleKeyUtilsTest.Msg1.generatedKey"),
                genKey(Msg1.class, "annotated", "prefix.c10n.share.utils.BundleKeyUtilsTest.Msg1.annotated",
                        Collections.<Class<? extends Annotation>>singleton(En.class))
        )));
    }

    private static <T> Set<T> set(T... args) {
        return new HashSet<T>(Arrays.asList(args));
    }

    private static C10NBundleKey customKey(Class<?> c10nInterface, String methodName, String key, String declaredKey) {
        return c10nBundleKey(c10nInterface, methodName, true, key, declaredKey,
                Collections.<Class<? extends Annotation>>emptySet());
    }

    private static C10NBundleKey customKey(Class<?> c10nInterface, String methodName, String key) {
        return c10nBundleKey(c10nInterface, methodName, true, key, key,
                Collections.<Class<? extends Annotation>>emptySet());
    }

    private static C10NBundleKey genKey(Class<?> c10nInterface, String methodName, String key) {
        return genKey(c10nInterface, methodName, key, Collections.<Class<? extends Annotation>>emptySet());
    }

    private static C10NBundleKey genKey(Class<?> c10nInterface, String methodName, String key,
                                        Set<Class<? extends Annotation>> annotations) {
        return c10nBundleKey(c10nInterface, methodName, false, key, null, annotations);
    }

    private static C10NBundleKey c10nBundleKey(Class<?> c10nInterface, String methodName, boolean isCustom, String key, String declaredKey,
                                               Set<Class<? extends Annotation>> annotations) {
        try {
            Method method = c10nInterface.getMethod(methodName);
            return new C10NBundleKey(c10nInterface, method, isCustom, key, declaredKey);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to find method with name: " + methodName, e);
        }
    }

    interface Msg1 {
        @C10NKey("custom.key")
        String customKey();

        @C10NKey(".absolute.key")
        String absoluteKey();

        String generatedKey();

        @En("annotated en")
        String annotated();
    }

    @C10NKey("msg2")
    interface Msg2 extends Msg1 {
        @C10NKey("custom.key2")
        String customKey2();

        @C10NKey(".absolute.key2")
        String absoluteKey2();

        String semiGeneratedKey();
    }
}
