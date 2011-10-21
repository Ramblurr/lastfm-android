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
import java.util.HashMap;

/**
 * Represents an event
 * 
 * @author Lukasz Wisniewski
 */
public class Event implements Serializable {
	private static final long serialVersionUID = 4832815335124538661L;
	private int id;
	private String title;
	private String[] artists;
	private String[] friends;
	private String headliner;

	private Venue venue;

	private Date startDate;
	private Date endDate;
	private String description;
	private ImageUrl[] images;
	private int attendance;
	private int reviews;
	private String tag;
	private String url;
	private String status;
	private HashMap<String, String> ticketUrls;
	private float score;

	public Event(int id, String title, String[] artists, String headliner, Venue venue, Date startDate, Date endDate, String description, ImageUrl[] images, int attendance,
			int reviews, String tag, String url, String status, HashMap<String, String>ticketUrls, String score, String[] friends) {
		super();
		this.id = id;
		this.title = title;
		this.artists = artists;
		this.headliner = headliner;
		this.venue = venue;
		this.startDate = startDate;
		this.endDate = endDate;
		this.description = description;
		this.images = images;
		this.attendance = attendance;
		this.reviews = reviews;
		this.tag = tag;
		this.url = url;
		this.status = status;
		this.ticketUrls = ticketUrls;
		if(score != null)
			this.score = Float.parseFloat(score);
		else
			this.score = 0.0f;
		this.friends = friends;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String[] getArtists() {
		return artists;
	}

	public void setArtists(String[] artists) {
		this.artists = artists;
	}

	public String getHeadliner() {
		return headliner;
	}

	public void setHeadliner(String headliner) {
		this.headliner = headliner;
	}

	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ImageUrl[] getImages() {
		return images;
	}

	public void setImages(ImageUrl[] images) {
		this.images = images;
	}

	public int getAttendance() {
		return attendance;
	}

	public void setAttendance(int attendance) {
		this.attendance = attendance;
	}

	public int getReviews() {
		return reviews;
	}

	public void setReviews(int reviews) {
		this.reviews = reviews;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public HashMap<String, String> getTicketUrls() {
		return this.ticketUrls;
	}

	public String getURLforImageSize(String size) {
		for (ImageUrl image : images) {
			if (image.getSize().contentEquals(size)) {
				return image.getUrl();
			}
		}
		return null;
	}
	
	public float getScore() {
		return score;
	}
	
	public String[] getFriends() {
		return friends;
	}
}
