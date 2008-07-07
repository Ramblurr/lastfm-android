package fm.last;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class User 
{
	/** Last.fm username */
	private String m_username;
	private URL m_imageUrl = null;
	private URL m_url = null;
	private int m_id = -1;
	
	private User( String username )
	{
		m_username = username;
	}
	
	public static User newUserFromFriendXML( Element userElement )
	{
		EasyElement e = new EasyElement( userElement );
		String name = e.e("name").value();

		URL imageUrl = null;
		URL url = null;
		
		try {
			imageUrl = new URL( e.e("image").value() );
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			url = new URL( e.e("url").value() );
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		User user = new User( name );
		user.m_imageUrl = imageUrl;
		user.m_url = url;
		
		SQLiteDatabase db = null;
		try
		{
			db = Application.instance().getDb();
		} catch ( FileNotFoundException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return user;
		}
		
		Cursor result = db.query( "FriendsMap", 					//table
								  new String[]{ "contactId" }, 		//columns
								  "username = ?", 					//WHERE clause
								  new String[]{ user.username() },	//WHERE arguments
								  null,								//groupBy
								  null,								//having
								  null );							//orderBy
		
		if( result.first() )
		{
			final int contactIdIndex = result.getColumnIndexOrThrow( "contactId" );
			user.m_id = result.getInt( contactIdIndex );
		}
		
		return user;
	}

	public String username() 
	{
		return m_username;
	}

	public URL imageUrl() 
	{
		return m_imageUrl;
	}

	public URL url() 
	{
		return m_url;
	}

	/** saves the username->android contact mapping to persistent storage */
	void setAndroidId( int id ) throws FileNotFoundException
	{
		m_id = id;
		
		try
		{
			SQLiteDatabase db = Application.instance().getDb();
			String nullColumn = null;
			ContentValues values = new ContentValues();
			values.put( "contactId", id );
			values.put( "username", username() );
			db.replace( "FriendsMap", nullColumn, values );
		} catch ( FileNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int androidId()
	{
		return m_id;
	}
}
