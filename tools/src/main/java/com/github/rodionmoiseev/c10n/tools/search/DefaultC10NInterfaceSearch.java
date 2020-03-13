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

package com.github.rodionmoiseev.c10n.tools.search;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DefaultC10NInterfaceSearch implements C10NInterfaceSearch {
    @Override
    public Set<Class<?>> find(Class<? extends Annotation> annotationClass, String... packagePrefixes) {
        return new Reflections(new ConfigurationBuilder()
                .filterInputsBy(getPackageInputFilter(packagePrefixes))
                .setUrls(getPackageURLs(packagePrefixes)))
                .getTypesAnnotatedWith(annotationClass);
    }


    private Set<URL> getPackageURLs(String... packagePrefixes) {
        Stream<URL> urls = Arrays.stream(
                packagePrefixes).map((prefix) -> ClasspathHelper.forPackage(prefix).stream()
        ).reduce(Stream.empty(), Stream::concat);
        return urls.collect(Collectors.toSet());
    }

    private FilterBuilder getPackageInputFilter(String... packagePrefixes) {
        final FilterBuilder inputFilter = new FilterBuilder();

        for (String prefix : packagePrefixes) {
            inputFilter.include(FilterBuilder.prefix(prefix));
        }

        return inputFilter;
    }
}
