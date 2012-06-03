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
import c10n.annotations.Ja;
import c10n.share.util.RuleUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 */
public class C10NFilterTest {
  @Rule
  public TestRule tmpC10N = RuleUtils.tmpC10NConfiguration();
  @Rule
  public TestRule tmpLocale = RuleUtils.tmpLocale();

  @Test
  public void builtInEnumFilter() {
    C10N.configure(new C10NConfigBase() {
      @Override
      protected void configure() {
        install(new DefaultC10NAnnotations());
        bindFilter(new EnumMappingFilterProvider<Status>(Status.class, StatusTr.class), Status.class);
      }
    });

    Locale.setDefault(Locale.ENGLISH);
    Messages msg = C10N.get(Messages.class);
    assertThat(msg.statusIs(Status.Open), is("status is: open"));
    assertThat(msg.statusIs(Status.Closed), is("status is: closed"));
    assertThat(msg.statusIs(Status.Pending), is("status is: pending"));

    Locale.setDefault(Locale.JAPANESE);
    assertThat(msg.statusIs(Status.Open), is("状態: 開"));
    assertThat(msg.statusIs(Status.Closed), is("状態: 閉"));
    assertThat(msg.statusIs(Status.Pending), is("状態: 進行中"));
  }

  @Test
  public void customFilterTest() {
    C10N.configure(new C10NConfigBase() {
      @Override
      protected void configure() {
        install(new DefaultC10NAnnotations());
        bindFilter(new CustomFilterProvider(), Status.class);
      }
    });

    Locale.setDefault(Locale.ENGLISH);
    Messages msg = C10N.get(Messages.class);
    assertThat(msg.statusIs(Status.Open), is("status is: open"));
    assertThat(msg.statusIs(Status.Closed), is("status is: closed"));
    assertThat(msg.statusIs(Status.Pending), is("status is: pending"));

    Locale.setDefault(Locale.JAPANESE);
    assertThat(msg.statusIs(Status.Open), is("状態: 開"));
    assertThat(msg.statusIs(Status.Closed), is("状態: 閉"));
    assertThat(msg.statusIs(Status.Pending), is("状態: 進行中"));
  }

  @Test
  @Ignore
  public void onlySpecifiedAnnotatedArgumentsGetTheFilterAppliedToThem() {
    C10N.configure(new C10NConfigBase() {
      @Override
      protected void configure() {
        install(new DefaultC10NAnnotations());
        bindFilter(new EnumMappingFilterProvider<Status>(Status.class, StatusTr.class), Status.class)
                .annotatedWith(C10NEnum.class);
      }
    });

    Locale.setDefault(Locale.ENGLISH);
    Messages msg = C10N.get(Messages.class);
    assertThat(msg.statusIs(Status.Open), is("status is: open"));
    assertThat(msg.statusIs2(Status.Open, Status.Closed), is("Open-closed"));

    Locale.setDefault(Locale.JAPANESE);
    assertThat(msg.statusIs(Status.Closed), is("状態: 開"));
    assertThat(msg.statusIs2(Status.Closed, Status.Open), is("Closed-開"));
  }

  private static final class CustomFilterProvider implements C10NFilterProvider<Status> {
    @Override
    public C10NFilter<Status> get() {
      return new StatusFilter();
    }
  }

  private static final class StatusFilter implements C10NFilter<Status> {
    private final StatusTr statusTr = C10N.get(StatusTr.class);

    @Override
    public Object apply(Status arg) {
      switch (arg) {
        case Open:
          return statusTr.open();
        case Closed:
          return statusTr.closed();
        case Pending:
          return statusTr.status_pending();
        default:
          throw new IllegalArgumentException("Unexpected status type: " + arg);
      }
    }
  }

  private static abstract class MapFilter<T> implements C10NFilter<T> {
    public abstract Map<T, String> mapping();

    @Override
    public Object apply(T arg) {
      return mapping().get(arg);
    }
  }

  private static final class MapStatusFilter extends MapFilter<Status> {
    private final StatusTr statusTr = C10N.get(StatusTr.class);

