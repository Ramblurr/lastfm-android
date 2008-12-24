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

import fm.last.api.Tag;
import fm.last.api.User;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class UserFunctions {
	private UserFunctions() {
	}

	public static User getUserInfo(String baseUrl, Map<String, String> params) throws IOException {
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
			Node userNode = XMLUtil.findNamedElementNode(lfmNode, "user");
			UserBuilder builder = new UserBuilder();
			return builder.build(userNode);
	    }
	}
	
	public static Tag[] getUserTopTags(String baseUrl, Map<String, String> params) throws IOException {
		return TagFunctions.getTopTags(baseUrl, params);
	}

}