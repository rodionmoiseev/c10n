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

package c10n.tools.codegen;

import c10n.gen.C10NGenMessages;
import c10n.gen.C10NGenValue;
import c10n.share.utils.ReflectionUtils;
import c10n.tools.search.C10NInterfaceSearch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

class DefaultBundleGenerator implements BundleGenerator {
  private final C10NInterfaceSearch search;

  public DefaultBundleGenerator(C10NInterfaceSearch search) {
    this.search = search;
  }

  @Override
  public void convertAll(String packagePrefix, File outputSrcFolder,
                         File baseFile) throws IOException {
    Map<String, Map<String, String>> translationsPerLocale = new HashMap<String, Map<String, String>>();
    Set<Class<?>> genClasses = this.search.find(packagePrefix,
            C10NGenMessages.class);
    for (Class<?> genClass : genClasses) {
      C10NGenMessages gm = genClass.getAnnotation(C10NGenMessages.class);
      if (null == gm) {
        throw new IllegalStateException("Failed to fetch "
                + C10NGenMessages.class.getName()
                + " annotation from class: " + genClass.getName());
      }
      String localeSuffix = gm.value();
      Map<String, String> localeTranslations = translationsPerLocale
              .get(localeSuffix);
      if (null == localeTranslations) {
        localeTranslations = new HashMap<String, String>();
        translationsPerLocale.put(localeSuffix, localeTranslations);
      }
      Class<?>[] c10nInterfaces = genClass.getInterfaces();
      if (c10nInterfaces == null || c10nInterfaces.length != 1) {
        throw new IllegalStateException("Generated class "
                + genClass.getName()
                + " must implement exactly one interface: "
                + Arrays.toString(c10nInterfaces));
      }
      for (Method m : genClass.getDeclaredMethods()) {
        C10NGenValue gv = m.getAnnotation(C10NGenValue.class);
        if (null != gv) {
          if (gv.defined()) {
            String value = gv.value();
            String key = ReflectionUtils.getDefaultKey(
                    c10nInterfaces[0], m);
            localeTranslations.put(key, value);
          }
        } else {
          // method is part of another interface so can be ignored
        }
      }
    }

    // generate bundles
    for (Entry<String, Map<String, String>> entry : translationsPerLocale
            .entrySet()) {
      String locale = entry.getKey().equals("")//
              ? ""//
              : "_" + entry.getKey();
      File bundleFile = new File(baseFile.getParentFile(),
              baseFile.getName() + locale + ".properties");

      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(bundleFile);
        @SuppressWarnings("serial")
        Properties prop = new Properties() {
          @Override
          public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<Object>(super
                    .keySet()));
          }
        };
        prop.putAll(entry.getValue());
        prop.store(new OutputStreamWriter(fos, Charset.forName("UTF-8")),
                null);
      } finally {
        if (null != fos) {
          fos.close();
        }
      }
    }
  }
}
