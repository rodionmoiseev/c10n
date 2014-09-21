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

package com.github.rodionmoiseev.c10n.test.utils;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.rules.ExternalResource;

import java.io.File;

/**
 * @author rodion
 */
public class UsingTmpDir extends ExternalResource {
    public final File dir;

    public static File systemTmpDir(String childFolder) {
        return new File(System.getProperty("java.io.tmpdir"), childFolder);
    }

    public UsingTmpDir() {
        this(systemTmpDir("JavaTest"));
    }

    public UsingTmpDir(String testDirName) {
        this(systemTmpDir(testDirName));
    }

    public UsingTmpDir(Class<?> clazz) {
        this(systemTmpDir(clazz.getSimpleName()));
    }

    public UsingTmpDir(File tmpDir) {
        this.dir = tmpDir;
    }

    @Before
    @Override
    protected void before() throws Throwable {
        FileUtils.forceMkdir(dir);
    }

    @After
    @Override
    protected void after() {
        super.after();
    }
}
