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

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

import fm.last.api.RadioTrack;
import fm.last.util.XMLUtil;
import fm.last.xml.XMLBuilder;

/**
 * @author Mike Jennings
 */
public class RadioTrackBuilder extends XMLBuilder<RadioTrack> {

	@Override
	public RadioTrack build(Node trackNode) {
		node = trackNode;
		String location = getText("location");
		String title = getText("title");
		String identifier = getText("identifier");
		String album = getText("album");
		String creator = getText("creator");
		String duration = getText("duration");
		String image = getText("image");

		String lovedStr = XMLUtil.findNamedElementNode(getChildNode("extension"), "loved").getFirstChild().getNodeValue();
		// probably don't need the IgnoreCase but just in case digits have case in the future...
		Boolean loved = lovedStr.equalsIgnoreCase("1");
		
		String[] context = null;
		String auth = "";
		Node extensionNode = XMLUtil.findNamedElementNode(node, "extension");
		if (extensionNode != null) {
			Node authNode = XMLUtil.findNamedElementNode(extensionNode, "trackauth");
			auth = authNode.getFirstChild().getNodeValue();
			Node contextNode = XMLUtil.findNamedElementNode(extensionNode, "context");
			if(contextNode != null) {
				List<String> contextList = new ArrayList<String>();
				for (Node node : XMLUtil.getChildNodes(contextNode, Node.ELEMENT_NODE)) {
					if(!node.getNodeName().equals("user"))
						contextList.add(node.getFirstChild().getNodeValue());
				}
				if(contextList.size() > 0)
					context = contextList.toArray(new String[contextList.size()]);
			}
		}
		return new RadioTrack(location, title, identifier, album, creator, duration, image, auth, loved, context);
	}
}
