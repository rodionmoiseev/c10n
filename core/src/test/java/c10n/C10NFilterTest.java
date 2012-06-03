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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
        bindFilter(C10NFilters.enumMapping(Status.class, StatusTr.class), Status.class);
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
  public void onlySpecifiedAnnotatedArgumentsGetTheFilterAppliedToThem() {
    C10N.configure(new C10NConfigBase() {
      @Override
      protected void configure() {
        install(new DefaultC10NAnnotations());
        bindFilter(C10NFilters.enumMapping(Status.class, StatusTr.class), Status.class)
                .annotatedWith(C10NEnum.class);
      }
    });

    Locale.setDefault(Locale.ENGLISH);
    Messages msg = C10N.get(Messages.class);
    assertThat(msg.statusIs(Status.Open), is("status is: open"));
    assertThat(msg.statusIs2(Status.Open, Status.Closed), is("Open-closed"));

    Locale.setDefault(Locale.JAPANESE);
    assertThat(msg.statusIs(Status.Closed), is("状態: 閉"));
    assertThat(msg.statusIs2(Status.Closed, Status.Open), is("Closed-開"));
  }

  @Test
  public void primitiveTypeFilters() {
    C10N.configure(new C10NConfigBase() {
      @Override
      protected void configure() {
        install(new DefaultC10NAnnotations());
        bindFilter(new IntFormattingFilter(), int.class).annotatedWith(Precise.class);
      }
    });

    Locale.setDefault(Locale.ENGLISH);
    Messages msg = C10N.get(Messages.class);
    assertThat(msg.primitiveTypeMapping(1, 2), is("precise 1.00 normal 2"));

    Locale.setDefault(Locale.JAPANESE);
    assertThat(msg.primitiveTypeMapping(1, 2), is("1.00 - 2"));
  }

  @Test
  public void multipleAnnotationTest() {
    C10N.configure(new C10NConfigBase() {
      @Override
      protected void configure() {
        install(new DefaultC10NAnnotations());
        bindFilter(C10NFilters.enumMapping(Status.class, StatusTr.class), Status.class)
                .annotatedWith(C10NEnum.class)
                .annotatedWith(Precise.class);
      }
    });

    Locale.setDefault(Locale.ENGLISH);
    Messages msg = C10N.get(Messages.class);
    assertThat(msg.statusIs2(Status.Open, Status.Closed), is("Open-closed"));
    assertThat(msg.multipleAnnotations(Status.Pending), is("pending"));

    Locale.setDefault(Locale.JAPANESE);
    assertThat(msg.statusIs2(Status.Closed, Status.Open), is("Closed-開"));
    assertThat(msg.multipleAnnotations(Status.Pending), is("進行中"));
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

    @En("precise {0} normal {1}")
    @Ja("{0} - {1}")
    String primitiveTypeMapping(@Precise int precise, int normal);

    @En("{0}")
    @Ja("{0}")
    String multipleAnnotations(@Precise Status status);
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

  public static final class IntFormattingFilter implements C10NFilter<Integer> {
    @Override
    public Object apply(Integer arg) {
      return String.valueOf(arg) + ".00";
    }
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Precise {
  }
}
