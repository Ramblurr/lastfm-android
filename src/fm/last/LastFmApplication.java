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

public class LastFmApplication extends Application {
	static private LastFmApplication instance;
	private String username;
	private String md5Password;
	private SharedPreferences applicationPreferences;
	private SQLiteDatabase m_db = null;
	private LastfmRadio radio;

	public void onCreate() {
		instance = this;
		radio = LastfmRadio.getInstance();
		applicationPreferences = getPrivatePreferences();
		username = applicationPreferences.getString("username", "");
		md5Password = applicationPreferences.getString("md5Password", "");

		if (noUsernameSaved()) {
			startLoginActivity();
		} else {
			// start grabbing a session key in the background
			// let the radio be notified of the session
			radio.obtainSession(null, username, md5Password, new ResultReceiver<Session>() {
				public void handle_exception(Throwable t) {
				}
				// if we get a valid session, we should save the credentials
				public void resultObtained(Session result) {
					saveCredentials(username, md5Password);
				}
			});
		}
	}

	private void startLoginActivity() {
		Intent i = new Intent("ACCOUNTSETTINGS");
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
	
	public boolean noUsernameSaved() {
		applicationPreferences = getPrivatePreferences();
		username = applicationPreferences.getString("username", "").trim();
		return (username.equals(""));
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
		return username;
	}
		
	public String password() {
		return md5Password;
	}
}
