/**
 * 
 */
package fm.last.android.activity;

import fm.last.android.LastFMApplication;
import fm.last.android.R;
import android.app.NotificationManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * @author sam
 *
 */
public class Preferences extends PreferenceActivity {
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
    	addPreferencesFromResource(R.xml.preferences);
    	
    	findPreference("scrobble").setOnPreferenceChangeListener(scrobbletoggle);
    	findPreference("scrobble_music_player").setOnPreferenceChangeListener(scrobbletoggle);
	}

	Preference.OnPreferenceChangeListener scrobbletoggle = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			NotificationManager nm = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
			nm.cancel( 1338 );
			return true;
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		LastFMApplication.getInstance().tracker.trackPageView("/Preferences");
	}
}
