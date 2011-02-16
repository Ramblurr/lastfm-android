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
 * @author jennings Date: Oct 21, 2008
 */
public class Album implements Serializable {

	private static final long serialVersionUID = -8821153568949520331L;
	private String artist;
	private String title;
	private String mbid;
	private String url;
	private ImageUrl[] images;

	public Album(String artist, String title, String mbid, String url, ImageUrl[] images) {
		this.artist = artist;
		this.title = title;
		this.mbid = mbid;
		this.url = url;
		this.images = images;
	}

	public String getArtist() {
		return artist;
	}

	public String getTitle() {
		return title;
	}

	public String getMbid() {
		return mbid;
	}

	public String getUrl() {
		return url;
	}

	public ImageUrl[] getImages() {
		return images;
	}

	public String getURLforImageSize(String size) {
		for (ImageUrl image : images) {
			if (image.getSize().contentEquals(size)) {
				return image.getUrl();
			}
		}
		return null;
	}
}
