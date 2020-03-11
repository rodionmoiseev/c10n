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

/**
 * @author rodion
 */
@SuppressWarnings({"serial", "UnusedDeclaration"})//rationale: public API
public class C10NConfigException extends C10NException {
    public C10NConfigException(String message) {
        super(message);
    }

    public C10NConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
