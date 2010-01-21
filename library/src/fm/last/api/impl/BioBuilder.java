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

import fm.last.api.Bio;
import fm.last.xml.XMLBuilder;

/**
 * @author Lukasz Wisniewski
 */
public class BioBuilder extends XMLBuilder<Bio> {

	@Override
	public Bio build(Node bioNode) {
		node = bioNode;
		String summary = "";
		String content = "";

		String published = getText("published");
		// FIXME String summary = getText("summary");
		if (getChildNode("summary") != null && getChildNode("summary").getFirstChild() != null)
			summary = getChildNode("summary").getFirstChild().getNodeValue();
		// FIXME String content = getText("content");
		if (getChildNode("content") != null && getChildNode("content").getFirstChild() != null)
			content = getChildNode("content").getFirstChild().getNodeValue();
		return new Bio(published, summary, content);
	}

}
