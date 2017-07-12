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

package com.github.rodionmoiseev.c10n;

import com.github.rodionmoiseev.c10n.formatters.MessageFormatter;
import com.github.rodionmoiseev.c10n.plugin.C10NPlugin;
import com.github.rodionmoiseev.c10n.share.EncodedResourceControl;
import com.github.rodionmoiseev.c10n.share.utils.Preconditions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("WeakerAccess")//rationale: designed for 3rd party usage
public abstract class C10NConfigBase {
    //DI
    private final C10NCoreModule coreModule = new C10NCoreModule();
    private LocaleProvider localeProvider = coreModule.defaultLocaleProvider();
    private ClassLoader proxyClassLoader = C10N.class.getClassLoader();
    private UntranslatedMessageHandler untranslatedMessageHandler = coreModule.defaultUnknownMessageHandler();
    private final Map<String, C10NBundleBinderEntry> bundleBinders = new HashMap<String, C10NBundleBinderEntry>();
    private final Map<Class<?>, C10NImplementationBinder<?>> binders = new HashMap<Class<?>, C10NImplementationBinder<?>>();
    private final Map<Class<? extends Annotation>, C10NAnnotationBinder> annotationBinders = new HashMap<Class<? extends Annotation>, C10NAnnotationBinder>();
    private final List<C10NFilterBinder<?>> filterBinders = new ArrayList<C10NFilterBinder<?>>();
    private final List<C10NConfigBase> childConfigs = new ArrayList<C10NConfigBase>();
    private final List<C10NPlugin> plugins = new ArrayList<C10NPlugin>();
    private String keyPrefix = "";

    private boolean debug = false;

    private boolean configured = false;
    private MessageFormatter formatter = new DefaultMessageFormatter();

    /**
     * <p>To be implemented by subclasses of {@link C10NConfigBase}.
     *
     * <p>Configuration methods are as follows:
     * <ul>
     * <li>{@link #bindAnnotation(Class)} - binds annotation that holds translation for a specific locale.</li>
     * <li>{@link #bindBundle(String)} - binds a resource bundle containing translated messages.</li>
     * <li>{@link #install(C10NConfigBase)} - includes configuration from another c10n configuration module</li>
     * <li>{@link #bind(Class)} - binds a custom class as an implementation for the given c10n interface</li>
     * <li>{@link #setLocaleProvider(LocaleProvider)} - customises the locale retrieval logic</li>
     * <li>{@link #setUntranslatedMessageHandler(UntranslatedMessageHandler)} - customises the placeholder for
     * unresolved translation mappings</li>
     * <li>{@link #setKeyPrefix(String)} - sets global key prefix to auto-prepend to all bundle keys</li>
     * </ul>
     */
    protected abstract void configure();

    /**
     * <p>Get the name of the package for which the current
     * module is responsible.
     *
     * <p>The name of the package is used to determine
     * which c10n interfaces this configuration is
     * responsible for
     *
     * <p>The default implementation, which returns
     * the string representation of the package of
     * the configuration class, should suffice in most
     * cases
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
     * Registers a C10N extension plugin to this configuration module.
     * @param plugin plugin to install (not-null)
     */
    protected void installPlugin(C10NPlugin plugin){
        plugins.add(plugin);
    }

    /**
     * Returns the current list of plugins.
     *
     * @return list of registered plugins (not-null)
     */
    protected List<C10NPlugin> getPlugins(){
        return plugins;
    }

    /**
     * <p>Install the given child c10n configuration module
     *
     * <p>This will apply the configuration to all c10n interfaces
     * located in the child configuration package or below.
     *
     * @param childConfig child c10n configuration to install (not-null)
     */
    protected void install(C10NConfigBase childConfig) {
        childConfig.doConfigure();
        childConfigs.add(childConfig);
    }

