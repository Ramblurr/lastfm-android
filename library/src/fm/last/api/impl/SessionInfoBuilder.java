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

import fm.last.api.SessionInfo;
import fm.last.xml.XMLBuilder;
import fm.last.util.XMLUtil;

/**
 * @author jennings Date: Oct 20, 2008
 */
public class SessionInfoBuilder extends XMLBuilder<SessionInfo> {

	@Override
	public SessionInfo build(Node applicationNode) {
		boolean expired = false;
		Integer playsLeft = 30;
		Integer playsElapsed = 0;
		
		node = applicationNode;
		Node radioPermissionsNode = XMLUtil.findNamedElementNode(node, "radioPermission");
		
		Node userNode = XMLUtil.findNamedElementNode(radioPermissionsNode, "user");
		boolean radio = !XMLUtil.getChildContents(userNode, "radio").contentEquals("0");
		boolean freeTrial = !XMLUtil.getChildContents(userNode, "freetrial").contentEquals("0");
		
		Node trialNode = XMLUtil.findNamedElementNode(userNode, "trial");
		if(trialNode != null) {
			expired = !XMLUtil.getChildContents(trialNode, "expired").contentEquals("0");
			playsLeft = Integer.parseInt(XMLUtil.getChildContents(trialNode, "playsleft"));
			playsElapsed  = Integer.parseInt(XMLUtil.getChildContents(trialNode, "playselapsed"));
		}
		
		return new SessionInfo(radio, freeTrial, expired, playsLeft, playsElapsed);
	}
}