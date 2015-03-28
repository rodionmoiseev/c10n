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
import com.github.rodionmoiseev.c10n.annotations.En;
import com.github.rodionmoiseev.c10n.annotations.Ja;
import com.github.rodionmoiseev.c10n.test.utils.RuleUtils;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class C10NMessageTest {
    @Rule
    public TestRule tmpC10NConfig = RuleUtils.tmpC10NConfiguration(new C10NConfigBase() {
        @Override
        protected void configure() {
            install(new DefaultC10NAnnotations());
        }
    });
    @Rule
    public TestRule tmpLocale = RuleUtils.tmpLocale(Locale.ENGLISH);

    @Test
    public void messagesForEachLocaleAreContained() throws Exception {
        MyMsg myMsg = C10N.get(MyMsg.class);
        assertThat(myMsg.myMsg().get(), is("english"));
        assertThat(myMsg.myMsg().get(Locale.ENGLISH), is("english"));
        assertThat(myMsg.myMsg().get(Locale.JAPANESE), is("japanese"));
        assertThat(myMsg.myMsg().get(Locale.FRENCH), nullValue());

        assertThat(myMsg.myMsgWithArg(123).get(), is("english 123"));
        assertThat(myMsg.myMsgWithArg(123).get(Locale.ENGLISH), is("english 123"));
        assertThat(myMsg.myMsgWithArg(123).get(Locale.JAPANESE), is("japanese 123"));
        assertThat(myMsg.myMsgWithArg(123).get(Locale.FRENCH), nullValue());
    }

    @Test
    public void defaultMessageFallsBackWhenNoMatchingLocaleIsPresent() throws Exception {
        MyMsg myMsg = C10N.get(MyMsg.class);
        Locale.setDefault(Locale.CHINESE);
        assertThat(myMsg.myMsg().get(), is("fallback"));
        assertThat(myMsg.myMsg().get(Locale.ENGLISH), is("english"));
        assertThat(myMsg.myMsg().get(Locale.JAPANESE), is("japanese"));
        assertThat(myMsg.myMsg().get(Locale.FRENCH), nullValue());

        assertThat(myMsg.myMsgWithArg(123).get(), is("fallback 123"));
        assertThat(myMsg.myMsgWithArg(123).get(Locale.ENGLISH), is("english 123"));
        assertThat(myMsg.myMsgWithArg(123).get(Locale.JAPANESE), is("japanese 123"));
        assertThat(myMsg.myMsgWithArg(123).get(Locale.FRENCH), nullValue());
    }

    @Test
    public void getCurrentLocaleGetsTheClosestMatch() throws Exception {
        MyMsg myMsg = C10N.get(MyMsg.class);
        assertThat(myMsg.myMsg().getCurrentLocale(), is(Locale.ENGLISH));
        assertThat(myMsg.myMsg().get(), is(myMsg.myMsg().get(myMsg.myMsg().getCurrentLocale())));

        Locale.setDefault(Locale.JAPANESE);
        assertThat(myMsg.myMsg().getCurrentLocale(), is(Locale.JAPANESE));
        assertThat(myMsg.myMsg().get(), is(myMsg.myMsg().get(myMsg.myMsg().getCurrentLocale())));

        Locale.setDefault(Locale.CHINESE);
        assertThat(myMsg.myMsg().getCurrentLocale(), is(C10N.FALLBACK_LOCALE));
        assertThat(myMsg.myMsg().get(), is(myMsg.myMsg().get(myMsg.myMsg().getCurrentLocale())));
    }

    @Test
    public void asMapContainsAllMappingAsAMap() throws Exception {
        MyMsg myMsg = C10N.get(MyMsg.class);
        assertThat(myMsg.myMsg().asMap(), CoreMatchers.<Map<Locale, String>>is(ImmutableMap.of(
                C10N.FALLBACK_LOCALE, "fallback",
                Locale.ENGLISH, "english",
                Locale.JAPANESE, "japanese")));
        assertThat(myMsg.myMsgWithArg(234).asMap(), CoreMatchers.<Map<Locale, String>>is(ImmutableMap.of(
                C10N.FALLBACK_LOCALE, "fallback 234",
                Locale.ENGLISH, "english 234",
                Locale.JAPANESE, "japanese 234")));
    }


    @C10NMessages
    private interface MyMsg {
        @En("english")
        @Ja("japanese")
        @C10NDef("fallback")
        C10NMessage myMsg();

        @En("english {0}")
        @Ja("japanese {0}")
        @C10NDef("fallback {0}")
        C10NMessage myMsgWithArg(int arg);
    }
}
