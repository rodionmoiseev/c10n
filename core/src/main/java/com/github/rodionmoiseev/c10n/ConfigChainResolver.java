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

import java.util.List;

/**
 * @author rodion
 */
interface ConfigChainResolver {
    /**
     * <p>Resolve all c10n configurations responsible for the specified
     * c10n interface.
     *
     * <p>Configurations are returned in the bottom-up order, that is the
     * most concrete configuration first, followed by parent configurations.
     * When multiple configurations exist on the same hierarchy level, they
     * are returned in alphabetical order of their class name.
     *
     * @param c10nInterface C10N interface to resolve configurations for (non-null)
     * @return List of configurations responsible for the given c10n interface
     */
    List<C10NConfigBase> resolve(Class<?> c10nInterface);
}
