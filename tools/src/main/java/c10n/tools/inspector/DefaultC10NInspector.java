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

import c10n.C10NMessages;
import c10n.ConfiguredC10NModule;
import c10n.share.utils.C10NBundleKey;
import c10n.share.utils.ReflectionUtils;
import c10n.tools.search.C10NInterfaceSearch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author rodion
 */
class DefaultC10NInspector implements C10NInspector {
    private final C10NInterfaceSearch c10NInterfaceSearch;
    private final ConfiguredC10NModule configuredC10NModule;
    private final Set<Locale> localesToCheck;

    DefaultC10NInspector(C10NInterfaceSearch c10NInterfaceSearch,
                         ConfiguredC10NModule configuredC10NModule,
                         Set<Locale> localesToCheck) {
        this.c10NInterfaceSearch = c10NInterfaceSearch;
        this.configuredC10NModule = configuredC10NModule;
        this.localesToCheck = localesToCheck;
    }

    @Override
    public List<C10NUnit> inspect(String... packagePrefixes) {
        Map<C10NBundleKey, C10NUnit> c10NUnitsByKey = Maps.newHashMap();

        Set<Class<?>> c10nInterfaces = c10NInterfaceSearch.find(C10NMessages.class, packagePrefixes);
        for (Class<?> c10nInterface : c10nInterfaces) {
            Set<Map.Entry<Class<? extends Annotation>, Set<Locale>>> annotationEntries =
                    configuredC10NModule.getAnnotationBindings(c10nInterface).entrySet();

            List<C10NBundleKey> keysForInterface = Lists.newArrayList();

            for (Method method : c10nInterface.getDeclaredMethods()) {
                String keyAnnotationValue = ReflectionUtils.getKeyAnnotationValue(method);
                String bundleKey = ReflectionUtils.getC10NKey(configuredC10NModule.getKeyPrefix(), method);
                boolean isCustom = ReflectionUtils.getKeyAnnotationBasedKey(method) != null;
                C10NBundleKey key = new C10NBundleKey(isCustom, bundleKey, keyAnnotationValue);

                for (Map.Entry<Class<? extends Annotation>, Set<Locale>> entry : annotationEntries) {
                    Class<? extends Annotation> annotation = entry.getKey();
                    for (Locale locale : entry.getValue()) {
                        C10NUnit unit = addC10NUnit(c10NUnitsByKey, c10nInterface, method, key);
                        C10NTranslations trs = addTranslations(unit, locale);
                        if (method.getAnnotation(annotation) != null) {
                            trs.getAnnotations().add(annotation);
                        }
                    }
                }

                keysForInterface.add(key);
            }

            for (Locale locale : localesToCheck) {
                List<ResourceBundle> bundles = configuredC10NModule.getBundleBindings(c10nInterface, locale);
                for (C10NBundleKey key : keysForInterface) {
                    C10NUnit unit = c10NUnitsByKey.get(key);
                    C10NTranslations trs = addTranslations(unit, locale);
                    for (ResourceBundle bundle : bundles) {
                        if (bundle.containsKey(key.getKey())) {
                            trs.getBundles().add(bundle);
                        }
                    }
                }
            }
        }

        return Lists.newArrayList(c10NUnitsByKey.values());
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

    private C10NTranslations addTranslations(C10NUnit unit, Locale locale) {
        C10NTranslations translations = unit.getTranslations().get(locale);
        if (null == translations) {
            translations = new C10NTranslations();
            unit.getTranslations().put(locale, translations);
        }
        return translations;
    }
}
