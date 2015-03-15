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

package com.github.rodionmoiseev.c10n;

import com.github.rodionmoiseev.c10n.annotations.DefaultC10NAnnotations;
import com.github.rodionmoiseev.c10n.annotations.Fr;
import com.github.rodionmoiseev.c10n.test.utils.RuleUtils;
import com.google.common.collect.Sets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Locale;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 */
public class ConfiguredC10NModuleTest {
    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

    @Test
    public void annotationBoundLocalesAreListedUnderAllLocales() {
        ConfiguredC10NModule cm = C10N.configure(new DefaultC10NAnnotations());
        assertThat(cm.getAllBoundLocales(), is(set(
                Locale.ENGLISH,
                Locale.GERMAN,
                Locale.FRENCH,
                Locale.ITALIAN,
                Locale.JAPANESE,
                Locale.KOREAN,
                new Locale("ru"),
                Locale.CHINESE,
                new Locale("es")
        )));
    }

    @Test
    public void allBoundLocalesAreListedUnderAllLocales() {
        ConfiguredC10NModule cm = C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                bind(Msg.class)
                        .to(MsgJa.class, Locale.JAPANESE)
                        .to(MsgGr.class, Locale.GERMAN);
                bind(Msg2.class)
                        .to(Msg2Ru.class, new Locale("ru"));
                bindAnnotation(Fr.class).toLocale(Locale.FRENCH);
            }
        });
        assertThat(cm.getAllBoundLocales(), is(set(
                Locale.JAPANESE,
                Locale.GERMAN,
                new Locale("ru"),
                Locale.FRENCH)));
    }

    private static Set<Locale> set(Locale... locales) {
        return Sets.newHashSet(locales);
    }

    interface Msg {
    }

    static class MsgJa implements Msg {
    }

    static class MsgGr implements Msg {
    }

    interface Msg2 {
    }

    static class Msg2Ru implements Msg2 {
    }
}
