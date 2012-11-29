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

import c10n.share.utils.C10NBundleKey;
import com.google.common.collect.Maps;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author rodion
 */
public final class C10NUnit {
    private final C10NBundleKey key;
    private final Map<Locale, C10NTranslations> translations = Maps.newHashMap();

    C10NUnit(C10NBundleKey key, Set<Locale> initLocales) {
        this.key = key;
        for (Locale locale : initLocales) {
            this.translations.put(locale, new C10NTranslations());
        }
    }

    public C10NBundleKey getKey() {
        return key;
    }

    public Map<Locale, C10NTranslations> getTranslations() {
        return translations;
    }

    @Override
    public String toString() {
        return "C10NUnit{" +
                "key=" + key +
                ", translations=" + translations +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        C10NUnit c10NUnit = (C10NUnit) o;

        if (!key.equals(c10NUnit.key)) return false;
        if (!translations.equals(c10NUnit.translations)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + translations.hashCode();
        return result;
    }
}
