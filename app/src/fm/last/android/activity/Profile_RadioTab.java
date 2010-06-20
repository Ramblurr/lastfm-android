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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ListView;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.LastFm;
import fm.last.android.R;
import fm.last.android.adapter.LastFMStreamAdapter;
import fm.last.android.adapter.SeparatedListAdapter;
import fm.last.android.db.RecentStationsDao;
import fm.last.android.player.IRadioPlayer;
import fm.last.android.player.RadioPlayerService;
import fm.last.api.LastFmServer;
import fm.last.api.RadioPlayList;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.api.Tasteometer;
import fm.last.api.User;

public class Profile_RadioTab extends ListActivity {

	private SeparatedListAdapter mMainAdapter;
	private LastFMStreamAdapter mMyStationsAdapter;
	private LastFMStreamAdapter mMyRecentAdapter;
	private LastFMStreamAdapter mMyPlaylistsAdapter;
	private User mUser;
	private String mUsername; // store this separate so we have access to it
								// before User obj is retrieved
	private boolean isAuthenticatedUser;
	LastFmServer mServer = AndroidLastFmServerFactory.getServer();

	private IntentFilter mIntentFilter;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mUsername = getIntent().getStringExtra("user");
		isAuthenticatedUser = getIntent().getBooleanExtra("authenticated", false);
		
		getListView().setDivider(new ColorDrawable(0xffd9d7d7));
		getListView().setSelector(new ColorDrawable(0x00000000));
		getListView().requestFocus();
		
		mMyRecentAdapter = new LastFMStreamAdapter(this);

