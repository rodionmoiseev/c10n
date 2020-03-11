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

package com.github.rodionmoiseev.c10n;

import com.github.rodionmoiseev.c10n.formatters.MessageFormatter;
import com.github.rodionmoiseev.c10n.plugin.C10NPlugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author rodion
 */
public interface ConfiguredC10NModule {
    Locale getCurrentLocale();

    Map<Class<? extends Annotation>, Set<Locale>> getAnnotationBindings(Class<?> c10nInterface);

    Set<Locale> getImplementationBindings(Class<?> c10nInterface);

    Class<?> getImplementationBinding(Class<?> c10nInterface, Locale locale);

    List<ResourceBundle> getBundleBindings(Class<?> c10nInterface, Locale locale);

    String getUntranslatedMessageString(Class<?> c10nInterface, Method method, Object[] methodArgs);

    Map<AnnotatedClass, C10NFilterProvider<?>> getFilterBindings(Class<?> c10nInterface);

    String getKeyPrefix();

    boolean isDebug();

    Set<Locale> getAllBoundLocales();

    List<C10NPlugin> getPlugins();

    MessageFormatter getMessageFormatter();

    ClassLoader getProxyClassLoader();
}

