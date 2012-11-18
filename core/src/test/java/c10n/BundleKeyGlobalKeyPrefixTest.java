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

package c10n;

import c10n.annotations.DefaultC10NAnnotations;
import c10n.share.util.RuleUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 */
public class BundleKeyGlobalKeyPrefixTest {
    @Rule
    public TestRule tmpLocale = RuleUtils.tmpLocale();

    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

    @Before
    public void c10nFixture() {
        C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
                bindBundle("c10n.testBundles.TestBundle");
                setKeyPrefix("com.myCompany");
                setDebug(true);
            }
        });
    }

    @Test
    public void methodKeyIsPrependedWithGlobalPrefix() {
        Messages msg = C10N.get(Messages.class);

        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.methodName(), is("en"));
        assertThat(msg.msgNoKey(), is("en msgNoKey"));
        Locale.setDefault(new Locale("ru"));
        assertThat(msg.methodName(), is("ru"));
        assertThat(msg.msgNoKey(), is("ru msgNoKey"));
    }

    @Test
    public void interfacesMarkedWithC10NKeyArePrependedWithGlobalPrefix() {
        MessagesWithKey msg = C10N.get(MessagesWithKey.class);

        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.methodName(), is("en"));
        assertThat(msg.msgNoKey(), is("en msgNoKey"));

        Locale.setDefault(new Locale("ru"));
        assertThat(msg.methodName(), is("ru"));
        assertThat(msg.msgNoKey(), is("ru msgNoKey"));
    }

    interface Messages {
        //expected key: com.myCompany.msg
        @C10NKey("msg")
        String methodName();

        //expected key: com.myCompany.c10n.BundleKeyGlobalKeyPrefixTest.Messages.msgNoKey
        String msgNoKey();
    }

    @C10NKey("MyMessages")
    interface MessagesWithKey {
        //expected key: com.myCompany.MyMessages.msg
        @C10NKey("msg")
        String methodName();

        //expected key: com.myCompany.MyMessages.msgNoKey
        String msgNoKey();
    }
}