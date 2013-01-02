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

package c10n.tools.inspector.test1;

import c10n.C10NKey;
import c10n.C10NMessages;
import c10n.annotations.En;
import c10n.annotations.Ja;

/**
 * @author rodion
 * @since 1.1
 */
@C10NKey("scope.class")
@C10NMessages
public interface ClassScopeKey {
    /*
     * key: scope.class.both
     */
    String both();

    /*
     * key: scope.class.bothInAnnotationAndInBundle
     */
    @En("[en]@annotation ClassScopeKey.bothInAnnotationAndInBundle")
    @Ja("[ja]@annotation ClassScopeKey.bothInAnnotationAndInBundle")
    String bothInAnnotationAndInBundle();

    /*
     * key: scope.class.bothInBundleWithParams_Integer_byte
     */
    String bothInBundleWithParams(Integer param1, byte param2);
}
