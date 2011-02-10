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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.util.Log;

import fm.last.api.Album;
import fm.last.api.Artist;
import fm.last.api.Tag;
import fm.last.api.Track;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

public class SearchFunctions {
	private SearchFunctions() {
	}

	public static Serializable[] multiSearch(String baseUrl, Map<String, String> params) throws IOException, WSError {
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
			Node matchesNode = XMLUtil.findNamedElementNode(resultsNode, "matches");

			Node[] elnodes = XMLUtil.getChildNodes(matchesNode, Node.ELEMENT_NODE);
			ArtistBuilder artistBuilder = new ArtistBuilder();
			AlbumBuilder albumBuilder = new AlbumBuilder();
			TrackBuilder trackBuilder = new TrackBuilder();
			TagBuilder tagBuilder = new TagBuilder();
			List<Serializable> results = new ArrayList<Serializable>();

			for (Node node : elnodes) {
				if(node.getNodeName().equals("artist")) {
					Artist artistObject = artistBuilder.build(node);
					results.add(artistObject);
				}
				if(node.getNodeName().equals("album")) {
					Album albumObject = albumBuilder.build(node);
					results.add(albumObject);
				}
				if(node.getNodeName().equals("track")) {
					Track trackObject = trackBuilder.build(node);
					results.add(trackObject);
				}
				if(node.getNodeName().equals("tag")) {
					Tag tagObject = tagBuilder.build(node);
					results.add(tagObject);
				}
			}
			return results.toArray(new Serializable[results.size()]);
		}
	}
}