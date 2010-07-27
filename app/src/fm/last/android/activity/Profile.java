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
import java.util.List;

import android.app.ActivityGroup;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TabHost;
import fm.last.android.LastFMApplication;
import fm.last.android.LastFm;
import fm.last.android.R;
import fm.last.android.player.IRadioPlayer;
import fm.last.android.player.RadioPlayerService;
import fm.last.android.sync.AccountAuthenticatorService;
import fm.last.api.Session;

public class Profile extends ActivityGroup {
	private TabHost mTabHost;
	private boolean mIsPlaying = false;
	
	public static boolean isHTCContactsInstalled(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			pm.getPackageInfo("com.android.htccontacts", 0);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	public void onCreate(Bundle icicle) {
		String username = "";
		boolean isAuthenticatedUser = false;

		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.home);
		Session session = LastFMApplication.getInstance().session;
		if (session == null || session.getName() == null || (Integer.decode(Build.VERSION.SDK) >= 6 && !AccountAuthenticatorService.hasLastfmAccount(this))) {
			LastFMApplication.getInstance().logout();
			Intent intent = new Intent(Profile.this, LastFm.class);
			if(getIntent() != null && getIntent().getStringExtra(SearchManager.QUERY) != null)
				intent.putExtra(SearchManager.QUERY, getIntent().getStringExtra(SearchManager.QUERY));
			startActivity(intent);
			finish();
			return;
		}
		
		if(Integer.decode(Build.VERSION.SDK) >= 6 && !isHTCContactsInstalled(this)) {
			SharedPreferences settings = getSharedPreferences(LastFm.PREFS, 0);
			if(!settings.getBoolean("sync_nag", false)) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("sync_nag", true);
				editor.commit();
				Intent intent = new Intent(Profile.this, SyncPrompt.class);
				startActivity(intent);
			}
		}
		
		Intent intent = getIntent();
		if (intent.getData() != null) {
			if(intent.getData().getScheme() != null && intent.getData().getScheme().equals("lastfm")) {
				LastFMApplication.getInstance().playRadioStation(this, intent.getData().toString(), true);
				finish();
				return;
			} else if(getIntent().getData().getScheme().equals("http")) {  //The search provider sent us an http:// URL, forward it to the metadata screen
				Intent i = null;
				if(intent.getData().getPath().contains("/user/")) {
					List<String> segments = getIntent().getData().getPathSegments();
					username = Uri.decode(segments.get(segments.size() - 1));
				} else {
					i = new Intent(this, Metadata.class);
					i.setData(intent.getData());
					startActivity(i);
					finish();
					return;
				}
			} else {
				Cursor cursor = managedQuery(getIntent().getData(), null, null, null, null);
				if(cursor.moveToNext()) {
					username = cursor.getString(cursor.getColumnIndex("DATA1"));
				}
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
		
		Intent eventsTabIntent = new Intent(this, Profile_EventsTab.class);
		eventsTabIntent.putExtra("user", username);
		
		Intent searchTabIntent = new Intent(this, Profile_SearchTab.class);
		if(getIntent() != null && getIntent().getStringExtra(SearchManager.QUERY) != null)
			searchTabIntent.putExtra(SearchManager.QUERY, getIntent().getStringExtra(SearchManager.QUERY));

		if (isAuthenticatedUser) {
			mTabHost.addTab(mTabHost.newTabSpec("profile")
	                .setIndicator(getString(R.string.profile_myprofile), getResources().getDrawable(R.drawable.ic_tab_profile))
	                .setContent(chartsTabIntent));
			mTabHost.addTab(mTabHost.newTabSpec("events")
	                .setIndicator(getString(R.string.profile_events), getResources().getDrawable(R.drawable.ic_tab_events))
	                .setContent(eventsTabIntent));
			mTabHost.addTab(mTabHost.newTabSpec("search")
	                .setIndicator(getString(R.string.profile_search), getResources().getDrawable(android.R.drawable.ic_menu_search))
	                .setContent(searchTabIntent));
			if(RadioPlayerService.radioAvailable(this)) {
				mTabHost.addTab(mTabHost.newTabSpec("radio")
		                .setIndicator(getString(R.string.profile_myradio), getResources().getDrawable(R.drawable.ic_tab_radio))
		                .setContent(radioTabIntent));
			}
			if(getIntent() != null && getIntent().getStringExtra(SearchManager.QUERY) != null) {
				mTabHost.setCurrentTabByTag("search");
			}
		} else {
			mTabHost.addTab(mTabHost.newTabSpec("profile")
	                .setIndicator(getString(R.string.profile_userprofile, username), getResources().getDrawable(R.drawable.ic_tab_profile))
	                .setContent(chartsTabIntent));
			mTabHost.addTab(mTabHost.newTabSpec("radio")
	                .setIndicator(getString(R.string.profile_userradio, username), getResources().getDrawable(R.drawable.ic_tab_radio))
	                .setContent(radioTabIntent));
		}

		File f = new File(Environment.getExternalStorageDirectory() + "/lastfm-logs.zip");
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
		mIsPlaying = false;
		
		LastFMApplication.getInstance().bindService(new Intent(LastFMApplication.getInstance(), fm.last.android.player.RadioPlayerService.class),
				new ServiceConnection() {
					public void onServiceConnected(ComponentName comp, IBinder binder) {
						IRadioPlayer player = IRadioPlayer.Stub.asInterface(binder);
						try {
							mIsPlaying = player.isPlaying();
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						LastFMApplication.getInstance().unbindService(this);
					}

					public void onServiceDisconnected(ComponentName comp) {
					}
				}, 0);

		if (LastFMApplication.getInstance().session == null) {
			finish(); // We shouldn't really get here, but sometimes the window
						// stack keeps us around
		}
		try {
			LastFMApplication.getInstance().tracker.trackPageView("/Profile");
		} catch (SQLiteException e) {
			//Google Analytics doesn't appear to be thread safe
		}
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
