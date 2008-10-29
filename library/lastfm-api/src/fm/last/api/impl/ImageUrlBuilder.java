// Copyright 2008 Google Inc. All Rights Reserved.

package fm.last.api.impl;

import org.w3c.dom.Node;

import fm.last.api.ImageUrl;
import fm.last.xml.XMLBuilder;
import fm.last.util.XMLUtil;

/**
 * a class for building ImageURL objects
 *
 * @author Mike Jennings
 */
public class ImageUrlBuilder extends XMLBuilder<ImageUrl> {

  public ImageUrl build(Node imageUrlnode) {
    node = imageUrlnode;
    String url = getText();
    String size = getAttribute("size");
    return new ImageUrl(url, size);
  }
}
