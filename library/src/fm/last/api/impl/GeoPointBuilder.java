package fm.last.api.impl;

import org.w3c.dom.Node;

import fm.last.api.GeoPoint;
import fm.last.xml.XMLBuilder;

/**
 * @author Lukasz Wisniewski
 */
public class GeoPointBuilder extends XMLBuilder<GeoPoint> {

	@Override
	public GeoPoint build(Node geoPointNode) {
		node = geoPointNode;
		double latitude = 0, longitude = 0;
		if(getText("geo:lat") != null)
			latitude = Double.parseDouble(getText("geo:lat"));

		if(getText("geo:long") != null)
			longitude = Double.parseDouble(getText("geo:long"));
		return new GeoPoint(latitude, longitude);
	}

}
