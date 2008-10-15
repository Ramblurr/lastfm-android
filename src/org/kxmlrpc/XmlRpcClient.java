/* kxmlrpc - XML-RPC for J2ME
 *
 * Copyright (C) 2001  Kyle Gabhart ( kyle@gabhart.com )
 *
 * Contributors: David Johnson ( djohnsonhk@users.sourceforge.net )
 * 				   Stefan Haustein
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.kxmlrpc;

import fm.last.rpc.RpcCall;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;

import org.apache.http.util.ByteArrayBuffer;

/* 
 * Those libs are imported from J2ME and are unsupported by the Android SDK 
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
 */

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;

import android.content.Context;
import android.net.http.EventHandler;
import android.net.http.Headers;
import android.net.http.RequestHandle;
import android.net.http.RequestQueue;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.util.Log; 
import android.net.http.RequestQueue;

/**
 * A single-threaded, reusable XML-RPC client object.
 */
public class XmlRpcClient implements RpcCall {
	private final String DEBUG_TAG = "XmlRpcClient";

    /**
     * Stores the full URL the client will connect with
     */
    String url;
    
    /**
     * Stores the response sent back by the server
     */
    Object result = null;
    
    /**
     * Turns debugging on/off
     */
    boolean debug = true;
    
    /**
     * Constructs an XML-RPC client with a specified string representing a URL.
     *
     * @param url The full URL for the XML-RPC server
     */
    public XmlRpcClient( String url ) {
        this.url = url;
    }//end KxmlRpcClient( String )
    
    /**
     * Construct an XML-RPC client for the specified hostname and port.
     *
     * @param hostname the name of the host server
     * @param the server's port number
     */
    public XmlRpcClient( String hostname, int port ) {
        int delim = hostname.indexOf("/");
        String context = "";
        if (delim>0) {
            context = hostname.substring(delim);
            hostname = hostname.substring(0, delim);
        }
        this.url = "http://" + hostname + ":" + port + context;
    }//end KxmlRpcClient( String, int )
    
    public String getURL() {
        return url;
    }//end getURL()
    
    public void setURL( String newUrl ) {
        url = newUrl;
    }//end setURL( String )
    
    /**
     * This method is the brains of the XmlRpcClient class. It opens an
     * HttpConnection on the URL stored in the url variable, sends an XML-RPC
     * request and processes the response sent back from the server.
     *
     * @param method contains the method on the server that the
     * client will access
     * @param params contains a list of parameters to be sent to
     * the server
     * @return the primitive, collection, or custom object
     * returned by the server
     */
    /** 
     * This is the original implementation of the kSML-RPC execute method.
     * It has incompatibilities with the Android Java SDK.
     * An Android specific implementation is provided bellow.
     */
//    public Object execute( String method, Vector params) throws Exception {
//        // kxmlrpc classes
//        KXmlSerializer          xw = null;
//        XmlRpcWriter            writer = null;
//        XmlRpcParser            parser = null;
//        // J2ME classes
//        HttpConnection          con = null;
//        InputStream             in = null;
//        OutputStream            out = null;
//        // Misc objects for buffering request
//        ByteArrayOutputStream   bos = null;
//        byte[]                  request;
//        int                     messageLength;
//        
//        try {
//            bos = new ByteArrayOutputStream();
//            xw = new KXmlSerializer();
//            xw.setOutput(new OutputStreamWriter(bos));
//            writer = new XmlRpcWriter(xw);
//            
//            writer.writeCall(method, params);
//            xw.flush();
//            
//            if (debug) System.out.println(bos.toString());
//            request = bos.toByteArray();
//            
//            messageLength = request.length;
//            
//            con = (HttpConnection) Connector.open(url, Connector.READ_WRITE);
//            con.setRequestMethod(HttpConnection.POST);
//            con.setRequestProperty("Content-Length", Integer.toString(messageLength));
//            con.setRequestProperty("Content-Type", "text/xml");
//            
//            // Obtain an output stream
//            out = con.openOutputStream();
//            // Push the request to the server
//            out.write( request );
//            // Open an input stream on the server's response
//            in = con.openInputStream();
//            
//            // Parse response from server
//            KXmlParser xp = new KXmlParser();
//            xp.setInput(new InputStreamReader(in));
//            parser = new XmlRpcParser(xp);
//            result = parser.parseResponse();
//            
//        } catch (Exception x) {
//            x.printStackTrace();
//        } finally {
//            try {
//                if (con != null) con.close();
//                if (in != null) in.close();
//                if (out != null) out.close();
//            } catch (IOException ioe) {
//                ioe.printStackTrace();
//            }//end try/catch
//        }//end try/catch/finally
//        
//        if(result instanceof Exception)
//            throw (Exception) result;
//        
//        return result;
//    }//end execute( String, Vector )
//    
//    /**
//     * Called when the return value has been parsed.
//     */
//    void setParsedObject(Object parsedObject) {
//        result = parsedObject;
//    }//end objectCompleted( Object )

