/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.github.rodionmoiseev.c10n.guice;

import com.github.rodionmoiseev.c10n.C10N;
import com.github.rodionmoiseev.c10n.C10NMessages;
import com.google.inject.AbstractModule;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")//rationale: public API
public class C10NModule extends AbstractModule {
    private final String[] packagePrefixes;

    public static C10NModule scanAllPackages() {
        return scanPackages("");
    }

    public static C10NModule scanPackages(String... packagePrefixes) {
        return new C10NModule(packagePrefixes);
    }

    private C10NModule(String[] packagePrefixes) {
        this.packagePrefixes = packagePrefixes;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        Set<Class<?>> c10nTypes =
                new Reflections(new ConfigurationBuilder().filterInputsBy(getPackageInputFilter()).setUrls(getPackageURLs()))
                        .getTypesAnnotatedWith(C10NMessages.class);
        for (Class<?> c10nType : c10nTypes) {
            if (c10nType.isInterface()) {
                bind((Class<Object>) c10nType)
                        .toInstance(C10N.get(c10nType));
            }
        }
    }

    private Set<URL> getPackageURLs() {
        return Arrays.asList(packagePrefixes).stream()
                .flatMap(prefix -> ClasspathHelper.forPackage(prefix).stream())
                .collect(Collectors.toSet());
    }

    private FilterBuilder getPackageInputFilter() {
        final FilterBuilder inputFilter = new FilterBuilder();

        for (String prefix : packagePrefixes) {
            inputFilter.include(FilterBuilder.prefix(prefix));
        }

        return inputFilter;
    }
}
