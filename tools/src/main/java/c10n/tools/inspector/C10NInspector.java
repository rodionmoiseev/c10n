/*
 * Licensed to the Apache Software Foundation (ASF) under one
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
 */

package c10n.tools.inspector;

import java.util.List;

/**
 * <p>A tool to discover all c10n enabled translations under the specified
 * list of package prefixes (searches recursively). The tool is desinged to
 * be useful for automated testing for missing translations or translations
 * that do not comply with team conventions.</p>
 * <p/>
 * <p/>
 * <p>Discovered translation units are populated with relevant data regarding
 * how the translation was declared, and for what locales translations are available.
 * See {@link C10NUnit} for more details.
 * </p>
 * <p/>
 * <p>Note that only interfaces marked with {@link c10n.C10NMessages} annotation
 * are currently detectable.</p>
 *
 * @author rodion
 * @since 1.1
 */
public interface C10NInspector {
    /**
     * <p>Perform the inspection for the given list of package prefixes.
     * Packages are searched recursively.</p>
     *
     * @param packagePrefixes list of packages to search under
     * @return a list of all detected translation units
     */
    List<C10NUnit> inspect(String... packagePrefixes);
}
