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

package com.github.rodionmoiseev.c10n;

import com.github.rodionmoiseev.c10n.formatters.ExtendedMessageFormatter;
import com.github.rodionmoiseev.c10n.formatters.NamedArg;
import org.junit.Test;

import java.util.HashMap;
import java.util.Locale;

import static com.github.rodionmoiseev.c10n.TestUtil.method;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExtendedMessageFormatterTest {
    private final ExtendedMessageFormatter fmt = new ExtendedMessageFormatter(new HashMap<String, String>() {{
        put("%n", "\n");
    }});

    private final Locale locale = Locale.ENGLISH;

    public interface ExtendedMsgFormatterTstMessages {
        @SuppressWarnings("unused")
        void noArgs();

        @SuppressWarnings("unused")
        void greet(@NamedArg("n") String name,
                   @NamedArg("s") String surname, int age);
    }

    @Test
    public void paramsWithoutArgsAreReplacedInOrder() throws Exception {
        assertThat(fmt.format(
                method(ExtendedMsgFormatterTstMessages.class, "greet"),
                "Hello {} {} of age {}!",
                locale,
                "rodion", "moiseev", 32),
                is(equalTo("Hello rodion moiseev of age 32!")));
    }

    @Test
    public void paramsWithIndexCanBeRepeated() throws Exception {
        assertThat(fmt.format(
                method(ExtendedMsgFormatterTstMessages.class, "greet"),
                "{0} {1} {0} {1} {2}",
                locale,
                "rodion", "moiseev", 32),
                is(equalTo("rodion moiseev rodion moiseev 32")));
    }

    @Test
    public void missingParamsAreIgnored() throws Exception {
        assertThat(fmt.format(
                method(ExtendedMsgFormatterTstMessages.class, "greet"),
                "Hello!",
                locale,
                "rodion", "moiseev", 32),
                is(equalTo("Hello!")));
    }

    @Test
    public void unknownParamsAreRenderedAsIs() throws Exception {
        assertThat(fmt.format(
                method(ExtendedMsgFormatterTstMessages.class, "greet"),
                "{abc}{-123}{123}",
                locale,
                "rodion", "moiseev", 32),
                is(equalTo("{abc}{-123}{123}")));
    }

    @Test
    public void nonIndexedArgsAreNotExpandedIntoIndexFormsIfNoArgsAreGivenForThatIndex() throws Exception {
        assertThat(fmt.format(
                method(ExtendedMsgFormatterTstMessages.class, "greet"),
                "Hello {}{}{}{}{}!",
                locale,
                "r", "m", 32),
                is(equalTo("Hello rm32{}{}!")));
    }

    @Test
    public void nullArgsBehavesAsNoArgs() throws Exception {
        assertThat(fmt.format(
                method(ExtendedMsgFormatterTstMessages.class, "noArgs"),
                "Hello {}!",
                locale,
                (Object[]) null),
                is(equalTo("Hello {}!")));
    }

    @Test
    public void customReplacementParams() throws Exception {
        assertThat(fmt.format(
                method(ExtendedMsgFormatterTstMessages.class, "noArgs"),
                "Hello new{%n}line!",
                locale,
                (Object[]) null),
                is(equalTo("Hello new\nline!")));
    }

    /*
     * Named params
     *
     * Note: these tests may require adding `-parameters` option to javac
     * when compiling in order to pass.
     */

    @Test
    public void testCanUseArgNamesProvidedArgumentNamesAreAvailable() throws Exception {
        assertThat(fmt.format(
                method(ExtendedMsgFormatterTstMessages.class, "greet"),
                "Hello {name} {surname} of age {age}!",
                locale,
                "rodion", "moiseev", 32),
                is(equalTo("Hello rodion moiseev of age 32!")));
    }

    @Test
    public void paramsCanBeAnnotatedForNameBasedReplacement() throws Exception {
        assertThat(fmt.format(
                method(ExtendedMsgFormatterTstMessages.class, "greet"),
                "Hello {n} {s} of age {age}!",
                locale,
                "rodion", "moiseev", 32),
                is(equalTo("Hello rodion moiseev of age 32!")));
    }
}
