package fm.last;

public class Log 
{
	final private static String TAG = "Last.fm";

	static void d( String s )
	{
		android.util.Log.d( TAG, s );
	}
	
	static void i( String s )
	{
		android.util.Log.i( TAG, s );
	}

	static void e( String s )
	{
		android.util.Log.e( TAG, s );
	}
	
	static void e( Throwable e )
	{
		android.util.Log.e( TAG, e.toString() );
	}
}