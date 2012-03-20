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

package c10n.guice;

import c10n.C10N;
import c10n.C10NMessages;
import com.google.inject.AbstractModule;
import org.reflections.Reflections;

import java.util.Set;

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
    Set<Class<?>> c10nTypes = new Reflections(packagePrefixes)
            .getTypesAnnotatedWith(C10NMessages.class);
    for (Class<?> c10nType : c10nTypes) {
      bind((Class<Object>) c10nType)
              .toInstance(C10N.get(c10nType));
    }
  }
}
