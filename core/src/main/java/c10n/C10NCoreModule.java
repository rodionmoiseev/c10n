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

import c10n.share.ShareModule;

import java.util.Locale;

import static c10n.share.utils.Preconditions.assertNotNull;

/**
 * @author rodion
 */
public class C10NCoreModule {
  public static final LocaleProvider defaultLocaleProvider = new DefaultLocaleProvider();
  public static final UntranslatedMessageHandler defaultUnknownMessageHandler = new DefaultUntranslatedMessageHandler();
  //DI
  private final ShareModule shareModule = new ShareModule();

  public C10NMsgFactory defaultC10NMsgFactory() {
    return defaultC10NMsgFactory(new UnconfiguredC10NConfig());
  }

  public C10NMsgFactory defaultC10NMsgFactory(C10NConfigBase rootConfig) {
    rootConfig.doConfigure();
    return new DefaultC10NMsgFactory(
            new DefaultConfiguredC10NModule(rootConfig, new DefaultConfigChainResolver(rootConfig)),
            shareModule.defaultLocaleMapping(),
            C10N.getProxyClassloader());
  }

  /**
   * <p>Locale provider that always delegates to
   * {@link java.util.Locale#getDefault()}.</p>
   * <p>The value may be changed by calling {@link Locale#setDefault(java.util.Locale)}</p>
   *
   * @return current locale
   */
  public LocaleProvider defaultLocaleProvider() {
    return defaultLocaleProvider;
  }

  public UntranslatedMessageHandler defaultUnknownMessageHandler() {
    return defaultUnknownMessageHandler;
  }

  public ConfiguredC10NModule createDefaultConfiguredModule(C10NConfigBase parentConfig, ConfigChainResolver chainResolver) {
    assertNotNull(parentConfig, "parentConfig");
    assertNotNull(chainResolver, "chainResolver");
    return new DefaultConfiguredC10NModule(parentConfig, chainResolver);
  }

  /**
   * <p>Filter provider that always returns the specified instance</p>
   * @param filter filter instance to return from the generated provider(not-null)
   * @param <T> Filter argument type
   * @return instance of filter provider (never-null)
   */
  public <T> C10NFilterProvider<T> staticFilterProvider(C10NFilter<T> filter){
    assertNotNull(filter, "filter");
    return new StaticC10NFilterProvider<T>(filter);
  }

  /**
   * <p>Decorates the specified filter provider with a simple static cache.
   * Only the first call will result in an execution of {@link c10n.C10NFilterProvider#get()} method.
   * The following calls will always return a cached instance of the first call.</p>
   * @param filterProvider filter provider to decorate with caching (not-null)
   * @param <T> Filter argument type
   * @return instance of a filter provider decorated with simple static cache (never-null)
   */
  public <T> C10NFilterProvider<T> cachedFilterProvider(C10NFilterProvider<T> filterProvider){
    assertNotNull(filterProvider, "filterProvider");
    return new CachedC10NFilterProvider<T>(filterProvider);
  }

  private static final class DefaultLocaleProvider implements LocaleProvider {
    @Override
    public Locale getLocale() {
      return Locale.getDefault();
    }
  }

  private static final class UnconfiguredC10NConfig extends C10NConfigBase{
    @Override
    protected void configure() {
    }
  }

  private static final class StaticC10NFilterProvider<T> implements C10NFilterProvider<T>{
    private final C10NFilter<T> filter;

    private StaticC10NFilterProvider(C10NFilter<T> filter) {
      this.filter = filter;
    }

    @Override
    public C10NFilter<T> get() {
      return filter;
    }
  }

  private static final class CachedC10NFilterProvider<T> implements C10NFilterProvider<T>{
    private final C10NFilterProvider<T> base;
    private C10NFilter<T> thunk = null;

    private CachedC10NFilterProvider(C10NFilterProvider<T> base) {
      this.base = base;
    }

    @Override
    public C10NFilter<T> get() {
      if(null==thunk){
        thunk = base.get();
      }
      return thunk;
    }
  }
}
