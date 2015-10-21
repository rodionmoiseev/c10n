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
import com.github.rodionmoiseev.c10n.annotations.En;
import com.github.rodionmoiseev.c10n.plugin.C10NPlugin;
import com.github.rodionmoiseev.c10n.plugin.PluginResult;
import com.github.rodionmoiseev.c10n.test.utils.RuleUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class PluginTest {
    @Rule
    public TestRule tmpLocale = RuleUtils.tmpLocale(Locale.ENGLISH);
    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

    @Before
    public void before() {
    }

    @C10NMessages
    public interface MyMessage {
        @En("Hello {0}!")
        String testMessage(String user);
    }

    @Test
    public void pluginExecutionStopsWhenPluginReturnsStopSignal() {
        final C10NPlugin plugin1 = Mockito.mock(C10NPlugin.class);
        pluginReturn(plugin1, PluginResult.last("last message"));
        final C10NPlugin plugin2 = Mockito.mock(C10NPlugin.class);
        configurePlugins(plugin1, plugin2);

        MyMessage mm = C10N.get(MyMessage.class);
        assertThat(mm.testMessage("rodion"), is("last message"));
        verify(plugin1).format(MyMessage.class,
                getMethod(MyMessage.class, "testMessage"),
                new Object[]{"rodion"},
                "Hello rodion!");
        verifyZeroInteractions(plugin2);
    }

    @Test
    public void pluginExecutionSkipsWhenPluginReturnsNull() {
        final C10NPlugin plugin1 = Mockito.mock(C10NPlugin.class);
        pluginReturn(plugin1, null);
        final C10NPlugin plugin2 = Mockito.mock(C10NPlugin.class);
        pluginReturn(plugin2, PluginResult.passOn("plugin result2"));
        configurePlugins(plugin1, plugin2);

        MyMessage mm = C10N.get(MyMessage.class);
        assertThat(mm.testMessage("rodion"), is("plugin result2"));
        verify(plugin1).format(MyMessage.class,
                getMethod(MyMessage.class, "testMessage"),
                new Object[]{"rodion"},
                "Hello rodion!");
        verify(plugin2).format(MyMessage.class,
                getMethod(MyMessage.class, "testMessage"),
                new Object[]{"rodion"},
                "Hello rodion!");// <-- receives the original message
    }


    @Test
    public void pluginPassesTheModifiedValueToTheNextPlugin() {
        final C10NPlugin plugin1 = Mockito.mock(C10NPlugin.class);
        pluginReturn(plugin1, PluginResult.passOn("plugin result1"));
        final C10NPlugin plugin2 = Mockito.mock(C10NPlugin.class);
        pluginReturn(plugin2, PluginResult.passOn("plugin result2"));
        configurePlugins(plugin1, plugin2);

        MyMessage mm = C10N.get(MyMessage.class);
        assertThat(mm.testMessage("rodion"), is("plugin result2"));
        verify(plugin1).format(MyMessage.class,
                getMethod(MyMessage.class, "testMessage"),
                new Object[]{"rodion"},
                "Hello rodion!");
        verify(plugin2).format(MyMessage.class,
                getMethod(MyMessage.class, "testMessage"),
                new Object[]{"rodion"},
                "plugin result1");
    }

    private void configurePlugins(final C10NPlugin... plugins) {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
                for (C10NPlugin plugin : plugins) {
                    installPlugin(plugin);
                }
            }
        });
    }

    private Method getMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new NoSuchElementException(methodName);
    }

    private void pluginReturn(C10NPlugin plugin, PluginResult result) {
        when(plugin.format(any(Class.class),
                any(Method.class),
                any(Object[].class),
                any())).thenReturn(result);
    }
}
