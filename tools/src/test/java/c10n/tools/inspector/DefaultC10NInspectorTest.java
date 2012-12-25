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

package c10n.tools.inspector;

import c10n.C10N;
import c10n.C10NConfigBase;
import c10n.ConfiguredC10NModule;
import c10n.annotations.DefaultC10NAnnotations;
import c10n.annotations.En;
import c10n.annotations.Ja;
import c10n.share.utils.C10NBundleKey;
import c10n.tools.inspector.test1.*;
import c10n.tools.search.SearchModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.JAPANESE;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author rodion
 */
public class DefaultC10NInspectorTest {

    private static final Map<Locale, Class<? extends Annotation>> locale2annotationClass = ImmutableMap.of(
            ENGLISH, En.class,
            JAPANESE, Ja.class);

    /*
     * Test all possible variations of keys:
     * - key with at least one annotation but not in any bundle (considered OK)
     * - key defined in all bundles but without annotations (considered OK)
     * - key missing in one bundle (considered an ERROR)
     * - key missing in all bundles (considered an ERROR)
     */
    @Test
    public void completeScenario() {
        Set<Locale> localesToCheck = Sets.newHashSet(ENGLISH, JAPANESE);


        C10NConfigBase conf = new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
                bindBundle("c10n.tools.inspector.test1.Test1");
            }
        };
        ConfiguredC10NModule confdModule = C10N.configure(conf);
        C10NInspector checker = new DefaultC10NInspector(SearchModule.reflectionsSearch(),
                confdModule,
                new DefaultDummyInstanceProvider(),
                localesToCheck);
        List<C10NUnit> units = checker.inspect("c10n.tools.inspector.test1");

        for (C10NUnit unit : units) {
            System.out.println(unit);
        }

        assertThat(Iterables.size(units), is(15));

        //key in both bundles, without annotations
        checkUnit(units,
                AnnotationOnly.class, "both",
                anyKey(),
                annotationTr(ENGLISH, "[en]@annotation AnnotationOnly.both"),
                annotationTr(JAPANESE, "[ja]@annotation AnnotationOnly.both"));
        checkUnit(units,
                AnnotationOnly.class, "onlyEn",
                anyKey(),
                annotationTr(ENGLISH, "[en]@annotation AnnotationOnly.onlyEn"));

        checkUnit(units,
                AutoKey.class, "both",
                autoKey("c10n.tools.inspector.test1.AutoKey.both"),
                bundleTr(ENGLISH, "[en]@bundle AutoKey.both"),
                bundleTr(JAPANESE, "[ja]@bundle AutoKey.both"));
        checkUnit(units,
                AutoKey.class, "onlyJa",
                autoKey("c10n.tools.inspector.test1.AutoKey.onlyJa"),
                bundleTr(JAPANESE, "[ja]@bundle AutoKey.onlyJa"));
        checkUnit(units,
                AutoKey.class, "enInAnnotationJaInBundle",
                autoKey("c10n.tools.inspector.test1.AutoKey.enInAnnotationJaInBundle"),
                annotationTr(ENGLISH, "[en]@annotation AutoKey.enInAnnotationJaInBundle"),
                bundleTr(JAPANESE, "[ja]@bundle AutoKey.enInAnnotationJaInBundle"));
        checkUnit(units,
                AutoKey.class, "none",
                anyKey());

        checkUnit(units,
                ClassScopeKey.class, "both",
                customKey("scope.class.both", null),
                bundleTr(ENGLISH, "[en]@bundle ClassScopeKey.both"),
                bundleTr(JAPANESE, "[ja]@bundle ClassScopeKey.both"));
        checkUnit(units,
                ClassScopeKey.class, "bothInAnnotationAndInBundle",
                customKey("scope.class.bothInAnnotationAndInBundle", null),
                annotationTr(ENGLISH, "[en]@bundle ClassScopeKey.bothInAnnotationAndInBundle", "[en]@annotation ClassScopeKey.bothInAnnotationAndInBundle"),
                annotationTr(JAPANESE, "[ja]@bundle ClassScopeKey.bothInAnnotationAndInBundle", "[ja]@annotation ClassScopeKey.bothInAnnotationAndInBundle"),
                bundleTr(ENGLISH, "[en]@bundle ClassScopeKey.bothInAnnotationAndInBundle"),
                bundleTr(JAPANESE, "[ja]@bundle ClassScopeKey.bothInAnnotationAndInBundle"));

        checkUnit(units,
                MethodOnlyKey.class, "both",
                customKey("scope.method", "scope.method"),
                bundleTr(ENGLISH, "[en]@bundle MethodOnlyKey.both"),
                bundleTr(JAPANESE, "[ja]@bundle MethodOnlyKey.both"));

        checkUnit(units,
                MethodScopeKey.class, "both",
                customKey("scope.class.scope.method.both", "scope.method.both"),
                bundleTr(ENGLISH, "[en]@bundle MethodScopeKey.both"),
                bundleTr(JAPANESE, "[ja]@bundle MethodScopeKey.both"));

        checkUnit(units,
                SubInterfaceOfAnnotationOnly.class, "sub_both",
                anyKey(),
                annotationTr(ENGLISH, "[en]@annotation SubInterfaceOfAnnotationOnly.sub_both"),
                annotationTr(JAPANESE, "[ja]@annotation SubInterfaceOfAnnotationOnly.sub_both"));

        checkUnit(units,
                SubInterfaceOfAutoKey.class, "sub_both",
                autoKey("c10n.tools.inspector.test1.SubInterfaceOfAutoKey.sub_both"),
                bundleTr(ENGLISH, "[en]@bundle SubInterfaceOfAutoKey.sub_both"),
                bundleTr(JAPANESE, "[ja]@bundle SubInterfaceOfAutoKey.sub_both"));

        checkUnit(units,
                SubInterfaceOfClassScopedKey.class, "sub_both",
                customKey("scope.subclass.sub_both", null),
                bundleTr(ENGLISH, "[en]@bundle SubInterfaceOfClassScopedKey.sub_both"),
                bundleTr(JAPANESE, "[ja]@bundle SubInterfaceOfClassScopedKey.sub_both"));

        checkUnit(units,
                SubInterfaceOfMethodScopedKey.class, "sub_both",
                customKey("scope.subclass.scope.submethod", "scope.submethod"),
                bundleTr(ENGLISH, "[en]@bundle SubInterfaceOfMethodScopedKey.sub_both"),
                bundleTr(JAPANESE, "[ja]@bundle SubInterfaceOfMethodScopedKey.sub_both"));

        checkUnit(units,
                SubInterfaceOfMethodOnlyKey.class, "sub_both",
                customKey("scope.submethod", "scope.submethod"),
                bundleTr(ENGLISH, "[en]@bundle SubInterfaceOfMethodOnlyKey.sub_both"),
                bundleTr(JAPANESE, "[ja]@bundle SubInterfaceOfMethodOnlyKey.sub_both"));

    }

    private C10NBundleKey anyKey() {
        return null;
    }

    private C10NBundleKey autoKey(String key) {
        return new C10NBundleKey(false, key, null);
    }

    private C10NBundleKey customKey(String key, String declaredKey) {
        return new C10NBundleKey(true, key, declaredKey);
    }

    private static Set<Locale> set(Locale... locales) {
        return new HashSet<Locale>(Arrays.asList(locales));
    }

    private static C10NUnit unitFor(String key, Iterable<C10NUnit> units) {
        for (C10NUnit unit : units) {
            if (unit.getKey().getKey().equals(key)) {
                return unit;
            }
        }
        throw new RuntimeException("c10n-unit for key '" + key + "' was not found in: " + units);
    }

    private void checkUnit(List<C10NUnit> units, Class<?> declaringInterface, String methodName, C10NBundleKey key, Translation... translations) {
        C10NUnit actual = findActualUnit(units, declaringInterface, methodName);
        if (null != key) {
            assertThat(actual.getKey(), is(equalTo(key)));
        }
        assertEquals(actual.getDeclaringInterface(), declaringInterface);
        assertThat(actual.getDeclaringMethod(), is(equalTo(getMethod(declaringInterface, methodName))));
        boolean shouldHaveAnnotations = false;
        boolean shouldhaveBundles = false;
        for (Translation tr : translations) {
            C10NTranslations c10NTranslations = actual.getTranslations().get(tr.locale);
            assertThat(c10NTranslations.getValue(), is(tr.renderedValue));
            if (tr.inBundle) {
                shouldhaveBundles = true;
                assertThat(c10NTranslations.getBundles().size(), is(greaterThan(0)));
            }
            if (tr.annotationClass != null) {
                shouldHaveAnnotations = true;
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
        for (Translation tr : translations) {
            C10NTranslations c10NTranslations = actual.getTranslations().get(tr.locale);
            if (!shouldHaveAnnotations) {
                assertThat(c10NTranslations.getAnnotations().size(), is(0));
            }
            if (!shouldhaveBundles) {
                assertThat(c10NTranslations.getBundles().size(), is(0));
            }
        }
    }

    private String getAnnotaionValue(Annotation annotation) {
        if (annotation instanceof En) {
            return ((En) annotation).value();
        } else if (annotation instanceof Ja) {
            return ((Ja) annotation).value();
        }
        fail("Annotation value could not be retrieved: annotation.class=" + annotation.getClass());
        return null;
    }

    private C10NUnit findActualUnit(List<C10NUnit> units, Class<?> declaringInterface, String methodName) {
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

    private static C10NUnit unit(Class<?> declaringInterface, String methodName, C10NBundleKey key) {
        return new C10NUnit(declaringInterface, getMethod(declaringInterface, methodName), key, Sets.newHashSet(ENGLISH, JAPANESE));
    }

    private static Method getMethod(Class<?> clazz, String methodName, Class<?>... args) {
        try {
            return clazz.getMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Translation bundleTr(Locale locale, String value) {
        return new Translation(locale, value, true, null, null);
    }

    private static Translation annotationTr(Locale locale, String value) {
        return new Translation(locale, value, false, locale2annotationClass.get(locale), null);
    }

    private static Translation annotationTr(Locale locale, String renderedValue, String valueInAnnotation) {
        return new Translation(locale, renderedValue, false, locale2annotationClass.get(locale), valueInAnnotation);
    }


    private static final class Translation {
        final Locale locale;
        final String renderedValue;
        final boolean inBundle;
        final Class<? extends Annotation> annotationClass;
        final String valueInAnnotation;

        private Translation(Locale locale, String renderedValue, boolean inBundle, Class<? extends Annotation> annotationClass, String valueInAnnotation) {
            this.locale = locale;
            this.renderedValue = renderedValue;
            this.inBundle = inBundle;
            this.annotationClass = annotationClass;
            this.valueInAnnotation = valueInAnnotation;
        }
    }
}
