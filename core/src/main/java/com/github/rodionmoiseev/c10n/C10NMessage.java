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

package com.github.rodionmoiseev.c10n;

import java.util.Locale;
import java.util.Map;

public class C10NMessage {
    private final Locale currentLocale;
    private final String currentLocaleMessage;
    private final Map<Locale, String> messages;

    public C10NMessage(Locale currentLocale, String currentLocaleMessage, Map<Locale, String> messages) {
        this.currentLocale = currentLocale;
        this.currentLocaleMessage = currentLocaleMessage;
        this.messages = messages;
    }

    /**
     * Retrieves the translation matching the current user locale.
     * The value would be equivalent to the value returned
     * if the method return type was {@link String}
     *
     * @return translation matching the current user locale, or {@code null} if none is present
     */
    public String get() {
        return currentLocaleMessage;
    }

    /**
     * Retrieves translation for the given locale.
     * The locale has to be an exact match with the locale
     * bound in the c10n-configuration.
     *
     * @param locale locale for which to fetch the translation
     * @return Translation for the given locale, or {@code null} if none is present
     */
    public String get(Locale locale) {
        return messages.get(locale);
    }

    /**
     * The locale from the mapping that is the closest match to the
     * user's current locale. Typically this means that {@code get()}
     * is equivalent to {@code get(getCurrentLocale())} (expect cases
     * when a custom method implementation is bound).
     *
     * @return The closest locale match to the current locale
     */
    public Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * All translation mappings for this method as a map.
     * <p>
     * <em>Note:</em> Map is not cloned, so modify at own risk.
     *
     * @return translation mapping
     */
    public Map<Locale, String> asMap() {
        return messages;
    }
}
