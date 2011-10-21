package fm.last.api.impl;

import org.w3c.dom.Node;

import fm.last.api.Metro;
import fm.last.xml.XMLBuilder;

public class MetroBuilder extends XMLBuilder<Metro> {
	@Override
	public Metro build(Node metroNode) {
		node = metroNode;
		String name = getText("name");
		String country = getText("country");
		return new Metro(name, country);
	}
}
