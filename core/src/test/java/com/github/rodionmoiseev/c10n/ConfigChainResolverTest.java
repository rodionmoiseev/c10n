/*
 *  Licensed to the Apache Software Foundation (ASF) under one
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
 *
 */

package com.github.rodionmoiseev.c10n;

import com.github.rodionmoiseev.c10n.annotations.DefaultC10NAnnotations;
import com.github.rodionmoiseev.c10n.inner.sub1.Sub1Config;
import com.github.rodionmoiseev.c10n.inner.sub1.Sub1Config2;
import com.github.rodionmoiseev.c10n.inner.sub1.Sub1Interface;
import com.github.rodionmoiseev.c10n.inner.sub1.sub11.Sub11Interface;
import com.github.rodionmoiseev.c10n.inner.sub1.sub12.Sub12Interface;
import com.github.rodionmoiseev.c10n.test.utils.RuleUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 */
public class ConfigChainResolverTest {
    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

    @Test
    public void resolveInterfaceInSamePackageAsThatOfParentConfig() {
        assertThat(resolveFor(ParentInterface.class), is(asList("ParentConfig")));
    }

    @Test
    public void resolveInterfaceInPackageAsThatOfNestedConfig() {
        assertThat(resolveFor(Sub1Interface.class), is(asList("Sub1Config", "Sub1Config2", "ParentConfig")));
    }

    @Test
    public void resolveInterfaceInSubPackageAsThatOfNestedConfig() {
        assertThat(resolveFor(Sub11Interface.class), is(asList("Sub1Config", "Sub1Config2", "ParentConfig")));
    }

    @Test
    public void resolveInterfaceInSamePackageAsThatOfDoubleNestedConfig() {
        assertThat(resolveFor(Sub12Interface.class), is(asList("Sub12Config", "Sub1Config", "Sub1Config2", "ParentConfig")));
    }

    @Test
    public void defaultAnnotationConfigAlwaysAppearsAtTheBottomOfTheHierarchy() {
        assertThat(resolveFor(new ParentConfigWithDefault(), Sub11Interface.class),
                is(asList("Sub1Config", "Sub1Config2", "ParentConfigWithDefault", "DefaultC10NAnnotations")));
    }

    private List<String> resolveFor(Class<?> c10nInterface) {
        return resolveFor(new ParentConfig(), c10nInterface);
    }

    private List<String> resolveFor(C10NConfigBase parent, Class<?> c10nInterface) {
        C10N.configure(parent);

        ConfigChainResolver resolver = create(parent);
        List<C10NConfigBase> chain = resolver.resolve(c10nInterface);
        List<String> names = new ArrayList<String>();
        for (C10NConfigBase config : chain) {
            names.add(config.getClass().getSimpleName());
        }
        return names;
    }

    private ConfigChainResolver create(C10NConfigBase parent) {
        return new DefaultConfigChainResolver(parent);
    }

    static class ParentConfig extends C10NConfigBase {
        @Override
        protected void configure() {
            install(new Sub1Config());
            install(new Sub1Config2());
        }
    }

    static class ParentConfigWithDefault extends C10NConfigBase {
        @Override
        protected void configure() {
            install(new DefaultC10NAnnotations());
            install(new Sub1Config());
            install(new Sub1Config2());
        }
    }

    interface ParentInterface {
    }
}