    /**
     * <p>Create a custom implementation binding for the given c10n interface
     *
     * <p>There are two basic usages:
     * <pre><code>
     *   bind(Messages.class).to(JapaneseMessagesImpl.class, Locale.JAPANESE);
     * </code></pre>
     *
     * which will use the <code>JapaneseMessagesImpl.class</code> when locale is
     * set to <code>Locale.JAPANESE</code>
     *
     * <p>The second usage is:
     * <pre><code>
     *   bind(Messages.class).to(FallbackMessagesImpl.class);
     * </code></pre>
     *
     * which will use the <code>FallbackMessagesImpl.class</code> when no other
     * implementation class was matched for the current locale.
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
     * <p>Sets the {@link LocaleProvider} for this configuration.
     *
     * <p>Locale provider is consulted every time a translation is requested,
     * that is every time a method on an c10n interface is called.
     *
     * <p>As a rule, there should be only one locale provider per application.
     * Any locale providers defined in child configurations (see {@link #install(C10NConfigBase)}
     * are disregarded.
     *
     * <p>Because locale provider has to be consulted on every translation request
     * {@link com.github.rodionmoiseev.c10n.LocaleProvider#getLocale()} should avoid any CPU intensive processing
     *
     * <p>Default locale provider implementation always returns the same result as
     * {@link java.util.Locale#getDefault()}
     *
     * @param localeProvider custom locale provider (not-null)
     */
    protected void setLocaleProvider(LocaleProvider localeProvider) {
        Preconditions.assertNotNull(localeProvider, "localeProvider");
        this.localeProvider = localeProvider;
    }

    /**
     * <p>Fixes the {@link java.util.Locale} to the specified locale.
     *
     * <p>Generally useful when your application needs to create separate
     * {@link C10NMsgFactory} instances for each locale.
     *
     * @param locale Locale to use
     */
    protected void setLocale(Locale locale) {
        this.localeProvider = LocaleProviders.fixed(locale);
    }

    /**
     * <p>Customise placeholder value for unresolved translations.
     *
     * <p>The default behaviour is to return a string in format:
     * <pre>
     *   [InterfaceName].[MethodName]([ArgumentValues])
     * </pre>
     *
     * @param handler custom implementation of untranslated message handler (not-null)
     */
    protected void setUntranslatedMessageHandler(UntranslatedMessageHandler handler) {
        Preconditions.assertNotNull(handler, "handler");
        this.untranslatedMessageHandler = handler;
    }

    /**
     * <p>Overrides the classloader used for loading c10n-interface proxies.
     * This maybe useful in the context of hot-swapping enabled classloaders, like
     * the one for Play framework 2.0, or OSGi.
     *
     * <p>By default, the classloader of {@link com.github.rodionmoiseev.c10n.C10N#getClass()} class is used.
     *
     * @param proxyClassLoader the classloader to use for loading c10n-interface proxies (not-null)
     */
    protected void setProxyClassLoader(ClassLoader proxyClassLoader) {
        Preconditions.assertNotNull(proxyClassLoader, "proxyClassLoader");
        this.proxyClassLoader = proxyClassLoader;
    }

    /**
     * <p>The c10n intefrace proxy classloader that will be used
     * by the current instance of c10n message factory.</p>
     *
     * @return the classloader to be used for loading c10n-interface proxies (not-null)
     */
    protected ClassLoader getProxyClassLoader() {
        return proxyClassLoader;
    }

    /**
     * <p>Create a method annotation binding to the specified locale
     *
     * <p>There are two basic usages:
     * <pre><code>
     *   bindAnnotation(Ja.class).to(Locale.JAPANESE);
     * </code></pre>
     *
     * which will tell c10n to take the value given in the <code>@Ja</code>
     * annotation whenever the current locale is <code>Locale.JAPANESE</code>
     *
     * <p>The second usage is:
     * <pre><code>
     *   bindAnnotation(En.class);
     * </code></pre>
     *
     * which will make c10n always fallback to the value given in the <code>@En</code>
     * annotation if no other annotation binding matched the current locale.
     *
     * <p>Note: Some default annotation bindings are defined in {@link com.github.rodionmoiseev.c10n.annotations.DefaultC10NAnnotations}.
     * In order to use <code>install(new DefaultC10NAnnotations());</code> somewhere in your configuration
     * (see {@link #install(C10NConfigBase)}
     *
     * @param annotationClass Class of the annotation to create a local binding for (not-null)
     * @return annotation locale binding DSL object
     */
    protected C10NAnnotationBinder bindAnnotation(Class<? extends Annotation> annotationClass) {
        Preconditions.assertNotNull(annotationClass, "annotationClass");
        checkAnnotationInterface(annotationClass);
        C10NAnnotationBinder binder = new C10NAnnotationBinder();
        annotationBinders.put(annotationClass, binder);
        return binder;
    }

