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
 * @author jennings Date: Oct 25, 2008 <track>
 *         <location>http://play.last.fm/user
 *         /e055dd8ad0b28d008625988c4cf37092.mp3</location> <title>Blue</title>
 *         <identifier>11904</identifier> <album>A Storm in Heaven</album>
 *         <creator>The Verve</creator> <duration>203000</duration>
 *         <image>http:/
 *         /images.amazon.com/images/P/B000000WJK.01.LZZZZZZZ.jpg</image>
 *         <extension application="http://www.last.fm">
 *         <trackauth>22046</trackauth> <albumid>1781</albumid>
 *         <artistid>1306</artistid> <recording>11904</recording>
 *         <artistpage>http://www.last.fm/music/The+Verve</artistpage>
 *         <albumpage
 *         >http://www.last.fm/music/The+Verve/A+Storm+in+Heaven</albumpage>
 *         <trackpage>http://www.last.fm/music/The+Verve/_/Blue</trackpage>
 *         <buyTrackURL
 *         >http://www.last.fm/affiliate_sendto.php?link=catchdl&amp;
 *         prod=&amp;pos=9b944892b67cd2d9f7d9da1c934c5428&amp;s=</buyTrackURL>
 *         <buyAlbumURL></buyAlbumURL> <freeTrackURL></freeTrackURL>
 *         </extension> </track>
 */
public class RadioTrack implements Serializable {
	private static final long serialVersionUID = -2981580772307269182L;
	private String locationUrl;
	private String title;
	private String identifier;
	private String album;
	private String creator;
	private int duration;
	private String imageUrl;
	private String trackAuth;
	private Boolean loved;
	private String[] context;

	public RadioTrack(String locationUrl, String title, String identifier, String album, String creator, String duration, String imageUrl, String trackAuth, Boolean loved, String[] context) {
		this.locationUrl = locationUrl;
		this.title = title;
		this.identifier = identifier;
		this.album = album;
		this.creator = creator;
		this.duration = new Integer(duration).intValue();
		this.imageUrl = imageUrl;
		this.trackAuth = trackAuth;
		this.loved = loved;
		this.context = context;
	}

	public String getLocationUrl() {
		return locationUrl;
	}
	
	public void setLocationUrl(String url) {
		locationUrl = url;
	}

	public String getTitle() {
		return title;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getAlbum() {
		return album;
	}

	public String getCreator() {
		return creator;
	}

	public int getDuration() {
		return duration;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getTrackAuth() {
		return trackAuth;
	}
	
	public Boolean getLoved() {
		return loved;
	}

	public void setLoved(boolean loved) {
		this.loved = loved;
	}
	
	public String[] getContext() {
		return context;
	}

}
