// Copyright 2008 Google Inc. All Rights Reserved.

package fm.last.api;

import java.io.Serializable;

/**
 * represents a URL to an image
 * 
 *
 * @author Mike Jennings
 */
public class ImageUrl implements Serializable {
  private String url;
  private String size;

  public String getUrl() {
    return url;
  }

  public String getSize() {
    return size;
  }

  public ImageUrl(String url, String size) {
    this.url = url;
    this.size = size;
  }
}
