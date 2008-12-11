package fm.last.api.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fm.last.api.Tag;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

/**
 * @author Casey Link
 */
public class TagFunctions {
	private TagFunctions() {}

	public static Tag[] searchForTag(String baseUrl, Map<String, String> params) throws IOException {
		String response = UrlUtil.doGet(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
		Node resultsNode = XMLUtil.findNamedElementNode(lfmNode, "results");
		Node artistMatches = XMLUtil.findNamedElementNode(resultsNode, "tagmatches");

		Node[] elnodes = XMLUtil.getChildNodes(artistMatches, Node.ELEMENT_NODE);
		TagBuilder tagBuilder = new TagBuilder();
		List<Tag> tags = new ArrayList<Tag>();
		for (Node node : elnodes) {
			Tag artistObject = tagBuilder.build(node);
			tags.add(artistObject);
		}
		return tags.toArray(new Tag[tags.size()]);
	}
	
	private static Tag[] getChildTags(String baseUrl, Map<String, String> params, String child) throws IOException{
		String response = UrlUtil.doGet(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
		Node childNode = XMLUtil.findNamedElementNode(lfmNode, child);

		Node[] elnodes = XMLUtil.getChildNodes(childNode, Node.ELEMENT_NODE);
		TagBuilder tagBuilder = new TagBuilder();
		List<Tag> tags = new ArrayList<Tag>();
		for (Node node : elnodes) {
			Tag artistObject = tagBuilder.build(node);
			tags.add(artistObject);
		}
		return tags.toArray(new Tag[tags.size()]);
	}
	
	public static Tag[] getTopTags(String baseUrl, Map<String, String> params) throws IOException{
		return getChildTags(baseUrl, params, "toptags");
	}
	
	public static Tag[] getTags(String baseUrl, Map<String, String> params) throws IOException{
		return getChildTags(baseUrl, params, "tags");
	}

}
