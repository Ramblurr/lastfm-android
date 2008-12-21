package fm.last.api.impl;

import org.w3c.dom.Node;

import fm.last.api.GeoPoint;
import fm.last.api.Location;
import fm.last.xml.XMLBuilder;

/**
 * @author Lukasz Wisniewski
 */
public class LocationBuilder extends XMLBuilder<Location> {

	@Override
	public Location build(Node locationNode) {
		node = locationNode;
		String city = getText("city");
		String country = getText("country");
		String street = getText("street");
		String postalcode = getText("postalcode");
		String timezone = getText("timezone");
		
		GeoPointBuilder geoPointBuilder = new GeoPointBuilder();
		Node geoPointNode = getChildNode("geo:point");
		GeoPoint geoPoint = geoPointBuilder.build(geoPointNode);
		return new Location(city, country, street, postalcode, geoPoint, timezone);
	}

}
