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
 *         Date: Oct 25, 2008
 *     <track>
        <location>http://play.last.fm/user/e055dd8ad0b28d008625988c4cf37092.mp3</location>
        <title>Blue</title>
        <identifier>11904</identifier>
        <album>A Storm in Heaven</album>
        <creator>The Verve</creator>
        <duration>203000</duration>
        <image>http://images.amazon.com/images/P/B000000WJK.01.LZZZZZZZ.jpg</image>
        <extension application="http://www.last.fm">
            <trackauth>22046</trackauth>
            <albumid>1781</albumid>
            <artistid>1306</artistid>
            <recording>11904</recording>
                        <artistpage>http://www.last.fm/music/The+Verve</artistpage>
            <albumpage>http://www.last.fm/music/The+Verve/A+Storm+in+Heaven</albumpage>
            <trackpage>http://www.last.fm/music/The+Verve/_/Blue</trackpage>
            <buyTrackURL>http://www.last.fm/affiliate_sendto.php?link=catchdl&amp;prod=&amp;pos=9b944892b67cd2d9f7d9da1c934c5428&amp;s=</buyTrackURL>
            <buyAlbumURL></buyAlbumURL>
            <freeTrackURL></freeTrackURL>
        </extension>
    </track>

 */
public class RadioTrack implements Serializable {
  private String locationUrl;
  private String title;
  private String identifier;
  private String album;
  private String creator;
  private String duration;
  private String imageUrl;

  public RadioTrack(String locationUrl, String title, String identifier,
                    String album, String creator, String duration, String imageUrl) {
    this.locationUrl = locationUrl;
    this.title = title;
    this.identifier = identifier;
    this.album = album;
    this.creator = creator;
    this.duration = duration;
    this.imageUrl = imageUrl;
  }
  
  public String getLocationUrl() {
    return locationUrl;
  }

  public String getTitle() {
    return title;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getAlbum() {
    return album;
  }

  public String getCreator() {
    return creator;
  }

  public String getDuration() {
    return duration;
  }

  public String getImageUrl() {
    return imageUrl;
  }

}
