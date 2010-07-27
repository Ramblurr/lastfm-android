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
import fm.last.api.Bio;
import fm.last.api.ImageUrl;
import fm.last.util.XMLUtil;
import fm.last.xml.XMLBuilder;

/**
 * @author Mike Jennings
 */
public class ArtistBuilder extends XMLBuilder<Artist> {
	private ImageUrlBuilder imageBuilder = new ImageUrlBuilder();

	@Override
	public Artist build(Node artistNode) {
		node = artistNode;
		String name = getText("name");
		String mbid = getText("mbid");
		String match = getText("match");
		String url = getText("url");
		String streamable = getText("streamable");
		String listeners = "0";
		String playcount = "0";
		String userplaycount = "0";
		Node statsNode = getChildNode("stats");
		if (statsNode != null) {
			playcount = XMLUtil.findNamedElementNode(statsNode, "playcount").getFirstChild().getNodeValue();
			listeners = XMLUtil.findNamedElementNode(statsNode, "listeners").getFirstChild().getNodeValue();
			try {
				userplaycount = XMLUtil.findNamedElementNode(statsNode, "userplaycount").getFirstChild().getNodeValue();
			} catch (Exception e) {
				//This node isn't always present
			}
		}

		List<Node> imageNodes = getChildNodes("image");
		if (imageNodes.size() > 1)
			imageNodes.remove(0); // remove smallest size if there is one
		ImageUrl[] images = new ImageUrl[imageNodes.size()];
		int i = 0;
		for (Node imageNode : imageNodes)
			images[i++] = imageBuilder.build(imageNode);

		Artist artist = new Artist(name, mbid, match, url, images, streamable, playcount, listeners, userplaycount);

		Node bioNode = getChildNode("bio");
		if (bioNode != null) {
			BioBuilder bioBuilder = new BioBuilder();
			Bio bio = bioBuilder.build(bioNode);
			artist.setBio(bio);
		}

		Node similarNode = getChildNode("similar");
		if (similarNode != null) {
			List<Node> similarArtistNodes = XMLUtil.findNamedElementNodes(similarNode, "artist");
			if (similarArtistNodes != null) {
				Artist[] similar = new Artist[similarArtistNodes.size()];
				int j = 0;
				ArtistBuilder similarArtistBuilder = new ArtistBuilder();
				for (Node similarArtistNode : similarArtistNodes) {
					similar[j++] = similarArtistBuilder.build(similarArtistNode);
				}
				artist.setSimilar(similar);
			}
		}

		return artist;
	}
}
