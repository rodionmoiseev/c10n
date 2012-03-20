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

package c10n.share.utils;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;

public final class ReflectionUtils {
  public static String getDefaultKey(Class<?> clazz, Method method) {
    StringBuilder sb = new StringBuilder();
    getDefaultKey(clazz, method, sb);
    return sb.toString();
  }

  public static void getDefaultKey(Class<?> clazz, Method method, StringBuilder sb) {
    getFQNString(clazz, sb);
    sb.append('.').append(method.getName());

    Class<?>[] params = method.getParameterTypes();
    if (params.length > 0) {
      sb.append('_');
      for (int i = 0; i < params.length; i++) {
        sb.append(params[i].getSimpleName());
        if (i + 1 < params.length) {
          sb.append("_");
        }
      }
    }
  }

  public static String getFQNString(Class<?> clazz) {
    StringBuilder sb = new StringBuilder();
    getFQNString(clazz, sb);
    return sb.toString();
  }

  public static void getFQNString(Class<?> clazz, StringBuilder sb) {
    sb.append(clazz.getPackage().getName()).append('.');
    getClassFQNString(clazz, sb, '.');
  }

  private static void getClassFQNString(Class<?> clazz, StringBuilder sb, char delim) {
    Class<?> parent = clazz;
    LinkedList<String> typeHierarchy = new LinkedList<String>();
    do {
      typeHierarchy.add(parent.getSimpleName());
    } while ((parent = parent.getEnclosingClass()) != null);

    Iterator<String> it = typeHierarchy.descendingIterator();
    while (it.hasNext()) {
      sb.append(it.next());
      if (it.hasNext()) {
        sb.append(delim);
      }
    }
  }
}
