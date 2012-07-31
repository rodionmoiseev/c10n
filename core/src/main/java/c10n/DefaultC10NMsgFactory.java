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

import c10n.share.Constants;
import c10n.share.LocaleMapping;
import c10n.share.utils.ReflectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DefaultC10NMsgFactory implements C10NMsgFactory {
  private final ConfiguredC10NModule conf;
  private final LocaleMapping localeMapping;
  private final ClassLoader proxyClassloader;

  DefaultC10NMsgFactory(ConfiguredC10NModule conf, LocaleMapping localeMapping, ClassLoader proxyClassloader) {
    this.conf = conf;
    this.localeMapping = localeMapping;
    this.proxyClassloader = proxyClassloader;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> c10nInterface) {
    if (null == c10nInterface) {
      throw new NullPointerException("c10nInterface is null");
    }
    return (T) Proxy.newProxyInstance(proxyClassloader,
            new Class[]{c10nInterface},
            C10NInvocationHandler.create(this, conf, localeMapping, c10nInterface));
  }

  private static final class C10NString {
    final String text;
    final boolean raw;

    public static C10NString def(String text) {
      return new C10NString(text, false);
    }

    public static C10NString raw(String text) {
      return new C10NString(text, true);
    }

    private C10NString(String text, boolean raw) {
      this.text = text;
      this.raw = raw;
    }
  }

  private static final class C10NInvocationHandler implements
          InvocationHandler {
    private static final Annotation[] NO_ANNOTATIONS = new Annotation[0];
    private final C10NMsgFactory c10nFactory;
    private final ConfiguredC10NModule conf;
    private final LocaleMapping localeMapping;
    private final Class<?> proxiedClass;
    private final Map<String, Map<Locale, C10NString>> translationsByMethod;
    //private final Set<Locale> availableLocales;
    private final Set<Locale> availableImplLocales;
    private final Map<AnnotatedClass, C10NFilterProvider<?>> filters;

    C10NInvocationHandler(C10NMsgFactory c10nFactory,
                          ConfiguredC10NModule conf,
                          LocaleMapping localeMapping,
                          Class<?> proxiedClass,
                          Map<String, Map<Locale, C10NString>> translationsByMethod) {
      this.c10nFactory = c10nFactory;
      this.conf = conf;
      this.localeMapping = localeMapping;
      this.proxiedClass = proxiedClass;
      this.translationsByMethod = translationsByMethod;
      this.availableImplLocales = conf.getImplementationBindings(proxiedClass);
      this.filters = conf.getFilterBindings(proxiedClass);
    }

    static C10NInvocationHandler create(C10NMsgFactory c10nFactory,
                                        ConfiguredC10NModule conf, LocaleMapping localeMapping, Class<?> c10nInterface) {
      Map<String, Map<Locale, C10NString>> translationsByMethod = new HashMap<String, Map<Locale, C10NString>>();

      //Translations defined in @C10NDef annotation are
      //always considered a fallback
      for (Method m : c10nInterface.getMethods()) {
        C10NDef c10nDef = m.getAnnotation(C10NDef.class);
        if (null != c10nDef) {
          Map<Locale, C10NString> defMapping = new HashMap<Locale, C10NString>();
          defMapping.put(C10N.FALLBACK_LOCALE, C10NString.def(c10nDef.value()));
          translationsByMethod.put(m.toString(), defMapping);
        }
      }

      // Process custom bound annotations
      for (Entry<Class<? extends Annotation>, Set<Locale>> entry : conf
              .getAnnotationBindings(c10nInterface).entrySet()) {
        Class<? extends Annotation> annotationClass = entry.getKey();
        for (Method m : c10nInterface.getMethods()) {
          Annotation a = m.getAnnotation(annotationClass);
          if (null != a) {
            try {
              C10NString translation = getAnnotationValue(c10nInterface, annotationClass, a);
              Map<Locale, C10NString> translationsByLocale = translationsByMethod.get(m.toString());
              if (null == translationsByLocale) {
                translationsByLocale = new HashMap<Locale, C10NString>();
                translationsByMethod.put(m.toString(), translationsByLocale);
              }
              for (Locale locale : entry.getValue()) {
                translationsByLocale.put(locale, translation);
              }
            } catch (SecurityException e) {
              throw new RuntimeException("Annotation "
                      + annotationClass.getName()
                      + " value() method is not accessible", e);
            } catch (NoSuchMethodException e) {
              throw new RuntimeException("Annotation "
                      + annotationClass.getName()
                      + " must declare value() method");
            } catch (RuntimeException e) {
              throw e;
            } catch (Exception e) {
              throw new RuntimeException(
                      "Could not call value() on annotation "
                              + annotationClass.getName(), e);
            }
          }
        }
      }

      return new C10NInvocationHandler(c10nFactory, conf, localeMapping, c10nInterface,
              translationsByMethod);
    }

    private static C10NString getAnnotationValue(Class<?> c10nInterface,
                                                 Class<? extends Annotation> annotationClass,
                                                 Annotation a)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
      boolean raw = extractAnnotationValue(annotationClass, "raw", a, false);
      Object valueTranslation = extractAnnotationValue(annotationClass, "value", a, Constants.UNDEF);
      if (valueTranslation.equals(Constants.UNDEF)) {
        //check for external resource declarations
        Object extRes = extractAnnotationValue(annotationClass, "extRes", a, Constants.UNDEF);
        if (extRes.equals(Constants.UNDEF)) {
          Object intRes = extractAnnotationValue(annotationClass, "intRes", a, Constants.UNDEF);
          if (intRes.equals(Constants.UNDEF)) {
            throw new RuntimeException("One of @" + annotationClass.getSimpleName() + " annotations on the " +
                    c10nInterface.getCanonicalName() +
                    " class does not have any of 'value' or 'extRes' or 'intRes' specified.");
          }
          return new C10NString(readTextFromInternalResource(replaceSystemProps(String.valueOf(intRes))), raw);
        }
        return new C10NString(readTextFromUrl(replaceSystemProps(String.valueOf(extRes))), raw);
      }
      return new C10NString(String.valueOf(valueTranslation), raw);
    }

    @SuppressWarnings("unchecked")
    private static <R> R extractAnnotationValue(Class<? extends Annotation> annotationClass, String method,
                                                Annotation annotation, R defaultValue) {
      try {
        return (R) annotationClass.getMethod(method).invoke(annotation);
      } catch (InvocationTargetException e) {
        return defaultValue;
      } catch (NoSuchMethodException e) {
        return defaultValue;
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("Failed to extract value of '" + method + "' from annotation" +
                "class " + annotationClass.getCanonicalName(), e);
      }
    }

    private static String replaceSystemProps(String string) {
      if (null == string) {
        return null;
      }
      String res = string;
      Pattern p = Pattern.compile("\\$\\{.*?\\}");
      Matcher m = p.matcher(string);
      while (m.find()) {
        String prop = string.substring(m.start() + 2, m.end() - 1);
        String propValue = System.getProperty(prop);
        if (propValue != null) {
          res = res.replace("${" + prop + "}", propValue);
        }
      }
      return res;
    }

    private static String readTextFromUrl(String urlString) {
      try {
        URL url = new URL(urlString);
        InputStream is = null;
        try {
          try {
            is = url.openStream();
            return readTextFromInputStream(is);
          } finally {
            if (is != null) {
              is.close();
            }
          }
        } catch (IOException e) {
          throw new RuntimeException("Failed to read text data from URL: " + urlString, e);
        }
      } catch (MalformedURLException e) {
        throw new RuntimeException("Could not interpret external resource URL: " + urlString, e);
      }
    }

    private static String readTextFromInternalResource(String path) {
      InputStream is = null;
      try {
        try {
          is = C10N.class.getClassLoader().getResourceAsStream(path);
          if (null == is) {
            throw new RuntimeException("Internal resource: " + path + " does not exist");
          }
          return readTextFromInputStream(is);
        } finally {
          if (null != is) {
            is.close();
          }
        }
      } catch (IOException e) {
        throw new RuntimeException("Failed to read text data from internal resource: " + path, e);
      }
    }

    private static String readTextFromInputStream(InputStream is) throws IOException {
      BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF8"), 1024 * 8);
      CharBuffer buf = CharBuffer.allocate(64);
      int read;
      do {
        read = br.read(buf);
        if (read == 0 && !buf.hasRemaining()) {
          CharBuffer newBuf = CharBuffer.allocate(buf.capacity() * 2);
          buf.flip();
          newBuf.put(buf);
          buf = newBuf;
        }
      } while (read != -1);

      buf.flip();
      return buf.toString();
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
            return format(bundle.getString(key), method, args);
          }
        }

        C10NString res = findTranslationFromAnnotations(method, locale);
        if (null == res) {
          return conf.getUntranslatedMessageString(proxiedClass, method, args);
        }
        return format(res, method, args);
      } else if (returnType.isInterface()) {
        if (null != returnType.getAnnotation(C10NMessages.class)) {
          return c10nFactory.get(returnType);
        }
      }
      // don't know how to handle this return type
      return null;
    }

    private C10NString findTranslationFromAnnotations(Method method, Locale locale) {
      Map<Locale, C10NString> translationsByLocale = translationsByMethod.get(method.toString());
      if (null != translationsByLocale) {
        return translationsByLocale.get(localeMapping.findClosestMatch(translationsByLocale.keySet(), locale));
      }
      return null;
    }

    private String format(C10NString message, Method method, Object... args) {
      return format(message.text, message.raw, method, args);
    }

    private String format(String message, Method method, Object... args) {
      return format(message, false, method, args);
    }

    private String format(String message, boolean raw, Method method, Object... args) {
      if (raw) {
        //Raw messages accept no parameters
        return message;
      }

      Annotation[][] argAnnotations = method.getParameterAnnotations();
      Class[] argTypes = method.getParameterTypes();
      if (args != null && args.length > 0) {
        Object[] filteredArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
          Annotation[] annotations = argAnnotations != null ? argAnnotations[i] : NO_ANNOTATIONS;
          filteredArgs[i] = applyArgFilterIfExists(annotations, argTypes[i], args[i]);
        }
        return MessageFormat.format(message, filteredArgs);
      }
      return MessageFormat.format(message, args);
    }

    private Object applyArgFilterIfExists(Annotation[] annotations, Class argType, Object arg) {
      //1. Look for first filter matching any of the annotations
      for (Annotation annotation : annotations) {
        C10NFilterProvider<Object> filter = findFilterFor(argType, annotation.annotationType());
        if (null != filter) {
          //filter found, look no further
          return filter.get().apply(arg);
        }
      }
      //2. Try annotation-less filter binding
      C10NFilterProvider<Object> filter = findFilterFor(argType, null);
      if (null != filter) {
        return filter.get().apply(arg);
      }
      //3. No filter found, return argument as-is
      return arg;
    }

    @SuppressWarnings("unchecked")
    private C10NFilterProvider<Object> findFilterFor(Class argType, Class<? extends Annotation> annotationClass) {
      return (C10NFilterProvider<Object>) filters
              .get(new AnnotatedClass(argType, annotationClass));
    }
  }
}
