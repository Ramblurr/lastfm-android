package fm.last.api.impl;

import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;

/**
* @author Casey Link <unnamedrambler@gmail.com>
*         Date: Jan 12, 2009
*/
public class LibraryFunctions {
    
    public static void addArtist(String baseUrl, Map<String, String> params) throws IOException, WSError
    {
        addItem(baseUrl, params);
    }
    
    public static void addAlbum(String baseUrl, Map<String, String> params) throws IOException, WSError
    {
        addItem(baseUrl, params);
    }
    
    public static void addTrack(String baseUrl, Map<String, String> params) throws IOException, WSError
    {
        addItem(baseUrl, params);
    }
    
    private static void addItem(String baseUrl, Map<String, String> params) throws IOException, WSError
    {
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
