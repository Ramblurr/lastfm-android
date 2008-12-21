package fm.last.api;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents an event
 * 
 * @author Lukasz Wisniewski
 */
public class Event implements Serializable{
	private int id;
	private String title;
	private String[] artists;
	private String headliner;
	
	private Venue venue;
	
	private Date startDate;
	private Date startTime;
	private String description;
	private ImageUrl[] images;
	private int attendance;
	private int reviews;
	private String tag;
	private String url;

	public Event(int id, String title, String[] artists, String headliner,
			Venue venue, Date startDate, Date startTime, String description,
			ImageUrl[] images, int attendance, int reviews, String tag,
			String url) {
		super();
		this.id = id;
		this.title = title;
		this.artists = artists;
		this.headliner = headliner;
		this.venue = venue;
		this.startDate = startDate;
		this.startTime = startTime;
		this.description = description;
		this.images = images;
		this.attendance = attendance;
		this.reviews = reviews;
		this.tag = tag;
		this.url = url;
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

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
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
	
}
