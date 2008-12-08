// Copyright 2008 Google Inc. All Rights Reserved.

package fm.last.api.impl;

import org.w3c.dom.Node;

import java.util.List;

import fm.last.api.Artist;
import fm.last.api.ImageUrl;
import fm.last.xml.XMLBuilder;

/**
 * @author Mike Jennings
 */
public class ArtistBuilder extends XMLBuilder<Artist> {
  private ImageUrlBuilder imageBuilder = new ImageUrlBuilder();

  public Artist build(Node artistNode) {
    node = artistNode;
    String name = getText("name");
    String mbid = getText("mbid");
    String match = getText("match");
    String url = getText("url");
    String streamable = getText("streamable");
    List<Node> imageNodes = getChildNodes("image");
    ImageUrl[] images = new ImageUrl[imageNodes.size()];
    int i = 0;
    for (Node imageNode : imageNodes) {
      images[i++] =imageBuilder.build(imageNode);
    }
    return new Artist(name, mbid, match, url, images, streamable);
  }
}
