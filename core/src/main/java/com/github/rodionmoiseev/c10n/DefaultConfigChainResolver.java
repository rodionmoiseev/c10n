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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author rodion
 */
public class DefaultConfigChainResolver implements ConfigChainResolver {
    private static final Comparator<C10NConfigBase> cmpByConfPkgName = new C10NConfigBaseComparator();
    private final C10NConfigBase parentConfig;

    public DefaultConfigChainResolver(C10NConfigBase parentConfig) {
        this.parentConfig = parentConfig;
    }

    @Override
    public List<C10NConfigBase> resolve(Class<?> c10nInterface) {
        List<C10NConfigBase> res = new ArrayList<C10NConfigBase>();
        traverse(parentConfig, c10nInterface, res);
        Collections.sort(res, cmpByConfPkgName);
        return res;
    }

    private void traverse(C10NConfigBase config, Class<?> c10nInterface, List<C10NConfigBase> result) {
        result.add(config);
        for (C10NConfigBase childConfig : config.getChildConfigs()) {
            if (isPackageAncestorOf(childConfig, c10nInterface)) {
                traverse(childConfig, c10nInterface, result);
            }
        }
    }

    private boolean isPackageAncestorOf(C10NConfigBase config, Class<?> c10nInterface) {
        String c10nPackage = c10nInterface.getPackage().getName();
        String configPackage = config.getConfigurationPackage();
        return c10nPackage.startsWith(configPackage);
    }

    private static final class C10NConfigBaseComparator implements Comparator<C10NConfigBase> {
        @Override
        public int compare(C10NConfigBase o1, C10NConfigBase o2) {
            //reverse the comparison order for package names
            int cmp = -1 * o1.getConfigurationPackage().compareTo(o2.getConfigurationPackage());
            if (0 == cmp) {
                cmp = o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
            }
            return cmp;
        }
    }
}
