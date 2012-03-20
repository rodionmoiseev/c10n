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

package c10n;

import java.util.Locale;

/**
 * <p>
 * Utility class for generating implementations of c10n message interfaces
 * annotated with &#64;{@link C10NMessages}.
 * </p>
 * <p/>
 * <p>
 * Usage similar to logger creating in frameworks like log4j or slf4j.
 * </p>
 * <p/>
 * <p>
 * Sample usage:
 * <p/>
 * <pre>
 *   -- MyMessages.java
 *     import c10n.C10NMessages;
 *
 *     &#64;C10NMessages
 *     public interface MyMessages {
 *
 *       &#64;C10NDef("Hello, {0}")
 *       String title(String who);
 *
 *       &#64;C10NDef("OK")
 *       String ok();
 *
 *       &#64;C10NDef("Cancel")
 *       String cancel();
 *     }
 *
 *   -- MyApplication.java
 *     import c10n.C10N;
 *     import javax.swing.*;
 *
 *     class MyApplication {
 *       private static final MyMessages msg = C10N.get(MyMessages.class);
 *
 *       public static void main(String[] args){
 *         JFrame frame = new JFrame(msg.title("World"));
 *         JButton okButton = new JButton(msg.ok());
 *         JButton cancelButton = new JButton(msg.cancel());
 *         ...
 *       }
 *     }
 * </pre>
 * <p/>
 * Implementation of <code>MyMessages</code> class is generated and will return
 * messages from configured bundle (TODO configuration).
 * </p>
 *
 * @author rodion
 */
public final class C10N {
  //DI
  private static final C10NCoreModule C_10_N_CORE_MODULE = new C10NCoreModule();
  private static C10NMsgFactory root = C_10_N_CORE_MODULE.defaultC10NMsgFactory();

  /**
   * Internal locale object used as a fallback when current locale does not
   * match any of the user-defined locale mappings.
   */
  public static final Locale FALLBACK_LOCALE = new Locale("c10n", "c10n",
          "c10n");

  public static C10NMsgFactory getRootFactory() {
    return root;
  }

  public static void setRootFactory(C10NMsgFactory newRoot) {
    root = newRoot;
  }

  public static <T> T get(Class<T> c10nInterface) {
    return root.get(c10nInterface);
  }

  public static void configure(C10NConfigBase conf) {
    root.configure(conf);
  }
}
