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

import c10n.ConfiguredC10NModule;
import c10n.share.utils.C10NBundleKey;
import c10n.tools.search.C10NBundleKeySearch;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author rodion
 */
class DefaultC10NInspector implements C10NInspector {
    private final C10NBundleKeySearch bundleKeySearch;
    private final ConfiguredC10NModule configuredC10NModule;
    private final Set<Locale> localesToCheck;

    DefaultC10NInspector(C10NBundleKeySearch bundleKeySearch,
                         ConfiguredC10NModule configuredC10NModule,
                         Set<Locale> localesToCheck) {
        this.bundleKeySearch = bundleKeySearch;
        this.configuredC10NModule = configuredC10NModule;
        this.localesToCheck = localesToCheck;
    }

    @Override
    public Iterable<C10NUnit> inspect(String... packagePrefixes) {
        Map<C10NBundleKey, C10NUnit> c10NUnitsByKey = Maps.newHashMap();

        Iterable<C10NBundleKey> allKeys = bundleKeySearch.findAllKeys(packagePrefixes);
        Multimap<Class<?>, C10NBundleKey> keysByClass = Multimaps.index(allKeys, new Function<C10NBundleKey, Class<?>>() {
            @Override
            public Class<?> apply(@Nullable C10NBundleKey input) {
                if (null != input) {
                    return input.getDeclaringInterface();
                }
                return null;
            }
        });

        for (Class<?> c10nInterface : keysByClass.keySet()) {
            Set<Map.Entry<Class<? extends Annotation>, Set<Locale>>> annotationEntries =
                    configuredC10NModule.getAnnotationBindings(c10nInterface).entrySet();

            for (C10NBundleKey key : keysByClass.get(c10nInterface)) {
                for (Map.Entry<Class<? extends Annotation>, Set<Locale>> entry : annotationEntries) {
                    Class<? extends Annotation> annotation = entry.getKey();
                    for (Locale locale : entry.getValue()) {
                        C10NTranslations trs = addC10NUnit(c10NUnitsByKey, key, locale);
                        if (key.getDeclaringMethod().getAnnotation(annotation) != null) {
                            trs.getAnnotations().add(annotation);
                        }
                    }
                }
            }

            for (Locale locale : localesToCheck) {
                List<ResourceBundle> bundles = configuredC10NModule.getBundleBindings(c10nInterface, locale);
                for (C10NBundleKey key : keysByClass.get(c10nInterface)) {
                    C10NTranslations trs = addC10NUnit(c10NUnitsByKey, key, locale);
                    for (ResourceBundle bundle : bundles) {
                        if (bundle.containsKey(key.getKey())) {
                            trs.getBundles().add(bundle);
                        }
                    }
                }
            }
        }

        return c10NUnitsByKey.values();
    }

    private C10NTranslations addC10NUnit(Map<C10NBundleKey, C10NUnit> unitByKey, C10NBundleKey key, Locale locale) {
        C10NUnit unit = unitByKey.get(key);
        if (null == unit) {
            unit = new C10NUnit(key, localesToCheck);
            unitByKey.put(key, unit);
        }
        C10NTranslations translations = unit.getTranslations().get(locale);
        if (null == translations) {
            translations = new C10NTranslations();
            unit.getTranslations().put(locale, translations);
        }
        return translations;
    }
}
