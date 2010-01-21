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
package fm.last.xml;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Node;

import fm.last.util.XMLUtil;

/**
 * @author jennings Date: Apr 28, 2008
 */
public abstract class XMLBuilder<T> {
	protected Node node;

	public abstract T build(Node node);

	protected String getText(String name) {
		return XMLUtil.getChildContents(node, name);
	}

	protected String getText() {
		return XMLUtil.getChildTextNodes(node);
	}

	protected String getAttribute(String attname) {
		return XMLUtil.getNodeAttribute(node, attname);
	}

	protected Node getChildNode(String name) {
		return XMLUtil.findNamedElementNode(node, name);
	}

	protected List<Node> getChildNodes(String name) {
		return XMLUtil.findNamedElementNodes(node, name);
	}

	protected Map<String, String> getLinks() {
		Map<String, String> linkMap = new TreeMap<String, String>();

		Node[] elnodes = XMLUtil.getChildNodes(node, Node.ELEMENT_NODE);
		int i;
		for (i = 0; i < elnodes.length; ++i) {
			if (elnodes[i].getNodeName().equals("link")) {
				String url = XMLUtil.getChildTextNodes(elnodes[i]);
				String rel = XMLUtil.getNodeAttribute(elnodes[i], "rel");
				if (url != null && rel != null && url.length() > 0) {
					linkMap.put(rel, url);
				}
			}
		}
		return linkMap;
	}
}
