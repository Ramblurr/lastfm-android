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
package fm.last.api;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

/**
 * @author jennings Date: Oct 20, 2008
 */
public class User implements Serializable {

	public enum Gender {
		MALE, FEMALE, UNKNOWN
	}

	private static final long serialVersionUID = 2047407259337226913L;

	public User(String name, String realname, String url, ImageUrl[] images, Locale country, String age, Gender gender, String playcount, String subscriber,
			Date joindate) {
		this.name = name;
		this.realname = realname;
		this.url = url;
		this.images = images;
		this.country = country;
		this.age = age;
		this.playcount = playcount;
		this.subscriber = subscriber;
		this.joindate = joindate;
		this.gender = gender;
	}

	public String getName() {
		return name;
	}

	public String getRealName() {
		return realname;
	}

	public String getUrl() {
		return url;
	}

	public Locale getCountry() {
		return country;
	}

	public String getAge() {
		return age;
	}

	public Gender getGender() {
		return gender;
	}

	public String getPlaycount() {
		return playcount;
	}

	public ImageUrl[] getImages() {
		return images;
	}

	public Date getJoinDate() {
		return joindate;
	}

	public String getSubscriber() {
		return subscriber;
	}

	private final String name;
	private final String url;
	private final ImageUrl[] images;
	private final Locale country;
	private final String age;
	private final Gender gender;
	private final String playcount;
	private final String realname;
	private final Date joindate;
	private final String subscriber;
}
