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

package c10n;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author rodion
 */
public class C10NFiltersTest {
    @Test
    public void cachedFilterProviderOnlyInstantiatesFilterOnce() {
        MyFilterProvider mfp = new MyFilterProvider();
        C10NFilterProvider<Object> cfp = C10NFilters.cachedFilterProvider(mfp);
        assertThat(mfp.called, is(0));
        C10NFilter<Object> mf = cfp.get();
        assertThat(mfp.called, is(1));
        cfp.get();
        cfp.get();

        //delegation only happens once
        assertThat(mfp.called, is(1));
        //the same object is always returned
        assertThat(cfp.get(), is(sameInstance(mf)));
    }

    private static final class MyFilterProvider implements C10NFilterProvider<Object> {
        int called = 0;

        @Override
        public C10NFilter<Object> get() {
            called++;
            return new MyFilter();
        }
    }

    private static final class MyFilter implements C10NFilter<Object> {
        @Override
        public Object apply(Object arg) {
            return arg;
        }
    }
}
