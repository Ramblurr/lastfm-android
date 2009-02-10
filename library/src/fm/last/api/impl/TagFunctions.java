/***************************************************************************
 *   Copyright 2005-2009 Last.fm Ltd.                                      *
 *   Portions contributed by Casey Link, Lukasz Wisniewski,                *
 *   Mike Jennings, and Michael Novak Jr.                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
 ***************************************************************************/
package fm.last.api.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fm.last.api.Tag;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

/**
 * @author Casey Link
 */
public class TagFunctions {
	private static int TAGS_PER_POST = 10;
	
	private TagFunctions() {}

	public static Tag[] searchForTag(String baseUrl, Map<String, String> params) throws IOException, WSError {
		String response = UrlUtil.doGet(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
	    String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
	    if(!status.contains("ok")) {
	    	Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
	    	if(errorNode != null) {
		    	WSErrorBuilder eb = new WSErrorBuilder();
		    	throw eb.build(params.get("method"), errorNode);
	    	}
	    	return null;
	    } else {
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
	}

	private static Tag[] getChildTags(String baseUrl, Map<String, String> params, String child) throws IOException, WSError {
		String response = UrlUtil.doGet(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
	    String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
	    if(!status.contains("ok")) {
	    	Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
	    	if(errorNode != null) {
		    	WSErrorBuilder eb = new WSErrorBuilder();
		    	throw eb.build(params.get("method"), errorNode);
	    	}
	    	return null;
	    } else {
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
	}

	public static Tag[] getTopTags(String baseUrl, Map<String, String> params) throws IOException, WSError {
		return getChildTags(baseUrl, params, "toptags");
	}

	public static Tag[] getTags(String baseUrl, Map<String, String> params) throws IOException, WSError {
		return getChildTags(baseUrl, params, "tags");
	}

	public static void addTags(String baseUrl, Map<String, String> params) throws IOException, WSError {
		String response = UrlUtil.doPost(baseUrl, params);
//		int n = (tag.length-1) / TAGS_PER_POST;
//		int i = 0;
//		do{
//			params.put("tags", buildTags(tag, i*TAGS_PER_POST));
//			
//		}while (i++ < n);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
	    String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
	    if(!status.contains("ok")) {
	    	Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
	    	if(errorNode != null) {
		    	WSErrorBuilder eb = new WSErrorBuilder();
		    	throw eb.build(params.get("method"), errorNode);
	    	}
	    }
	}

	public static String buildTags(String[] tag){
		String tags = "";
		if(tag!=null && tag.length > 0){
			tags = tag[0];
			for(int i=1; i<TAGS_PER_POST && i<tag.length; i++){
				tags += ","+tag[i];
			}
		}
		return tags;
	}

	public static void removeTag(String baseUrl, Map<String, String> params) throws IOException {
		String response = UrlUtil.doPost(baseUrl, params);
		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
	    String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
	    if(!status.contains("ok")) {
	    	Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
	    	if(errorNode != null) {
		    	WSErrorBuilder eb = new WSErrorBuilder();
		    	throw eb.build(params.get("method"), errorNode);
	    	}
	    }
	}

}
