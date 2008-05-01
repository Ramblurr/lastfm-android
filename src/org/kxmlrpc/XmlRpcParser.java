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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.kobjects.base64.Base64;
import org.kxmlrpc.util.IsoDate;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * This abstract base class provides basic XML-RPC parsing capabilities. The 
 * kxml parser is required by this class.
 */
public class XmlRpcParser
{
	KXmlParser   parser;
	String       methodName;
	Vector       params = new Vector();

	/**
	 * @param parser    a kxml parser object reference
	 */
	public XmlRpcParser( KXmlParser parser ) {
		this.parser = parser;
	}//end XmlRpcParser( KXmlParser )

	public String getMethodName() {
		return methodName;
	}//end getMethodName()

	public Vector getParams() {
		return params;
	}//end getParams()

	/*
    // This method is used to parse an incoming Client request
    //  kxmlrpc does not currently support this feature
    public void parseCall() throws IOException {
	parser.skip(); 
	parser.read( Xml.START_TAG, "", "methodCall" );

	parser.skip();
	parser.read( Xml.START_TAG, "", "methodName" );
        methodName = parser.readText();
	parser.read( Xml.END_TAG, "", "methodName" );

	parser.skip();

	if( parser.peek().getType() != Xml.END_TAG ) 
	    parseParams();

	parser.read( Xml.END_TAG, "", "methodCall" );
	parser.skip();
	parser.read( Xml.END_DOCUMENT, null, null );
    }//end parseCall()
	 */

	/** 
	 * Called by a client to parse an XML-RPC response returned by a server.
	 *
	 * @return The return parameter sent back by the server.
	 */
	public Object parseResponse() throws IOException, XmlPullParserException {
		Object result;
		parser.require(XmlPullParser.START_DOCUMENT, null, null);
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "methodResponse" );
		result = null;
		if( parser.nextTag() == XmlPullParser.START_TAG ) {
			parser.require(XmlPullParser.START_TAG, null, null);
			// If an error occurred, the server will return a Fault
			if( "fault".equals( parser.getName() ) ) {
				parser.nextTag();
				// Fault's are returned as structs (which are mapped to Hashtables)
				Hashtable fault = (Hashtable) parseValue();
				parser.nextTag();
				parser.require(XmlPullParser.END_TAG, null, "fault" );
				// Ultimately, a client-side exception object is generated
				result = new XmlRpcException 
				( ( (Integer) fault.get( "faultCode" ) ).intValue(),
						(String) fault.get( "faultString" ) );
			}
			/* The current version of the XML-RPC spec -- http://www.xmlrpc.org/spec
                does not permit multiple parameter values to be returned, although
                a complex type (struct or array) containing multiple values (and even
                other complext types) is permitted.  This aspect of the spec is currently
                being debated and may be changed in the future. */
			else if( "params".equals( parser.getName() ) ) {
				parseParams();
				if( params.size() > 1 ) 
					throw new IOException( "too many return parameters" );
				else if( params.size() == 1 ) 
					result = params.elementAt(0);
			}
			else throw new IOException 
			( "<fault> or <params> expected instead of " + parser.getName() );
		}//end if( event.getType() == Xml.START_TAG ) {

		parser.nextTag();
		parser.require( XmlPullParser.END_TAG, null, "methodResponse" );
		parser.next();
		parser.require( XmlPullParser.END_DOCUMENT, null, null );

		return result;
	}//end parseResponse()

	/**
	 * All data in an XML-RPC call is passed as a parameter. This method parses 
	 * the parameter values out of each parameter by calling the parseValue() 
	 * method. 
	 */
	void parseParams() throws IOException, XmlPullParserException {
		parser.require( XmlPullParser.START_TAG, null, "params" );

		while( parser.nextTag() != XmlPullParser.END_TAG ) {
			parser.require( XmlPullParser.START_TAG, null, "param" );
			// Retrieve a Java representation of the XML-RPC parameter value
			parser.nextTag();
			params.addElement( parseValue() );
			parser.nextTag();
			parser.require( XmlPullParser.END_TAG, null, "param" );
		}//end while( parser.peek().getType() != Xml.END_TAG )

		parser.require( XmlPullParser.END_TAG, null, "params" );
	}//end parseParams()

	/** 
	 * Core method for parsing XML-RPC values into Java data types. It is called 
	 * recursively by the parseStruct() and parseArray() methods when handling 
	 * complex data types 
	 * 
	 * @return A Java representation of the XML-RPC value
	 */
	 Object parseValue() throws XmlPullParserException, IOException { 
          Object      result = null; 
          parser.require(XmlPullParser.START_TAG, null, "value"); 
          parser.nextTag(); 

          parser.require( XmlPullParser.START_TAG, null, null ); 
          String name = parser.getName(); 
          if( name.equals("string") ) 
               result = parser.nextText(); 
          else if( name.equals("i4") || name.equals("int") ) 
               result = new Integer 
               ( Integer.parseInt( parser.nextText().trim() ) ); 
          else if( name.equals("boolean") ) 
               result = new Boolean( parser.nextText().trim().equals("1") ); 
          else if( name.equals("dateTime.iso8601") ) 
               result = IsoDate.stringToDate   
               ( parser.nextText() ); 
          else if( name.equals("base64") ) 
               result = Base64.decode( parser.nextText() ); 
          else if( name.equals("struct") ) 
               result = parseStruct(); 
          else if( name.equals("array") ) 
               result = parseArray(); 
          // kxmlrpc does not currently support the XML-RPC double data type 
          //  the temporary workaround is to process double values as strings 
          else if( name.equals("double") ) 
               result = parser.nextText(); 
                    // return as text if the tag is not part of XML-RPC grammar 
                    // this is a temporary workaround as we need to be able to return 
                    // XML and HTML as a whole long String without terminating with </value> 
                    else result = parser.nextText(); 

          parser.require( XmlPullParser.END_TAG, null, name ); 
          parser.nextTag(); 

          parser.require( XmlPullParser.END_TAG, "", "value" ); 
          return result; 
     }//end parseValue() 

	/**
	 * @return kxmlrpc maps XML-RPC structs to java.util.Hashtables
	 */
	Hashtable parseStruct() throws IOException, XmlPullParserException {
		Hashtable h = new Hashtable();

		while( parser.nextTag() != XmlPullParser.END_TAG ) {
			parser.require( XmlPullParser.START_TAG, null, "member" );
			parser.nextTag();
			parser.require( XmlPullParser.START_TAG, null, "name" );
			String name = parser.nextText();
			parser.require( XmlPullParser.END_TAG, null, "name" );
			parser.nextTag();
			h.put( name, parseValue() ); // parse this member value
			parser.nextTag();
			parser.require( XmlPullParser.END_TAG, null, "member" );
		}//end while( parser.nextTag() != XmlPullParser.END_TAG )
		return h;
	}//end parseStruct()

	/**
	 * @return kxmlrpc maps XML-RPC arrays to java.util.Vectors
	 */
	Vector parseArray() throws IOException, XmlPullParserException {
		Vector v = new Vector();
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "data");
		while( parser.nextTag() != XmlPullParser.END_TAG ) {
			v.addElement( parseValue() ); // parse this element value
		}//end while( parser.peek().getType() != Xml.END_TAG )
		parser.require(XmlPullParser.END_TAG, null, "data");
		parser.nextTag();
		return v;
	}//end parseArray()
}//end XmlRpcParser