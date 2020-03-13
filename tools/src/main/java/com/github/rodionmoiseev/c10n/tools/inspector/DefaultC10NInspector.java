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

import com.github.rodionmoiseev.c10n.C10N;
import com.github.rodionmoiseev.c10n.C10NMessages;
import com.github.rodionmoiseev.c10n.C10NMsgFactory;
import com.github.rodionmoiseev.c10n.ConfiguredC10NModule;
import com.github.rodionmoiseev.c10n.share.utils.C10NBundleKey;
import com.github.rodionmoiseev.c10n.share.utils.ReflectionUtils;
import com.github.rodionmoiseev.c10n.tools.search.C10NInterfaceSearch;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author rodion
 */
class DefaultC10NInspector implements C10NInspector {
    private final C10NInterfaceSearch c10NInterfaceSearch;
    private final ConfiguredC10NModule configuredC10NModule;
    private final DummyInstanceProvider dummyInstanceProvider;
    private final Set<Locale> localesToCheck;
    private final boolean fetchTranslations;

    DefaultC10NInspector(C10NInterfaceSearch c10NInterfaceSearch,
                         ConfiguredC10NModule configuredC10NModule,
                         DummyInstanceProvider dummyInstanceProvider,
                         Set<Locale> localesToCheck,
                         boolean fetchTranslations) {
        this.c10NInterfaceSearch = c10NInterfaceSearch;
        this.configuredC10NModule = configuredC10NModule;
        this.dummyInstanceProvider = dummyInstanceProvider;
        this.localesToCheck = localesToCheck;
        this.fetchTranslations = fetchTranslations;
    }

    @Override
    public List<C10NUnit> inspect(String... packagePrefixes) {
        List<C10NUnit> res = new ArrayList<>();

        @SuppressWarnings("deprecation")
        C10NMsgFactory c10NMsgFactory = C10N.createMsgFactory(configuredC10NModule);

        Set<Class<?>> c10nInterfaces = c10NInterfaceSearch.find(C10NMessages.class, packagePrefixes);
        for (Class<?> c10nInterface : c10nInterfaces) {
            Set<Map.Entry<Class<? extends Annotation>, Set<Locale>>> annotationEntries =
                    configuredC10NModule.getAnnotationBindings(c10nInterface).entrySet();


            List<C10NUnit> unitsForInterface = new ArrayList<>();

            for (Method method : c10nInterface.getDeclaredMethods()) {
                String keyAnnotationValue = ReflectionUtils.getKeyAnnotationValue(method);
                String bundleKey = ReflectionUtils.getC10NKey(configuredC10NModule.getKeyPrefix(), method);
                boolean isCustom = ReflectionUtils.getKeyAnnotationBasedKey(method) != null;
                C10NBundleKey key = new C10NBundleKey(isCustom, bundleKey, keyAnnotationValue);
                C10NUnit unit = new C10NUnit(c10nInterface, method, key, localesToCheck);
                for (Map.Entry<Class<? extends Annotation>, Set<Locale>> entry : annotationEntries) {
                    Class<? extends Annotation> annotationClass = entry.getKey();
                    for (Locale locale : entry.getValue()) {
                        if (localesToCheck.contains(locale)) {
                            String translatedValue = extractTranslatedValue(c10NMsgFactory,
                                    c10nInterface,
                                    method,
                                    locale);
                            C10NTranslations trs = addTranslations(unit, locale, translatedValue);
                            Annotation annotation = method.getAnnotation(annotationClass);
                            if (null != annotation) {
                                trs.getAnnotations().add(annotation);
                            }
                        }
                    }
                }
                unitsForInterface.add(unit);
            }

            for (Locale locale : localesToCheck) {
                List<ResourceBundle> bundles = configuredC10NModule.getBundleBindings(c10nInterface, locale);
                for (C10NUnit unit : unitsForInterface) {
                    String translatedValue = extractTranslatedValue(c10NMsgFactory,
                            c10nInterface,
                            unit.getDeclaringMethod(),
                            locale);
                    C10NTranslations trs = addTranslations(unit, locale, translatedValue);
                    for (ResourceBundle bundle : bundles) {
                        if (bundle.containsKey(unit.getKey().getKey())) {
                            trs.getBundles().add(bundle);
                        }
                    }
                }
            }

            res.addAll(unitsForInterface);
        }

        return res;
    }

    private String extractTranslatedValue(C10NMsgFactory c10NMsgFactory,
                                          Class<?> c10nInterface,
                                          Method method,
                                          Locale locale) {
        if (fetchTranslations) {
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] args = new Object[paramTypes.length];
            try {
                for (int i = 0; i < args.length; i++) {
                    args[i] = dummyInstanceProvider.getInstance(c10nInterface, method, paramTypes[i], i);
                    if (null == args[i]) {
                        throw new C10NInspectorException("Cannot create dummy instance for" +
                                "type: " + paramTypes[i].getName());
                    }
                }
                Object v = method.invoke(c10NMsgFactory.get(c10nInterface, locale), args);
                if (null != v) {
                    return v.toString();
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch translated value for method='"
                        + method.toGenericString()
                        + "' with arguments="
                        + Arrays.toString(args));
                e.printStackTrace(System.err);
            }
        }
        return null;
    }

    private C10NUnit addC10NUnit(Map<C10NBundleKey, C10NUnit> unitByKey,
                                 Class<?> declaringInterface,
                                 Method declaringMethod,
                                 C10NBundleKey key) {
        C10NUnit unit = unitByKey.get(key);
        if (null == unit) {
            unit = new C10NUnit(declaringInterface, declaringMethod, key, localesToCheck);
            unitByKey.put(key, unit);
        }
        return unit;
    }

    private C10NTranslations addTranslations(C10NUnit unit, Locale locale, String value) {
        C10NTranslations translations = unit.getTranslations().get(locale);
        if (null == translations) {
            translations = new C10NTranslations();
            unit.getTranslations().put(locale, translations);
        }
        translations.setValue(value);
        return translations;
    }


    private static final class C10NInspectorException extends Exception {
        private static final long serialVersionUID = 1L;

        C10NInspectorException(String message) {
            super(message);
        }
    }
}
