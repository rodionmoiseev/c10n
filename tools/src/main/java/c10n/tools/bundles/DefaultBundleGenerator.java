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

import java.lang.reflect.Method;
import java.util.Map;

import c10n.C10NDef;
import c10n.C10NMessages;
import c10n.share.utils.ReflectionUtils;
import c10n.tools.search.C10NInterfaceSearch;

public class DefaultBundleGenerator implements BundleGenerator {

	private final C10NInterfaceSearch search;
	
	public DefaultBundleGenerator(C10NInterfaceSearch search){
		this.search = search;
	}
	
	@Override
	public void generate(Class<?> clazz, Map<String, String> builder) {
		for (Method method : clazz.getDeclaredMethods()) {
			String val = null;
			C10NDef defVal = method.getAnnotation(C10NDef.class);
			if (null != defVal) {
				val = defVal.value();
			}
			builder.put(ReflectionUtils.getDefaultKey(clazz, method), val);
		}
	}

	@Override
	public void generateForPackage(String packagePrefix,
			Map<String, String> builder) {
		for(Class<?> c10nInterface : search.find(packagePrefix, C10NMessages.class)){
			generate(c10nInterface, builder);
		}
	}
}
