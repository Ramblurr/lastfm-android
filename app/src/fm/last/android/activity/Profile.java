/***************************************************************************
 *   Copyright 2005-2009 Last.fm Ltd.                                      *
 *   Portions contributed by Casey Link, Lukasz Wisniewski,                *
 *   Mike Jennings, and Michael Novak Jr.                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
 ***************************************************************************/
package fm.last.android.activity;

import java.io.File;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TabHost;
import fm.last.android.LastFMApplication;
import fm.last.android.LastFm;
import fm.last.android.R;
import fm.last.android.sync.AccountAuthenticatorService;
import fm.last.api.Session;

public class Profile extends ActivityGroup {
	private TabHost mTabHost;
	private boolean mIsPlaying = false;
	
	@Override
	public void onCreate(Bundle icicle) {
		String username = "";
		boolean isAuthenticatedUser = false;

		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.home);
		Session session = LastFMApplication.getInstance().session;
		if (session == null || (Integer.decode(Build.VERSION.SDK) >= 6 && !AccountAuthenticatorService.hasLastfmAccount(this))) {
			LastFMApplication.getInstance().logout();
			Intent intent = new Intent(Profile.this, LastFm.class);
			startActivity(intent);
			finish();
		}
		
		if(Integer.decode(Build.VERSION.SDK) >= 6) {
			SharedPreferences settings = getSharedPreferences(LastFm.PREFS, 0);
			if(!settings.getBoolean("sync_nag", false)) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("sync_nag", true);
				editor.commit();
				Intent intent = new Intent(Profile.this, SyncPrompt.class);
				startActivity(intent);
			}
		}
		
		if(getIntent().getData() != null) {
			Cursor cursor = managedQuery(getIntent().getData(), null, null, null, null);
			if(cursor.moveToNext()) {
				username = cursor.getString(cursor.getColumnIndex("DATA1"));
			}
		} else {
			username = getIntent().getStringExtra("lastfm.profile.username");
		}
		
		if (username == null) {
			username = session.getName();
			isAuthenticatedUser = true;
		} else
			isAuthenticatedUser = false;

		mTabHost = (TabHost)findViewById(R.id.TabBar);
		mTabHost.setup(getLocalActivityManager());

		Intent radioTabIntent = new Intent(this, Profile_RadioTab.class);
		radioTabIntent.putExtra("user", username);
		radioTabIntent.putExtra("authenticated", isAuthenticatedUser);
		
		Intent chartsTabIntent = new Intent(this, Profile_ChartsTab.class);
		chartsTabIntent.putExtra("user", username);
		
		if (isAuthenticatedUser) {
			mTabHost.addTab(mTabHost.newTabSpec("radio")
	                .setIndicator(getString(R.string.profile_myradio), getResources().getDrawable(R.drawable.radio))
	                .setContent(radioTabIntent));
			mTabHost.addTab(mTabHost.newTabSpec("profile")
	                .setIndicator(getString(R.string.profile_myprofile), getResources().getDrawable(R.drawable.profile))
	                .setContent(chartsTabIntent));
		} else {
			mTabHost.addTab(mTabHost.newTabSpec("radio")
	                .setIndicator(getString(R.string.profile_userradio, username), getResources().getDrawable(R.drawable.radio))
	                .setContent(radioTabIntent));
			mTabHost.addTab(mTabHost.newTabSpec("profile")
	                .setIndicator(getString(R.string.profile_userprofile, username), getResources().getDrawable(R.drawable.profile))
	                .setContent(chartsTabIntent));
		}

		if (getIntent().getBooleanExtra("lastfm.profile.new_user", false))
			startActivity(new Intent(Profile.this, NewStation.class));

		File f = new File("/sdcard/lastfm-logs.zip");
		if (f.exists()) {
			Log.i("Last.fm", "Removing stale bug report archive");
			f.delete();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// the event list adapter (a SeparatedListAdapter) doesn't serialise,
		// so move away from it if we happen to be looking at it now.
		// FIXME: make the SeparatedListAdapter serialize.

		outState.putString("selected_tab", mTabHost.getCurrentTabTag());
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		mTabHost.setCurrentTabByTag(state.getString("selected_tab"));
	}

	@Override
	public void onResume() {
		if (LastFMApplication.getInstance().session == null) {
			finish(); // We shouldn't really get here, but sometimes the window
						// stack keeps us around
		}
		LastFMApplication.getInstance().tracker.trackPageView("/Profile");
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Parameters for menu.add are:
		// group -- Not used here.
		// id -- Used only when you want to handle and identify the click
		// yourself.
		// title
		MenuItem logout = menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.action_logout));
		logout.setIcon(R.drawable.logout);

		MenuItem settings = menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.action_settings));
		settings.setIcon(android.R.drawable.ic_menu_preferences);

		//TODO: Finish the help document and then re-enable this item
		//MenuItem help = menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.action_help));
		//help.setIcon(android.R.drawable.ic_menu_help);

		MenuItem nowPlaying = menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.action_nowplaying));
		nowPlaying.setIcon(R.drawable.view_artwork);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(3).setEnabled(mIsPlaying);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case 0:
			LastFMApplication.getInstance().logout();
			intent = new Intent(Profile.this, LastFm.class);
			startActivity(intent);
			finish();
			break;
		case 1:
			intent = new Intent(Profile.this, Preferences.class);
			startActivity(intent);
			return true;
		case 2:
			intent = new Intent(Profile.this, Help.class);
			startActivity(intent);
			return true;
		case 3:
			intent = new Intent(Profile.this, Player.class);
			startActivity(intent);
			return true;
		}
		return false;
	}
}
