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

package c10n.guice;

import c10n.C10NDef;
import c10n.C10NMessages;
import com.google.inject.Guice;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GuiceLoaderTest {
  @Test
  public void guiceTest() {
    MyGuiceMessages msg = Guice.createInjector(C10NModule.scanAllPackages())
            .getInstance(MyGuiceMessages.class);
    assertThat(msg.greet(), is("Hello, Guice!"));
  }
}

@C10NMessages
interface MyGuiceMessages {
  @C10NDef("Hello, Guice!")
  String greet();
}
