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

package com.github.rodionmoiseev.c10n.share;

import java.util.Locale;
import java.util.Set;

/**
 * @author rodion
 */
class DefaultLocaleMapping implements LocaleMapping {

    @Override
    public Locale findClosestMatch(Set<Locale> fromSet, Locale forLocale) {
        String variant = forLocale.getDisplayVariant();
        String country = forLocale.getCountry();
        String language = forLocale.getLanguage();
        Locale[] c = new Locale[4];
        if (null != variant && !variant.isEmpty()) {
            c[0] = forLocale;
        }
        if (null != country && !country.isEmpty()) {
            c[1] = new Locale(language, country);
        }
        if (null != language && !language.isEmpty()) {
            c[2] = new Locale(language);
        }
        c[3] = Locale.ROOT;
        for (Locale candidateLocale : c) {
            if (fromSet.contains(candidateLocale)) {
                return candidateLocale;
            }
        }
        //This code intentionally uses Locale.getDefault()
        //in order to behave in the same was as the default
        //resource bundle locale search mechanism.
        //source: http://docs.oracle.com/javase/tutorial/i18n/resbundle/concept.html
        Locale systemDefaultLocale = Locale.getDefault();
        if (!systemDefaultLocale.equals(forLocale)) {
            return findClosestMatch(fromSet, systemDefaultLocale);
        }
        return null;
    }
}
