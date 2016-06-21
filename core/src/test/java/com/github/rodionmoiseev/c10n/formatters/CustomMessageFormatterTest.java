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

package com.github.rodionmoiseev.c10n.formatters;

import com.github.rodionmoiseev.c10n.C10N;
import com.github.rodionmoiseev.c10n.C10NConfigBase;
import com.github.rodionmoiseev.c10n.C10NMessages;
import com.github.rodionmoiseev.c10n.annotations.DefaultC10NAnnotations;
import com.github.rodionmoiseev.c10n.annotations.En;
import com.github.rodionmoiseev.c10n.test.utils.RuleUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by rodexion on 2015/10/22.
 */
public class CustomMessageFormatterTest {
    @Rule
    public TestRule tmpLocale = RuleUtils.tmpLocale(Locale.ENGLISH);
    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

    @Before
    public void before() {
    }

    @C10NMessages
    public interface MyMessageForFormatting {
        @En("Hello {0}!")
        String testMessage(String user);
    }

    @Test
    public void customFormatterTest() throws Exception {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
                setMessageFormatter(new MessageFormatter() {
                    @Override
                    public String format(Method method, String message, Locale locale, Object... args) {
                        return method.getName() + ":" +
                                message + ":" +
                                locale.getDisplayName() + ":" +
                                args[0];
                    }
                });
            }
        });

        MyMessageForFormatting msg = C10N.get(MyMessageForFormatting.class);
        assertThat(msg.testMessage("rodion"), is(equalTo("testMessage:Hello {0}!:English:rodion")));
    }
}