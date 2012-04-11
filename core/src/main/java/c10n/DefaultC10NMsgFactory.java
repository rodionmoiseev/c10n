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

import c10n.share.LocaleMapping;
import c10n.share.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

class DefaultC10NMsgFactory implements C10NMsgFactory {
  private final ConfiguredC10NModule conf;
  private final LocaleMapping localeMapping;

  DefaultC10NMsgFactory(ConfiguredC10NModule conf, LocaleMapping localeMapping) {
    this.conf = conf;
    this.localeMapping = localeMapping;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> c10nInterface) {
    if (null == c10nInterface) {
      throw new NullPointerException("c10nInterface is null");
    }
    return (T) Proxy.newProxyInstance(C10N.class.getClassLoader(),
            new Class[]{c10nInterface},
            C10NInvocationHandler.create(this, conf, localeMapping, c10nInterface));
  }

  private static final class C10NInvocationHandler implements
          InvocationHandler {
    private final C10NMsgFactory c10nFactory;
    private final ConfiguredC10NModule conf;
    private final LocaleMapping localeMapping;
    private final Class<?> proxiedClass;
    private final Map<Locale, Map<String, String>> translationsByLocale;
    private final Set<Locale> availableLocales;
    private final Set<Locale> availableImplLocales;

    C10NInvocationHandler(C10NMsgFactory c10nFactory,
                          ConfiguredC10NModule conf,
                          LocaleMapping localeMapping,
                          Class<?> proxiedClass,
                          Map<Locale, Map<String, String>> translationsByLocale) {
      this.c10nFactory = c10nFactory;
      this.conf = conf;
      this.localeMapping = localeMapping;
      this.proxiedClass = proxiedClass;
      this.translationsByLocale = translationsByLocale;
      this.availableLocales = translationsByLocale.keySet();
      this.availableImplLocales = conf.getImplementationBindings(proxiedClass);
    }

    static C10NInvocationHandler create(C10NMsgFactory c10nFactory,
                                        ConfiguredC10NModule conf, LocaleMapping localeMapping, Class<?> c10nInterface) {
      Map<Locale, Map<String, String>> translationsByLocale = new HashMap<Locale, Map<String, String>>();

      Map<String, String> vals = new HashMap<String, String>();
      for (Method m : c10nInterface.getMethods()) {
        C10NDef c10n = m.getAnnotation(C10NDef.class);
        if (null != c10n) {
          vals.put(m.toString(), c10n.value());
        }
      }
      translationsByLocale.put(C10N.FALLBACK_LOCALE, vals);

      // Process custom bound annotations
      for (Entry<Class<? extends Annotation>, Set<Locale>> entry : conf
              .getAnnotationBindings(c10nInterface).entrySet()) {
        Class<? extends Annotation> annotationClass = entry.getKey();
        Map<String, String> translations = new HashMap<String, String>();
        for (Method m : c10nInterface.getMethods()) {
          Annotation a = m.getAnnotation(annotationClass);
          if (null != a) {
            try {
              String translation = String.valueOf(annotationClass
                      .getMethod("value").invoke(a));
              translations.put(m.toString(), translation);
            } catch (SecurityException e) {
              throw new RuntimeException("Annotation "
                      + annotationClass.getName()
                      + " value() method is not accessible", e);
            } catch (NoSuchMethodException e) {
              throw new RuntimeException("Annotation "
                      + annotationClass.getName()
                      + " must declare value() method");
            } catch (Exception e) {
              throw new RuntimeException(
                      "Could not call value() on annotation "
                              + annotationClass.getName(), e);
            }
          }
        }

        if (!translations.isEmpty()) {
          for (Locale locale : entry.getValue()) {
            Map<String, String> tr = translationsByLocale.get(locale);
            if (null == tr) {
              tr = new HashMap<String, String>();
              translationsByLocale.put(locale, tr);
            }
            tr.putAll(translations);
          }
        }
      }

      return new C10NInvocationHandler(c10nFactory, conf, localeMapping, c10nInterface,
              translationsByLocale);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
      Locale locale = conf.getCurrentLocale();

      Locale implLocale = localeMapping.findClosestMatch(availableImplLocales, locale);

      Class<?> binding = conf.getImplementationBinding(proxiedClass, implLocale);
      if (null != binding) {
        // user specified binding exists
        // simply delegate the call to the binding
        Object instance = binding.newInstance();
        return method.invoke(instance, args);
      }

      Class<?> returnType = method.getReturnType();
      if (returnType.isAssignableFrom(String.class)) {
        // For methods returning String or CharSequence

        List<ResourceBundle> bundles = conf.getBundleBindings(proxiedClass, locale);
        for (ResourceBundle bundle : bundles) {
          StringBuilder sb = new StringBuilder();
          ReflectionUtils.getDefaultKey(proxiedClass, method, sb);
          String key = sb.toString();
          if (bundle.containsKey(key)) {
            return MessageFormat
                    .format(bundle.getString(key), args);
          }
        }

        String res = null;
        Map<String, String> trs = getTranslations(locale);
        if (trs != null) {
          res = trs.get(method.toString());
        }
        if (null == res) {
          return conf.getUntranslatedMessageString(proxiedClass, method, args);
        }
        return MessageFormat.format(res, args);
      } else if (returnType.isInterface()) {
        if (null != returnType.getAnnotation(C10NMessages.class)) {
          return c10nFactory.get(returnType);
        }
      }
      // don't know how to handle this return type
      return null;
    }

    private Map<String, String> getTranslations(Locale locale) {
      return translationsByLocale.get(localeMapping.findClosestMatch(availableLocales, locale));
    }
  }
}
