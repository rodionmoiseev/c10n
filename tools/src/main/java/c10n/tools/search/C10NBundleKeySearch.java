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

package c10n.tools.search;

import c10n.share.utils.C10NBundleKey;

/**
 * <p>Extracts all c10n bundle keys for a given set of
 * c10n interface classes.</p>
 *
 * @author rodion
 */
public interface C10NBundleKeySearch {
    /**
     * <p>Retrieve all c10n bundle keys for inspection, from the current classpath.
     * Only the packages starting with the given package prefix(es) will be included
     * in search results.</p>
     * <p>Packages will be searched recursively into sub-packages, for each prefix.</p>
     *
     * @param packagePrefixes prefixes of packages to search (not null)
     * @return a collection of all matching bundle keys (not null)
     */
    Iterable<C10NBundleKey> findAllKeys(String... packagePrefixes);
}