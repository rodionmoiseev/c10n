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
import c10n.test.utils.RuleUtils;
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
public class BundleKeysTest {
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
            }
        });
    }

    @Test
    public void c10nKeyAnnotationIsUsedForFetchingBundleKeysIfPresent() {
        AbsoluteMessages msg = C10N.get(AbsoluteMessages.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.msg(), is("en"));

        Locale.setDefault(new Locale("ru"));
        assertThat(msg.msg(), is("ru"));
    }

    @Test
    public void keysPrefixedWithDotAreAssumedToBeAbsolute_dotIsIgnored() {
        AbsoluteMessages msg = C10N.get(AbsoluteMessages.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.absMsg(), is("en"));

        Locale.setDefault(new Locale("ru"));
        assertThat(msg.absMsg(), is("ru"));
    }

    @Test
    public void keysOnMethodsAreRelativeToParentKey() {
        RelativeMessages msg = C10N.get(RelativeMessages.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.msg(), is("en"));

        Locale.setDefault(new Locale("ru"));
        assertThat(msg.msg(), is("ru"));
    }

    @Test
    public void parentKeyIsIgnoredIfMethodKeyIsAbsolute() {
        RelativeMessages msg = C10N.get(RelativeMessages.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.absMsg(), is("en"));

        Locale.setDefault(new Locale("ru"));
        assertThat(msg.absMsg(), is("ru"));
    }

    @Test
    public void keyIsMethodNamePlusParentKeyWhenParentKeyIsPresent() {
        RelativeMessages2 msg = C10N.get(RelativeMessages2.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.msg(), is("en"));
        assertThat(msg.msg("hello", 1), is("en hello 1"));

        Locale.setDefault(new Locale("ru"));
        assertThat(msg.msg(), is("ru"));
        assertThat(msg.msg("hello", 2), is("ru hello 2"));
    }

    @Test
    public void extendingInterfacesCanDeclareTheirOwnKeys() {
        ParentMessages parent = C10N.get(ParentMessages.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(parent.parentMsg(), is("en parent"));
        assertThat(parent.parentMsgNoKey(), is("en parentNoKey"));

        Locale.setDefault(new Locale("ru"));
        assertThat(parent.parentMsg(), is("ru parent"));
        assertThat(parent.parentMsgNoKey(), is("ru parentNoKey"));

        InheritedMessages child = C10N.get(InheritedMessages.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(child.parentMsg(), is("en parent"));
        assertThat(child.parentMsgNoKey(), is("en parentNoKey"));
        assertThat(child.childMsg(), is("en child"));
        assertThat(child.childMsgNoKey(), is("en childNoKey"));

        Locale.setDefault(new Locale("ru"));
        assertThat(child.parentMsg(), is("ru parent"));
        assertThat(child.parentMsgNoKey(), is("ru parentNoKey"));
        assertThat(child.childMsg(), is("ru child"));
        assertThat(child.childMsgNoKey(), is("ru childNoKey"));
    }

    @Test
    public void interfacesInheritParentsClassKey() {
        InheritedMessages2 child = C10N.get(InheritedMessages2.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(child.childMsg(), is("en child2"));

        Locale.setDefault(new Locale("ru"));
        assertThat(child.childMsg(), is("ru child2"));
    }

    @Test
    public void whenNoC10NKeysAreSpecifiedKeyIsBasedOnFQDN_and_methodName() {
        NonC10NKeyMessages msg = C10N.get(NonC10NKeyMessages.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.msg(), is("en plain"));
        Locale.setDefault(new Locale("ru"));
        assertThat(msg.msg(), is("ru plain"));

    }

    interface NonC10NKeyMessages {
        String msg();
    }

    interface AbsoluteMessages {
        @C10NKey("com.myCompany.msg")
        String msg();

        /*
         * absolute key -> leading dot is ignored
         */
        @C10NKey(".com.myCompany.msg")
        String absMsg();
    }

    @C10NKey("com.myCompany")
    interface RelativeMessages {
        @C10NKey("msg")
        String msg();

        /*
         * absolute key -> parent key is ignored, so is the leading dot.
         */
        @C10NKey(".com.myCompany.msg")
        String absMsg();
    }

    @C10NKey("com.myCompany")
    interface RelativeMessages2 {
        String msg();

        String msg(String a, int b);
    }

    @C10NKey("com.parent")
    interface ParentMessages {
        @C10NKey("parentMsg")
        String parentMsg();

        String parentMsgNoKey();
    }

    @C10NKey("com.child")
    interface InheritedMessages extends ParentMessages {
        @C10NKey("childMsg")
        String childMsg();

        String childMsgNoKey();
    }

    interface ParentWithNoKey {
    }

    interface InheritedMessages2 extends ParentWithNoKey, ParentMessages {
        @C10NKey("childMsg")
        String childMsg();
    }
}
