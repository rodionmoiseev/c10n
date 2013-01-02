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

package c10n;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author rodion
 */
class DefaultConfiguredC10NModule implements ConfiguredC10NModule {
    private final C10NConfigBase parentConfig;
    private final ConfigChainResolver configResolver;

    DefaultConfiguredC10NModule(C10NConfigBase parentConfig, ConfigChainResolver configResolver) {
        this.parentConfig = parentConfig;
        this.configResolver = configResolver;
    }

    @Override
    public Locale getCurrentLocale() {
        return this.parentConfig.getCurrentLocale();
    }

    @Override
    public Map<Class<? extends Annotation>, Set<Locale>> getAnnotationBindings(Class<?> c10nInterface) {
        Map<Class<? extends Annotation>, Set<Locale>> res = new HashMap<Class<? extends Annotation>, Set<Locale>>();
        List<C10NConfigBase> configChain = configResolver.resolve(c10nInterface);
        Collections.reverse(configChain);
        for (C10NConfigBase config : configChain) {
            //config chain is reversed (parent -> .. -> child) to
            //make sure child configurations overwrite parent ones
            //to take precedence
            res.putAll(config.getAnnotationToLocaleMapping());
        }
        return res;
    }

    @Override
    public Set<Locale> getImplementationBindings(Class<?> c10nInterface) {
        Set<Locale> res = new HashSet<Locale>();
        List<C10NConfigBase> configChain = configResolver.resolve(c10nInterface);
        for (C10NConfigBase config : configChain) {
            res.addAll(config.getImplLocales(c10nInterface));
        }
        return res;
    }

    @Override
    public Class<?> getImplementationBinding(Class<?> c10nInterface, Locale locale) {
        List<C10NConfigBase> configChain = configResolver.resolve(c10nInterface);
        for (C10NConfigBase config : configChain) {
            Class<?> impl = config.getBindingForLocale(c10nInterface, locale);
            if (null != impl) {
                return impl;
            }
        }
        return null;
    }

    @Override
    public List<ResourceBundle> getBundleBindings(Class<?> c10nInterface, Locale locale) {
        List<ResourceBundle> res = new ArrayList<ResourceBundle>();
        List<C10NConfigBase> configChain = configResolver.resolve(c10nInterface);
        for (C10NConfigBase config : configChain) {
            res.addAll(config.getBundlesForLocale(c10nInterface, locale));
        }
        return res;
    }

    @Override
    public Map<AnnotatedClass, C10NFilterProvider<?>> getFilterBindings(Class<?> c10nInterface) {
        List<C10NConfigBase> configChain = configResolver.resolve(c10nInterface);
        Map<AnnotatedClass, C10NFilterProvider<?>> res = new HashMap<AnnotatedClass, C10NFilterProvider<?>>();
        Collections.reverse(configChain);
        for (C10NConfigBase config : configChain) {
            for (C10NConfigBase.C10NFilterBinder<?> filterBinder : config.getFilterBinders()) {
                if (filterBinder.getAnnotatedWith().isEmpty()) {
                    res.put(new AnnotatedClass(filterBinder.getType(), null), filterBinder.getFilterProvider());
                } else {
                    for (Class<? extends Annotation> annotation : filterBinder.getAnnotatedWith()) {
                        res.put(new AnnotatedClass(filterBinder.getType(), annotation), filterBinder.getFilterProvider());
                    }
                }
            }
        }
        return res;
    }

    @Override
    public String getUntranslatedMessageString(Class<?> c10nInterface, Method method, Object[] methodArgs) {
        return parentConfig.getUntranslatedMessageString(c10nInterface, method, methodArgs);
    }

    @Override
    public String getKeyPrefix() {
        return this.parentConfig.getKeyPrefix();
    }

    @Override
    public boolean isDebug() {
        return this.parentConfig.isDebug();
    }

    @Override
    public Set<Locale> getAllBoundLocales() {
        Set<Locale> res = new HashSet<Locale>();
        for (C10NConfigBase config : traverseConfigs(parentConfig)) {
            res.addAll(config.getAllImplementationBoundLocales());
            for (Set<Locale> locales : config.getAnnotationToLocaleMapping().values()) {
                res.addAll(locales);
            }
        }
        return res;
    }

    private List<C10NConfigBase> traverseConfigs(C10NConfigBase config) {
        List<C10NConfigBase> res = new ArrayList<C10NConfigBase>();
        traverseConfigs(config, res);
        return res;
    }

    private void traverseConfigs(C10NConfigBase config, List<C10NConfigBase> result) {
        result.add(config);
        for (C10NConfigBase childConfig : config.getChildConfigs()) {
            traverseConfigs(childConfig, result);
        }
    }
}
