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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import static c10n.share.utils.Preconditions.assertNotNull;

public abstract class C10NConfigBase {
  //DI
  private final C10NCoreModule coreModule = new C10NCoreModule();
  private LocaleProvider localeProvider = coreModule.defaultLocaleProvider();
  private UntranslatedMessageHandler untranslatedMessageHandler = coreModule.defaultUnknownMessageHandler();
  private final Map<String, C10NBundleBinder> bundleBinders = new HashMap<String, C10NBundleBinder>();
  private final Map<Class<?>, C10NImplementationBinder<?>> binders = new HashMap<Class<?>, C10NImplementationBinder<?>>();
  private final Map<Class<? extends Annotation>, C10NAnnotationBinder> annotationBinders = new HashMap<Class<? extends Annotation>, C10NAnnotationBinder>();
  private final List<C10NFilterBinder<?>> filterBinders = new ArrayList<C10NFilterBinder<?>>();
  private final List<C10NConfigBase> childConfigs = new ArrayList<C10NConfigBase>();

  private boolean configured = false;

  /**
   * <p>To be implemented by subclasses of {@link C10NConfigBase}.</p>
   * <p>Configuration methods are as follows:
   * <ul>
   * <li>{@link #bindAnnotation(Class)} - binds annotation that holds translation for a specific locale.</li>
   * <li>{@link #bindBundle(String)} - binds a resource bundle containing translated messages.</li>
   * <li>{@link #install(C10NConfigBase)} - includes configuration from another c10n configuration module</li>
   * <li>{@link #bind(Class)} - binds a custom class as an implementation for the given c10n interface</li>
   * <li>{@link #setLocaleProvider(LocaleProvider)} - customises the locale retrieval logic</li>
   * <li>{@link #setUntranslatedMessageHandler(UntranslatedMessageHandler)} - customises the placeholder for
   *      unresolved translation mappings</li>
   * </ul>
   * </p>
   */
  protected abstract void configure();

  /**
   * <p>Get the name of the package for which the current
   * module is responsible.</p>
   * <p/>
   * <p>The name of the package is used to determine
   * which c10n interfaces this configuration is
   * responsible for</p>
   * <p/>
   * <p>The default implementation, which returns
   * the string representation of the package of
   * the configuration class, should suffice in most
   * cases</p>
   *
   * @return name of package the current module is responsible for
   */
  protected String getConfigurationPackage() {
    Package pkg = getClass().getPackage();
    return pkg != null ? pkg.getName() : "";
  }

  void doConfigure() {
    if (!configured) {
      configure();
    }
    configured = true;
  }

  /**
   * <p>Install the given child c10n configuration module</p>
   * <p/>
   * <p>This will apply the configuration to all c10n interfaces
   * located in the child configuration package or below.</p>
   *
   * @param childConfig child c10n configuration to install (not-null)
   */
  protected void install(C10NConfigBase childConfig) {
    childConfig.doConfigure();
    childConfigs.add(childConfig);
  }

  /**
   * <p>Create a custom implementation binding for the given c10n interface</p>
   * <p/>
   * <p>There are two basic usages:
   * <pre><code>
   *   bind(Messages.class).to(JapaneseMessagesImpl.class, Locale.JAPANESE);
   * </code></pre>
   * <p/>
   * which will use the <code>JapaneseMessagesImpl.class</code> when locale is
   * set to <code>Locale.JAPANESE</code></p>
   * <p/>
   * <p>The second usage is:
   * <pre><code>
   *   bind(Messages.class).to(FallbackMessagesImpl.class);
   * </code></pre>
   * <p/>
   * which will use the <code>FallbackMessagesImpl.class</code> when no other
   * implementation class was matched for the current locale.</p>
   *
   * @param c10nInterface C10N interface to create an implementation binding for (not-null)
   * @param <T>           C10N interface type
   * @return implementation binding DSL object
   */
  protected <T> C10NImplementationBinder<T> bind(Class<T> c10nInterface) {
    C10NImplementationBinder<T> binder = new C10NImplementationBinder<T>();
    binders.put(c10nInterface, binder);
    return binder;
  }

