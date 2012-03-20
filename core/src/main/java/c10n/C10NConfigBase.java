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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import static c10n.share.utils.Preconditions.assertNotNull;

public abstract class C10NConfigBase {
  private final Map<String, C10NBundleBinder> bundleBinders = new HashMap<String, C10NBundleBinder>();
  private final Map<Class<?>, C10NImplementationBinder<?>> binders = new HashMap<Class<?>, C10NImplementationBinder<?>>();
  private final Map<Class<? extends Annotation>, C10NAnnotationBinder<?>> annotationBinders = new HashMap<Class<? extends Annotation>, C10NAnnotationBinder<?>>();

  public abstract void configure();

  public <T> C10NImplementationBinder<T> bind(Class<T> c10nInterface) {
    C10NImplementationBinder<T> binder = new C10NImplementationBinder<T>();
    binders.put(c10nInterface, binder);
    return binder;
  }

  public <T extends Annotation> C10NAnnotationBinder<T> bindAnnotation(Class<T> annotationClass) {
    assertNotNull(annotationClass, "annotationClass");
    checkAnnotationInterface(annotationClass);
    C10NAnnotationBinder<T> binder = new C10NAnnotationBinder<T>();
    annotationBinders.put(annotationClass, binder);
    return binder;
  }

  private <T extends Annotation> void checkAnnotationInterface(Class<T> annotationClass) {
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

  public C10NBundleBinder bindBundle(String baseName) {
    C10NBundleBinder binder = new C10NBundleBinder();
    bundleBinders.put(baseName, binder);
    return binder;
  }

  ResourceBundle getBundleForLocale(Class<?> c10nInterface, Locale locale) {
    for (Entry<String, C10NBundleBinder> entry : bundleBinders.entrySet()) {
      C10NBundleBinder binder = entry.getValue();
      if (binder.getBoundInterfaces().isEmpty()
              || binder.getBoundInterfaces().contains(c10nInterface)) {
        return ResourceBundle.getBundle(entry.getKey(), locale,
                new EncodedResourceControl("UTF-8"));
      }
    }
    return null;
  }

  Map<Class<? extends Annotation>, C10NAnnotationBinder<?>> getAnnotationBinders() {
    return annotationBinders;
  }

  Class<?> getBindingForLocale(Class<?> c10nInterface, Locale locale) {
    C10NImplementationBinder<?> binder = binders.get(c10nInterface);
    if (null != binder) {
      return binder.getBindingForLocale(locale);
    }
    return null;
  }

  public static class C10NAnnotationBinder<T> {
    private Locale locale = C10N.FALLBACK_LOCALE;

    public void toLocale(Locale locale) {
      assertNotNull(locale, "locale");
      this.locale = locale;
    }

    public Locale getLocale() {
      return locale;
    }
  }

  public static final class C10NImplementationBinder<T> {
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
