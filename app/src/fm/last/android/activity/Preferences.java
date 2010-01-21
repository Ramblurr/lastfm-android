/**
 * 
 */
package fm.last.android.activity;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import fm.last.android.LastFMApplication;
import fm.last.android.R;

/**
 * @author sam
 * 
 */
public class Preferences extends PreferenceActivity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.preferences);

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
		LastFMApplication.getInstance().tracker.trackPageView("/Preferences");
	}
}
