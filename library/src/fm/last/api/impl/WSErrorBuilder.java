package fm.last.api.impl;

import org.w3c.dom.Node;

import fm.last.api.WSError;
import fm.last.xml.XMLBuilder;

public class WSErrorBuilder extends XMLBuilder<WSError> {

	public WSError build(String method, Node errorNode) {
		node = errorNode;
		String message = getText();
		Integer code = Integer.parseInt(this.getAttribute("code"));
		return new WSError(method, message, code);
	}
	
	public WSError build(Node errorNode) {
		return this.build("", errorNode);
	}
}
