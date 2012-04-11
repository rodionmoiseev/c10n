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

import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 */
public class UntranslatedMessageHandlerTest {
  private final UntranslatedMessageHandler h = new DefaultUntranslatedMessageHandler();

  @Test
  public void noArgs() {
    assertThat(h.render(Messages.class, noArgsMethod(), new Object[0]), is("Messages.noArgs"));
  }

  @Test
  public void withArgs() {
    assertThat(h.render(Messages.class, withArgsMethod(), new Object[]{"hello", 123}),
            is("Messages.withArgs(\"hello\", 123)"));
  }

  @Test
  public void emptyStringArgumentCheck() {
    assertThat(h.render(Messages.class, withArgsMethod(), new Object[]{"", -123}),
            is("Messages.withArgs(\"\", -123)"));
  }

  @Test
  public void longArgumentValuesGetTruncatedTo10Chars() {
    assertThat(h.render(Messages.class, withArgsMethod(), new Object[]{"a very long text message", 123}),
            is("Messages.withArgs(\"a very lon...\", 123)"));
  }

  private Method noArgsMethod() {
    try {
      return Messages.class.getMethod("noArgs");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private Method withArgsMethod() {
    try {
      return Messages.class.getMethod("withArgs", new Class[]{String.class, Integer.class});
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  interface Messages {
    String noArgs();

    String withArgs(String str, Integer i);
  }
}
