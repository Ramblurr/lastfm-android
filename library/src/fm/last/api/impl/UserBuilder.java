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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Node;

import fm.last.api.ImageUrl;
import fm.last.api.User;
import fm.last.api.User.Gender;
import fm.last.util.XMLUtil;
import fm.last.xml.XMLBuilder;

/**
 * @author jennings Date: Oct 20, 2008
 */
public class UserBuilder extends XMLBuilder<User> {
	private ImageUrlBuilder imageBuilder = new ImageUrlBuilder();

	@Override
	public User build(Node userNode) {
		node = userNode;
		String name = getText("name");
		String realname = getText("realname");
		String url = getText("url");
		String age = getText("age");
		String playcount = getText("playcount");
		String subscriber = getText("subscriber");

		List<Node> imageNodes = getChildNodes("image");
		if (imageNodes.size() > 1)
			imageNodes.remove(0); // remove smallest size if there is one
		ImageUrl[] images = new ImageUrl[imageNodes.size()];
		int i = 0;
		for (Node imageNode : imageNodes)
			images[i++] = imageBuilder.build(imageNode);

		// create locale for country
		Locale countryLocale = null;
		String country = getText("country");
		if (country != null && country.trim().length() > 0) {
			countryLocale = new Locale("", country);
		}

		// create date from UNIX time
		Date registeredDate = null;
		Node registered = getChildNode("registered");
		if (registered != null) {
			String date = XMLUtil.getNodeAttribute(registered, "unixtime");
			registeredDate = new Date(Long.parseLong(date) * 1000);
		}

		Gender genderEnum = Gender.UNKNOWN;
		String gender = getText("gender");
		if (gender != null) {
			if (gender.equalsIgnoreCase("m")) {
				genderEnum = Gender.MALE;
			} else if (gender.equalsIgnoreCase("f")) {
				genderEnum = Gender.FEMALE;
			}
		}

		return new User(name, realname, url, images, countryLocale, age, genderEnum, playcount, subscriber, registeredDate);
	}
}