  /**
   * <p>Sets the {@link LocaleProvider} for this configuration.</p>
   * <p/>
   * <p>Locale provider is consulted every time a translation is requested,
   * that is every time a method on an c10n interface is called.</p>
   * <p/>
   * <p>As a rule, there should be only one locale provider per application.
   * Any locale providers defined in child configurations (see {@link #install(C10NConfigBase)}
   * are disregarded.</p>
   * <p/>
   * <p>Because locale provider has to be consulted on every translation request
   * {@link c10n.LocaleProvider#getLocale()} should avoid any CPU intensive processing</p>
   * <p/>
   * <p>Default locale provider implementation always returns the same result as
   * {@link java.util.Locale#getDefault()}</p>
   *
   * @param localeProvider custom locale provider (not-null)
   */
  protected void setLocaleProvider(LocaleProvider localeProvider) {
    assertNotNull(localeProvider, "localeProvider");
    this.localeProvider = localeProvider;
  }

  /**
   * <p>Customise placeholder value for unresolved translations.</p>
   * <p/>
   * <p>The default behaviour is to return a string in format:
   * <pre>
   *   [InterfaceName].[MethodName]([ArgumentValues])
   * </pre>
   * </p>
   *
   * @param handler custom implementation of untranslated message handler (not-null)
   */
  protected void setUntranslatedMessageHandler(UntranslatedMessageHandler handler) {
    assertNotNull(handler, "handler");
    this.untranslatedMessageHandler = handler;
  }

  /**
   * <p>Create a method annotation binding to the specified locale</p>
   * <p/>
   * <p>There are two basic usages:
   * <pre><code>
   *   bindAnnotation(Ja.class).to(Locale.JAPANESE);
   * </code></pre>
   * <p/>
   * which will tell c10n to take the value given in the <code>@Ja</code>
   * annotation whenever the current locale is <code>Locale.JAPANESE</code></p>
   * <p/>
   * <p>The second usage is:
   * <pre><code>
   *   bindAnnotation(En.class);
   * </code></pre>
   * <p/>
   * which will make c10n always fallback to the value given in the <code>@En</code>
   * annotation if no other annotation binding matched the current locale.</p>
   * <p/>
   * <p>Note: Some default annotation bindings are defined in {@link c10n.annotations.DefaultC10NAnnotations}.
   * In order to use <code>install(new DefaultC10NAnnotations());</code> somewhere in your configuration
   * (see {@link #install(C10NConfigBase)}</p>
   *
   * @param annotationClass Class of the annotation to create a local binding for (not-null)
   * @return annotation locale binding DSL object
   */
  protected C10NAnnotationBinder bindAnnotation(Class<? extends Annotation> annotationClass) {
    assertNotNull(annotationClass, "annotationClass");
    checkAnnotationInterface(annotationClass);
    C10NAnnotationBinder binder = new C10NAnnotationBinder();
    annotationBinders.put(annotationClass, binder);
    return binder;
  }

  /**
   * <p>Create a filter binding for one or more argument types.</p>
   * <p>All arguments passed to c10n-interfaces with the specified type(s) will
   * be converted to string using the filter generated by the given filter provider,
   * instead of the conventional <code>toString()</code> method.</p>
   *
   * <p>Filter creation (using {@link c10n.C10NFilterProvider#get()} method) will be
   * deferred until the first call to a c10n-interface method with a matching
   * argument type is executed.</p>
   * @param c10NFilterProvider provider of filter implementation (not-null)
   * @param type method argument type to which the filter should be applied
   * @return filter binding DSL object
   * @see C10NFilterBinder
   */
  protected <T> C10NFilterBinder<T> bindFilter(C10NFilterProvider<T> c10NFilterProvider, Class<T> type) {
    assertNotNull(c10NFilterProvider, "c10nFilterProvider");
    assertNotNull(type, "type");
    C10NFilterBinder<T> filterBinder = new C10NFilterBinder<T>(c10NFilterProvider, type);
    filterBinders.add(filterBinder);
    return filterBinder;
  }

  /**
   * <p>Create a filter binding for one or more argument types.</p>
   * <p>All arguments passed to c10n-interfaces with the specified type(s) will
   * be converted to string using this filter, instead of the conventional <code>toString()</code>
   * method.</p>
   * @param c10nFilter filter implementation (not-null)
   * @param type method argument type to which the filter should be applied
   * @return filter binding DSL object
   * @see C10NFilterBinder
   */
  protected <T> C10NFilterBinder<T> bindFilter(C10NFilter<T> c10nFilter, Class<T> type) {
    assertNotNull(c10nFilter, "c10nFilter");
    assertNotNull(type, "type");
    C10NFilterBinder<T> filterBinder = new C10NFilterBinder<T>(C10NFilters.staticFilterProvider(c10nFilter), type);
    filterBinders.add(filterBinder);
    return filterBinder;
  }

