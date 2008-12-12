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
import fm.last.api.Track;
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
public class TrackFunctions {
	private TrackFunctions() {
	}

	public static Track getTrackInfo(String baseUrl, Map<String, String> params) throws IOException {
		String response = UrlUtil.doGet(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
		Node trackNode = XMLUtil.findNamedElementNode(lfmNode, "track");
		TrackBuilder trackBuilder = new TrackBuilder();
		return trackBuilder.build(trackNode);
	}

	public static Tag[] getTrackTopTags(String baseUrl, Map<String, String> params) throws IOException {
	    return TagFunctions.getTopTags(baseUrl, params);
	}
	
	public static Tag[] getTrackTags(String baseUrl, Map<String, String> params) throws IOException {
	    return TagFunctions.getTags(baseUrl, params);
	}
	
	public static void addTrackTags(String baseUrl, Map<String, String> params) throws IOException {
		TagFunctions.addTags(baseUrl, params);
	}

	public static void removeTrackTag(String baseUrl, Map<String, String> params) throws IOException {
		TagFunctions.removeTag(baseUrl, params);
	}

}