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
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.kobjects.base64.Base64;
import org.kxmlrpc.util.IsoDate;
import org.kxml2.io.KXmlSerializer;

/** 
 * This class builds XML-RPC method calls using the kxml pull parser's
 * KXmlSerializer class
 */
public class XmlRpcWriter {

	/**
	 * Used to access the kxml serializer
	 */
	KXmlSerializer writer;

	public XmlRpcWriter( KXmlSerializer writer ) {
		this.writer = writer;
	}//end XmlRpcWriter( KXmlSerializer )

	/**
	 * Builds the XML-RPC XML document
	 *
	 * @param name the method's name
	 * @param params the parameters to be passed to the server
	 */
	public void writeCall( String name, Vector params ) throws IOException {
		writer.startTag( null, "methodCall" );
		writer.startTag( null, "methodName" );
		writer.text( name );
		writer.endTag(null, "methodName");

		if( params != null && params.size () > 0 ) {
			writer.startTag( null, "params" );

			for( int i = 0; i < params.size (); i++ ) {
				writer.startTag( null, "param" );
				// The writeValue() method is called for each parameter that is
				//  encoded in the call
				writeValue( params.elementAt(i) );
				writer.endTag(null, "param");
			}//end for( int i = 0; i < params.size (); i++ )

			writer.endTag(null, "params");
		}//end if( params != null && params.size () > 0 )
		writer.endTag(null, "methodCall");
	}//end writeCall( String, Vector ) 

	/*
	 * Maps from Java data types to XML-RPC data types and encodes the parameter 
	 * value(s) using XML-RPC elements
	 */
	private void writeValue( Object value ) throws IOException {
		writer.startTag( null, "value" );

		if( value instanceof String ) {
			writer.startTag( null, "string" );
			writer.text( (String) value );
			writer.endTag(null, "string");
		}
		else if( value instanceof Integer ) {
			writer.startTag( null, "i4" );
			writer.text( "" + ( (Integer) value ).intValue() );
			writer.endTag(null, "i4");
		}
		else if( value instanceof Boolean ) {
			writer.startTag( null, "boolean" );
			writer.text( ( (Boolean) value ).booleanValue() ? "1" : "0" );
			writer.endTag(null, "boolean");
		}
		// XML-RPC dates must be formatted using the iso8601 standard
		else if( value instanceof Date ) {
			writer.startTag( null, "dateTime.iso8601" );
			writer.text 
			( IsoDate.dateToString ( (Date) value ) );
			writer.endTag(null, "dateTime.iso8601");
		}
		// java.util.Vector maps to an XML-RPC array
		else if( value instanceof Vector ) {
			writer.startTag( null, "array" );
                        writer.startTag( null, "data" );
			Vector v = (Vector) value;
			for( int i = 0; i < v.size(); i++ )
				writeValue( v.elementAt(i) );// recursive call
                        writer.endTag( null, "data" );
			writer.endTag(null, "array");
		}
		// java.util.Hashtable maps to an XML-RPC struct
		else if( value instanceof Hashtable ) {
			writer.startTag( null, "struct" );
			Hashtable h = (Hashtable) value;
			for( Enumeration e = h.keys(); e.hasMoreElements(); ) {
				Object key = e.nextElement();
				writer.startTag( null, "member" );
				writer.startTag( null, "name" );
				writer.text( key.toString() );// recursive call
				writer.endTag(null, "name");// </name>
				writeValue( h.get(key) );
				writer.endTag(null, "member");// </member>
			}//end for( Enumeration e = h.keys (); e.hasMoreElements(); )
			writer.endTag(null, "struct");
		}
		// byte arrays must be encoded using the Base64 encoding
		else if( value instanceof byte[] ) {
			writer.startTag( null, "base64" );
			writer.text( Base64.encode( (byte[]) value ) );
			writer.endTag(null, "base64");
		}
		else throw new IOException( "Unknown data type: " + value );
		writer.endTag(null, "value");// </value>
	}//end writeValue( Object )
}//end class XmlRpcWriter