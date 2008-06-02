package fm.last;

import android.content.SharedPreferences;

public class Application extends android.app.Application
{
	static private Application instance;
	private String m_user;
	private String m_pass;
	private String m_sessionKey;
	private SharedPreferences m_preferences;
	
	public void onCreate()
	{
		m_preferences = getSharedPreferences( "Last.fm", MODE_PRIVATE );
		m_user = m_preferences.getString( "username", "" );
		m_pass = m_preferences.getString( "md5Password", "" );
		m_sessionKey = m_preferences.getString( "sessionKey", "" );
		
		instance = this;
	}
	
	public static Application instance()
	{
		return instance;
	}
	
	public String userName()
	{
		return m_user;
	}

	public String sessionKey()
	{
		return m_sessionKey;
	}
	
	public boolean setSessionKey( String sessionKey )
	{
		m_sessionKey = sessionKey;
		return
			m_preferences.edit().putString( "sessionKey", sessionKey )
						 		.commit();
	}

	public String password()
	{
		return m_pass;
	}
}
