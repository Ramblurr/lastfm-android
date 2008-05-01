package fm.last;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.w3c.dom.Node;

import android.util.Log;

public class Utils 
{
	static String md5( String in )
	{
	    try
	    {
	    	MessageDigest m = MessageDigest.getInstance( "MD5" );
			m.update( in.getBytes(), 0, in.length() );
			BigInteger bi = new BigInteger( 1, m.digest() );
			return bi.toString(16);
		}
	    catch( java.security.NoSuchAlgorithmException e )
	    {
	    	//TODO we should prolly throw this, as otherwise the user will get a BADAUTH error
	    	// which isn't accurate
	        Log.e( "Last.fm", e.toString() );
	        return "";
	    }
	}
	
	static long now()
	{
		//TODO check this is UTC
		return System.currentTimeMillis() / 1000;
	}
	
	static String version()
	{
		return "1.5"; //TODO correct
	}
	
	/** java removed the Node.toString() function! */
	static String toString( Node node )
	{
		if (node.getNodeType() == Node.TEXT_NODE)
		{
			return node.getNodeValue().toString();			
		}

		String s = "<" + node.getNodeName().toString();
		if (node.hasAttributes()) 
		{
			for (int m = 0; m < node.getAttributes().getLength();m++) 
			{
				Node n = node.getAttributes().item( m );
				s += " " + n.getNodeName() + "='" + n.getNodeValue() + "'";
			}
		}
		s += ">";
		
		if (node.hasChildNodes()) 
		{
			for (int i = 0; i < node.getChildNodes().getLength(); i++)
			{
				s += toString( node.getChildNodes().item( i ) );
			}
		}
		s += "</" + node.getNodeName() + ">";
		
		return s;
	}
	
}
