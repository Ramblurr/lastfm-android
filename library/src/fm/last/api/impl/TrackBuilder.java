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

import java.util.List;

import org.w3c.dom.Node;

import fm.last.api.Artist;
import fm.last.api.ImageUrl;
import fm.last.api.Track;
import fm.last.xml.XMLBuilder;

/**
 * @author Mike Jennings
 */
public class TrackBuilder extends XMLBuilder<Track> {
	private ArtistBuilder artistBuilder = new ArtistBuilder();
	private ImageUrlBuilder imageBuilder = new ImageUrlBuilder();

	@Override
	public Track build(Node trackNode) {
		node = trackNode;
		String nowPlaying = getAttribute("nowplaying");
		String id = getText("id");
		String name = getText("name");
		String mbid = getText("mbid");
		String url = getText("url");
		String duration = getText("duration");
		String streamable = getText("streamable");
		String listeners = getText("listeners");
		String playcount = getText("playcount");
		
		Artist artist;
		Node artistNode = getChildNode("artist");
		if (artistNode.getChildNodes().getLength() > 1) {
			artist = artistBuilder.build(artistNode);
		} else {
			String artistName = getText("artist");
			artist = new Artist(artistName, "", "", "", null, "", "", "", "");
		}

		List<Node> imageNodes = getChildNodes("image");
		ImageUrl[] images = new ImageUrl[imageNodes.size()];
		int i = 0;
		for (Node imageNode : imageNodes) {
			images[i++] = imageBuilder.build(imageNode);
		}

		if(getChildNode("date") != null)
			node = getChildNode("date");
		String date = getAttribute("uts");

		return new Track(id, name, mbid, url, duration, streamable, listeners, playcount, artist, null, images, date, nowPlaying);
	}
}