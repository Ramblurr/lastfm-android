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

import fm.last.api.Friends;
import fm.last.api.User;
import fm.last.xml.XMLBuilder;

/**
 * @author jennings Date: Oct 20, 2008
 */
public class FriendsBuilder extends XMLBuilder<Friends> {
	private UserBuilder userBuilder = new UserBuilder();

	@Override
	public Friends build(Node friendsNode) {
		node = friendsNode;

		List<Node> userNodes = getChildNodes("user");
		User[] users = new User[userNodes.size()];
		int i = 0;
		for (Node imageNode : userNodes) {
			users[i++] = userBuilder.build(imageNode);
		}
		return new Friends(users);
	}
}
