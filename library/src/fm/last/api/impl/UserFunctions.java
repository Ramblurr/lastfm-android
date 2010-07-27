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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fm.last.api.Album;
import fm.last.api.Artist;
import fm.last.api.Event;
import fm.last.api.RadioPlayList;
import fm.last.api.Station;
import fm.last.api.Tag;
import fm.last.api.Track;
import fm.last.api.User;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

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
		if (!status.contains("ok")) {
			Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
			if (errorNode != null) {
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

	public static Artist[] getUserTopArtists(String baseUrl, Map<String, String> params) throws IOException {
		return ArtistFunctions.getTopArtists(baseUrl, params);
	}

	public static Artist[] getUserRecommendedArtists(String baseUrl, Map<String, String> params) throws IOException {
		return ArtistFunctions.getRecommendedArtists(baseUrl, params);
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

	public static Event[] getUserEvents(String baseUrl, Map<String, String> params) throws IOException, WSError {
		String response = UrlUtil.doGet(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
		String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
		if (!status.contains("ok")) {
			Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
			if (errorNode != null) {
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
			for (Node eventNode : eventNodes) {
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
		if (!status.contains("ok")) {
			Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
			if (errorNode != null) {
				WSErrorBuilder eb = new WSErrorBuilder();
				throw eb.build(params.get("method"), errorNode);
			}
		}
	}

	public static RadioPlayList[] getUserPlaylists(String baseUrl, Map<String, String> params) throws IOException, WSError {
		String response = UrlUtil.doPost(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
		String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
		if (!status.contains("ok")) {
			Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
			if (errorNode != null) {
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
			for (Node playlistNode : playlistNodes) {
				playlists[i++] = playlistBuilder.build(playlistNode);
			}

			return playlists;
		}
	}

	public static Station[] getUserRecentStations(String baseUrl, Map<String, String> params) throws IOException, WSError {
		String response = UrlUtil.doPost(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
		String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
		if (!status.contains("ok")) {
			Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
			if (errorNode != null) {
				WSErrorBuilder eb = new WSErrorBuilder();
				throw eb.build(params.get("method"), errorNode);
			}
			return null;
		} else {
			Node playlistsNode = XMLUtil.findNamedElementNode(lfmNode, "recentstations");

			List<Node> stationNodes = XMLUtil.findNamedElementNodes(playlistsNode, "station");
			StationBuilder stationBuilder = new StationBuilder();
			Station[] stations = new Station[stationNodes.size()];
			int i = 0;
			for (Node stationNode : stationNodes) {
				stations[i++] = stationBuilder.build(stationNode);
			}

			return stations;
		}
	}

	public static void signUp(String baseUrl, Map<String, String> params) throws IOException, WSError {
		String response = UrlUtil.doPost(baseUrl, params);
		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
		String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
		if (!status.contains("ok")) {
			Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
			if (errorNode != null) {
				WSErrorBuilder eb = new WSErrorBuilder();
				throw eb.build(params.get("method"), errorNode);
			}
		}
	}
}