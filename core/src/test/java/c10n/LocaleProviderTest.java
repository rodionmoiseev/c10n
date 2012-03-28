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

import c10n.annotations.En;
import c10n.annotations.Ja;
import c10n.annotations.Ru;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 */
public class LocaleProviderTest {
  @Test
  public void defaultLocaleIsFetchedFromTheGivenLocaleProvider() {
    final MyLocaleProvider lc = new MyLocaleProvider();
    C10N.configure(new C10NConfigBase() {
      @Override
      protected void configure() {
        bindLocaleProvider(lc);
      }
    });

    Buttons msg = C10N.get(Buttons.class);
    lc.locale = new Locale("ru");
    assertThat(msg.ok(), is(equalTo("Ugu")));
    lc.locale = Locale.ENGLISH;
    assertThat(msg.ok(), is(equalTo("OK")));
    lc.locale = Locale.JAPANESE;
    assertThat(msg.ok(), is(equalTo("Hai")));
  }

  private interface Buttons{
    @En("OK")
    @Ru("Ugu")
    @Ja("Hai")
    String ok();
  }

  private static final class MyLocaleProvider implements LocaleProvider {
    public Locale locale = Locale.getDefault();

    @Override
    public Locale getLocale() {
      return locale;
    }
  }
}
