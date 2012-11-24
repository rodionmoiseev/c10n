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

package c10n.tools.search;

import c10n.share.utils.C10NBundleKey;
import c10n.tools.search.test1.Buttons;
import c10n.tools.search.test1.Window;
import c10n.tools.search.test1.labels.Labels1;
import c10n.tools.search.test1.labels.Labels2;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 */
public class DefaultC10NBundleKeySearchTest {
    @Test
    public void c10nInterfaceSearchIntegration() {
        C10NBundleKeySearch bundleKeySearch = new DefaultC10NBundleKeySearch(new DefaultC10NInterfaceSearch(), "");
        assertThat(simpleKeys(bundleKeySearch.findAllKeys("c10n.tools.search.test1")), is(set(
                k(Window.class, "window.title"),
                k(Window.class, "window.author"),
                k(Buttons.class, "buttons.ok"),
                k(Buttons.class, "buttons.cancel"),
                k(Labels1.class, "labels.oops"),
                k(Labels1.class, "labels.label1"),
                k(Labels2.class, "labels.label2")
        )));
    }

    @Test
    public void c10nInterfaceSearchIntegrationWithGlobalPrefix() {
        C10NBundleKeySearch bundleKeySearch = new DefaultC10NBundleKeySearch(
                new DefaultC10NInterfaceSearch(),
                "prefix");
        assertThat(simpleKeys(bundleKeySearch.findAllKeys("c10n.tools.search.test1")), is(set(
                k(Window.class, "prefix.window.title"),
                k(Window.class, "prefix.window.author"),
                k(Buttons.class, "prefix.buttons.ok"),
                k(Buttons.class, "prefix.buttons.cancel"),
                k(Labels1.class, "prefix.labels.oops"),
                k(Labels1.class, "prefix.labels.label1"),
                k(Labels2.class, "prefix.labels.label2")
        )));
    }

    private static Set<SimpleKey> simpleKeys(Iterable<C10NBundleKey> bundleKeys) {
        Set<SimpleKey> res = new HashSet<SimpleKey>();
        for (C10NBundleKey bundleKey : bundleKeys) {
            res.add(new SimpleKey(bundleKey.getDeclaringInterface(), bundleKey.getKey()));
        }
        return res;
    }

    private static Set<SimpleKey> set(SimpleKey... keys) {
        Set<SimpleKey> res = new HashSet<SimpleKey>();
        Collections.addAll(res, keys);
        return res;
    }

    private static SimpleKey k(Class<?> clazz, String key) {
        return new SimpleKey(clazz, key);
    }

    private static final class SimpleKey {
        final Class<?> c10nInterface;
        final String key;

        private SimpleKey(Class<?> c10nInterface, String key) {
            this.c10nInterface = c10nInterface;
            this.key = key;
        }

        @SuppressWarnings("RedundantIfStatement")//rationale: generated code
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleKey simpleKey = (SimpleKey) o;

            if (!c10nInterface.equals(simpleKey.c10nInterface)) return false;
            if (!key.equals(simpleKey.key)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = c10nInterface.hashCode();
            result = 31 * result + key.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "SimpleKey{" +
                    "c10nInterface=" + c10nInterface +
                    ", key='" + key + '\'' +
                    '}';
        }
    }
}
