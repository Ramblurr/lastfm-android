// Copyright 2008 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package fm.last.api.impl;

import fm.last.api.Session;
import fm.last.xml.XMLBuilder;
import org.w3c.dom.Node;

/**
 * @author jennings
 *         Date: Oct 20, 2008
 */
public class SessionBuilder extends XMLBuilder<Session> {

  public Session build(Node sessionNode) {
    node = sessionNode;
    String name = getText("name");
    String key = getText("key");
    String subscriber = getText("subscriber");
    return new Session(name, key, subscriber);
  }
}