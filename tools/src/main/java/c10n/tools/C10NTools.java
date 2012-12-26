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

package c10n.tools;

import c10n.ConfiguredC10NModule;
import c10n.tools.inspector.C10NInspector;
import c10n.tools.inspector.InspectorModule;
import com.google.common.collect.Sets;

import java.util.Locale;
import java.util.Set;

/**
 * <p>C10N translation inspection tools.</p>
 * <p>Typical inspector usage:
 * <p/>
 * <ol>
 * <li>Instanciate the inspector:
 * <pre>
 *  ConfiguredC10NModule module = C10N.configure(new C10NConfigBase(){
 *      &#64;Override
 *      protected void configure(){
 *          //your configuration
 *      }
 *  };
 *
 *  //Instanciate the inspector with a list of locales you wish
 *  //to verify against.
 *  C10NInspector inspector = C10NTools.inspector(module,
 *      Locale.ENGLISH,
 *      Locale.FRENCH,
 *      Locale.GERMAN);
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
 *     Examine the returned list of c10n units, to determine if any translations
 *     are missing, or do not comply with your rules. You can also examine
 *     the actual translation values (see {@link c10n.tools.inspector.C10NTranslations#getValue()}
 *     obtained via {@link c10n.tools.inspector.C10NUnit#getTranslations()}, with the caveat
 *     that values for parameterised methods may not be available if paramer types are not one of
 *     {@link String}, {@link CharSequence} or one of the primitive types.
 * </li>
 * </ol>
 * </p>
 *
 * @author rodion
 * @since 1.1
 */
public final class C10NTools {
    /**
     * <p>Creates a new c10n translation inspector</p>
     *
     * @param configuredC10NModule c10n module to inspect (not null)
     * @param localesToCheck       a list of locales to check (not null)
     * @return c10n translation inspector implementation (not null)
     */
    public static C10NInspector inspector(ConfiguredC10NModule configuredC10NModule,
                                          Locale... localesToCheck) {
        return inspector(configuredC10NModule, Sets.newHashSet(localesToCheck));
    }

    /**
     * <p>Creates a new c10n translation inspector</p>
     *
     * @param configuredC10NModule c10n module to inspect (not null)
     * @param localesToCheck       a list of locales to check (not null)
     * @return c10n translation inspector implementation (not null)
     */
    public static C10NInspector inspector(ConfiguredC10NModule configuredC10NModule,
                                          Set<Locale> localesToCheck) {
        return InspectorModule.defaultInspector(configuredC10NModule, localesToCheck);
    }
}