    /**
     * <p>Create a filter binding for one or more argument types.
     * <p>All arguments passed to c10n-interfaces with the specified type(s) will
     * be converted to string using the filter generated by the given filter provider,
     * instead of the conventional <code>toString()</code> method.
     *
     * <p>Filter creation (using {@link com.github.rodionmoiseev.c10n.C10NFilterProvider#get()} method) will be
     * deferred until the first call to a c10n-interface method with a matching
     * argument type is executed.
     *
     * @param c10NFilterProvider provider of filter implementation (not-null)
     * @param type               method argument type to which the filter should be applied
     * @param <T>                method argument type to which the filter should be applied
     * @return filter binding DSL object
     * @see C10NFilterBinder
     */
    protected <T> C10NFilterBinder<T> bindFilter(C10NFilterProvider<T> c10NFilterProvider, Class<T> type) {
        Preconditions.assertNotNull(c10NFilterProvider, "c10nFilterProvider");
        Preconditions.assertNotNull(type, "type");
        C10NFilterBinder<T> filterBinder = new C10NFilterBinder<T>(c10NFilterProvider, type);
        filterBinders.add(filterBinder);
        return filterBinder;
    }

    /**
     * <p>Create a filter binding for one or more argument types.
     * <p>All arguments passed to c10n-interfaces with the specified type(s) will
     * be converted to string using this filter, instead of the conventional <code>toString()</code>
     * method.
     *
     * @param c10nFilter filter implementation (not-null)
     * @param type       method argument type to which the filter should be applied
     * @param <T>        method argument type to which the filter should be applied
     * @return filter binding DSL object
     * @see C10NFilterBinder
     */
    protected <T> C10NFilterBinder<T> bindFilter(C10NFilter<T> c10nFilter, Class<T> type) {
        Preconditions.assertNotNull(c10nFilter, "c10nFilter");
        Preconditions.assertNotNull(type, "type");
        C10NFilterBinder<T> filterBinder = new C10NFilterBinder<T>(C10NFilters.staticFilterProvider(c10nFilter), type);
        filterBinders.add(filterBinder);
        return filterBinder;
    }

    /**
     * <p>Set global key prefix. All other keys will be automatically prepended with the global key.
     * <p>Settings key prefix to an empty string resets to default behaviour (no prefix).
     *
     * @param key the key to use at configuration scope (not null)
     */
    protected void setKeyPrefix(String key) {
        Preconditions.assertNotNull(key, "key");
        keyPrefix = key;
    }

    String getKeyPrefix() {
        return keyPrefix;
    }

    /**
     * <p>If set to 'true', c10n will output debugging information to std-out at configuration and lookup time.
     *
     * @param debug debug flag
     */
    protected void setDebug(boolean debug) {
        this.debug = debug;
    }

    boolean isDebug() {
        return debug;
    }

    protected void setMessageFormatter(MessageFormatter formatter){
        this.formatter = formatter;
    }

    public MessageFormatter getMessageFormatter(){
        return formatter;
    }

    List<C10NFilterBinder<?>> getFilterBinders() {
        return filterBinders;
    }

    private void checkAnnotationInterface(Class<? extends Annotation> annotationClass) {
        if (noMethod(annotationClass, "value") &&
                noMethod(annotationClass, "intRes") &&
                noMethod(annotationClass, "extRes"))
            throw new C10NConfigException("Annotation could not be bound because it's missing any of value()," +
                    " intRes() or extRes() methods that return String. " +
                    "Please add at least one of those methods with return type of String. " +
                    "annotationClass=" + annotationClass.getName());
    }

