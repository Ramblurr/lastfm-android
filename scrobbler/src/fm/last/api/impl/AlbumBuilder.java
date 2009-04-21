/***************************************************************************
 *   Copyright 2005-2009 Last.fm Ltd.                                      *
 *   Portions contributed by Casey Link, Lukasz Wisniewski,                *
 *   Mike Jennings, and Michael Novak Jr.                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
 ***************************************************************************/
package fm.last.api.impl;

import org.w3c.dom.Node;

import java.util.List;

import fm.last.api.ImageUrl;
import fm.last.api.Album;
import fm.last.util.XMLUtil;
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
    if (imageNodes.size() > 1)
    	imageNodes.remove( 0 ); //remove smallest size if there is one
    ImageUrl[] images = new ImageUrl[imageNodes.size()];	    	    
    int i = 0;
    for (Node imageNode : imageNodes)
  		images[i++] = imageBuilder.build(imageNode);
    
    return new Album(artist, title, mbid, url, images);
  }
  
  public Album buildFromTopList(Node albumNode) {
      node = albumNode;
      Node artistNode = getChildNode("artist");
      String artist = XMLUtil.getChildContents(artistNode, "name");
      String title = getText("name");
      String mbid = getText("mbid");
      String url = getText("url");

      List<Node> imageNodes = getChildNodes("image");
      if (imageNodes.size() > 1)
      	imageNodes.remove( 0 ); //remove smallest size if there is one
      ImageUrl[] images = new ImageUrl[imageNodes.size()];	    	    
      int i = 0;
      for (Node imageNode : imageNodes)
    		images[i++] = imageBuilder.build(imageNode);
      
      return new Album(artist, title, mbid, url, images);
    }
}