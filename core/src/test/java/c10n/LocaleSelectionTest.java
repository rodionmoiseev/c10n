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

import c10n.test.utils.RuleUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 */
public class LocaleSelectionTest {
    @Rule
    public TestRule tmpLocale = RuleUtils.tmpLocale();
    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

    @Before
    public void setUp() throws Exception {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                bindAnnotation(RuPrecise.class).toLocale(new Locale("ru", "RU", "dialect"));
                bindAnnotation(RuFallback1.class).toLocale(new Locale("ru", "RU"));
                bindAnnotation(RuFallback2.class).toLocale(new Locale("ru"));
                bindAnnotation(Fallback.class);
            }
        });
    }

    @Test
    public void preciseMatchAndFallback() {
        Msg msg = C10N.get(Msg.class);
        Locale.setDefault(new Locale("ru", "RU", "dialect"));
        assertThat(msg.greet(), is("precise"));
        Locale.setDefault(new Locale("ru", "RU", "unknownDialect"));
        assertThat(msg.greet(), is("fallback1"));
        Locale.setDefault(new Locale("ru", "UZ"));
        assertThat(msg.greet(), is("fallback2"));
        Locale.setDefault(new Locale("en"));
        assertThat(msg.greet(), is("last resort"));
    }

    @C10NMessages
    interface Msg {
        @RuPrecise("precise")
        @RuFallback1("fallback1")
        @RuFallback2("fallback2")
        @Fallback("last resort")
        String greet();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RuPrecise {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RuFallback1 {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RuFallback2 {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Fallback {
        String value();
    }
}
