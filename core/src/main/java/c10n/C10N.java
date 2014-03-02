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

import java.util.Locale;

/**
 * <p>
 * Utility class for generating implementations of c10n message interfaces
 * annotated with &#64;{@link C10NMessages}.
 * </p>
 * <p/>
 * <p>
 * Usage similar to logger creating in frameworks like log4j or slf4j.
 * </p>
 * <p/>
 * <p>
 * Sample usage:
 * <p/>
 * <pre>
 *   -- MyMessages.java
 *     import c10n.C10NMessages;
 *
 *     &#64;C10NMessages
 *     public interface MyMessages {
 *
 *       &#64;C10NDef("Hello, {0}")
 *       String title(String who);
 *
 *       &#64;C10NDef("OK")
 *       String ok();
 *
 *       &#64;C10NDef("Cancel")
 *       String cancel();
 *     }
 *
 *   -- MyApplication.java
 *     import c10n.C10N;
 *     import javax.swing.*;
 *
 *     class MyApplication {
 *       private static final MyMessages msg = C10N.get(MyMessages.class);
 *
 *       public static void main(String[] args){
 *         JFrame frame = new JFrame(msg.title("World"));
 *         JButton okButton = new JButton(msg.ok());
 *         JButton cancelButton = new JButton(msg.cancel());
 *         ...
 *       }
 *     }
 * </pre>
 * <p/>
 * Implementation of <code>MyMessages</code> class is generated and will return
 * messages from configured bundle (TODO configuration).
 * </p>
 *
 * @author rodion
 */
public final class C10N {
    /**
     * <p>Classloader to use for loading c10n-interface proxies</p>
     */
    private static ClassLoader proxyClassloader = C10N.class.getClassLoader();

    //DI
    private static final C10NCoreModule coreModule = new C10NCoreModule();
    private static ConfiguredC10NModule rootConfiguredModule = coreModule.resolve(coreModule.defaultConfig());
    private static C10NMsgFactory root = coreModule.defaultC10NMsgFactory(rootConfiguredModule);

    /**
     * Internal locale object used as a fallback when current locale does not
     * match any of the user-defined locale mappings.
     */
    public static final Locale FALLBACK_LOCALE = Locale.ROOT;

    public static ConfiguredC10NModule getRootConfiguredModule() {
        return rootConfiguredModule;
    }

    @SuppressWarnings("unused")
    public static C10NMsgFactory getRootFactory() {
        return root;
    }

    public static void setRootFactory(C10NMsgFactory newRoot) {
        root = newRoot;
    }

    public static <T> T get(Class<T> c10nInterface) {
        return root.get(c10nInterface);
    }

    public static <T> T get(Class<T> c10nInterface, Locale locale) {
        return root.get(c10nInterface, locale);
    }

    public static ConfiguredC10NModule configure(C10NConfigBase conf) {
        rootConfiguredModule = coreModule.resolve(conf);
        root = coreModule.defaultC10NMsgFactory(rootConfiguredModule);
        return rootConfiguredModule;
    }

    /**
     * <p>Creates a message factory from a configured c10n configuration object.</p>
     * <p>This method is mainly intended for internal use.</p>
     *
     * @param configuredModule pre-configured c10n configuration
     * @return message factory for the given configuration
     * @deprecated Use {@link #createMsgFactory(C10NConfigBase)} instead
     */
    @Deprecated
    public static C10NMsgFactory createMsgFactory(ConfiguredC10NModule configuredModule) {
        return coreModule.defaultC10NMsgFactory(configuredModule);
    }

    /**
     * <p>Creates and configures a message factory for the given configuration.</p>
     * <p>This method does not modify any of the static state variables and is therefore
     * safe to be used when multple instances of c10n message factories need to be created within
     * one JVM.</p>
     *
     * @param conf c10n configuration
     * @return message factory for the given configuration
     */
    public static C10NMsgFactory createMsgFactory(C10NConfigBase conf) {
        return coreModule.defaultC10NMsgFactory(coreModule.resolve(conf));
    }

    /**
     * <p>Overrides the classloader used for loading c10n-interface proxies.
     * This maybe useful in the context of hot-swapping enabled classloaders, like
     * the one for Play framework 2.0</p>
     * <p/>
     * <p>Overriding should be done before any calls to {@link C10N#get(Class)}, to be effective.</p>
     * <p/>
     * <p>By default, the classloader of {@link c10n.C10N#getClass()} class is used.</p>
     *
     * @param classloader the classloader to use for loading c10n-interface proxies (not-null)
     */
    @SuppressWarnings("UnusedDeclaration")//rationale: public API
    public static void setProxyClassloader(ClassLoader classloader) {
        if (null == classloader) {
            throw new IllegalArgumentException("classloader is null");
        }
        proxyClassloader = classloader;
    }

    /**
     * Get the instance of the classloader currently used for loading c10n-interface proxies.
     *
     * @return classloader instance (not-null)
     */
    public static ClassLoader getProxyClassloader() {
        return proxyClassloader;
    }
}
