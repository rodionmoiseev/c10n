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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CustomImplementationBindingTest {
  @Rule
  public static TestRule tmpLocale = RuleUtils.tmpLocale();

  @Test
  public void localeBinding() {
    C10N.configure(new MyC10NConfigBase());
    Locale.setDefault(Locale.ENGLISH);
    Labels msg = C10N.get(Labels.class);
    assertThat(msg.label(), is(equalTo("English")));

    Locale.setDefault(Locale.JAPANESE);
    assertThat(msg.label(), is(equalTo("Japanese")));
  }
}

class MyC10NConfigBase extends C10NConfigBase {
  @Override
  public void configure() {
    bind(Labels.class)
            .to(LabelsEng.class, Locale.ENGLISH)
            .to(LabelsJapanese.class, Locale.JAPANESE);
  }
}

@C10NMessages
interface Labels {
  String label();
}

class LabelsEng implements Labels {
  @Override
  public String label() {
    return "English";
  }
}

class LabelsJapanese implements Labels {
  @Override
  public String label() {
    return "Japanese";
  }
}
