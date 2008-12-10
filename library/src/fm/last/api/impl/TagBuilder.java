package fm.last.api.impl;

import java.util.List;

import org.w3c.dom.Node;

import fm.last.api.Tag;
import fm.last.xml.XMLBuilder;

/**
 * @author Casey Link
 */
public class TagBuilder extends XMLBuilder<Tag> {

	public Tag build(Node tagNode) {
		node = tagNode;
	    String name = getText("name");
	    String tagcountStr = getText("tagcount");
	    int tagcount = -1;
	    try {
	    	tagcount = Integer.parseInt(tagcountStr);
	    } catch (NumberFormatException e){} // ignore failed tagcount parsing 
	    String url = getText("url");
	    return new Tag(name, tagcount, url);
	}

}
