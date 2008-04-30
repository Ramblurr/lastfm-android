/* kxmlrpc - XML-RPC for J2ME
 *
 * Copyright (C) 2001  Kyle Gabhart ( kyle@gabhart.com )
 * 
 * Contributor: Stefan Haustein 
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

/**
 * This exception is thrown by a kxmlrpc component to indicate an error on the remote servern 
 * (a Fault element has been returned by the server). In contrast, client-side errors throw 
 * a java.io.IOException object.
 */

public class XmlRpcException extends Exception {
    
    /**
     * These three fields represent three potential error levels
     */
    static final int NONE = 0;
    static final int RECOVERABLE = 1;
    static final int FATAL = 2;

    /**
     * The fault code of the exception.
     */
    public final int code;

    /**
     * This is the sole constructor for the KxmlRpcException class.
     * @param code an integer representing the error code
     * @param message a String containing the error message
     */    
    public XmlRpcException( int code, String message ) {
	super( message );
	this.code = code;
    }//end KxmlRpcException( int, String )
}//end class KxmlRpcException