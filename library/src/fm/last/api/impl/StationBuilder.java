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

import org.w3c.dom.Node;

import fm.last.api.Station;
import fm.last.xml.XMLBuilder;

/**
 * @author Mike Jennings
 */
public class StationBuilder extends XMLBuilder<Station> {

  public Station build(Node stationNode) {
    node = stationNode;
    String name = getText("name");
    String type = getText("type");
    String url = getText("url");
    String supportsdiscovery = getText("supportsdiscovery");
    return new Station(name, type, url, supportsdiscovery);
  }
}