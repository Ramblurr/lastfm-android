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

package fm.last.api;

import java.io.Serializable;

/**
 * @author jennings
 *         Date: Oct 23, 2008
 */
public class Station implements Serializable {

  private static final long serialVersionUID = 1499806102972292532L;
  private String name;
  private String type;
  private String url;
  private String supportsDiscovery;

  public Station(String name, String type, String url, String supportsDiscovery) {
    this.name = name;
    this.type = type;
    this.url = url;
    this.supportsDiscovery = supportsDiscovery;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getUrl() {
    return url;
  }

  public String getSupportsDiscovery() {
    return supportsDiscovery;
  }
}
