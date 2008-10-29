// Copyright 2008 Google Inc. All Rights Reserved.

package fm.last.xml;

import org.w3c.dom.Node;

import java.util.Map;
import java.util.TreeMap;
import java.util.List;

import fm.last.util.XMLUtil;

/**
 * @author jennings
 *         Date: Apr 28, 2008
 */
public abstract class XMLBuilder<T> {
  protected Node node;

  public abstract T build(Node node);

  protected String getText(String name) {
    return XMLUtil.getChildContents(node, name);
  }

  protected String getText() {
    return XMLUtil.getChildTextNodes(node);
  }

  protected String getAttribute(String attname) {
    return XMLUtil.getNodeAttribute(node, attname);    
  }

  protected Node getChildNode(String name) {
    return XMLUtil.findNamedElementNode(node, name);
  }

  protected List<Node> getChildNodes(String name) {
    return XMLUtil.findNamedElementNodes(node, name);
  }

  protected Map<String, String> getLinks() {
    Map<String, String> linkMap = new TreeMap<String, String>();

    Node[] elnodes = XMLUtil.getChildNodes(node, Node.ELEMENT_NODE);
    int i;
    for (i = 0; i < elnodes.length; ++i) {
      if (elnodes[i].getNodeName().equals("link")) {
        String url = XMLUtil.getChildTextNodes(elnodes[i]);
        String rel = XMLUtil.getNodeAttribute(elnodes[i], "rel");
        if (url != null && rel != null && url.length() > 0) {
          linkMap.put(rel, url);
        }
      }
    }
    return linkMap;
  }
}

