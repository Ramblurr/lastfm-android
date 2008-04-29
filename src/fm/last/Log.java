package fm.last;

public class Log 
{
	final private static String TAG = "Last.fm";
	
	static void i( String s )
	{
		android.util.Log.i( TAG, s );
	}
}