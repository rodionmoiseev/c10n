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

import c10n.annotations.DefaultC10NAnnotations;
import c10n.annotations.En;
import c10n.annotations.Ja;
import c10n.test.utils.RuleUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DelegationTest {
    @Rule
    public TestRule tmpLocale = RuleUtils.tmpLocale();
    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

    @Test
    public void generatedClassesCanBeDelegated() {
        Window msg = C10N.get(Window.class);
        assertThat(msg.title(), is("MyApp"));
        assertThat(msg.buttons().ok(), is("OK"));
    }

    @Test
    public void delegationCanBeSelfReferencing() {
        Window msg = C10N.get(Window.class);
        assertThat(msg.buttons().parent().buttons().parent().title(), is("MyApp"));
        assertThat(msg.buttons().parent().buttons().parent().buttons().ok(), is("OK"));

    }

    @Test
    public void delegateToStringIsEvaluatedToDefaultUntranslatedMessageOfTheDelegatingMethod() {
        assertThat(C10N.get(Window.class).buttons().toString(), is("Window.buttons"));
    }

    @Test
    public void delegateToStringDefaultsToDelegatingMethodAnnotationValueWhenPresent() {
        assertThat(C10N.get(Window.class).buttonsWithDef().toString(), is("buttons-def"));
    }

    @Test
    public void delegateToStringBehavesLikeANormalStringMethod() {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
            }
        });
        Window msg = C10N.get(Window.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.buttonsInternationalised().toString(), is("buttons-en"));
        Locale.setDefault(Locale.JAPANESE);
        assertThat(msg.buttonsInternationalised().toString(), is("buttons-ja"));
    }

    @C10NMessages
    interface Buttons {
        @C10NDef("OK")
        String ok();

        Window parent();
    }

    @C10NMessages
    interface Window {
        Buttons buttons();

        @C10NDef("buttons-def")
        Buttons buttonsWithDef();

        @En("buttons-en")
        @Ja("buttons-ja")
        Buttons buttonsInternationalised();

        @C10NDef("MyApp")
        String title();
    }
}
