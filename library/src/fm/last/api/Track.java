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
public class Track implements Serializable {
  private static final long serialVersionUID = 8485165481980957393L;
  private String id;
  private String name;
  private String mbid;
  private String url;
  private String duration;
  private ImageUrl[] images;
  private String streamable;
  private String listeners;
  private String playcount;
  private Artist artist;
  private Album album;

  public Track(String id, String name, String mbid, String url, String duration,
               String streamable, String listeners, String playcount,
               Artist artist, Album album, ImageUrl[] images) {
    this.id = id;
    this.name = name;
    this.mbid = mbid;
    this.url = url;
    this.duration = duration;
    this.streamable = streamable;
    this.listeners = listeners;
    this.playcount = playcount;
    this.artist = artist;
    this.album = album;
    this.images = images;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
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

  public String getDuration() {
    return duration;
  }

  public String getStreamable() {
    return streamable;
  }

  public String getListeners() {
    return listeners;
  }

  public String getPlaycount() {
    return playcount;
  }

  public Artist getArtist() {
    return artist;
  }

  public Album getAlbum() {
    return album;
  }
}
