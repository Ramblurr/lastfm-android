package fm.last.api;

import java.io.Serializable;

/**
 * Represents a venue
 * 
 * @author Lukasz Wisniewski
 */
public class Venue implements Serializable{
	private String name;
	private String url;
	private Location location;
	
	public Venue(String name, String url, Location location) {
		super();
		this.name = name;
		this.url = url;
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	
}
