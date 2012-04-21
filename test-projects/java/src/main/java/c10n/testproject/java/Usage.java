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

import c10n.annotations.En;
import c10n.annotations.Ja;
import c10n.annotations.Ru;

/**
 * @author rodion
 */
public interface Usage {
  @En("C10N simple java test project\n" +
          "Synopsis:\n" +
          "\t<Main> [options]\n" +
          "Options:\n" +
          "\t--locale\t\tUse the specified locale\n" +
          "\t--action\t\tRun the specified action\n" +
          "\t--help\t\tShow this help message and exit\n")
  @Ja("C10N邁｡譏笛ava繝�せ繝医⊃繝励Ο繧ｸ繧ｧ繧ｯ繝�n" +
          "讎りｦ�\n" +
          "\t<Main> [options]\n" +
          "繧ｪ繝励す繝ｧ繝ｳ:\n" +
          "\t--locale\t\t謖�ｮ壹�繝ｭ繧ｱ繝ｼ繧九ｒ菴ｿ逕ｨ縺吶ｋ\n" +
          "\t--action\t\t謖�ｮ壹�繧｢繧ｯ繧ｷ繝ｧ繝ｳ繧貞ｮ溯｡後☆繧欺n" +
          "\t--help\t\t繝倥Ν繝励Γ繝�そ繝ｼ繧ｸ繧定｡ｨ遉ｺ\n")
  @Ru("ﾐ湲�ｾﾑ�ひｾﾐｹ ﾑひｵﾑ��ﾐｿﾑ�ｾﾑ災ｺﾑ�ﾐｴﾐｻﾑ�C10N\n" +
          "ﾐ榧ｱﾐｷﾐｾﾑ�\n" +
          "\t<Main> [options]\n" +
          "ﾐ榧ｿﾑ�ｸﾐｸ:\n" +
          "\t--locale\t\tﾐ佯�ｿﾐｾﾐｻﾑ糊ｷﾐｲﾐｰﾑび�ﾐｴﾐｰﾐｽﾐｽﾑτ�ﾐｻﾐｾﾐｺﾐｰﾐｻﾑ圭n" +
          "\t--action\t\tﾐ佯�ｿﾐｾﾐｻﾐｽﾐｸﾑび�ﾑσｺﾐｰﾐｷﾐｰﾐｽﾐｽﾐｾﾐｵ ﾐｴﾐｵﾐｹﾑ�ひｲﾐｸﾐｵ\n" +
          "\t--help\t\tﾐ渙ｾﾐｺﾐｰﾐｷﾐｰﾑび�ﾐｿﾐｾﾐｼﾐｾﾑ禾圭n")
  String usage();
}
