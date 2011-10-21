package fm.last.api.impl;

import org.w3c.dom.Node;

import fm.last.api.Geo;
import fm.last.xml.XMLBuilder;

public class GeoBuilder extends XMLBuilder<Geo> {
	@Override
	public Geo build(Node geoNode) {
		node = geoNode;
		String countrycode = getText("countrycode");
		String countryname = getText("countryname");
		return new Geo(countrycode, countryname);
	}
}
