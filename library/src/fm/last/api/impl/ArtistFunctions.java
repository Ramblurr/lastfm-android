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
import fm.last.api.Event;
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
public class ArtistFunctions {
	private ArtistFunctions() {
	}

	public static Artist[] getSimilarArtists(String baseUrl, Map<String, String> params) throws IOException {
		String response = UrlUtil.doGet(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
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

	public static Artist[] searchForArtist(String baseUrl, Map<String, String> params) throws IOException {
		String response = UrlUtil.doGet(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
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
	
	public static Artist getArtistInfo(String baseUrl, Map<String, String> params) throws IOException {
		String response = UrlUtil.doGet(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
		Node artistNode = XMLUtil.findNamedElementNode(lfmNode, "artist");

		ArtistBuilder artistBuilder = new ArtistBuilder();

		return artistBuilder.build(artistNode);
	}

	public static Event[] getArtistEvents(String baseUrl,
			Map<String, String> params) throws IOException {
		String response = UrlUtil.doGet(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
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
