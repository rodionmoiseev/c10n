/*
 * Copyright 2012 Rodion Moiseev (https://github.com/rodionmoiseev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rodionmoiseev.c10n;

import com.github.rodionmoiseev.c10n.share.ShareModule;

import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author rodion
 */
public class C10NCoreModule {
    private static final LocaleProvider defaultLocaleProvider = new DefaultLocaleProvider();
    private static final UntranslatedMessageHandler defaultUnknownMessageHandler = new DefaultUntranslatedMessageHandler();
    //DI
    private final ShareModule shareModule = new ShareModule();

    public C10NConfigBase defaultConfig() {
        return new UnconfiguredC10NConfig();
    }

    public ConfiguredC10NModule resolve(C10NConfigBase rootConfig) {
        rootConfig.doConfigure();
        return new DefaultConfiguredC10NModule(rootConfig, new DefaultConfigChainResolver(rootConfig));
    }

    public C10NMsgFactory defaultC10NMsgFactory(ConfiguredC10NModule configuredModule) {
        return new DefaultC10NMsgFactory(configuredModule,
                shareModule.defaultLocaleMapping());
    }

    /**
     * <p>Locale provider that always delegates to
     * {@link java.util.Locale#getDefault()}.
     * <p>The value may be changed by calling {@link Locale#setDefault(java.util.Locale)}
     *
     * @return current locale
     */
    public LocaleProvider defaultLocaleProvider() {
        return defaultLocaleProvider;
    }

    public UntranslatedMessageHandler defaultUnknownMessageHandler() {
        return defaultUnknownMessageHandler;
    }

    private static final class DefaultLocaleProvider implements LocaleProvider {
        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }
    }

    private static final class UnconfiguredC10NConfig extends C10NConfigBase {
        @Override
        protected void configure() {
        }

        @Override
        Map<Class<? extends Annotation>, Set<Locale>> getAnnotationToLocaleMapping() {
            throw new C10NException("Your c10n instance has not yet been configured. " +
                    "Please make sure C10N.configure() method is called prior to " +
                    "performing a C10N.get().");
        }
    }
}
