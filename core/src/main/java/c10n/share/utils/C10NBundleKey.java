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

package c10n.share.utils;

import static c10n.share.utils.Preconditions.assertNotNull;

/**
 * @author rodion
 */
public class C10NBundleKey {
    private final boolean customKey;
    private final String key;
    private final String declaredKey;

    public C10NBundleKey(boolean customKey, String key, String declaredKey) {
        assertNotNull(key, "key");
        this.customKey = customKey;
        this.key = key;
        this.declaredKey = declaredKey;
    }

    /**
     * <p>Determines whether the key was automatically generated
     * by c10n, or customised by the user using
     * the {@link c10n.C10NKey} annotation, either on one of the
     * parent classes, or the method directly, or both.</p>
     *
     * @return If key is customised, <code>true</code>, else <code>false</code>.
     */
    public boolean isCustomKey() {
        return customKey;
    }

    /**
     * <p>The bundle key used for retrieval from the bundle.</p>
     *
     * @return bundle key(not null)
     */
    public String getKey() {
        return key;
    }

    /**
     * <p>The bundle key as it appeared in the {@link c10n.C10NKey} annotation
     * value. If annotation was not specified, returns <code>null</code></p>
     *
     * @return value of the {@link c10n.C10NKey} annotation, or <code>null</code> if
     *         none was declared.
     */
    public String getDeclaredKey() {
        return declaredKey;
    }

    @SuppressWarnings("RedundantIfStatement")//rationale: generated code
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        C10NBundleKey that = (C10NBundleKey) o;

        if (customKey != that.customKey) return false;
        if (declaredKey != null ? !declaredKey.equals(that.declaredKey) : that.declaredKey != null) return false;
        if (!key.equals(that.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (customKey ? 1 : 0);
        result = 31 * result + key.hashCode();
        result = 31 * result + (declaredKey != null ? declaredKey.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "C10NBundleKey{" +
                "customKey=" + customKey +
                ", key='" + key + '\'' +
                ", declaredKey='" + declaredKey + '\'' +
                '}';
    }
}
