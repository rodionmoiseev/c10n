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

import c10n.share.ShareModule;

import java.util.Locale;

/**
 * @author rodion
 */
public class C10NCoreModule {
  public static final LocaleProvider defaultLocaleProvider = new DefaultLocaleProvider();
  //DI
  private final ShareModule shareModule = new ShareModule();

  public C10NMsgFactory defaultC10NMsgFactory() {
    return new DefaultC10NMsgFactory(shareModule.defaultLocaleMapping());
  }

  /**
   * <p>Locale provider that always delegates to
   * {@link java.util.Locale#getDefault()}.</p>
   * <p>The value may be changed by calling {@link Locale#setDefault(java.util.Locale)}</p>
   *
   * @return current locale
   */
  public LocaleProvider defaultLocaleProvider() {
    return defaultLocaleProvider;
  }

  private static final class DefaultLocaleProvider implements LocaleProvider{
    @Override
    public Locale getLocale() {
      return Locale.getDefault();
    }
  }
}
