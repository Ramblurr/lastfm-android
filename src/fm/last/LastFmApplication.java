package fm.last;

import java.io.FileNotFoundException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

public class LastFmApplication extends android.app.Application {
	static private LastFmApplication instance;
	private String m_user;
	private String m_pass;
	private String m_sessionKey;
	private SharedPreferences m_preferences;
	private SQLiteDatabase m_db = null;

	public void onCreate() {
		instance = this;
		m_preferences = getPrivatePreferences();
		m_user = m_preferences.getString("username", "");
		m_pass = m_preferences.getString("md5Password", "");
		m_sessionKey = m_preferences.getString("sessionKey", "");

		if (noUsernameSaved()) {
			startLoginActivity();
		}
	}

	private void startLoginActivity() {
		Intent i = new Intent("ACCOUNTSETTINGS");
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
	
	public boolean noUsernameSaved() {
		m_preferences = getPrivatePreferences();
		m_user = m_preferences.getString("username", "").trim();
		return (m_user.equals(""));
	}
	
	private SharedPreferences getPrivatePreferences() {
		return getSharedPreferences("Last.fm", Context.MODE_PRIVATE);
	}

  public void saveCredentials(String username, String md5Password) {
      SharedPreferences prefs = getPrivatePreferences();
      SharedPreferences.Editor prefEdit = prefs.edit();
      prefEdit.putString("username", username);
      prefEdit.putString("md5Password", md5Password);
      prefEdit.commit();
  }
	
	
	public static LastFmApplication instance() {
		return instance;
	}

	public SQLiteDatabase getDb() throws FileNotFoundException {
		if (m_db != null) {
			return m_db;
		}

		m_db = openOrCreateDatabase("lastFm", MODE_PRIVATE, null);
		createTables();
		return m_db;

	}

	private void createTables() {
		m_db
				.execSQL("CREATE TABLE FriendsMap (contactId integer PRIMARY_KEY, username VARCHAR);");
	}

	public String userName() {
		return m_user;
	}

	public String sessionKey() {
		return m_sessionKey;
	}

	public boolean setSessionKey(String sessionKey) {
		m_sessionKey = sessionKey;
		return m_preferences.edit().putString("sessionKey", sessionKey).commit();
	}

	public String password() {
		return m_pass;
	}
}
