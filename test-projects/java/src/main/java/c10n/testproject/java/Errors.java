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
public interface Errors {
  @En("No action has be specified")
  @Ja("繧｢繧ｯ繧ｷ繝ｧ繝ｳ縺梧欠螳壹＆繧後※縺�∪縺帙ｓ")
  @Ru("ﾐ頒ｵﾐｹﾑ�ひｲﾐｸﾐｵ ﾐｽﾐｵ ﾐｲﾑ巾ｱﾑ�ｰﾐｽﾐｾ")
  String noAction();
}
