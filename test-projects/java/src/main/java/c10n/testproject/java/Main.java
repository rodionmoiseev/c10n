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

package c10n.testproject.java;

import c10n.C10N;
import c10n.C10NConfigBase;
import c10n.annotations.DefaultC10NAnnotations;
import c10n.annotations.En;

import java.util.Locale;

public class Main {
  public static void main(String... args) {
    /*
     * Configure C10N
     */
    C10N.configure(new C10NConfigBase() {
      @Override
      protected void configure() {
        install(new DefaultC10NAnnotations());
        bindAnnotation(En.class); //also bind En as fallback
      }
    });

    /*
     * Parse arguments
     */
    boolean showHelp = false;
    Locale locale = Locale.ENGLISH;
    String action = "none";
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.equals("--help")) {
        showHelp = true;
      } else if (arg.equals("--locale")) {
        i++;
        locale = parseLocale(args[i]);
      } else if (arg.equals("--action")){
        i++;
        action = args[i];
      }
    }

    //Set the locale
    Locale.setDefault(locale);

    /*
     * Act upon arguments
     */
    if(showHelp){
      System.out.println(C10N.get(Usage.class).usage());
    }else if(action.equals("action1")){
      action1();
    }else{
      System.out.println(C10N.get(Errors.class).noAction());
    }
  }

  private static void action1() {
    //TODO
  }

  private static Locale parseLocale(String localeStr) {
    String[] s = localeStr.split("_", 3);
    switch (s.length) {
      case 1:
        return new Locale(s[0]);
      case 2:
        return new Locale(s[0], s[1]);
      default:
        return new Locale(s[0], s[1], s[2]);
    }
  }
}  
