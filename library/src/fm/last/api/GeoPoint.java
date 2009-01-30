package fm.last.api;

import java.io.Serializable;

/**
 * Represents a geo point
 * 
 * @author Lukasz Wisniewski
 */
public class GeoPoint implements Serializable {
	private static final long serialVersionUID = 4241815999032525503L;
	private double latitude;
	private double longitude;
	
	public GeoPoint(double latitude, double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
