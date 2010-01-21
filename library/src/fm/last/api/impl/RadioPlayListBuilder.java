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

import fm.last.api.RadioPlayList;
import fm.last.api.RadioTrack;
import fm.last.util.XMLUtil;
import fm.last.xml.XMLBuilder;

/**
 * @author jennings Date: Oct 25, 2008
 */
public class RadioPlayListBuilder extends XMLBuilder<RadioPlayList> {
	private RadioTrackBuilder trackBuilder = new RadioTrackBuilder();

	@Override
	public RadioPlayList build(Node radioTracklistNode) {
		node = radioTracklistNode;

		String title = getText("title");
		String creator = getText("creator");
		String date = getText("date");
		String link = getText("link");
		String id = getText("id");
		boolean streamable = true;
		if (getText("streamable") != null && getText("streamable").contentEquals("0"))
			streamable = false;
		Node trackListNode = getChildNode("trackList");
		RadioTrack[] tracks = null;
		if (trackListNode != null) {
			List<Node> trackNodes = XMLUtil.findNamedElementNodes(trackListNode, "track");
			tracks = new RadioTrack[trackNodes.size()];
			int i = 0;
			for (Node trackNode : trackNodes) {
				tracks[i++] = trackBuilder.build(trackNode);
			}
		}
		return new RadioPlayList(title, creator, date, link, tracks, id, streamable);
	}
}
