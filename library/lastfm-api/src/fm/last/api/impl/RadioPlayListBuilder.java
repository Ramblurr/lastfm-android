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

import fm.last.api.RadioPlayList;
import fm.last.api.RadioTrack;
import fm.last.xml.XMLBuilder;
import fm.last.util.XMLUtil;
import org.w3c.dom.Node;

import java.util.List;

/**
 * @author jennings
 *         Date: Oct 25, 2008
 */
public class RadioPlayListBuilder extends XMLBuilder<RadioPlayList> {
  private RadioTrackBuilder trackBuilder = new RadioTrackBuilder();

  public RadioPlayList build(Node radioTracklistNode) {
    node = radioTracklistNode;

    String title = getText("title");
    String creator = getText("creator");
    String date = getText("date");
    String link = getText("link");
    Node trackListNode = getChildNode("trackList");
    List<Node> trackNodes = XMLUtil.findNamedElementNodes(trackListNode, "track");
    RadioTrack[] tracks = new RadioTrack[trackNodes.size()];
    int i = 0;
    for (Node trackNode : trackNodes) {
      tracks[i++] = trackBuilder.build(trackNode);
    }
    return new RadioPlayList(title, creator, date, link, tracks);
  }
}
