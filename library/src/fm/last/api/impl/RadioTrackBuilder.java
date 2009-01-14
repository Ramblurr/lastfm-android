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

import android.util.Log;

import fm.last.api.Artist;
import fm.last.api.Track;
import fm.last.api.Album;
import fm.last.api.RadioTrack;
import fm.last.util.XMLUtil;
import fm.last.xml.XMLBuilder;

/**
 * @author Mike Jennings
 */
public class RadioTrackBuilder extends XMLBuilder<RadioTrack> {

  public RadioTrack build(Node trackNode) {
    node = trackNode;
    String location = getText("location");
    String title = getText("title");
    String identifier = getText("identifier");
    String album = getText("album");
    String creator = getText("creator");
    String duration = getText("duration");
    String image = getText("image");
    String auth = "";
    Node extensionNode = XMLUtil.findNamedElementNode(node, "extension");
    if(extensionNode != null) {
    	Node authNode = XMLUtil.findNamedElementNode(extensionNode, "trackauth");
    	auth = authNode.getFirstChild().getNodeValue();
    }
    return new RadioTrack(location, title, identifier, album, creator, duration, image, auth);
  }
}