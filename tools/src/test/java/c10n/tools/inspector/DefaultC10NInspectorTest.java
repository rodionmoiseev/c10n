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

package c10n.tools.inspector;

import c10n.C10N;
import c10n.C10NConfigBase;
import c10n.ConfiguredC10NModule;
import c10n.annotations.DefaultC10NAnnotations;
import c10n.annotations.En;
import c10n.annotations.Ja;
import c10n.tools.C10NTools;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author rodion
 */
public class DefaultC10NInspectorTest {
    /*
     * Test all possible variations of keys:
     * - key with at least one annotation but not in any bundle (considered OK)
     * - key defined in all bundles but without annotations (considered OK)
     * - key missing in one bundle (considered an ERROR)
     * - key missing in all bundles (considered an ERROR)
     */
    @Test
    public void completeScenario() {
        Set<Locale> localesToCheck = Sets.newHashSet(Locale.ENGLISH, Locale.JAPANESE);


        C10NConfigBase conf = new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
                bindBundle("c10n.tools.inspector.test1.Test1");
            }
        };
        ConfiguredC10NModule confdModule = C10N.configure(conf);
        C10NInspector checker = new DefaultC10NInspector(C10NTools.bundleKeySearch(confdModule.getKeyPrefix()),
                confdModule,
                localesToCheck);
        Iterable<C10NUnit> units = checker.inspect("c10n.tools.inspector.test1");

        for (C10NUnit unit : units) {
            System.out.println(unit);
        }

        assertThat(Iterables.size(units), is(6));
        //key in both bundles, without annotations
        assertThat(unitFor("msg1.key1", units).getTranslations().get(Locale.ENGLISH).getBundles().size(), is(1));
        assertThat(unitFor("msg1.key1", units).getTranslations().get(Locale.JAPANESE).getBundles().size(), is(1));
        assertThat(unitFor("msg1.key1", units).getTranslations().get(Locale.ENGLISH).getAnnotations().size(), is(0));
        assertThat(unitFor("msg1.key1", units).getTranslations().get(Locale.JAPANESE).getAnnotations().size(), is(0));

        //key in just one bundle, without annotations
        assertThat(unitFor("msg1.key2", units).getTranslations().get(Locale.ENGLISH).getBundles().size(), is(0));
        assertThat(unitFor("msg1.key2", units).getTranslations().get(Locale.JAPANESE).getBundles().size(), is(1));
        assertThat(unitFor("msg1.key2", units).getTranslations().get(Locale.ENGLISH).getAnnotations().size(), is(0));
        assertThat(unitFor("msg1.key2", units).getTranslations().get(Locale.JAPANESE).getAnnotations().size(), is(0));

        //key with both annotations, in no bundles
        assertThat(unitFor("msg1.annotatedValue", units).getTranslations().get(Locale.ENGLISH).getBundles().size(), is(0));
        assertThat(unitFor("msg1.annotatedValue", units).getTranslations().get(Locale.JAPANESE).getBundles().size(), is(0));
        assertTrue(unitFor("msg1.annotatedValue", units).getTranslations().get(Locale.ENGLISH).getAnnotations().contains(En.class));
        assertTrue(unitFor("msg1.annotatedValue", units).getTranslations().get(Locale.JAPANESE).getAnnotations().contains(Ja.class));

        //key in both bundles, without annotations
        assertThat(unitFor("msg2.key3", units).getTranslations().get(Locale.ENGLISH).getBundles().size(), is(1));
        assertThat(unitFor("msg2.key3", units).getTranslations().get(Locale.JAPANESE).getBundles().size(), is(1));
        assertThat(unitFor("msg2.key3", units).getTranslations().get(Locale.ENGLISH).getAnnotations().size(), is(0));
        assertThat(unitFor("msg2.key3", units).getTranslations().get(Locale.JAPANESE).getAnnotations().size(), is(0));

        //key not to be found anywhere
        assertThat(unitFor("msg2.key4", units).getTranslations().get(Locale.ENGLISH).getBundles().size(), is(0));
        assertThat(unitFor("msg2.key4", units).getTranslations().get(Locale.JAPANESE).getBundles().size(), is(0));
        assertThat(unitFor("msg2.key4", units).getTranslations().get(Locale.ENGLISH).getAnnotations().size(), is(0));
        assertThat(unitFor("msg2.key4", units).getTranslations().get(Locale.JAPANESE).getAnnotations().size(), is(0));

        //key with just one annotation, in no bundles
        assertThat(unitFor("msg2.annotatedValue", units).getTranslations().get(Locale.ENGLISH).getBundles().size(), is(0));
        assertThat(unitFor("msg2.annotatedValue", units).getTranslations().get(Locale.JAPANESE).getBundles().size(), is(0));
        assertTrue(unitFor("msg2.annotatedValue", units).getTranslations().get(Locale.ENGLISH).getAnnotations().contains(En.class));
        assertThat(unitFor("msg2.annotatedValue", units).getTranslations().get(Locale.JAPANESE).getAnnotations().size(), is(0));
    }

    private static Set<Locale> set(Locale... locales) {
        return new HashSet<Locale>(Arrays.asList(locales));
    }

    private static C10NUnit unitFor(String key, Iterable<C10NUnit> units) {
        for (C10NUnit unit : units) {
            if (unit.getKey().getKey().equals(key)) {
                return unit;
            }
        }
        throw new RuntimeException("c10n-unit for key '" + key + "' was not found in: " + units);
    }
}
