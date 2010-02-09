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
public class Track implements Serializable {
	private static final long serialVersionUID = 8485165481980957393L;
	private String id;
	private String name;
	private String mbid;
	private String url;
	private String duration;
	private ImageUrl[] images;
	private String streamable;
	private String listeners;
	private String playcount;
	private Artist artist;
	private Album album;
	private String date;
	private String nowPlaying;
	
	public Track(String id, String name, String mbid, String url, String duration, String streamable, String listeners, String playcount, Artist artist,
			Album album, ImageUrl[] images, String date, String nowPlaying) {
		this.id = id;
		this.name = name;
		this.mbid = mbid;
		this.url = url;
		this.duration = duration;
		this.streamable = streamable;
		this.listeners = listeners;
		this.playcount = playcount;
		this.artist = artist;
		this.album = album;
		this.images = images;
		this.date = date;
		this.nowPlaying = nowPlaying;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
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

	public String getDuration() {
		return duration;
	}

	public String getStreamable() {
		return streamable;
	}

	public String getListeners() {
		return listeners;
	}

	public String getPlaycount() {
		return playcount;
	}

	public Artist getArtist() {
		return artist;
	}

	public Album getAlbum() {
		return album;
	}

	public String getDate() {
		return date;
	}

	public String getNowPlaying() {
		return nowPlaying;
	}
}
