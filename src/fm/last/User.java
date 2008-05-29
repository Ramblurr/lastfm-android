package fm.last;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

public class User {
	private String m_username;
	private URL m_imageUrl = null;
	private URL m_url = null;
	
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
}
