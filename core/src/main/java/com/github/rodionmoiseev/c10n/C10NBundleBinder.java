/*
 * Copyright 2012 Rodion Moiseev (https://github.com/rodionmoiseev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rodionmoiseev.c10n;

import java.util.ArrayList;
import java.util.List;

public class C10NBundleBinder {
    private final String charsetName;
    private final List<Class<?>> boundInterfaces = new ArrayList<Class<?>>();

    public C10NBundleBinder(String charsetName) {
        this.charsetName = charsetName;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public void to(Class<?> c10nInterface) {
        boundInterfaces.add(c10nInterface);
    }

    List<Class<?>> getBoundInterfaces() {
        return boundInterfaces;
    }
}
