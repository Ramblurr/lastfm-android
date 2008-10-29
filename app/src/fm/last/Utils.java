package fm.last;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.w3c.dom.Node;

public class Utils 
{
	public static String md5( String in )
	{
	    try
	    {
	    	MessageDigest m = MessageDigest.getInstance( "MD5" );
			m.update( in.getBytes( "UTF-8" ), 0, in.length() );
			BigInteger bi = new BigInteger( 1, m.digest() );
			return bi.toString(16);
		}
	    catch( java.security.NoSuchAlgorithmException e )
	    {
	    	//TODO we should prolly throw this, as otherwise the user will get a BADAUTH error
	    	// which isn't accurate
	        Log.e( e.toString() );
	        return "";
	    } catch ( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			Log.e( e.toString() );
			return "";
		}
	}
	
	public static long now()
	{
		//TODO check this is UTC
		return System.currentTimeMillis() / 1000;
	}
	
	public static String version()
	{
		return "1.5"; //TODO correct
	}
	
	/** java removed the Node.toString() function! */
	public static String toString( Node node )
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