    /**
     * An Android specific implementation of the "execute" method
     */
    public Map<String, String> execute(String method, List<String> params, Context context) throws Exception {
		KXmlSerializer			xw = null;
		XmlRpcWriter			writer = null;
		XmlRpcParser			parser = null;
		RequestQueue			con = null;
		ByteArrayOutputStream	bos = null;
		ByteArrayInputStream	bis = null;

		try {
			// Prepare the arguments for posting
			bos = new ByteArrayOutputStream();
			xw = new KXmlSerializer();
			xw.setOutput(new OutputStreamWriter(bos));
			writer = new XmlRpcWriter(xw);
			writer.writeCall(method, params);
			xw.flush();
			
			if (debug)
				Log.d(DEBUG_TAG, bos.toString());
			
			byte[] request = bos.toByteArray();
			bis = new ByteArrayInputStream(request); 

			Map<String, String> headers = new HashMap<String, String>(); 
			headers.put("Content-Type", "text/xml");
			
			XmlRpcEventHandler eventHandler = new XmlRpcEventHandler();

			// Create the connection and post the arguments
			con = new RequestQueue(context);
			RequestHandle handle = con.queueRequest(url, "POST", headers, eventHandler, bis, request.length, false);
			handle.waitUntilComplete();
			
			ByteArrayInputStream in = new ByteArrayInputStream(eventHandler.getBytes());
			
			// Parse response from server
			KXmlParser xp = new KXmlParser();
			xp.setInput(new InputStreamReader(in));
			parser = new XmlRpcParser(xp);
			result = parser.parseResponse();

		} catch (Exception x) {
			Log.e(DEBUG_TAG + ".error", x.getMessage());
		}//end try/catch/finally

		if (result instanceof Exception)
			throw (Exception) result;

		return (Map<String, String>) result;
	}//end execute( String, Vector )

    
    /**
     * This class receives the server response
     */
    private class XmlRpcEventHandler implements EventHandler {
		private final String DEBUG_TAG = "XmlRpcEventHandler";
		private ByteArrayBuffer m_byteArray = new ByteArrayBuffer(20);

		XmlRpcEventHandler() {
		}

		public void data(byte[] bytes, int len) {
			m_byteArray.append(bytes, 0, len);
		}

		public void endData() {
			Log.d(DEBUG_TAG + ".endData", new String(m_byteArray.toByteArray()));
		}

		public void status(int arg0, int arg1, int arg2, String s) {
			Log.d(DEBUG_TAG + ".status", "status [" + s + "]");
		}

		public void error(int i, String s) {
			Log.d(DEBUG_TAG + ".error", "error [" + s + "]");
		}

		public void handleSslErrorRequest(int arg0, String arg1, SslCertificate arg2) {
		}

		public void headers(Iterator arg0) {
		}

		public void headers(Headers arg0) {
		}
		
		public byte[] getBytes() {
			return m_byteArray.toByteArray();
		}

		public void certificate(SslCertificate arg0) {
			// TODO Auto-generated method stub
			
		}

		public void handleSslErrorRequest(SslError arg0) {
			// TODO Auto-generated method stub
			
		}
	}    
    
}//end class KXmlRpcClie