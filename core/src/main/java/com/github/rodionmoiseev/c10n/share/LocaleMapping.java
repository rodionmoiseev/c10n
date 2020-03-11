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
public interface LocaleMapping {
    /**
     * <p>Find the locale from the given locale set that
     * is the closes match to the forLocale.
     *
     * <p>Provided the set contains {@link Locale#ROOT}, the
     * result is guaranteed to be non-null.
     *
     * @param fromList  possible locales to pick from
     * @param forLocale locale to find the closest match for
     * @return closest locale match from the list, or null.
     */
    Locale findClosestMatch(Set<Locale> fromList, Locale forLocale);
}
