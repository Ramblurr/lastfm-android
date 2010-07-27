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

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.adapter.NotificationAdapter;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.UserTask;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.User;
import fm.last.api.WSError;

/**
 * Activity for sharing tracks with Last.fm users and address book entries
 * 
 * The track metadata is passed via intent extras INTENT_EXTRA_TRACK and
 * INTENT_EXTRA_ARTIST
 * 
 * @author Sam Steele <sam@last.fm>
 */
public class Share extends Activity {
	private ListView mFriendsList;
	private ListAdapter mFriendsAdapter;
	private ImageCache mImageCache;
	ListView mDialogList;
	private Dialog mDialog;
	LastFmServer mServer = AndroidLastFmServerFactory.getServer();

	public static final String INTENT_EXTRA_TRACK = "lastfm.track";
	public static final String INTENT_EXTRA_ARTIST = "lastfm.artist";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);

		mFriendsList = (ListView) findViewById(R.id.friends_list_view);
		new LoadFriendsTask().execute((Void) null);
	}

	private ImageCache getImageCache() {
		if (mImageCache == null) {
			mImageCache = new ImageCache();
		}
		return mImageCache;
	}

	private class ShareTrackTask extends UserTask<Void, Void, Boolean> {
		String mArtist;
		String mTrack;
		String mRecipient;

		public ShareTrackTask(String artist, String track, String recipient) {
			mArtist = artist;
			mTrack = track;
			mRecipient = recipient;
		}

		@Override
		public Boolean doInBackground(Void... params) {
			try {
				Session session = LastFMApplication.getInstance().session;
				if(mTrack != null)
					mServer.shareTrack(mArtist, mTrack, mRecipient, session.getKey());
				else
					mServer.shareArtist(mArtist, mRecipient, session.getKey());
				return true;
			} catch (WSError e) {
				// can't presentError here. it's not a UI thread. the app
				// crashes.
				// leave it to the toasting in onPostExecute
				// LastFMApplication.getInstance().presentError(Share.this, e);
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public void onPostExecute(Boolean result) {
			if (mFriendsAdapter != null)
				mFriendsAdapter.disableLoadBar();
			if (mDialog != null) {
				mDialog.dismiss();
				mDialog = null;
			}
			if (result) {
				Share.this.finish();
				Toast.makeText(Share.this, getString(R.string.share_trackshared), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(Share.this, getString(R.string.share_error), Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class LoadFriendsTask extends UserTask<Void, Void, ArrayList<ListEntry>> {
		@Override
		public void onPreExecute() {
			mFriendsList.setAdapter(new NotificationAdapter(Share.this, NotificationAdapter.LOAD_MODE, getString(R.string.common_loading)));
			mFriendsList.setOnItemClickListener(null);
		}

		@Override
		public ArrayList<ListEntry> doInBackground(Void... params) {
			try {
				Session session = LastFMApplication.getInstance().session;
				User[] friends = mServer.getFriends(session.getName(), null, null).getFriends();
				if (friends.length == 0)
					return null;

				ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
				for (int i = 0; i < friends.length; i++) {
					ListEntry entry = new ListEntry(friends[i], R.drawable.profile_unknown, friends[i].getName(), friends[i].getImages().length == 0 ? ""
							: friends[i].getImages()[0].getUrl()); // some
																	// tracks
																	// don't
																	// have
																	// images
					iconifiedEntries.add(entry);
				}
				return iconifiedEntries;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onPostExecute(ArrayList<ListEntry> iconifiedEntries) {
			if (iconifiedEntries != null) {
				mFriendsAdapter = new ListAdapter(Share.this, getImageCache());
				mFriendsAdapter.setSourceIconified(iconifiedEntries);
				mFriendsList.setAdapter(mFriendsAdapter);
				mFriendsList.setOnItemClickListener(new OnItemClickListener() {

					public void onItemClick(AdapterView<?> l, View v, int position, long id) {
						mFriendsAdapter.enableLoadBar(position);
						User user = (User) mFriendsAdapter.getItem(position);
						String artist = getIntent().getStringExtra(INTENT_EXTRA_ARTIST);
						String track = getIntent().getStringExtra(INTENT_EXTRA_TRACK);
						new ShareTrackTask(artist, track, user.getName()).execute((Void) null);
					}
				});
			} else {
				mFriendsList.setAdapter(new NotificationAdapter(Share.this, NotificationAdapter.INFO_MODE, getString(R.string.share_nofriends)));
			}
		}
	}
}
