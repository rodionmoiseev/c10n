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

package c10n;

import c10n.share.EncodedResourceControl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import static c10n.share.utils.Preconditions.assertNotNull;

public abstract class C10NConfigBase {
  private final Map<String, C10NBundleBinder> bundleBinders = new HashMap<String, C10NBundleBinder>();
  private final Map<Class<?>, C10NImplementationBinder<?>> binders = new HashMap<Class<?>, C10NImplementationBinder<?>>();
  private final Map<Class<? extends Annotation>, C10NAnnotationBinder> annotationBinders = new HashMap<Class<? extends Annotation>, C10NAnnotationBinder>();
  private final List<C10NConfigBase> childConfigs = new ArrayList<C10NConfigBase>();

  private boolean configured = false;

  /**
   * <p>To be implemeted by subclasses of {@link C10NConfigBase}.</p>
   * <p>Configuration methods are as follows:
   * <ul>
   * <li>{@link #bindAnnotation(Class)} - binds annotation that holds translation for a specific locale.</li>
   * <li>{@link #bindBundle(String)} - binds a resource bundle containing translated messages.</li>
   * <li>{@link #install(C10NConfigBase)} - includes configuration from another c10n configuration module</li>
   * </ul>
   * </p>
   */
  protected abstract void configure();

  void doConfigure() {
    if (!configured) {
      configure();
    }
    configured = true;
  }

  protected void install(C10NConfigBase childConfig) {
    childConfig.doConfigure();
    childConfigs.add(childConfig);
  }

  <T> C10NImplementationBinder<T> bind(Class<T> c10nInterface) {
    C10NImplementationBinder<T> binder = new C10NImplementationBinder<T>();
    binders.put(c10nInterface, binder);
    return binder;
  }

  protected C10NAnnotationBinder bindAnnotation(Class<? extends Annotation> annotationClass) {
    assertNotNull(annotationClass, "annotationClass");
    checkAnnotationInterface(annotationClass);
    C10NAnnotationBinder binder = new C10NAnnotationBinder();
    annotationBinders.put(annotationClass, binder);
    return binder;
  }

  private void checkAnnotationInterface(Class<?> annotationClass) {
    Method valueMethod;
    try {
      valueMethod = annotationClass.getMethod("value");
    } catch (NoSuchMethodException e) {
      throw new C10NConfigException("Annotation could not be bound because it's missing a value() method. " +
              "Please add a value() method with return type of String. " +
              "annotationClass=" + annotationClass.getName(), e);
    }

    if (!valueMethod.getReturnType().isAssignableFrom(String.class)) {
      throw new C10NConfigException("The value() method's return type has to be String. " +
              "annotationClass=" + annotationClass.getName());
    }
  }

  protected C10NBundleBinder bindBundle(String baseName) {
    C10NBundleBinder binder = new C10NBundleBinder();
    bundleBinders.put(baseName, binder);
    return binder;
  }

  List<ResourceBundle> getBundlesForLocale(Class<?> c10nInterface, Locale locale) {
    List<ResourceBundle> res = new ArrayList<ResourceBundle>();
    for (Entry<String, C10NBundleBinder> entry : bundleBinders.entrySet()) {
      C10NBundleBinder binder = entry.getValue();
      if (binder.getBoundInterfaces().isEmpty()
              || binder.getBoundInterfaces().contains(c10nInterface)) {
        res.add(ResourceBundle.getBundle(entry.getKey(), locale,
                new EncodedResourceControl("UTF-8")));
      }
    }
    for (C10NConfigBase childConfig : childConfigs) {
      res.addAll(childConfig.getBundlesForLocale(c10nInterface, locale));
    }
    return res;
  }

  Map<Class<? extends Annotation>, C10NAnnotationBinder> getAnnotationBinders() {
    Map<Class<? extends Annotation>, C10NAnnotationBinder> res = new HashMap<Class<? extends Annotation>, C10NAnnotationBinder>();
    res.putAll(annotationBinders);
    for(C10NConfigBase childConfig : childConfigs){
      res.putAll(childConfig.getAnnotationBinders());
    }
    return res;
  }

  Class<?> getBindingForLocale(Class<?> c10nInterface, Locale locale) {
    C10NImplementationBinder<?> binder = binders.get(c10nInterface);
    if (null != binder) {
      return binder.getBindingForLocale(locale);
    }
    return null;
  }

  protected static class C10NAnnotationBinder {
    private Locale locale = C10N.FALLBACK_LOCALE;

    public void toLocale(Locale locale) {
      assertNotNull(locale, "locale");
      this.locale = locale;
    }

    public Locale getLocale() {
      return locale;
    }
  }

  protected static final class C10NImplementationBinder<T> {
    private final Map<Locale, Class<?>> bindings = new HashMap<Locale, Class<?>>();

    public C10NImplementationBinder<T> to(Class<? extends T> to, Locale forLocale) {
      bindings.put(forLocale, to);
      return this;
    }

    Class<?> getBindingForLocale(Locale locale) {
      return bindings.get(locale);
    }
  }
}
