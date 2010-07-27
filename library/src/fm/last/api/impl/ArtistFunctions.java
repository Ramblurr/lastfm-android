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

import fm.last.api.Artist;
import fm.last.api.Event;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

/**
 * @author jennings Date: Oct 20, 2008
 */
public class ArtistFunctions {
	private ArtistFunctions() {
	}

	public static Artist[] getSimilarArtists(String baseUrl, Map<String, String> params) throws IOException, WSError {
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
			Node similarArtistsNode = XMLUtil.findNamedElementNode(lfmNode, "similarartists");

			Node[] elnodes = XMLUtil.getChildNodes(similarArtistsNode, Node.ELEMENT_NODE);
			ArtistBuilder artistBuilder = new ArtistBuilder();
			List<Artist> artists = new ArrayList<Artist>();
			for (Node node : elnodes) {
				Artist artistObject = artistBuilder.build(node);
				artists.add(artistObject);
			}
			return artists.toArray(new Artist[artists.size()]);
		}
	}

	public static Artist[] searchForArtist(String baseUrl, Map<String, String> params) throws IOException, WSError {
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
			Node resultsNode = XMLUtil.findNamedElementNode(lfmNode, "results");
			Node artistMatches = XMLUtil.findNamedElementNode(resultsNode, "artistmatches");

			Node[] elnodes = XMLUtil.getChildNodes(artistMatches, Node.ELEMENT_NODE);
			ArtistBuilder artistBuilder = new ArtistBuilder();
			List<Artist> artists = new ArrayList<Artist>();
			for (Node node : elnodes) {
				Artist artistObject = artistBuilder.build(node);
				artists.add(artistObject);
			}
			return artists.toArray(new Artist[artists.size()]);
		}
	}

	public static Artist getArtistInfo(String baseUrl, Map<String, String> params) throws IOException, WSError {
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
			Node artistNode = XMLUtil.findNamedElementNode(lfmNode, "artist");

			ArtistBuilder artistBuilder = new ArtistBuilder();

			return artistBuilder.build(artistNode);
		}
	}

	public static Event[] getArtistEvents(String baseUrl, Map<String, String> params) throws IOException, WSError {
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

	public static Artist[] getTopArtists(String baseUrl, Map<String, String> params) throws IOException, WSError {
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
			Node topartistsNode = XMLUtil.findNamedElementNode(lfmNode, "topartists");

			Node[] elnodes = XMLUtil.getChildNodes(topartistsNode, Node.ELEMENT_NODE);
			ArtistBuilder artistBuilder = new ArtistBuilder();
			List<Artist> artists = new ArrayList<Artist>();
			for (Node node : elnodes) {
				Artist artistObject = artistBuilder.build(node);
				artists.add(artistObject);
			}
			return artists.toArray(new Artist[artists.size()]);
		}
	}

	public static Artist[] getRecommendedArtists(String baseUrl, Map<String, String> params) throws IOException, WSError {
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
			Node topartistsNode = XMLUtil.findNamedElementNode(lfmNode, "recommendations");

			Node[] elnodes = XMLUtil.getChildNodes(topartistsNode, Node.ELEMENT_NODE);
			ArtistBuilder artistBuilder = new ArtistBuilder();
			List<Artist> artists = new ArrayList<Artist>();
			for (Node node : elnodes) {
				Artist artistObject = artistBuilder.build(node);
				artists.add(artistObject);
			}
			return artists.toArray(new Artist[artists.size()]);
		}
	}

}
