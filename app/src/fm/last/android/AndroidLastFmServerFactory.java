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
package fm.last.android;

import fm.last.api.LastFmServer;
import fm.last.api.LastFmServerFactory;

public class AndroidLastFmServerFactory {
	private static final String API_KEY = PrivateAPIKey.KEY;
	private static final String API_SECRET = PrivateAPIKey.SECRET;
	private static final String XMLRPC_ROOT_URL = "http://dane.ws.prod-context.dev.audioscrobbler.com/2.0/";//"http://ws.audioscrobbler.com/2.0/";
	private static LastFmServer server;

	private AndroidLastFmServerFactory() {
	}

	public static LastFmServer getServer() {
		if (server == null) {
			server = LastFmServerFactory.getServer(XMLRPC_ROOT_URL, API_KEY, API_SECRET);
		}
		return server;
	}

}
