package fm.last;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

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

	/** saves the username->android contact mapping to persistent storage 
	 * @throws FileNotFoundException */
	void setAndroidId( int id ) throws FileNotFoundException
	{
		m_id = id;
		
		Db db = new Db();
		db.execSQL( "REPLACE INTO " + Db.CONTACT_MAP +
				    "SET (lastfm_username, android_id) " +
				    "VALUES (" + m_username + "," + m_id + ");" );
	}
	
	public int androidId()
	{
		return m_id;
	}
}
