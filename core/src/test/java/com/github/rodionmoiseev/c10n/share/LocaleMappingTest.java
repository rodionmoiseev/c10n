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

package com.github.rodionmoiseev.c10n.share;

import com.github.rodionmoiseev.c10n.test.utils.RuleUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 */
public class LocaleMappingTest {
    @Rule
    public TestRule tmpLocale = RuleUtils.tmpLocale();

    @SuppressWarnings("serial")
    private final Set<Locale> candidateLocales = new HashSet<Locale>() {{
        add(new Locale("a", "b", "c"));
        add(new Locale("a", "b", "d"));
        add(new Locale("a", "b"));
        add(new Locale("a"));
        add(new Locale("x", "y", "z"));
        add(new Locale("x"));
        add(new Locale("p", "q", "r"));
    }};

    @Test
    public void preciseMatch() {
        assertMapping(new Locale("a", "b", "c"), new Locale("a", "b", "c"));
        assertMapping(new Locale("a", "b"), new Locale("a", "b"));
        assertMapping(new Locale("x"), new Locale("x"));
    }

    @Test
    public void noMatch() {
        assertThat(impl().findClosestMatch(candidateLocales, new Locale("unknown")), is(nullValue()));
        assertThat(impl().findClosestMatch(candidateLocales, new Locale("p")), is(nullValue()));
    }

    @Test
    public void noMatchWithFallback() {
        candidateLocales.add(Locale.ROOT);
        assertThat(impl().findClosestMatch(candidateLocales, new Locale("unknown")), is(Locale.ROOT));
        assertThat(impl().findClosestMatch(candidateLocales, new Locale("p")), is(Locale.ROOT));
    }

    @Test
    public void oneLevelFallback() {
        assertMapping(new Locale("a", "b", "-"), new Locale("a", "b"));
        assertMapping(new Locale("x", "y", "-"), new Locale("x"));
    }

    @Test
    public void twoLevelFallback() {
        assertMapping(new Locale("a", "-", "-"), new Locale("a"));
        assertMapping(new Locale("a", "-"), new Locale("a"));
    }

    @Test
    public void fallsBackOntoDefaultLocaleIfDifferentToTheSpecified() {
        Locale.setDefault(new Locale("x", "y", "z"));
        assertMapping(new Locale("p"), new Locale("x", "y", "z"));

        Locale.setDefault(new Locale("x", "y", "-"));
        assertMapping(new Locale("p"), new Locale("x"));

        candidateLocales.add(Locale.ROOT);
        Locale.setDefault(new Locale("unknown"));
        assertMapping(new Locale("p"), Locale.ROOT);
    }

    private LocaleMapping impl() {
        return new DefaultLocaleMapping();
    }

    private void assertMapping(Locale forLocale, Locale expectedMapping) {
        LocaleMapping lm = impl();
        assertThat(lm.findClosestMatch(candidateLocales, forLocale), is(expectedMapping));
    }
}
