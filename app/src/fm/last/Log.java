package fm.last;

public class Log 
{
	final private static String TAG = "Last.fm";

	public static void d( String s )
	{
		android.util.Log.d( TAG, s );
	}
	
	public static void i( String s )
	{
		android.util.Log.i( TAG, s );
	}

	public static void e( String s )
	{
		android.util.Log.e( TAG, s );
	}
	
	public static void e( Throwable e )
	{
		android.util.Log.e( TAG, "", e);
	}
	
	public static void e(String msg, Throwable e )
	{
		android.util.Log.e( TAG, msg, e);
	}
}