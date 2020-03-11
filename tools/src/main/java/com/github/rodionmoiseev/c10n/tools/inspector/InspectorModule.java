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

package com.github.rodionmoiseev.c10n.tools.inspector;

import com.github.rodionmoiseev.c10n.ConfiguredC10NModule;
import com.github.rodionmoiseev.c10n.tools.search.SearchModule;

import java.util.Locale;
import java.util.Set;

/**
 * @author rodion
 */
public class InspectorModule {
    public static DummyInstanceProvider defaultDummyInstanceProvider() {
        return new DefaultDummyInstanceProvider();
    }

    public static C10NInspector defaultInspector(ConfiguredC10NModule configuredC10NModule,
                                                 Set<Locale> localesToCheck) {
        return defaultInspector(defaultDummyInstanceProvider(), configuredC10NModule, localesToCheck, true);
    }

    public static C10NInspector defaultInspector(DummyInstanceProvider dummyInstanceProvider,
                                                 ConfiguredC10NModule configuredC10NModule,
                                                 Set<Locale> localesToCheck,
                                                 boolean fetchTranslations) {
        return new DefaultC10NInspector(SearchModule.reflectionsSearch(),
                configuredC10NModule,
                dummyInstanceProvider,
                localesToCheck,
                fetchTranslations);
    }
}
