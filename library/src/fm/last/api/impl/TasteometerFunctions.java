package fm.last.api.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fm.last.api.Tasteometer;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

/**
 * @author Casey Link <unnamedrambler@gmail.com>
 */
public class TasteometerFunctions {
    
    public static Tasteometer compare(String baseUrl, Map<String, String> params) throws IOException, WSError
    {
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
            Node comparisonNode = XMLUtil.findNamedElementNode(lfmNode, "comparison");
            Node resultNode = XMLUtil.findNamedElementNode(comparisonNode, "result");
            
            String score = XMLUtil.getChildContents(resultNode, "score");
            Node artistsNode = XMLUtil.findNamedElementNode(resultNode, "artists");
            Node[] elnodes = XMLUtil.getChildNodes(artistsNode, Node.ELEMENT_NODE);
            List<String> results = new ArrayList<String>();
            for (Node node : elnodes) {
                String name = XMLUtil.getChildContents(node, "name");
                results.add(name);
            }
            return new Tasteometer(score, results.toArray(new String[results.size()]));
        }
    }

}