    @Override
    public Map<Status, String> mapping() {
      Map<Status, String> map = new HashMap<Status, String>();
      map.put(Status.Open, statusTr.open());
      return map;
    }

    @Override
    public Object apply(Status arg) {
      switch (arg) {
        case Open:
          return statusTr.open();
        case Closed:
          return statusTr.closed();
        case Pending:
          return statusTr.status_pending();
        default:
          throw new IllegalArgumentException("Unexpected status type: " + arg);
      }
    }
  }

  private static final class EnumMappingFilterProvider<E extends Enum<?>> implements C10NFilterProvider<E> {
    private final Class<E> enumClass;
    private final Class<?> c10nMappedInterface;

    private EnumMappingFilterProvider(Class<E> enumClass, Class<?> c10nMappedInterface) {
      this.enumClass = enumClass;
      this.c10nMappedInterface = c10nMappedInterface;
    }

    @Override
    public C10NFilter<E> get() {
      return new EnumMappingFilter<E>(enumClass, c10nMappedInterface);
    }
  }

  private static final class EnumMappingFilter<E extends Enum<?>> implements C10NFilter<E> {
    private final Class<?> enumC10NInterface;
    private final Object enumC10NInterfaceInstance;
    private final Map<Enum<?>, Method> c10nInfMethodMapping;

    EnumMappingFilter(Class<E> enumClass, Class<?> c10nInterface) {
      this.enumC10NInterface = c10nInterface;
      this.enumC10NInterfaceInstance = C10N.get(enumC10NInterface);
      this.c10nInfMethodMapping = genMapping(enumClass, enumC10NInterface);
    }

    private static <E extends Enum<?>> Map<Enum<?>, Method> genMapping(Class<E> enumClass, Class<?> enumC10NInterface) {
      Map<String, Method> allMethods = new HashMap<String, Method>();
      for (Method m : enumC10NInterface.getMethods()) {
        allMethods.put(m.getName().toLowerCase(), m);
      }

      Map<Enum<?>, Method> res = new HashMap<Enum<?>, Method>();

      for (Enum<?> enumValue : enumClass.getEnumConstants()) {
        //1. Check of methods for pattern: ClassName_EnumValue()
        Method m = allMethods.get(enumClass.getSimpleName().toLowerCase() + "_" + enumValue.name().toLowerCase());
        if (null == m || !noArgMethod(m) || !returnsObject(m)) {
          //no good ...
          //2. Check for methods for pattern: EnumValue()
          m = allMethods.get(enumValue.name().toLowerCase());
          if (null == m || !noArgMethod(m) || !returnsObject(m)) {
            throw new IllegalStateException("method mapping for " +
                    enumClass.getSimpleName() + "." + enumValue.name() + " was not found!!");
          }
        }
        res.put(enumValue, m);
      }
      return res;
    }

    private static boolean returnsObject(Method m) {
      return !m.getReturnType().equals(Void.TYPE);
    }

    private static boolean noArgMethod(Method m) {
      Class[] paramTypes = m.getParameterTypes();
      return paramTypes == null || paramTypes.length == 0;
    }

    @Override
    public Object apply(E arg) {
      Method m = c10nInfMethodMapping.get(arg);
      try {
        return m.invoke(enumC10NInterfaceInstance);
      } catch (Exception e) {
        throw new RuntimeException("Failed to dispatch invocation to " +
                m.getDeclaringClass().getSimpleName() + "." + m.getName() + "() method.", e);
      }
    }
  }

  enum Status {
    Open,
    Closed,
    Pending
  }

  interface Messages {
    @En("status is: {0}")
    @Ja("状態: {0}")
    String statusIs(@C10NEnum Status status);

    @En("{0}-{1}")
    @Ja("{0}-{1}")
    String statusIs2(Status status, @C10NEnum Status annotatedStatus);
  }

  public interface StatusTr {
    @En("open")
    @Ja("開")
    String open();

    @En("closed")
    @Ja("閉")
    String closed();

    @En("pending")
    @Ja("進行中")
    String status_pending();
  }
}
