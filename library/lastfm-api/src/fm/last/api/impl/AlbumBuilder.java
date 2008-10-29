// Copyright 2008 Google Inc. All Rights Reserved.

package fm.last.api.impl;

import org.w3c.dom.Node;

import java.util.List;

import fm.last.api.Artist;
import fm.last.api.ImageUrl;
import fm.last.api.Album;
import fm.last.xml.XMLBuilder;

/**
 * @author Mike Jennings
 */
public class AlbumBuilder extends XMLBuilder<Album> {
  private ImageUrlBuilder imageBuilder = new ImageUrlBuilder();

  public Album build(Node albumNode) {
    node = albumNode;
    String artist = getText("artist");
    String title = getText("title");
    String mbid = getText("mbid");
    String url = getText("url");
    List<Node> imageNodes = getChildNodes("image");
    ImageUrl[] images = new ImageUrl[imageNodes.size()];
    int i = 0;
    for (Node imageNode : imageNodes) {
      images[i++] =imageBuilder.build(imageNode);
    }
    return new Album(artist, title, mbid, url, images);
  }
}