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

package c10n.tools.bundles;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import c10n.C10NDef;
import c10n.tools.search.C10NInterfacesSearches;

public class DefaultBundleGeneratorTest {
	private final BundleGenerator bg = new DefaultBundleGenerator(
			C10NInterfacesSearches.reflectionsSearch());

	@Test
	public void simpleKeyGenerator() {
		Map<String, String> builder = new HashMap<String, String>();
		bg.generate(Buttons.class, builder);
		assertThat(
				builder,
				is(map("c10n.tools.bundles.DefaultBundleGeneratorTest.Buttons.ok",
						null,//
						"c10n.tools.bundles.DefaultBundleGeneratorTest.Buttons.cancel",
						"Cancel")));
	}

	@Test
	public void generateFromAllC10NInterfaces() {
		Map<String, String> builder = new HashMap<String, String>();
		bg.generateForPackage("c10n.tools.search.test1", builder);
		assertThat(
				builder,
				is(map("c10n.tools.search.test1.Buttons.cancel",
						"Cancel",//
						"c10n.tools.search.test1.Buttons.ok",
						"OK",//
						"c10n.tools.search.test1.labels.Labels1.label1",
						"label 1",//
						"c10n.tools.search.test1.labels.Labels1.oops",
						// no default generates null
						null,//
						"c10n.tools.search.test1.labels.Labels2.label2",
						"label 2",//
						"c10n.tools.search.test1.Window.author", "rodion",//
						"c10n.tools.search.test1.Window.title", "Test01")));
	}

	private static final Map<String, String> map(String... args) {
		Map<String, String> res = new HashMap<String, String>();
		for (int i = 0; i < args.length; i += 2) {
			res.put(args[i], args[i + 1]);
		}
		return res;
	}

	interface Buttons {
		String ok();

		@C10NDef("Cancel")
		String cancel();
	}
}
