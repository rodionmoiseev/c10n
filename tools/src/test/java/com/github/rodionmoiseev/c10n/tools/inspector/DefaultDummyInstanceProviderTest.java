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

package com.github.rodionmoiseev.c10n.tools.inspector;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author rodion
 * @since 1.1
 */
public class DefaultDummyInstanceProviderTest {
    private final DummyInstanceProvider prov = new DefaultDummyInstanceProvider();

    @Test
    public void primitiveTypeTest() {
        assertThat((Byte) prov.getInstance(null, null, byte.class, 1), is((byte) 1));
        assertThat((Byte) prov.getInstance(null, null, Byte.class, 1), is(Byte.valueOf((byte) 1)));
        assertThat((Short) prov.getInstance(null, null, short.class, 2), is((short) 2));
        assertThat((Short) prov.getInstance(null, null, Short.class, 2), is(Short.valueOf((short) 2)));
        assertThat((Integer) prov.getInstance(null, null, int.class, 3), is(3));
        assertThat((Integer) prov.getInstance(null, null, Integer.class, 3), is(Integer.valueOf(3)));
        assertThat((Long) prov.getInstance(null, null, long.class, 4), is(4L));
        assertThat((Long) prov.getInstance(null, null, Long.class, 4), is(Long.valueOf(4L)));
        assertThat((Float) prov.getInstance(null, null, float.class, 5), is(5f));
        assertThat((Float) prov.getInstance(null, null, Float.class, 5), is(Float.valueOf(5f)));
        assertThat((Double) prov.getInstance(null, null, double.class, 6), is(6d));
        assertThat((Double) prov.getInstance(null, null, Double.class, 6), is(Double.valueOf(6d)));
        assertThat((Character) prov.getInstance(null, null, char.class, 7), is('7'));
        assertThat((Character) prov.getInstance(null, null, Character.class, 7), is(Character.valueOf('7')));
        assertThat((Boolean) prov.getInstance(null, null, boolean.class, 8), is(false));
        assertThat((Boolean) prov.getInstance(null, null, Boolean.class, 8), is(Boolean.FALSE));
    }

    @Test
    public void stringAndCharSequenceTypes() {
        assertThat((String) prov.getInstance(null, null, String.class, 9), is("{9}"));
        assertThat((CharSequence) prov.getInstance(null, null, CharSequence.class, 10), is((CharSequence) "{10}"));
    }

    @Test
    public void returnsNullForUnknownObjectTypes() {
        assertThat(prov.getInstance(null, null, DefaultDummyInstanceProviderTest.class, 123), is(nullValue()));
    }
}
