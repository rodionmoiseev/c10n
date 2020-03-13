/*
 * Copyright 2012 Rodion Moiseev (https://github.com/rodionmoiseev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rodionmoiseev.c10n.tools.inspector;

import com.github.rodionmoiseev.c10n.share.utils.C10NBundleKey;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.github.rodionmoiseev.c10n.share.utils.Preconditions.assertNotNull;

/**
 * <p>One c10n translation unit, representing one single message.
 *
 * @author rodion
 * @since 1.1
 */
public final class C10NUnit {
    private final Class<?> declaringInterface;
    private final Method declaringMethod;
    private final C10NBundleKey key;
    private final Map<Locale, C10NTranslations> translations = new HashMap<>();

    C10NUnit(Class<?> declaringInterface, Method declaringMethod, C10NBundleKey key, Set<Locale> initLocales) {
        assertNotNull(declaringInterface, "declaringInterface");
        assertNotNull(declaringMethod, "declaringMethod");
        assertNotNull(key, "key");
        assertNotNull(initLocales, "initLocales");
        this.declaringInterface = declaringInterface;
        this.declaringMethod = declaringMethod;
        this.key = key;
        for (Locale locale : initLocales) {
            this.translations.put(locale, new C10NTranslations());
        }
    }

    /**
     * <p>c10n interface declaring the method to which this key is bound.
     *
     * @return c10n interface class (not null)
     */
    public Class<?> getDeclaringInterface() {
        return declaringInterface;
    }

    /**
     * <p>Method to which this key is bound
     *
     * @return bound method (not null)
     */
    public Method getDeclaringMethod() {
        return declaringMethod;
    }

    /**
     * <p>Resource bundle key bound to this unit
     *
     * @return bound bundle key (not null)
     */
    public C10NBundleKey getKey() {
        return key;
    }

    /**
     * <p>All detected translations and their values for the
     * given set of locales specified at inspection time.
     *
     * @return set of translations per locale
     */
    public Map<Locale, C10NTranslations> getTranslations() {
        return translations;
    }

    @Override
    public String toString() {
        return "C10NUnit{" +
                "declaringInterface=" + declaringInterface +
                ", declaringMethod=" + declaringMethod +
                ", key=" + key +
                ", translations=" + translations +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        C10NUnit c10NUnit = (C10NUnit) o;

        if (!declaringInterface.equals(c10NUnit.declaringInterface)) return false;
        if (!declaringMethod.equals(c10NUnit.declaringMethod)) return false;
        if (!key.equals(c10NUnit.key)) return false;
        if (!translations.equals(c10NUnit.translations)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = declaringInterface.hashCode();
        result = 31 * result + declaringMethod.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + translations.hashCode();
        return result;
    }
}
