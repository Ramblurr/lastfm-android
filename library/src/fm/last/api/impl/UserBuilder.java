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

import fm.last.api.User;
import fm.last.api.ImageUrl;
import fm.last.xml.XMLBuilder;
import org.w3c.dom.Node;

import java.util.List;

/**
 * @author jennings
 *         Date: Oct 20, 2008
 */
public class UserBuilder extends XMLBuilder<User> {
  private ImageUrlBuilder imageBuilder = new ImageUrlBuilder();
  
  public User build(Node userNode) {
    node = userNode;
    String name = getText("name");
    String url = getText("url");
    String country = getText("country");
    String age = getText("age");
    String gender = getText("gender");
    String playcount = getText("playcount");
    
    List<Node> imageNodes = getChildNodes("image");
    if (imageNodes.size() > 1)
    	imageNodes.remove( 0 ); //remove smallest size if there is one
    ImageUrl[] images = new ImageUrl[imageNodes.size()];	    	    
    int i = 0;
    for (Node imageNode : imageNodes)
  		images[i++] = imageBuilder.build(imageNode);
    
    return new User(name, url, images, country, age, gender, playcount);
  }
  
  /**
   * Build a user from the old 1.0 style service
   * http://ws.audioscrobbler.com/1.0/user/c99koder/profile.xml
   */
  public User buildOld(Node userNode) {
      node = userNode;
      String name = getAttribute("username");
      String url = getText("url");
      String country = getText("country");
      String age = getText("age");
      String gender = getText("gender");
      String playcount = getText("playcount");
      String realname = getText("realname");
      String date = getText("registered");

      List<Node> imageNodes = getChildNodes("avatar");
      if (imageNodes.size() > 1)
      	imageNodes.remove( 0 ); //remove smallest size if there is one
      ImageUrl[] images = new ImageUrl[imageNodes.size()];	    	    
      int i = 0;
      for (Node imageNode : imageNodes)
    		images[i++] = imageBuilder.build(imageNode);

      return new User(name, url, images, country, age, gender, playcount, realname, date);
    }
}
