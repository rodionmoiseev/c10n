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

package com.github.rodionmoiseev.c10n.test.utils;

import com.github.rodionmoiseev.c10n.C10N;
import com.github.rodionmoiseev.c10n.C10NConfigBase;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;

import java.util.Locale;

public class RuleUtils {
    private static final C10NConfigBase emptyConfig = new C10NConfigBase() {
        @Override
        protected void configure() {
        }
    };

    public static TestRule tmpC10NConfiguration() {
        return new TmpC10NConfiguration(emptyConfig);
    }

    public static TestRule tmpC10NConfiguration(C10NConfigBase config) {
        return new TmpC10NConfiguration(config);
    }

    public static TestRule tmpLocale() {
        return new TmpLocale(null);
    }

    public static TestRule tmpLocale(Locale tmpLocale) {
        return new TmpLocale(tmpLocale);
    }

    public static UsingTmpDir tmpDir(String testDirName) {
        return new UsingTmpDir(testDirName);
    }

    public static UsingTmpDir tmpDir(Class<?> clazz) {
        return new UsingTmpDir(clazz);
    }

    public static UsingTmpDir tmpDir() {
        return new UsingTmpDir();
    }

    private static final class TmpC10NConfiguration extends ExternalResource {
        private final C10NConfigBase config;

        TmpC10NConfiguration(C10NConfigBase config) {
            this.config = config;
        }

        @Override
        protected void after() {
            C10N.configure(config);
        }
    }

    private static final class TmpLocale extends ExternalResource {
        private Locale oldLocale = null;
        private final Locale tmpLocale;

        TmpLocale(Locale tmpLocale) {
            this.tmpLocale = tmpLocale;
        }

        @Override
        protected void before() throws Throwable {
            oldLocale = Locale.getDefault();
            if (null != tmpLocale) {
                Locale.setDefault(tmpLocale);
            }
        }

        @Override
        protected void after() {
            Locale.setDefault(oldLocale);
        }
    }
}
