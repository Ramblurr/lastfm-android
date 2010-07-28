/**
 * 
 */
package fm.last.android.activity;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.player.RadioPlayerService;
import fm.last.android.sync.AccountAuthenticatorService;

/**
 * @author sam
 * 
 */
public class Preferences extends PreferenceActivity {
	private boolean shouldForceSync = false;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.preferences_scrobbler);
		if(RadioPlayerService.radioAvailable(this))
			addPreferencesFromResource(R.xml.preferences_player);
		if(Integer.decode(Build.VERSION.SDK) >= 6) {
			addPreferencesFromResource(R.xml.preferences_sync);
			findPreference("sync_icons").setOnPreferenceChangeListener(syncToggle);
			findPreference("sync_names").setOnPreferenceChangeListener(syncToggle);
			findPreference("sync_taste").setOnPreferenceChangeListener(syncToggle);
		}
		addPreferencesFromResource(R.xml.preferences_about);
		findPreference("scrobble").setOnPreferenceChangeListener(scrobbletoggle);
		findPreference("scrobble_music_player").setOnPreferenceChangeListener(scrobbletoggle);
		findPreference("tos").setOnPreferenceClickListener(urlClick);
		findPreference("privacy").setOnPreferenceClickListener(urlClick);
		try {
			findPreference("version").setSummary(getPackageManager().getPackageInfo("fm.last.android", 0).versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if(shouldForceSync) {
			AccountAuthenticatorService.resyncAccount(this);
		}
	}

	Preference.OnPreferenceChangeListener syncToggle = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			shouldForceSync = true;
			return true;
		}
	};

	Preference.OnPreferenceClickListener urlClick = new Preference.OnPreferenceClickListener() {

		public boolean onPreferenceClick(Preference preference) {
			Intent i = null;
			if (preference.getKey().equals("tos"))
				i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.last.fm/legal/terms"));
			if (preference.getKey().equals("privacy"))
				i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.last.fm/legal/privacy"));

			if (i != null)
				startActivity(i);
			return false;
		}
	};

	Preference.OnPreferenceChangeListener scrobbletoggle = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			nm.cancel(1338);
			return true;
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		try {
			LastFMApplication.getInstance().tracker.trackPageView("/Preferences");
		} catch (SQLiteException e) {
			//Google Analytics doesn't appear to be thread safe
		}
	}
}
