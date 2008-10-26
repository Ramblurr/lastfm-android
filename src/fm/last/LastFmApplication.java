package fm.last;

import java.io.FileNotFoundException;

import fm.last.api.Session;
import fm.last.tasks.AuthenticationTask;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import androidx.util.GUITaskQueue;
import androidx.util.ResultReceiver;

public class LastFmApplication extends Application implements ResultReceiver<Session> {
	static private LastFmApplication instance;
	private String m_user;
	private String m_pass;
	private SharedPreferences m_preferences;
	private SQLiteDatabase m_db = null;
	private LastfmRadio radio;
	private Session session;

	public void onCreate() {
		instance = this;
		radio = LastfmRadio.getInstance();
		m_preferences = getPrivatePreferences();
		m_user = m_preferences.getString("username", "");
		m_pass = m_preferences.getString("md5Password", "");

		if (noUsernameSaved()) {
			startLoginActivity();
		} else {
			// start grabbing a session key in the background
			GUITaskQueue.getInstance().addTask(
					new AuthenticationTask(m_user, m_pass, this));
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
	
	public void resultObtained(Session session) {
		this.session = session;
	}	
	
	// called if there was a problem authenticating
	public void handle_exception(Throwable t) {
		Log.e(t);
	}

	public Session getSession() {
		return session;
	}
	
	public String password() {
		return m_pass;
	}
}