		new LoadUserTask().execute((Void) null);
		SetupMyStations();

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(RadioPlayerService.PLAYBACK_ERROR);
		mIntentFilter.addAction(RadioPlayerService.STATION_CHANGED);
		mIntentFilter.addAction("fm.last.android.ERROR");
	}
	
	private void fetchRecentStations() {
		LastFmServer server = AndroidLastFmServerFactory.getServer();

		Session session = LastFMApplication.getInstance().session;
		// Is it worth it?
		if (session != null) {
			try {
				// Let me work it
				Station stations[] = server.getUserRecentStations(session.getName(), session.getKey());
				if (stations != null && stations.length > 0) {
					// I put my thing down, flip it, and reverse it
					List<Station> list = Arrays.asList(stations);
					Collections.reverse(list);
					stations = (Station[]) list.toArray();
					RecentStationsDao.getInstance().clearTable();
					for (Station station : stations) {
						RecentStationsDao.getInstance().appendRecentStation(station.getUrl(), station.getName());
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class LoadUserTask extends AsyncTask<Void, Void, Boolean> {
		Tasteometer tasteometer;

		@Override
		public void onPreExecute() {
			mMyPlaylistsAdapter = null;
		}

		@Override
		public Boolean doInBackground(Void... params) {
			RadioPlayList[] playlists;
			boolean success = false;
			Session session = LastFMApplication.getInstance().session;
			fetchRecentStations();

			// Check our subscriber status
			LastFmServer server = AndroidLastFmServerFactory.getServer();
			try {
				User user = server.getUserInfo(null, session.getKey());
				if (user != null) {
					String subscriber = user.getSubscriber();
					SharedPreferences settings = getSharedPreferences(LastFm.PREFS, 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("lastfm_subscriber", subscriber);
					editor.commit();
					session = new Session(session.getName(), session.getKey(), subscriber);
					LastFMApplication.getInstance().session = session;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if (mUsername == null) {
					mUser = mServer.getUserInfo(null, session.getKey());
					playlists = mServer.getUserPlaylists(session.getName());
				} else {
					mUser = mServer.getUserInfo(mUsername, null);
					tasteometer = mServer.tasteometerCompare(mUsername, session.getName(), 8);
					playlists = mServer.getUserPlaylists(mUsername);
				}
				if (playlists.length > 0) {
					mMyPlaylistsAdapter = new LastFMStreamAdapter(Profile_RadioTab.this);
					for (RadioPlayList playlist : playlists) {
						if (playlist.isStreamable())
							mMyPlaylistsAdapter.putStation(playlist.getTitle(), "lastfm://playlist/" + playlist.getId() + "/shuffle");
					}
				}
				success = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return success;
		}

		@Override
		public void onPostExecute(Boolean result) {
			Session session = LastFMApplication.getInstance().session;
			if (session != null) {
				SetupRecentStations();

				if (!isAuthenticatedUser && Profile_RadioTab.this.mUser != null) {
					SetupCommonArtists(tasteometer);
				}
				if (session.getSubscriber().equals("1") && mMyPlaylistsAdapter != null && mMyPlaylistsAdapter.getCount() > 0)
					mMainAdapter.addSection(getString(R.string.profile_userplaylists, mUsername), mMyPlaylistsAdapter);
				mMainAdapter.notifyDataSetChanged();
			}
		}
	}

	void SetupCommonArtists(Tasteometer ts) {
		mMyRecentAdapter.resetList();

		if(ts != null) {
			for (String name : ts.getResults()) {
				String url = "lastfm://artist/" + Uri.encode(name) + "/similarartists";
				mMyRecentAdapter.putStation(name, url);
			}
		}

		mMyRecentAdapter.updateModel();
	}

	private void RebuildMainMenu() {
		Session session = LastFMApplication.getInstance().session;
		mMainAdapter = new SeparatedListAdapter(this);
		if (isAuthenticatedUser) {
			mMainAdapter.addSection(getString(R.string.profile_mystations), mMyStationsAdapter);
			if (mMyRecentAdapter.getCount() > 0)
				mMainAdapter.addSection(getString(R.string.profile_recentstations), mMyRecentAdapter);
			if (session.getSubscriber().equals("1") && mMyPlaylistsAdapter != null && mMyPlaylistsAdapter.getCount() > 0) {
				mMainAdapter.addSection(getString(R.string.profile_myplaylists), mMyPlaylistsAdapter);
			}
		} else {
			mMainAdapter.addSection(getString(R.string.profile_userstations, mUsername), mMyStationsAdapter);
			mMainAdapter.addSection(getString(R.string.profile_commonartists), mMyRecentAdapter);
			if (session.getSubscriber().equals("1") && mMyPlaylistsAdapter != null && mMyPlaylistsAdapter.getCount() > 0) {
				mMainAdapter.addSection(getString(R.string.profile_userplaylists, mUsername), mMyPlaylistsAdapter);
			}
		}
		if (mMyStationsAdapter != null && mMyStationsAdapter.getCount() > 0)
			mMyStationsAdapter.updateNowPlaying();
		if (mMyRecentAdapter != null && mMyRecentAdapter.getCount() > 0)
			mMyRecentAdapter.updateNowPlaying();
		if (mMyPlaylistsAdapter != null && mMyPlaylistsAdapter.getCount() > 0)
			mMyPlaylistsAdapter.updateNowPlaying();
		setListAdapter(mMainAdapter);
		mMainAdapter.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		registerReceiver(mStatusListener, mIntentFilter);

		SetupRecentStations();
		RebuildMainMenu();
		mMainAdapter.disableLoadBar();
		getListView().setEnabled(true);

		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mStatusListener);
		super.onPause();
	}

	private BroadcastReceiver mStatusListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (action.equals(RadioPlayerService.PLAYBACK_ERROR) || action.equals("fm.last.android.ERROR")) {
				// see also repeated code one page above in OnResume
				RebuildMainMenu();
				mMainAdapter.disableLoadBar();
			} else if (action.equals(RadioPlayerService.STATION_CHANGED)) {
				// Update now playing buttons after the service is re-bound
				SetupRecentStations();
				RebuildMainMenu();
			}
		}
	};

	private void SetupRecentStations() {
		if (!isAuthenticatedUser)
			return;
		mMyRecentAdapter.resetList();
		List<Station> stations = RecentStationsDao.getInstance().getRecentStations();
		if (stations != null) {
			for (Station station : stations) {
				String name = station.getName();
				String url = station.getUrl();
				mMyRecentAdapter.putStation(name, url);
			}
		}
		mMyRecentAdapter.updateModel();
	}

	private void SetupMyStations() {
		Session session = LastFMApplication.getInstance().session;
		mMyStationsAdapter = new LastFMStreamAdapter(this);
		if (isAuthenticatedUser) {
			mMyStationsAdapter.putStation(getString(R.string.profile_mylibrary), "lastfm://user/" + Uri.encode(mUsername) + "/personal");
			if (session.getSubscriber().equals("1"))
				mMyStationsAdapter.putStation(getString(R.string.profile_myloved), "lastfm://user/" + Uri.encode(mUsername) + "/loved");
			mMyStationsAdapter.putStation(getString(R.string.profile_myrecs), "lastfm://user/" + Uri.encode(mUsername) + "/recommended");
			mMyStationsAdapter.putStation(getString(R.string.profile_myneighborhood), "lastfm://user/" + Uri.encode(mUsername) + "/neighbours");
		} else {
			mMyStationsAdapter.putStation(getString(R.string.profile_userlibrary, mUsername), "lastfm://user/" + Uri.encode(mUsername) + "/personal");
			if (session.getSubscriber().equals("1"))
				mMyStationsAdapter.putStation(getString(R.string.profile_userloved, mUsername), "lastfm://user/" + Uri.encode(mUsername) + "/loved");
			mMyStationsAdapter.putStation(getString(R.string.profile_myrecs), "lastfm://user/" + Uri.encode(mUsername) + "/recommended");
			mMyStationsAdapter.putStation(getString(R.string.profile_userneighborhood, mUsername), "lastfm://user/" + Uri.encode(mUsername) + "/neighbours");
		}

		mMyStationsAdapter.updateModel();
	}

	@Override
	public void onListItemClick(ListView l, View v, int p, long id) {
		final ListView list = l;
		final int position = p;

		if (!mMainAdapter.isEnabled(position))
			return;

		LastFMApplication.getInstance().bindService(new Intent(LastFMApplication.getInstance(), fm.last.android.player.RadioPlayerService.class),
				new ServiceConnection() {
					public void onServiceConnected(ComponentName comp, IBinder binder) {
						IRadioPlayer player = IRadioPlayer.Stub.asInterface(binder);
						try {
							String adapter_station = mMainAdapter.getStation(position);
							String current_station = player.getStationUrl();
							if (player.isPlaying() && adapter_station.equals(current_station)) {
								Intent intent = new Intent(Profile_RadioTab.this, Player.class);
								startActivity(intent);
							} else {
								list.setEnabled(false);
								mMainAdapter.enableLoadBar(position);
								LastFMApplication.getInstance().playRadioStation(Profile_RadioTab.this, adapter_station, true);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						LastFMApplication.getInstance().unbindService(this);
					}

					public void onServiceDisconnected(ComponentName comp) {
					}
				}, Context.BIND_AUTO_CREATE);
	}
}
