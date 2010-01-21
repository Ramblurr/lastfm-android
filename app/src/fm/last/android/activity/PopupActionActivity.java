/**
 * 
 */
package fm.last.android.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import fm.last.android.Amazon;
import fm.last.android.LastFMApplication;
import fm.last.android.R;

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

		mArtistName = getIntent().getStringExtra("lastfm.artist");
		mTrackName = getIntent().getStringExtra("lastfm.track");
		String[] actions = new String[(Amazon.getAmazonVersion(this) > 0) ? 5 : 4];
		actions[0] = getString(R.string.action_viewinfo);
		actions[1] = getString(R.string.action_share);
		actions[2] = getString(R.string.action_tag);
		actions[3] = getString(R.string.action_addplaylist);
		if (Amazon.getAmazonVersion(this) > 0) {
			actions[4] = getString(R.string.action_amazon);
		}
		this.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, actions));
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
