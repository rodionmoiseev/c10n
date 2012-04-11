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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FallbackC10NFactoryTest {
  @Test
  public void messageDeclaredInAnnotationIsFallenBackOnto() {
    assertThat(C10N.get(WithFallback.class).message(),
            is("Fallback message"));
  }

  @Test
  public void methodsInSuperInterfacesAreVisible() {
    SubInterfaceWithFallback msg = C10N
            .get(SubInterfaceWithFallback.class);
    assertThat(msg.message(), is("Fallback message"));
    assertThat(msg.msg2(), is("msg2"));
  }

  @Test
  public void methodsWithoutDefaultValuesDefaultToMethodName() {
    NoFallback msg = C10N.get(NoFallback.class);
    assertThat(msg.noDefaultValue(), is("NoFallback.noDefaultValue"));
  }

  @Test
  public void messagesCanTakeArgumentsInMsgFormat() {
    WithArguments msg = C10N.get(WithArguments.class);
    assertThat(msg.greet("World"), is("Hello, World!"));
    assertThat(msg.noDefaultValue("value", "value2"),
            is("WithArguments.noDefaultValue(\"value\", \"value2\")"));
  }
}

interface WithFallback {
  @C10NDef("Fallback message")
  String message();
}

interface SubInterfaceWithFallback extends WithFallback {
  @C10NDef("msg2")
  String msg2();
}

interface NoFallback {
  String noDefaultValue();
}

interface WithArguments {
  @C10NDef("Hello, {0}!")
  String greet(String who);

  String noDefaultValue(Object arg, Object arg2);
}