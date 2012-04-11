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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author rodion
 */
interface ConfiguredC10NModule {
  Locale getCurrentLocale();
  Map<Class<? extends Annotation>, Set<Locale>> getAnnotationBindings(Class<?> c10nInterface);
  Set<Locale> getImplementationBindings(Class<?> c10nInterface);
  Class<?> getImplementationBinding(Class<?> c10nInterface, Locale locale);
  List<ResourceBundle> getBundleBindings(Class<?> c10nInterface, Locale locale);
  String getUntranslatedMessageString(Class<?> c10nInterface, Method method, Object[] methodArgs);
}
