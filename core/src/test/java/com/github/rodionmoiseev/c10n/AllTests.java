/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.github.rodionmoiseev.c10n;

import com.github.rodionmoiseev.c10n.formatters.CustomMessageFormatterTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author rodion
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        com.github.rodionmoiseev.c10n.annotations.AllTests.class,
        com.github.rodionmoiseev.c10n.guice.AllTests.class,
        com.github.rodionmoiseev.c10n.resources.AllTests.class,
        com.github.rodionmoiseev.c10n.share.AllTests.class,
        AnnotationBindingConfigurationErrorTest.class,
        BundleKeyGlobalKeyPrefixTest.class,
        BundleKeysTest.class,
        C10NConfigBaseInstallTest.class,
        C10NFiltersTest.class,
        C10NFilterTest.class,
        C10NTest.class,
        ConfigChainResolverTest.class,
        ConfiguredC10NModuleTest.class,
        CustomAnnotationBindingTest.class,
        CustomImplementationBindingTest.class,
        DelegationTest.class,
        ExtendedMessageFormatterTest.class,
        FallbackC10NFactoryTest.class,
        LocaleProviderTest.class,
        LocaleSelectionTest.class,
        CustomMessageFormatterTest.class,
        PackageLocalInterfaceTest.class,
        PluginTest.class,
        ResourceBundleBindingTest.class,
        UntranslatedMessageHandlerTest.class
})
public class AllTests {
}
