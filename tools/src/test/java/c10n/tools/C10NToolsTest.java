/*
 *  Licensed to the Apache Software Foundation (ASF) under one
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
 *
 */

package c10n.tools;

import c10n.C10N;
import c10n.C10NConfigBase;
import c10n.C10NMessages;
import c10n.annotations.DefaultC10NAnnotations;
import c10n.annotations.En;
import c10n.annotations.Fr;
import c10n.annotations.Ja;
import c10n.test.utils.RuleUtils;
import c10n.tools.inspector.AbstractC10NInspectorTest;
import c10n.tools.inspector.C10NInspector;
import c10n.tools.inspector.C10NUnit;
import c10n.tools.inspector.DummyInstanceProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

/**
 * @author rodion
 * @since 1.1
 */
public class C10NToolsTest extends AbstractC10NInspectorTest {
    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

    /*
     * 1. checks that only english and japanese locales are checked for,
     *    and french locale is excluded
     * 2. checked that dummy instance provider is used instead of the default one
     */
    @Test
    public void builderHonorsCheckLocalesAndDummyInstanceProvider() {
        C10NInspector inspector = C10NTools.inspectorBuilder()
                .checkLocales(Locale.ENGLISH, Locale.JAPANESE)
                .dummyInstanceProvider(new MyDummyInstanceProvider())
                .module(C10N.configure(new DefaultC10NAnnotations()))
                .build();

        List<C10NUnit> units = inspector.inspect(getClass().getPackage().getName());
        checkUnit(units,
                C10NToolsTestMsg.class, method("greeting", String.class),
                anyKey(),
                annotationTr(Locale.ENGLISH, "[en]Hello, dummy!", "[en]Hello, {0}!"),
                annotationTr(Locale.JAPANESE, "[ja]Hello, dummy!", "[ja]Hello, {0}!"));
    }

    @Test
    public void builderHonorsFetchTranslations() {
        C10NInspector inspector = C10NTools.inspectorBuilder()
                .checkLocales(Locale.ENGLISH, Locale.FRENCH)
                .fetchTranslations(false)
                .module(C10N.configure(new DefaultC10NAnnotations()))
                .build();
        List<C10NUnit> units = inspector.inspect(getClass().getPackage().getName());
        checkUnit(units,
                C10NToolsTestMsg.class, method("greeting", String.class),
                anyKey(),
                annotationTr(Locale.ENGLISH, null),
                annotationTr(Locale.FRENCH, null));
    }

    /*
     * 1. Builder should use the last configuration set with C10N.configure
     * 2. Locales to check are set to those used in the config
     * 3. dummy instance provider is the default one
     * 4. translations are fetched by default
     */
    @Test
    public void builderUsesSensibleDefaultValues() {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                bindAnnotation(En.class).toLocale(Locale.ENGLISH);
                bindAnnotation(Ja.class).toLocale(Locale.JAPANESE);
            }
        });
        C10NInspector inspector = C10NTools.inspectorBuilder().build();
        List<C10NUnit> units = inspector.inspect(getClass().getPackage().getName());
        checkUnit(units,
                C10NToolsTestMsg.class, method("greeting", String.class),
                anyKey(),
                annotationTr(Locale.ENGLISH, "[en]Hello, {0}!", "[en]Hello, {0}!"),
                annotationTr(Locale.JAPANESE, "[ja]Hello, {0}!", "[ja]Hello, {0}!"));
    }

    private static class MyDummyInstanceProvider implements DummyInstanceProvider {
        @Override
        public Object getInstance(Class<?> c10nInterface, Method method, Class<?> paramType, int paramIndex) {
            return "dummy";
        }
    }

    @C10NMessages
    public interface C10NToolsTestMsg {
        @En("[en]Hello, {0}!")
        @Ja("[ja]Hello, {0}!")
        @Fr("[fr]Hello, {0}!")
        String greeting(String name);
    }
}
