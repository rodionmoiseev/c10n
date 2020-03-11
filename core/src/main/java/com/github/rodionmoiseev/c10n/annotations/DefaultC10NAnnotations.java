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

package com.github.rodionmoiseev.c10n.annotations;

import com.github.rodionmoiseev.c10n.C10NConfigBase;

import java.util.Locale;

/**
 * @author rodion
 */
public class DefaultC10NAnnotations extends C10NConfigBase {
    /**
     * <p>Always returns an empty package name to make
     * sure this configuration is always the parent
     * of all other configurations in the hierarchy
     *
     * @return Empty string
     */
    @Override
    protected String getConfigurationPackage() {
        return "";
    }

    @Override
    protected void configure() {
        bindAnnotation(En.class).toLocale(Locale.ENGLISH);
        bindAnnotation(Es.class).toLocale(new Locale("es"));
        bindAnnotation(De.class).toLocale(Locale.GERMAN);
        bindAnnotation(Fr.class).toLocale(Locale.FRENCH);
        bindAnnotation(It.class).toLocale(Locale.ITALIAN);
        bindAnnotation(Ja.class).toLocale(Locale.JAPANESE);
        bindAnnotation(Ko.class).toLocale(Locale.KOREAN);
        bindAnnotation(Ru.class).toLocale(new Locale("ru"));
        bindAnnotation(Zh.class).toLocale(Locale.CHINESE);
    }
}
