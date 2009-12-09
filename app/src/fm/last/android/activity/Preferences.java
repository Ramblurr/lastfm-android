/**
 * 
 */
package fm.last.android.activity;

import fm.last.android.LastFMApplication;
import fm.last.android.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author sam
 *
 */
public class Preferences extends PreferenceActivity {
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
    	addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onResume() {
		super.onResume();
		LastFMApplication.getInstance().tracker.trackPageView("/Preferences");
	}
}
