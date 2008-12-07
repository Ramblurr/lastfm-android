package net.roarsoftware.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <code>DomElement</code> wraps around an {@link Element} and provides convenience methods.
 *
 * @author Janni Kovacs
 */
public class DomElement {
	private org.w3c.dom.Element e;

	/**
	 * Creates a new wrapper around the given {@link Element}.
	 *
	 * @param elem An w3c Element
	 */
	public DomElement(org.w3c.dom.Element elem) {
		this.e = elem;
	}

	/**
	 * @return the original Element
	 */
	public Element getElement() {
		return e;
	}

	/**
	 * Returns the attribute value to a given attribute name or <code>null</code> if the attribute doesn't exist.
	 *
	 * @param name The attribute's name
	 * @return Attribute value or <code>null</code>
	 */
	public String getAttribute(String name) {
		return e.hasAttribute(name) ? e.getAttribute(name) : null;
	}

	/**
	 * @return the text content of the element
	 */
	public String getText() {
		StringBuffer buffer = new StringBuffer();
		NodeList childList = e.getChildNodes();
		for (int i = 0; i < childList.getLength(); i++) {
		    Node child = childList.item(i);
		    if (child.getNodeType() != Node.TEXT_NODE)
		    	continue; // skip non-text nodes
		    buffer.append(child.getNodeValue());
		}
		return buffer.toString();
	}

	/**
	 * Checks if this element has a child element with the given name.
	 *
	 * @param name The child's name
	 * @return <code>true</code> if this element has a child element with the given name
	 */
	public boolean hasChild(String name) {
		NodeList list = e.getElementsByTagName(name);
		for (int i = 0, j = list.getLength(); i < j; i++) {
			Node item = list.item(i);
			if(item.getParentNode() == e)
				return true;
		}
		return false;
	}

	/**
	 * Returns the child element with the given name or <code>null</code> if it doesn't exist.
	 *
	 * @param name The child's name
	 * @return the child element or <code>null</code>
	 */
	public DomElement getChild(String name) {
		NodeList list = e.getElementsByTagName(name);
		if (list.getLength() == 0)
			return null;
		for (int i = 0, j = list.getLength(); i < j; i++) {
			Node item = list.item(i);
			if (item.getParentNode() == e)
				return new DomElement((Element) item);
		}
		return null;
	}

	/**
	 * Returns the text content of a child node with the given name. If no such child exists or the child
	 * does not have text content, <code>null</code> is returned.
	 *
	 * @param name The child's name
	 * @return the child's text content or <code>null</code>
	 */
	public String getChildText(String name) {
		DomElement child = getChild(name);
		return child != null ? child.getText() : null;
	}

	/**
	 * @return all children of this element
	 */
	public Collection<DomElement> getChildren() {
		return getChildren("*");
	}

	/**
	 * Returns all children of this element with the given tag name.
	 *
	 * @param name The children's tag name
	 * @return all matching children
	 */
	public Collection<DomElement> getChildren(String name) {
		List<DomElement> l = new ArrayList<DomElement>();
		NodeList list = e.getElementsByTagName(name);
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node.getParentNode() == e)
				l.add(new DomElement((Element) node));
		}
		return l;
	}
}
