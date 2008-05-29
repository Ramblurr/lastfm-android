package fm.last;

import java.io.FileNotFoundException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Db
{
	private static final String NAME = "primary";
	private static final int VERSION = 1;
	
	public static final String CONTACT_MAP = "contact_map";
	
	private SQLiteDatabase db = null;
	
	public Db() throws FileNotFoundException
	{
		try
		{
			db = Application.instance().openDatabase( NAME, null );
		}
		catch( FileNotFoundException e )
		{
			db = Application.instance().createDatabase( NAME, VERSION, Context.MODE_PRIVATE, null );
		}
		
		db.execSQL( "CREATE TABLE IF NOT EXISTS " + CONTACT_MAP + " " +
                    "(lastfm_username VARCHAR, android_id INT UNIQUE);" );
	}
	
	public void close()
	{
		db.close();
	}
	
	public void execSQL( String sql )
	{
		db.execSQL( sql );
	}
}
