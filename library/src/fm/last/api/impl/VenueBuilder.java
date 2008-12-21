package fm.last.api.impl;

import org.w3c.dom.Node;

import fm.last.api.Location;
import fm.last.api.Venue;
import fm.last.xml.XMLBuilder;

/**
 * @author Lukasz Wisniewski
 */
public class VenueBuilder extends XMLBuilder<Venue> {

	@Override
	public Venue build(Node venueNode) {
		node = venueNode;
		String name = getText("name");
		LocationBuilder locationBuilder = new LocationBuilder();
		Node locationNode = getChildNode("location");
		Location location = locationBuilder.build(locationNode);
		String url = getText("url");
		return new Venue(name, url, location);
	}

}
