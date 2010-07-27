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

/**
 * Represents an Artist
 * 
 * @author Mike Jennings
 */
public class Artist implements Serializable {
	private static final long serialVersionUID = -2072451730401030848L;
	private String name;
	private String mbid;
	private String match;
	private String url;
	private ImageUrl[] images;
	private Artist[] similar;
	private String streamable;
	private Bio bio;
	private String playcount;
	private String listeners;
	private String userplaycount;

	public Artist(String name, String mbid, String match, String url, ImageUrl[] images, String streamable, String playcount, String listeners, String userplaycount) {
		this.name = name;
		this.mbid = mbid;
		this.match = match;
		this.url = url;
		this.images = images;
		this.streamable = streamable;
		this.playcount = playcount;
		this.listeners = listeners;
		this.userplaycount = userplaycount;
	}

	public String getName() {
		return name;
	}

	public String getMbid() {
		return mbid;
	}

	public String getMatch() {
		return match;
	}

	public String getUrl() {
		return url;
	}

	public ImageUrl[] getImages() {
		return images;
	}

	public String getStreamable() {
		return streamable;
	}

	public void setBio(Bio bio) {
		this.bio = bio;
	}

	public Bio getBio() {
		return bio;
	}

	public void setSimilar(Artist[] similar) {
		this.similar = similar;
	}

	public Artist[] getSimilar() {
		return similar;
	}

	public String getPlaycount() {
		return playcount;
	}

	public String getListeners() {
		return listeners;
	}
	
	public String getUserPlaycount() {
		return userplaycount;
	}
}
