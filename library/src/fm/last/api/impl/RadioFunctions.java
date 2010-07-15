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
import java.net.URL;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fm.last.api.RadioPlayList;
import fm.last.api.Station;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

/**
 * @author jennings Date: Oct 20, 2008
 */
public class RadioFunctions {
	private RadioFunctions() {
	}

	public static Station searchForStation(String baseUrl, Map<String, String> params) throws IOException, WSError {
		String query = UrlUtil.buildQuery(params);
		String response = UrlUtil.doPost(new URL(baseUrl), query);

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
			Node stationsNode = XMLUtil.findNamedElementNode(lfmNode, "stations");
			Node stationNode = XMLUtil.findNamedElementNode(stationsNode, "station");
			StationBuilder sb = new StationBuilder();
			return sb.build(stationNode);
		}
	}

	public static Station tuneToStation(String baseUrl, Map<String, String> params) throws IOException, WSError {
		String query = UrlUtil.buildQuery(params);
		String response = UrlUtil.doPost(new URL(baseUrl), query);

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
			Node stationNode = XMLUtil.findNamedElementNode(lfmNode, "station");
			StationBuilder sb = new StationBuilder();
			return sb.build(stationNode);
		}
	}

	/*
	 * 
	 * <lfm status="ok"> <playlist version="1" xmlns="http://xspf.org/ns/0/">
	 * <title>Radiohead+Radio</title> <creator>Last.fm</creator>
	 * <date>2008-10-25T19:19:56</date> <link
	 * rel="http://www.last.fm/expiry">3600</link> <trackList> <track>
	 * <location>
	 * http://play.last.fm/user/e055dd8ad0b28d008625988c4cf37092.mp3</location>
	 * <title>Blue</title> <identifier>11904</identifier> <album>A Storm in
	 * Heaven</album> <creator>The Verve</creator> <duration>203000</duration>
	 * <image
	 * >http://images.amazon.com/images/P/B000000WJK.01.LZZZZZZZ.jpg</image>
	 * <extension application="http://www.last.fm"> <trackauth>22046</trackauth>
	 * <albumid>1781</albumid> <artistid>1306</artistid>
	 * <recording>11904</recording>
	 * <artistpage>http://www.last.fm/music/The+Verve</artistpage>
	 * <albumpage>http
	 * ://www.last.fm/music/The+Verve/A+Storm+in+Heaven</albumpage>
	 * <trackpage>http://www.last.fm/music/The+Verve/_/Blue</trackpage>
	 * <buyTrackURL
	 * >http://www.last.fm/affiliate_sendto.php?link=catchdl&amp;prod
	 * =&amp;pos=9b944892b67cd2d9f7d9da1c934c5428&amp;s=</buyTrackURL>
	 * <buyAlbumURL></buyAlbumURL> <freeTrackURL></freeTrackURL> </extension>
	 * </track> <track>
	 * <location>http://play.last.fm/user/92f6a7efb6bf31669b7d8b43c7315293
	 * .mp3</location> <title>Satellite</title> <identifier>2456433</identifier>
	 * <album>Young Liars</album> <creator>TV on the Radio</creator>
	 * <duration>273000</duration>
	 * <image>http://ec1.images-amazon.com/images/P/B00009V7RA
	 * .01._SCMZZZZZZZ_.jpg</image> <extension application="http://www.last.fm">
	 * <trackauth>49fee</trackauth> <albumid>1416579</albumid>
	 * <artistid>1199215</artistid> <recording>2456433</recording>
	 * <artistpage>http://www.last.fm/music/TV+on+the+Radio</artistpage>
	 * <albumpage
	 * >http://www.last.fm/music/TV+on+the+Radio/Young+Liars</albumpage>
	 * <trackpage
	 * >http://www.last.fm/music/TV+on+the+Radio/_/Satellite</trackpage>
	 * <buyTrackURL
	 * >http://www.last.fm/affiliate_sendto.php?link=catchdl&amp;prod
	 * =1416579&amp
	 * ;rt=8&amp;pos=9b944892b67cd2d9f7d9da1c934c5428&amp;s=56</buyTrackURL>
	 * <buyAlbumURL></buyAlbumURL> <freeTrackURL></freeTrackURL> </extension>
	 * </track> <track>
	 * <location>http://play.last.fm/user/d3464a03fa93bc88ebaf6f039d2a9881
	 * .mp3</location> <title>Almost Forgot Myself</title>
	 * <identifier>12699652</identifier> <album>Some Cities</album>
	 * <creator>Doves</creator> <duration>281000</duration>
	 * <image>http://images.
	 * amazon.com/images/P/B0007735HG.01.MZZZZZZZ.jpg</image> <extension
	 * application="http://www.last.fm"> <trackauth>017a7</trackauth>
	 * <albumid>2024933</albumid> <artistid>1187</artistid>
	 * <recording>12699652</recording>
	 * <artistpage>http://www.last.fm/music/Doves</artistpage>
	 * <albumpage>http://www.last.fm/music/Doves/Some+Cities</albumpage>
	 * <trackpage
	 * >http://www.last.fm/music/Doves/_/Almost+Forgot+Myself</trackpage>
	 * <buyTrackURL
	 * >http://www.last.fm/affiliate_sendto.php?link=catchdl&amp;prod
	 * =&amp;pos=9b944892b67cd2d9f7d9da1c934c5428&amp;s=</buyTrackURL>
	 * <buyAlbumURL></buyAlbumURL> <freeTrackURL></freeTrackURL> </extension>
	 * </track> <track>
	 * <location>http://play.last.fm/user/a1391ff9e156bd2b75e8820672305a56
	 * .mp3</location> <title>Smokers Outside The Hospital Doors</title>
	 * <identifier>97695966</identifier> <album>Various Artists - RAZZMATAZZ#07
	 * (Disc 1)_ Compiled and mixed by Dj Amable</album>
	 * <creator>Editors</creator> <duration>289000</duration>
	 * <image>http://userserve-ak.last.fm/serve/174s/4534143.jpg</image>
	 * <extension application="http://www.last.fm"> <trackauth>9460a</trackauth>
	 * <albumid>3733200</albumid> <artistid>9982235</artistid>
	 * <recording>113487567</recording>
	 * <artistpage>http://www.last.fm/music/Editors</artistpage>
	 * <albumpage>http:
	 * //www.last.fm/music/Various+Artists/Various%2BArtists%2B-%
	 * 2BRAZZMATAZZ%252307
	 * %2B%2528Disc%2B1%2529_%2BCompiled%2Band%2Bmixed%2Bby%2BDj
	 * %2BAmable</albumpage>
	 * <trackpage>http://www.last.fm/music/Editors/_/Smokers
	 * +Outside+The+Hospital+Doors</trackpage>
	 * <buyTrackURL>http://www.last.fm/affiliate_sendto
	 * .php?link=catchdl&amp;prod
	 * =3733200&amp;rt=8&amp;pos=9b944892b67cd2d9f7d9da1c934c5428
	 * &amp;s=56</buyTrackURL> <buyAlbumURL></buyAlbumURL>
	 * <freeTrackURL></freeTrackURL> </extension> </track> <track>
	 * <location>http
	 * ://play.last.fm/user/32255ea50975e267d38ec6c47e7c9065.mp3</location>
	 * <title>Rain King</title> <identifier>1097399</identifier> <album>Daydream
	 * Nation</album> <creator>Sonic Youth</creator> <duration>247000</duration>
	 * <image>http://userserve-ak.last.fm/serve/174s/8588923.jpg</image>
	 * <extension application="http://www.last.fm"> <trackauth>518ec</trackauth>
	 * <albumid>339</albumid> <artistid>296</artistid>
	 * <recording>1097399</recording>
	 * <artistpage>http://www.last.fm/music/Sonic+Youth</artistpage>
	 * <albumpage>http
	 * ://www.last.fm/music/Sonic+Youth/Daydream+Nation</albumpage>
	 * <trackpage>http://www.last.fm/music/Sonic+Youth/_/Rain+King</trackpage>
	 * <buyTrackURL
	 * >http://www.last.fm/affiliate_sendto.php?link=catchdl&amp;prod
	 * =&amp;pos=9b944892b67cd2d9f7d9da1c934c5428&amp;s=</buyTrackURL>
	 * <buyAlbumURL></buyAlbumURL> <freeTrackURL></freeTrackURL> </extension>
	 * </track> </trackList> </playlist> </lfm>
	 */

	public static RadioPlayList getRadioPlaylist(String baseUrl, Map<String, String> params) throws IOException, WSError {
		String query = UrlUtil.buildQuery(params);
		String response = UrlUtil.doPost(new URL(baseUrl), query);

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
			Node playListNode = XMLUtil.findNamedElementNode(lfmNode, "playlist");
			RadioPlayListBuilder playlistBuilder = new RadioPlayListBuilder();
			RadioPlayList playList = playlistBuilder.build(playListNode);
			return playList;
		}
	}
}