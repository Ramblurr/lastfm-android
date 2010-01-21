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
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author jennings Date: Oct 20, 2008
 */
public class Friends implements Serializable {
	private static final long serialVersionUID = 50693901684109497L;
	private User[] friends;

	public Friends(User[] friends) {
		this.friends = friends;
		Arrays.sort(this.friends, new Comparator<User>() {
			public int compare(User u1, User u2) {
				return u1.getName().toLowerCase().compareTo(u2.getName().toLowerCase());
			}
		});
	}

	public User[] getFriends() {
		return friends;
	}

}
