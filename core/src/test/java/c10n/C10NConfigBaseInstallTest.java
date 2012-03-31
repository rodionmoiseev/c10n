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


import c10n.annotations.DefaultC10NAnnotations;
import c10n.annotations.En;
import c10n.share.util.RuleUtils;
import c10n.share.utils.ReflectionUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 */
public class C10NConfigBaseInstallTest {
  @Rule
  public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();
  @Rule
  public TestRule tmpLocale = RuleUtils.tmpLocale();
  
  @Test
  public void bundleSettingsAreComposedWithInstalledModules() {
    final C10NConfigBase childConfig = new C10NConfigBase() {
      @Override
      public void configure() {
        bindBundle(ChildResourceBundle.class.getName());
      }
    };

    C10NConfigBase config = new C10NConfigBase() {
      @Override
      public void configure() {
        install(childConfig);
        bindBundle(ParentResourceBundle.class.getName());
      }
    };
    config.doConfigure();

    List<ResourceBundle> b = config.getBundlesForLocale(C10NConfigBaseInstallTest.class, Locale.ENGLISH);
    assertThat(b.size(), is(2));
    assertThat(b.get(0).getString("parentKey"), is("parentBundle"));
    assertThat(b.get(1).getString("childKey"), is("childBundle"));

    C10N.configure(config);
    assertThat(C10N.get(Messages.class).greeting(), is("parentHello"));
    assertThat(C10N.get(ChildMessages.class).greeting(), is("childHello"));
  }

  @Test
  public void annotationSettingsAreComposedWithInstalledModules() {
    final C10NConfigBase childConfig = new C10NConfigBase() {
      @Override
      public void configure() {
        bindAnnotation(Jp.class).toLocale(Locale.JAPANESE);
      }
    };

    C10NConfigBase config = new C10NConfigBase() {
      @Override
      public void configure() {
        install(childConfig);
        bindAnnotation(Eng.class).toLocale(Locale.ENGLISH);
      }
    };

    C10N.configure(config);
    Locale.setDefault(Locale.ENGLISH);
    assertThat(C10N.get(Messages.class).greeting(), is("hello"));
    Locale.setDefault(Locale.JAPANESE);
    assertThat(C10N.get(ChildMessages.class).greeting(), is("こんにちは"));
  }
  
  @Test
  public void annotationsCanBeAdditionallyBoundAsAFallbackLocale(){
      C10N.configure(new C10NConfigBase(){
        @Override
        protected void configure() {
          install(new DefaultC10NAnnotations());
          //additionally bind En as fallback
          bindAnnotation(En.class);
        }
      });

      Locale.setDefault(Locale.JAPANESE);
      assertThat(C10N.get(EnOnlyMessages.class).value(), is("fallback here"));
  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Eng {
    String value();
  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Jp {
    String value();
  }

  interface Messages {
    @Eng("hello")
    String greeting();
  }

  interface ChildMessages {
    @Jp("こんにちは")
    String greeting();
  }

  interface EnOnlyMessages{
    @En("fallback here")
    String value();
  }

  public static final class ParentResourceBundle extends ResourceBundle {
    @Override
    protected Object handleGetObject(String key) {
      if ("parentKey".equals(key)) {
        return "parentBundle";
      } else if (keyFor(Messages.class, "greeting").equals(key)) {
        return "parentHello";
      }
      return null;
    }

    @Override
    public Enumeration<String> getKeys() {
      return Collections.enumeration(Arrays.asList("parentKey", keyFor(Messages.class, "greeting")));
    }
  }

  public static final class ChildResourceBundle extends ResourceBundle {
    @Override
    protected Object handleGetObject(String key) {
      if ("childKey".equals(key)) {
        return "childBundle";
      } else if (keyFor(ChildMessages.class, "greeting").equals(key)) {
        return "childHello";
      }
      return null;
    }

    @Override
    public Enumeration<String> getKeys() {
      return Collections.enumeration(Arrays.asList("childKey", keyFor(ChildMessages.class, "greeting")));
    }
  }

  private static String keyFor(Class<?> clazz, String methodName) {
    try {
      return ReflectionUtils.getDefaultKey(clazz, clazz.getMethod(methodName));
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