  List<C10NFilterBinder<?>> getFilterBinders(){
    return filterBinders;
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
    return res;
  }

  /**
   * For each annotation bound in this configuration find all
   * locales it has been bound to.
   *
   * @return annotation -&gt; set of locale mapping
   */
  Map<Class<? extends Annotation>, Set<Locale>> getAnnotationToLocaleMapping() {
    Map<Class<? extends Annotation>, Set<Locale>> res = new HashMap<Class<? extends Annotation>, Set<Locale>>();
    for (Entry<Class<? extends Annotation>, C10NAnnotationBinder> entry : annotationBinders.entrySet()) {
      Set<Locale> locales = getLocales(entry.getKey(), res);
      locales.add(entry.getValue().getLocale());
    }
    return res;
  }

  private Set<Locale> getLocales(Class<? extends Annotation> key, Map<Class<? extends Annotation>, Set<Locale>> res) {
    Set<Locale> locales = res.get(key);
    if (null == locales) {
      locales = new HashSet<Locale>();
      res.put(key, locales);
    }
    return locales;
  }

  Class<?> getBindingForLocale(Class<?> c10nInterface, Locale locale) {
    C10NImplementationBinder<?> binder = binders.get(c10nInterface);
    if (null != binder) {
      Class<?> impl = binder.getBindingForLocale(locale);
      if (null != impl) {
        return impl;
      }
    }
    return null;
  }

  /**
   * List of all installed child configurations in
   * the order they were installed.
   *
   * @return List of child configurations
   */
  List<C10NConfigBase> getChildConfigs() {
    return childConfigs;
  }

  /**
   * Find all locales that have explicit implementation class
   * bindings for this c10n interface.
   *
   * @param c10nInterface interface to find bindings for (not-null)
   * @return Set of locales (not-null)
   */
  Set<Locale> getImplLocales(Class<?> c10nInterface) {
    Set<Locale> res = new HashSet<Locale>();
    C10NImplementationBinder<?> binder = binders.get(c10nInterface);
    if (binder != null) {
      res.addAll(binder.bindings.keySet());
    }
    return res;
  }

  /**
   * Get the current locale as stipulated by the locale provider
   *
   * @return current locale
   */
  Locale getCurrentLocale() {
    return localeProvider.getLocale();
  }

  String getUntranslatedMessageString(Class<?> c10nInterface, Method method, Object[] methodArgs) {
    return untranslatedMessageHandler.render(c10nInterface, method, methodArgs);
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

    public C10NImplementationBinder<T> to(Class<? extends T> to) {
      bindings.put(C10N.FALLBACK_LOCALE, to);
      return this;
    }

    Class<?> getBindingForLocale(Locale locale) {
      return bindings.get(locale);
    }
  }

  /**
   * <p>Filter binding DSL object.</p>
   * <p>Use {@link #annotatedWith(Class)} method to restrict the filter
   * to arguments annotated with the specified annotation(s). Multiple
   * annotations may be specified using chained {@link #annotatedWith(Class)} methods.</p>
   */
  protected static final class C10NFilterBinder<T> {
    private final C10NFilterProvider<T> filter;
    private Class<T> type;
    private final List<Class<? extends Annotation>> annotatedWith = new ArrayList<Class<? extends Annotation>>();

    private C10NFilterBinder(C10NFilterProvider<T> filter, Class<T> type) {
      this.filter = filter;
      this.type = type;
    }

    /**
     * <p>Restrict filter application only to argumets annotated with the
     * given annotation.</p>
     * <p>Multiple annotations can be specified using method chaining.</p>
     * @param annotation annotation class to restrict filter application to
     * @return this DLS object for method chaining
     */
    public C10NFilterBinder annotatedWith(Class<? extends Annotation> annotation){
      this.annotatedWith.add(annotation);
      return this;
    }

    C10NFilterProvider<T> getFilterProvider(){
      return filter;
    }

    Class<T> getType(){
      return type;
    }

    public List<Class<? extends Annotation>> getAnnotatedWith() {
      for(Class<? extends Annotation> a : annotatedWith){

      }
      return annotatedWith;

    }
  }
}
