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

package c10n;

import c10n.share.util.RuleUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ResourceBundleBindingTest {
  @Rule
  public TestRule tmpLocale = RuleUtils.tmpLocale(Locale.ENGLISH);

  @Rule
  public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();

  @Test
  public void rootBundleBinding() {
    C10N.configure(new C10NConfigBase() {
      @Override
      public void configure() {
        bindBundle("c10n.testBundles.TestBundle");
      }
    });
    Labels labels = C10N.get(Labels.class);
    assertThat(labels.greeting(), is("Hello, World!"));
    assertThat(labels.argGreeting("C10N"), is("Hello, C10N!"));
  }

  @Test
  public void bundlesExplicitlyBoundToOtherClassesDoNotMatch() {
    C10N.configure(new C10NConfigBase() {
      @Override
      public void configure() {
        bindBundle("c10n.testBundles.TestBundle")
                .to(Buttons.class);
      }
    });
    Labels labels = C10N.get(Labels.class);
    assertThat(labels.greeting(), is("Labels.greeting"));

    Buttons buttons = C10N.get(Buttons.class);
    assertThat(buttons.ok(), is("OK!"));
  }

  @Test
  public void multiLanguageBundleBinding() {
    C10N.configure(new C10NConfigBase() {
      @Override
      public void configure() {
        bindBundle("c10n.testBundles.TestBundle");
      }
    });
    Labels labels = C10N.get(Labels.class);
    Buttons buttons = C10N.get(Buttons.class);

    Locale.setDefault(Locale.JAPANESE);
    assertThat(labels.greeting(), is("こんにちは世界!"));
    assertThat(labels.argGreeting("C10N"), is("こんにちはC10N!"));
    assertThat(buttons.ok(), is("はい"));

    Locale.setDefault(Locale.ENGLISH);
    assertThat(labels.greeting(), is("Hello, World!"));
    assertThat(labels.argGreeting("C10N"), is("Hello, C10N!"));
    assertThat(buttons.ok(), is("OK!"));
  }

  interface Labels {

    String greeting();

    String argGreeting(String who);
  }

  interface Buttons {
    String ok();
  }
}