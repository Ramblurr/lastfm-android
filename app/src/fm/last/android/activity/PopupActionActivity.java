/**
 * 
 */
package fm.last.android.activity;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;
import fm.last.android.Amazon;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.player.RadioPlayerService;
import fm.last.android.utils.ImageCache;
import fm.last.api.LastFmServer;

/**
 * @author sam
 * 
 */
public class PopupActionActivity extends ListActivity {
	private String mArtistName;
	private String mTrackName;
	private String mAlbumName;
	private IntentFilter mIntentFilter;

	@Override
	public void onCreate(Bundle icicle) {
		ListEntry entry;
		
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.popup);
		
		mArtistName = getIntent().getStringExtra("lastfm.artist");
		mTrackName = getIntent().getStringExtra("lastfm.track");
		mAlbumName = getIntent().getStringExtra("lastfm.album");
		
		ListAdapter adapter = new ListAdapter(this, new ImageCache());
		ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();

		if(!RadioPlayerService.radioAvailable(this) || getIntent().getBooleanExtra("lastfm.nowplaying", false)) {
			entry = new ListEntry(R.drawable.info_dark, R.drawable.info_dark, getResources().getString(R.string.action_viewinfo));
			iconifiedEntries.add(entry);
		} else {
			entry = new ListEntry(R.drawable.radio_dark, R.drawable.radio_dark, getResources().getString(R.string.action_similar));
			iconifiedEntries.add(entry);
		}
		
		if(mTrackName != null) {
			entry = new ListEntry(R.drawable.share_dark, R.drawable.share_dark, getString(R.string.action_share));
			iconifiedEntries.add(entry);
	
			entry = new ListEntry(R.drawable.tag_dark, R.drawable.tag_dark, getString(R.string.action_tagtrack));
			iconifiedEntries.add(entry);
	
			entry = new ListEntry(R.drawable.playlist_dark, R.drawable.playlist_dark, getString(R.string.action_addplaylist));
			iconifiedEntries.add(entry);
			
			if(!getIntent().getBooleanExtra("lastfm.nowplaying", false)) {
				entry = new ListEntry(R.drawable.love, R.drawable.love, getString(R.string.action_love));
				iconifiedEntries.add(entry);
				
				entry = new ListEntry(R.drawable.ban, R.drawable.ban, getString(R.string.action_ban));
				iconifiedEntries.add(entry);
			}
		}
		
		if (Amazon.getAmazonVersion(this) > 0) {
			entry = new ListEntry(R.drawable.shopping_cart_dark, R.drawable.shopping_cart_dark, getString(R.string.action_amazon)); // TODO
			iconifiedEntries.add(entry);
		}

		adapter.setSourceIconified(iconifiedEntries);
		adapter.setIconsUnscaled();
		setListAdapter(adapter);
		getListView().setDivider(new ColorDrawable(0xffd9d7d7));
		
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(RadioPlayerService.PLAYBACK_ERROR);
		mIntentFilter.addAction(RadioPlayerService.STATION_CHANGED);
		mIntentFilter.addAction("fm.last.android.ERROR");
	}

	@Override
	public void onResume() {
		registerReceiver(mStatusListener, mIntentFilter);
		((ListAdapter)getListAdapter()).disableLoadBar();
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
				((ListAdapter)getListAdapter()).disableLoadBar();
			} else {
				finish();
			}
		}
	};
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = null;
		Integer i = (Integer)getListAdapter().getItem(position);
		
		switch (i) {
		case R.drawable.info_dark:
			intent = new Intent(this, Metadata.class);
			intent.putExtra("artist", mArtistName);
			intent.putExtra("track", mTrackName);
			startActivity(intent);
			break;
		case R.drawable.radio_dark:
			((ListAdapter)getListAdapter()).enableLoadBar(position);
			LastFMApplication.getInstance().playRadioStation(this, "lastfm://artist/" + Uri.encode(mArtistName) + "/similarartists", true);
			return;
		case R.drawable.share_dark:
			intent = new Intent(this, ShareResolverActivity.class);
			intent.putExtra("lastfm.artist", mArtistName);
			if(mTrackName != null)
				intent.putExtra("lastfm.track", mTrackName);
			if(mAlbumName != null)
				intent.putExtra("lastfm.album", mAlbumName);
			startActivity(intent);
			break;
		case R.drawable.tag_dark:
			intent = new Intent(this, Tag.class);
			intent.putExtra("lastfm.artist", mArtistName);
			intent.putExtra("lastfm.track", mTrackName);
			startActivity(intent);
			break;
		case R.drawable.playlist_dark:
			intent = new Intent(this, AddToPlaylist.class);
			intent.putExtra("lastfm.artist", mArtistName);
			intent.putExtra("lastfm.track", mTrackName);
			startActivity(intent);
			break;
		case R.drawable.love:
			try {
				LastFmServer server = AndroidLastFmServerFactory.getServer();
				server.loveTrack(mArtistName, mTrackName, LastFMApplication.getInstance().session.getKey());
				Toast.makeText(LastFMApplication.getInstance(), getString(R.string.scrobbler_trackloved), Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
			}
			break;
		case R.drawable.ban:
			try {
				LastFmServer server = AndroidLastFmServerFactory.getServer();
				server.banTrack(mArtistName, mTrackName, LastFMApplication.getInstance().session.getKey());
				Toast.makeText(LastFMApplication.getInstance(), getString(R.string.scrobbler_trackbanned), Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
			}
			break;
		case R.drawable.shopping_cart_dark:
			if(getIntent().getBooleanExtra("lastfm.nowplaying", false)) {
				try {
					LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
							"widget-buy", // Action
							"", // Label
							0); // Value
				} catch (SQLiteException e) {
					//Google Analytics doesn't appear to be thread safe
				}
			} else {
				try {
					LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
							"charts-buy", // Action
							"", // Label
							0); // Value
				} catch (SQLiteException e) {
					//Google Analytics doesn't appear to be thread safe
				}
			}
			if(mTrackName != null)
				Amazon.searchForTrack(this, mArtistName, mTrackName);
			
			if(mAlbumName != null)
				Amazon.searchForAlbum(this, mArtistName, mAlbumName);
			break;
		default:
			break;
		}
		finish();
	}
}
