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

package fm.last.api.impl;

import fm.last.api.Artist;
import fm.last.api.Friends;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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