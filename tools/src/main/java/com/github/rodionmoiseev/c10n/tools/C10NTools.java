/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.rodionmoiseev.c10n.tools;

import com.github.rodionmoiseev.c10n.C10N;
import com.github.rodionmoiseev.c10n.ConfiguredC10NModule;
import com.github.rodionmoiseev.c10n.tools.inspector.C10NInspector;
import com.github.rodionmoiseev.c10n.tools.inspector.DummyInstanceProvider;
import com.github.rodionmoiseev.c10n.tools.inspector.InspectorModule;
import com.google.common.collect.Sets;

import java.util.Locale;
import java.util.Set;

import static com.github.rodionmoiseev.c10n.share.utils.Preconditions.assertNotNull;

/**
 * <p>C10N translation inspection tools.
 * <p>Typical inspector usage:
 *
 * <ol>
 * <li>Instanciate the inspector:
 * <pre>
 *  C10N.configure(new C10NConfigBase(){
 *      &#64;Override
 *      protected void configure(){
 *          //your configuration
 *      }
 *  };
 *
 *  //Instanciate the inspector for all locales referenced from
 *  //your configuration.
 *  C10NInspector inspector = C10NTools.inspectorBuilder().build();
 *     </pre>
 * </li>
 *
 * <li>
 * Invoke inspector with a list of package prefixes to scan.
 * Packages will be scanned recursively.
 * <pre>
 *  List&lt;C10NUnit&gt; units = inspector.inspect("com.example.messages");
 * </pre></li>
 *
 * <li>
 * Examine the returned list of c10n units, to determine if any translations
 * are missing, or do not comply with your rules. You can also examine
 * the actual translation values (see {@link com.github.rodionmoiseev.c10n.tools.inspector.C10NTranslations#getValue()}
 * obtained via {@link com.github.rodionmoiseev.c10n.tools.inspector.C10NUnit#getTranslations()}, with the caveat
 * that values for parameterised methods may not be available if paramer types are not one of
 * {@link String}, {@link CharSequence} or one of the primitive types.
 * </li>
 * </ol>
 *
 *
 * @author rodion
 * @since 1.1
 */
public final class C10NTools {

    /**
     * <p>Start a new inspector builder
     *
     * @return inspector builder instance.
     * @see C10NTools Typical usage
     */
    public static C10NInspectorBuilder inspectorBuilder() {
        return new C10NInspectorBuilder();
    }

    /**
     * <p>{@link C10NInspector} instance builder
     */
    public static final class C10NInspectorBuilder {
        private ConfiguredC10NModule configuredModule = C10N.getRootConfiguredModule();
        private Set<Locale> localesToCheck = null;
        private DummyInstanceProvider dummyInstanceProvider = InspectorModule.defaultDummyInstanceProvider();
        private boolean fetchTranslations = true;

        /**
         * <p>Specify the C10N module to inspect. Defaults to {@link com.github.rodionmoiseev.c10n.C10N#getRootConfiguredModule()}
         *
         * @param module c10n module to inspect (not null)
         * @return this builder instance
         */
        public C10NInspectorBuilder module(ConfiguredC10NModule module) {
            assertNotNull(module, "module");
            this.configuredModule = module;
            return this;
        }

        /**
         * <p>Specify the list of locales to check against
         *
         * @param locales a list of locales to check (not null)
         * @return this builder instance
         */
        public C10NInspectorBuilder checkLocales(Locale... locales) {
            assertNotNull(locales, "locales");
            this.localesToCheck = Sets.newHashSet(locales);
            return this;
        }

        /**
         * <p>Specify the provider for dummy instances for parameterised methods
         *
         * @param dummyInstanceProvider provider for dummy instances for parameterised methods (not null)
         * @return this builder instance
         */
        public C10NInspectorBuilder dummyInstanceProvider(DummyInstanceProvider dummyInstanceProvider) {
            assertNotNull(dummyInstanceProvider, "dummyInstanceProvider");
            this.dummyInstanceProvider = dummyInstanceProvider;
            return this;
        }

        /**
         * <p>Specify whether to fetch actual translation values for each
         * of the checked locales. If false, translated values will be set to <code>null</code>
         *
         * @param enable fetch translated values if <code>true</code>, else skip fetching.
         * @return this builder instance
         */
        public C10NInspectorBuilder fetchTranslations(boolean enable) {
            this.fetchTranslations = enable;
            return this;
        }

        /**
         * <p>Create inspector instance based on configured values.
         *
         * @return inspector instance (not null)
         */
        public C10NInspector build() {
            return InspectorModule.defaultInspector(dummyInstanceProvider,
                    configuredModule,
                    localesToCheck != null ? localesToCheck : configuredModule.getAllBoundLocales(),
                    fetchTranslations);
        }
    }
}
