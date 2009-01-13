package fm.last.api.impl;

import org.w3c.dom.Node;

import fm.last.api.Bio;
import fm.last.xml.XMLBuilder;

/**
 * @author Lukasz Wisniewski
 */
public class BioBuilder extends XMLBuilder<Bio> {

	@Override
	public Bio build(Node bioNode) {
		node = bioNode;
		String summary = "";
		String content = "";
		
		String published = getText("published");
		// FIXME String summary = getText("summary");
		if(getChildNode("summary") != null && getChildNode("summary").getFirstChild() != null)
			summary = getChildNode("summary").getFirstChild().getNodeValue();
		// FIXME String content = getText("content");
		if(getChildNode("content") != null && getChildNode("content").getFirstChild() != null)
			content = getChildNode("content").getFirstChild().getNodeValue();
		return new Bio(published, summary, content);
	}

}
