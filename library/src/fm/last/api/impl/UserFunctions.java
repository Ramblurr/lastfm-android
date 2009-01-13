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
import fm.last.api.Event;
import fm.last.api.RadioPlayList;
import fm.last.api.Tag;
import fm.last.api.Artist;
import fm.last.api.Track;
import fm.last.api.User;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

import java.io.IOException;
import java.net.URL;
import java.util.List;
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
	
	   public static User getAnyUserInfo(String url) throws IOException {
	        URL theUrl = new URL(url);
	        String response = UrlUtil.doGet(theUrl);


	        Document responseXML = null;
	        try {
	            responseXML = XMLUtil.stringToDocument(response);
	        } catch (SAXException e) {
	            throw new IOException(e.getMessage());
	        }

	        Node profileNode = XMLUtil.findNamedElementNode(responseXML, "profile");
            UserBuilder builder = new UserBuilder();
            return builder.buildOld(profileNode);
	    }
	
	public static Tag[] getUserTopTags(String baseUrl, Map<String, String> params) throws IOException {
		return TagFunctions.getTopTags(baseUrl, params);
	}
	
	public static Artist[] getUserTopArtists(String baseUrl, Map<String, String> params) throws IOException {
        return ArtistFunctions.getTopArtists(baseUrl, params);
    }
	
	public static Album[] getUserTopAlbums(String baseUrl, Map<String, String> params) throws IOException {
        return AlbumFunctions.getTopAlbums(baseUrl, params);
    }
	
	public static Track[] getUserTopTracks(String baseUrl, Map<String, String> params) throws IOException {
        return TrackFunctions.getTopTracks(baseUrl, params);
    }
	
	public static Track[] getUserRecentTracks(String baseUrl, Map<String, String> params) throws IOException {
        return TrackFunctions.getRecentTracks(baseUrl, params);
    }

	public static Event[] getUserEvents(String baseUrl,
			Map<String, String> params) throws IOException, WSError {
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
			Node eventsNode = XMLUtil.findNamedElementNode(lfmNode, "events");
			
			List<Node> eventNodes = XMLUtil.findNamedElementNodes(eventsNode, "event");
			EventBuilder eventBuilder = new EventBuilder();
			Event[] events = new Event[eventNodes.size()];
			int i = 0;
			for(Node eventNode : eventNodes){
				events[i++] = eventBuilder.build(eventNode);
			}
	
			return events;
	    }
	}

	public static void attendEvent(String baseUrl, Map<String, String> params) throws IOException {
		String response = UrlUtil.doPost(baseUrl, params);
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
	    }
	}

	public static RadioPlayList[] getUserPlaylists(String baseUrl,
			Map<String, String> params) throws IOException, WSError {
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
			Node playlistsNode = XMLUtil.findNamedElementNode(lfmNode, "playlists");
			
			List<Node> playlistNodes = XMLUtil.findNamedElementNodes(playlistsNode, "playlist");
			RadioPlayListBuilder playlistBuilder = new RadioPlayListBuilder();
			RadioPlayList[] playlists = new RadioPlayList[playlistNodes.size()];
			int i = 0;
			for(Node playlistNode : playlistNodes){
				playlists[i++] = playlistBuilder.build(playlistNode);
			}
	
			return playlists;
	    }
	}
}