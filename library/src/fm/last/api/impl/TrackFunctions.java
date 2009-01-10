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

import fm.last.api.Album;
import fm.last.api.Tag;
import fm.last.api.Track;
import fm.last.api.User;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
	    String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
	    if(!status.contains("ok")) {
	    	Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
	    	if(errorNode != null) {
		    	WSErrorBuilder eb = new WSErrorBuilder();
		    	throw eb.build(params.get("method"), errorNode);
	    	}
	    	return null;
	    } else {
			Node trackNode = XMLUtil.findNamedElementNode(lfmNode, "track");
			TrackBuilder trackBuilder = new TrackBuilder();
			return trackBuilder.build(trackNode);
	    }
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
	
	public static User[] getTrackTopFans(String baseUrl, Map<String, String> params) throws IOException {
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
			Node topFansNode = XMLUtil.findNamedElementNode(lfmNode, "topfans");
			UserBuilder userBuilder = new UserBuilder();
			List<Node> fansNodes = XMLUtil.findNamedElementNodes(topFansNode, "user");
			User[] fans = new User[fansNodes.size()];
			int i = 0;
			for(Node fanNode : fansNodes){
				fans[i++] = userBuilder.build(fanNode);
			}
			return fans;
	    }
	}
	
	public static Track[] getTopTracks(String baseUrl, Map<String, String> params) throws IOException, WSError {
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
            Node topalbumsNode = XMLUtil.findNamedElementNode(lfmNode, "toptracks");
    
            Node[] elnodes = XMLUtil.getChildNodes(topalbumsNode, Node.ELEMENT_NODE);
            TrackBuilder trackBuilder = new TrackBuilder();
            List<Track> tracks = new ArrayList<Track>();
            for (Node node : elnodes) {
                Track trackObject = trackBuilder.build(node);
                System.out.println("Got Track: " + trackObject.getName());
                tracks.add(trackObject);
            }
            return tracks.toArray(new Track[tracks.size()]);
        }
    }
	
	public static Track[] getRecentTracks(String baseUrl, Map<String, String> params) throws IOException, WSError {
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
            Node recenttracksNode = XMLUtil.findNamedElementNode(lfmNode, "recenttracks");
    
            Node[] elnodes = XMLUtil.getChildNodes(recenttracksNode, Node.ELEMENT_NODE);
            TrackBuilder trackBuilder = new TrackBuilder();
            List<Track> tracks = new ArrayList<Track>();
            for (Node node : elnodes) {
                Track trackObject = trackBuilder.build(node);
                System.out.println("Got Track: " + trackObject.getName());
                tracks.add(trackObject);
            }
            return tracks.toArray(new Track[tracks.size()]);
        }
    }

}