    private boolean noMethod(Class<? extends Annotation> annotationClass, String methodName) {
        Method valueMethod;
        try {
            valueMethod = annotationClass.getMethod(methodName);
            if (!valueMethod.getReturnType().isAssignableFrom(String.class)) {
                return true;
            }
        } catch (NoSuchMethodException e) {
            return true;
        }
        return false;
    }

    protected C10NBundleBinder bindBundle(String baseName, String charsetName) {
        C10NBundleBinderEntry entry = new C10NBundleBinderEntry(baseName, charsetName, new C10NBundleBinder());
        bundleBinders.put(entry.getBaseName(), entry);
        return entry.getBinder();
    }

    protected C10NBundleBinder bindBundle(String baseName) {
        return bindBundle(baseName, "UTF-8");
    }

    List<ResourceBundle> getBundlesForLocale(Class<?> c10nInterface, Locale locale) {
        List<ResourceBundle> res = new ArrayList<ResourceBundle>();
        for (C10NBundleBinderEntry entry : bundleBinders.values()) {
            if (entry.getBinder().getBoundInterfaces().isEmpty()
                    || entry.getBinder().getBoundInterfaces().contains(c10nInterface)) {
                res.add(ResourceBundle.getBundle(entry.getBaseName(), locale,
                        new EncodedResourceControl(entry.getCharsetName())));
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
     * <p>Get a set of all locales explicitly declared in implementation bindings
     *
     * @return set of all bound locales
     */
    Set<Locale> getAllImplementationBoundLocales() {
        Set<Locale> res = new HashSet<Locale>();
        for (C10NImplementationBinder<?> binder : binders.values()) {
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

    protected static class C10NBundleBinderEntry {
        private final String baseName;
        private final String charsetName;
        private final C10NBundleBinder binder;

        public C10NBundleBinderEntry(String baseName, String charsetName, C10NBundleBinder binder) {
            this.baseName = baseName;
            this.charsetName = charsetName;
            this.binder = binder;
        }

        public String getBaseName() {
            return baseName;
        }

        public String getCharsetName() {
            return charsetName;
        }

        public C10NBundleBinder getBinder() {
            return binder;
        }
    }

    protected static class C10NAnnotationBinder {
        private Locale locale = C10N.FALLBACK_LOCALE;

        public void toLocale(Locale locale) {
            Preconditions.assertNotNull(locale, "locale");
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
     * <p>Filter binding DSL object.
     * <p>Use {@link #annotatedWith(Class)} method to restrict the filter
     * to arguments annotated with the specified annotation(s). Multiple
     * annotations may be specified using chained {@link #annotatedWith(Class)} methods.
     */
    protected static final class C10NFilterBinder<T> {
        private final C10NFilterProvider<T> filter;
        private final Class<T> type;
        private final List<Class<? extends Annotation>> annotatedWith = new ArrayList<Class<? extends Annotation>>();

        private C10NFilterBinder(C10NFilterProvider<T> filter, Class<T> type) {
            this.filter = filter;
            this.type = type;
        }

        /**
         * <p>Restrict filter application only to arguments annotated with the
         * given annotation.
         * <p>Multiple annotations can be specified using method chaining.
         *
         * @param annotation annotation class to restrict filter application to
         * @return this DLS object for method chaining
         */
        public C10NFilterBinder<T> annotatedWith(Class<? extends Annotation> annotation) {
            this.annotatedWith.add(annotation);
            return this;
        }

        C10NFilterProvider<T> getFilterProvider() {
            return filter;
        }

        Class<T> getType() {
            return type;
        }

        public List<Class<? extends Annotation>> getAnnotatedWith() {
            for (Class<? extends Annotation> a : annotatedWith) {

            }
            return annotatedWith;

        }
    }
}
