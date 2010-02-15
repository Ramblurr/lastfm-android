/**
 * 
 */
package fm.last.android.activity;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import fm.last.android.Amazon;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.utils.ImageCache;

/**
 * @author sam
 * 
 */
public class PopupActionActivity extends ListActivity {
	private String mArtistName;
	private String mTrackName;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.popup);
		
		mArtistName = getIntent().getStringExtra("lastfm.artist");
		mTrackName = getIntent().getStringExtra("lastfm.track");
		
		ListAdapter adapter = new ListAdapter(this, new ImageCache());
		ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();

		ListEntry entry = new ListEntry(R.string.action_viewinfo, R.drawable.info_dark, getResources().getString(R.string.action_viewinfo));
		iconifiedEntries.add(entry);

		entry = new ListEntry(R.string.action_share, R.drawable.share_dark, getString(R.string.action_share));
		iconifiedEntries.add(entry);

		entry = new ListEntry(R.string.action_tagtrack, R.drawable.tag_dark, getString(R.string.action_tagtrack));
		iconifiedEntries.add(entry);

		entry = new ListEntry(R.string.action_addplaylist, R.drawable.playlist_dark, getString(R.string.action_addplaylist));
		iconifiedEntries.add(entry);

		if (Amazon.getAmazonVersion(this) > 0) {
			entry = new ListEntry(R.string.action_amazon, R.drawable.shopping_cart_dark, getString(R.string.action_amazon)); // TODO
			iconifiedEntries.add(entry);
		}

		adapter.setSourceIconified(iconifiedEntries);
		adapter.setIconsUnscaled();
		adapter.disableLoadBar();
		setListAdapter(adapter);
		getListView().setDivider(new ColorDrawable(0xffd9d7d7));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = null;

		switch (position) {
		case 0:
			intent = new Intent(this, Metadata.class);
			// TODO: this is inconsistant
			intent.putExtra("artist", mArtistName);
			intent.putExtra("track", mTrackName);
			startActivity(intent);
			break;
		case 1:
			intent = new Intent(this, ShareResolverActivity.class);
			intent.putExtra("lastfm.artist", mArtistName);
			intent.putExtra("lastfm.track", mTrackName);
			startActivity(intent);
			break;
		case 2:
			intent = new Intent(this, Tag.class);
			intent.putExtra("lastfm.artist", mArtistName);
			intent.putExtra("lastfm.track", mTrackName);
			startActivity(intent);
			break;
		case 3:
			intent = new Intent(this, AddToPlaylist.class);
			intent.putExtra("lastfm.artist", mArtistName);
			intent.putExtra("lastfm.track", mTrackName);
			startActivity(intent);
			break;
		case 4:
			LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
					"widget-buy", // Action
					"", // Label
					0); // Value
			Amazon.searchForTrack(this, mArtistName, mTrackName);
			break;
		default:
			break;
		}
		finish();
	}
}
