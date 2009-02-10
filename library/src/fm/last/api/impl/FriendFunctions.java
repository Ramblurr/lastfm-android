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

import fm.last.api.Friends;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author jennings
 *         Date: Oct 20, 2008
 */
public class FriendFunctions {
  private FriendFunctions() {
  }

  public static Friends getFriends(String baseUrl, Map<String, String> params) throws IOException, WSError {
    String response = UrlUtil.doGet(baseUrl, params);

    Document responseXML = null;
    try {
      responseXML = XMLUtil.stringToDocument(response);
    } catch (SAXException e) {
      throw new IOException(e.getMessage());
    }

    Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
    String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
    if(!status.contains("ok")) {
    	Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
    	if(errorNode != null) {
	    	WSErrorBuilder eb = new WSErrorBuilder();
	    	throw eb.build(params.get("method"), errorNode);
    	}
    	return null;
    } else {
	    Node friendsNode = XMLUtil.findNamedElementNode(lfmNode, "friends");
	    FriendsBuilder builder = new FriendsBuilder();
	    return builder.build(friendsNode);
    }
  }

}