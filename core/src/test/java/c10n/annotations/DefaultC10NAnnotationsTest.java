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

package c10n.annotations;

import c10n.C10N;
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
public class DefaultC10NAnnotationsTest {
  @Rule
  public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();
  @Rule
  public TestRule tmpLocale = RuleUtils.tmpLocale();
  private Msg msg = null;

  @Before
  public void setupDefaultAnnotations(){
    C10N.configure(new DefaultC10NAnnotations());
    msg = C10N.get(Msg.class);
  }

  @Test
  public void testDefaultLocales(){
    assertMsgForLocaleIs(Locale.GERMAN, "de");
    assertMsgForLocaleIs(Locale.ENGLISH, "en");
    assertMsgForLocaleIs(Locale.FRENCH, "fr");
    assertMsgForLocaleIs(Locale.ITALIAN, "it");
    assertMsgForLocaleIs(Locale.JAPANESE, "ja");
    assertMsgForLocaleIs(Locale.KOREAN, "ko");
    assertMsgForLocaleIs(new Locale("ru"), "ru");
    assertMsgForLocaleIs(Locale.CHINESE, "zh");
  }

  private void assertMsgForLocaleIs(Locale locale, String expectedMsg){
    Locale.setDefault(locale);
    assertThat(msg.msg(), is(expectedMsg));
  }

  interface Msg{
    @De("de")
    @En("en")
    @Fr("fr")
    @It("it")
    @Ja("ja")
    @Ko("ko")
    @Ru("ru")
    @Zh("zh")
    String msg();
  }
}
