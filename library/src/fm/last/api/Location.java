package fm.last.api;

import java.io.Serializable;

/**
 * Represents a location
 * 
 * @author Lukasz Wisniewski
 */
public class Location implements Serializable {
	private static final long serialVersionUID = 7670348098081408246L;
	private String city;
	private String country;
	private String street;
	private String postalcode;
	private GeoPoint geoPoint;
	private String timezone;

	public Location(String city, String country, String street,
			String postalcode, GeoPoint geoPoint, String timezone) {
		super();
		this.city = city;
		this.country = country;
		this.street = street;
		this.postalcode = postalcode;
		this.geoPoint = geoPoint;
		this.timezone = timezone;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getPostalcode() {
		return postalcode;
	}

	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}

	public GeoPoint getGeoPoint() {
		return geoPoint;
	}

	public void setGeoPoint(GeoPoint geoPoint) {
		this.geoPoint = geoPoint;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getTimezone() {
		return timezone;
	}
	
	
}
