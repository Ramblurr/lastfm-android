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

package fm.last.api;

import java.io.Serializable;

/**
 * @author jennings
 *         Date: Oct 21, 2008
 */
public class Album implements Serializable {

  private static final long serialVersionUID = -8821153568949520331L;
  private String artist;
  private String title;
  private String mbid;
  private String url;
  private ImageUrl[] images;

  public Album(String artist, String title, String mbid, String url, ImageUrl[] images) {
    this.artist = artist;
    this.title = title;
    this.mbid = mbid;
    this.url = url;
    this.images = images;
  }

  public String getArtist() {
    return artist;
  }

  public String getTitle() {
    return title;
  }

  public String getMbid() {
    return mbid;
  }

  public String getUrl() {
    return url;
  }

  public ImageUrl[] getImages() {
    return images;
  }

}
