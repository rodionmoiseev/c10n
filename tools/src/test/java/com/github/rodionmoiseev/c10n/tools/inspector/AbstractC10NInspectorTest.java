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

package com.github.rodionmoiseev.c10n.tools.inspector;

import com.github.rodionmoiseev.c10n.annotations.De;
import com.github.rodionmoiseev.c10n.annotations.En;
import com.github.rodionmoiseev.c10n.annotations.Fr;
import com.github.rodionmoiseev.c10n.annotations.Ja;
import com.github.rodionmoiseev.c10n.share.utils.C10NBundleKey;
import com.github.rodionmoiseev.c10n.tools.inspector.C10NTranslations;
import com.github.rodionmoiseev.c10n.tools.inspector.C10NUnit;
import com.google.common.base.Function;
import com.google.common.collect.*;
import org.junit.Ignore;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Locale.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author rodion
 * @since 1.1
 */
@Ignore("abstract test")
public class AbstractC10NInspectorTest {

    private final Map<Locale, Class<? extends Annotation>> locale2annotationClass = ImmutableMap.of(
            ENGLISH, En.class,
            JAPANESE, Ja.class,
            FRENCH, Fr.class,
            GERMAN, De.class);

    private final Set<Locale> expectedLocalesToCheck;

    protected AbstractC10NInspectorTest(Locale... expectedLocalesToCheck) {
        this.expectedLocalesToCheck = Sets.newHashSet(expectedLocalesToCheck);
    }

    protected C10NBundleKey anyKey() {
        return null;
    }

    protected C10NBundleKey autoKey(String key) {
        return new C10NBundleKey(false, key, null);
    }

    protected C10NBundleKey customKey(String key, String declaredKey) {
        return new C10NBundleKey(true, key, declaredKey);
    }

    protected static Set<Locale> set(Locale... locales) {
        return new HashSet<Locale>(Arrays.asList(locales));
    }

    protected void checkUnit(List<C10NUnit> units, Class<?> declaringInterface, String methodName, C10NBundleKey key, Translation... translations) {
        checkUnit(units, declaringInterface, method(methodName), key, translations);
    }

    protected void checkUnit(List<C10NUnit> units, Class<?> declaringInterface, Method2 method, C10NBundleKey key, Translation... translations) {
        C10NUnit actual = findActualUnit(units, declaringInterface, method.name);
        if (null != key) {
            assertThat(actual.getKey(), is(equalTo(key)));
        }
        assertEquals(actual.getDeclaringInterface(), declaringInterface);
        assertThat(actual.getDeclaringMethod(), is(equalTo(getMethod(declaringInterface, method))));
        Map<Locale, C10NTranslations> actualTranslations = Maps.newHashMap(actual.getTranslations());
        Multimap<Locale, Translation> translationsByLocale = Multimaps.index(Arrays.asList(translations), new Function<Translation, Locale>() {
            @Override
            public Locale apply(@Nullable Translation input) {
                return input.locale;
            }
        });
        for (Locale locale : translationsByLocale.keySet()) {
            C10NTranslations c10NTranslations = actualTranslations.remove(locale);
            for (Translation tr : translationsByLocale.get(locale)) {
                assertThat(c10NTranslations.getValue(), is(tr.renderedValue));
                if (tr.inBundle) {
                    assertThat(c10NTranslations.getBundles().size(), is(greaterThan(0)));
                }
                if (tr.annotationClass != null) {
                    boolean ok = false;
                    for (Annotation annotation : c10NTranslations.getAnnotations()) {
                        if (annotation.annotationType().equals(tr.annotationClass)) {
                            ok = true;
                            if (tr.valueInAnnotation != null) {
                                assertThat(getAnnotaionValue(annotation), is(tr.valueInAnnotation));
                            }
                        }
                    }
                    if (!ok) {
                        fail("Expected to find annotation of type=" + tr.annotationClass.getSimpleName()
                                + " but found=" + c10NTranslations.getAnnotations());
                    }
                }
            }
        }
        for (Map.Entry<Locale, C10NTranslations> entry : actualTranslations.entrySet()) {
            Locale locale = entry.getKey();
            C10NTranslations tr = entry.getValue();
            assertThat(expectedLocalesToCheck, hasItem(locale));
            assertThat(tr.getAnnotations().isEmpty(), is(true));
            assertThat(tr.getBundles().isEmpty(), is(true));
        }
    }

    protected String getAnnotaionValue(Annotation annotation) {
        if (annotation instanceof En) {
            return ((En) annotation).value();
        } else if (annotation instanceof Ja) {
            return ((Ja) annotation).value();
        }
        fail("Annotation value could not be retrieved: annotation.class=" + annotation.getClass());
        return null;
    }

    protected C10NUnit findActualUnit(List<C10NUnit> units, Class<?> declaringInterface, String methodName) {
        for (C10NUnit unit : units) {
            if (unit.getDeclaringInterface().equals(declaringInterface) &&
                    unit.getDeclaringMethod().getName().equals(methodName)) {
                return unit;
            }
        }
        throw new RuntimeException("c10n-unit for interface '" + declaringInterface.getSimpleName()
                + "' method '" + methodName
                + "' was not found in: " + units);
    }

    protected static Method getMethod(Class<?> clazz, Method2 ref) {
        try {
            return clazz.getMethod(ref.name, ref.argTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected Translation bundleTr(Locale locale, String value) {
        return new Translation(locale, value, true, null, null);
    }

    protected Translation annotationTr(Locale locale, String value) {
        return new Translation(locale, value, false, locale2annotationClass.get(locale), null);
    }

    protected Translation annotationTr(Locale locale, String renderedValue, String valueInAnnotation) {
        return new Translation(locale, renderedValue, false, locale2annotationClass.get(locale), valueInAnnotation);
    }

    protected Method2 method(String name, Class<?>... argTypes) {
        return new Method2(name, argTypes);
    }

    protected static final class Translation {
        final Locale locale;
        final String renderedValue;
        final boolean inBundle;
        final Class<? extends Annotation> annotationClass;
        final String valueInAnnotation;

        protected Translation(Locale locale, String renderedValue, boolean inBundle, Class<? extends Annotation> annotationClass, String valueInAnnotation) {
            this.locale = locale;
            this.renderedValue = renderedValue;
            this.inBundle = inBundle;
            this.annotationClass = annotationClass;
            this.valueInAnnotation = valueInAnnotation;
        }
    }

    protected static final class Method2 {
        final String name;
        final Class<?>[] argTypes;

        protected Method2(String name, Class<?>[] argTypes) {
            this.name = name;
            this.argTypes = argTypes;
        }
    }
}
