package fm.last;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
}
