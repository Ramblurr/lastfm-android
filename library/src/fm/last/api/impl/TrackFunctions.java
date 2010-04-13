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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fm.last.api.Tag;
import fm.last.api.Track;
import fm.last.api.User;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

/**
 * @author jennings Date: Oct 20, 2008
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
		if (!status.contains("ok")) {
			Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
			if (errorNode != null) {
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
		if (!status.contains("ok")) {
			Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
			if (errorNode != null) {
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
			for (Node fanNode : fansNodes) {
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
		if (!status.contains("ok")) {
			Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
			if (errorNode != null) {
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
		if (!status.contains("ok")) {
			Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
			if (errorNode != null) {
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
				tracks.add(trackObject);
			}
			return tracks.toArray(new Track[tracks.size()]);
		}
	}

	public static void loveTrack(String baseUrl, Map<String, String> params) throws IOException {
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

	public static void banTrack(String baseUrl, Map<String, String> params) throws IOException {
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

	public static void shareTrack(String baseUrl, Map<String, String> params) throws IOException {
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

	public static void addTrackToPlaylist(String baseUrl, Map<String, String> params) throws IOException {
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