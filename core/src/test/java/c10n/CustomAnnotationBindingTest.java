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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CustomAnnotationBindingTest {
    @Rule
    public static TestRule tmpLocale = RuleUtils.tmpLocale();
    @Rule
    public static TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

    @Before
    public void fixture() {
        C10N.configure(new C10NConfigBase() {
            @Override
            public void configure() {
                bindAnnotation(Def.class);
                bindAnnotation(Eng.class).toLocale(Locale.ENGLISH);
                bindAnnotation(Jp.class).toLocale(Locale.JAPANESE);
            }
        });
    }

    @Test
    public void messagesAreFetchedBasedOnBoundLocale() {
        Locale.setDefault(Locale.ENGLISH);
        Labels msg = C10N.get(Labels.class);
        assertThat(msg.label(), is(equalTo("English")));

        Locale.setDefault(Locale.JAPANESE);
        assertThat(msg.label(), is(equalTo("Japanese")));
    }

    @Test
    public void unboundAnnotationBehavesAsAFallback() {
        Labels msg = C10N.get(Labels.class);
        Locale.setDefault(new Locale("unbound"));
        assertThat(msg.label(), is(equalTo("Default")));
        assertThat(msg.label2("def"), is(equalTo("Default def")));
    }

    @Test
    public void annotationBoundMessagesCanBeParameterized() {
        Locale.setDefault(Locale.ENGLISH);
        Labels msg = C10N.get(Labels.class);
        assertThat(msg.label2("arg"), is(equalTo("English arg")));
        assertThat(msg.books(0), is("There are no books."));
        assertThat(msg.books(3), is("There are 3 books."));

        Locale.setDefault(Locale.JAPANESE);
        assertThat(msg.label2("hikisuu"), is(equalTo("Japanese hikisuu")));
        assertThat(msg.books(0), is("本がありません。"));
        assertThat(msg.books(3), is("本が3本あります。"));

        Locale.setDefault(new Locale("unbound"));
        assertThat(msg.label2("def"), is(equalTo("Default def")));
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Eng {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Jp {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Def {
        String value();
    }

    interface Labels {
        @Eng("English")
        @Jp("Japanese")
        @Def("Default")
        String label();

        @Eng("English {0}")
        @Jp("Japanese {0}")
        @Def("Default {0}")
        String label2(String arg);

        @Eng("There are {0,choice,0#no books|0<{0} books}.")
        @Jp("本が{0,choice,0#ありません|0<{0}本あります}。")
        String books(int n);
    }
}