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
import c10n.annotations.En;
import c10n.annotations.It;
import c10n.annotations.Ja;
import c10n.test.utils.RuleUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * <p>Created: 7/31/12 11:33 AM</p>
 *
 * @author rodion
 */
public class C10NTest {

    private final static String containsApostrophe = "L\'Italiano";

    @Rule
    public TestRule tmpLocale = RuleUtils.tmpLocale();
    @Rule
    public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

    @Test
    public void parametrisationIsDisabledWhenRawFalseIsPresent() {
        C10N.configure(new DefaultC10NAnnotations());
        Messages msg = C10N.get(Messages.class);
        Locale.setDefault(Locale.ENGLISH);
        assertThat(msg.text("ignored"), is("{} {0} {hello}"));
    }

    @Test
    public void multipleC10NMsgFactoriesCanBeCreatedAndUsedIndividually() {
        C10NMsgFactory enC10nFactory = C10N.createMsgFactory(C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
                setLocale(Locale.ENGLISH);
            }
        }));
        assertThat(enC10nFactory.get(Messages.class).text(), is("english"));

        C10NMsgFactory jpC10nFactory = C10N.createMsgFactory(C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
                setLocale(Locale.JAPANESE);
            }
        }));
        assertThat(jpC10nFactory.get(Messages.class).text(), is("japanese"));
    }

    @Test
    public void configureTimeLocaleProviderIsIgnoreWhenLocaleIsPassedAtMessageCreationTime() {
        C10NMsgFactory c10nFactory = C10N.createMsgFactory(C10N.configure(new C10NConfigBase() {
            @Override
            protected void configure() {
                install(new DefaultC10NAnnotations());
                setLocale(Locale.ENGLISH);
            }
        }));
        assertThat(c10nFactory.get(Messages.class, Locale.JAPANESE).text(), is("japanese"));
        assertThat(c10nFactory.get(Messages.class, Locale.ENGLISH).text(), is("english"));
    }

    /**
     * Test for <a href="https://github.com/rodionmoiseev/c10n/issues/22">issue 22</a>
     */
    @Test
    public void issue22_apostrophe() throws Exception {
        C10NMsgFactory itC10nFactory = C10N.createMsgFactory(C10N
                .configure(new C10NConfigBase() {
                    @Override
                    protected void configure() {
                        install(new DefaultC10NAnnotations());
                        setLocale(Locale.ITALIAN);
                    }
                }));

        assertThat(itC10nFactory.get(Messages.class).apostropheIssue(),
                is(containsApostrophe));
    }

    interface Messages {
        @En(value = "{} {0} {hello}", raw = true)
        String text(String ignored);

        @En("english")
        @Ja("japanese")
        String text();

        @It(containsApostrophe)
        String apostropheIssue();
    }
}
