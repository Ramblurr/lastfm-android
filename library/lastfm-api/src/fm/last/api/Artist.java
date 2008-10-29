// Copyright 2008 Google Inc. All Rights Reserved.

package fm.last.api;

import java.io.Serializable;

/**
 * Represents an Artist
 *
 * @author Mike Jennings
 */
public class Artist implements Serializable {
  private String name;
  private String mbid;
  private String match;
  private String url;
  private ImageUrl[] images;

  public Artist(String name, String mbid, String match, String url, ImageUrl[] images) {
    this.name = name;
    this.mbid = mbid;
    this.match = match;
    this.url = url;
    this.images = images;
  }

  public String getName() {
    return name;
  }

  public String getMbid() {
    return mbid;
  }

  public String getMatch() {
    return match;
  }

  public String getUrl() {
    return url;
  }

  public ImageUrl[] getImages() {
    return images;
  }
}
