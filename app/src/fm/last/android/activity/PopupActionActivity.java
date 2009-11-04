/**
 * 
 */
package fm.last.android.activity;

import fm.last.android.LastFMApplication;
import fm.last.android.R;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * @author sam
 *
 */
public class PopupActionActivity extends ListActivity {
	private String mArtistName;
	private String mTrackName;
	
	private boolean isAmazonInstalled() {
		PackageManager pm = getPackageManager();
		boolean result = false;
		try {
			pm.getPackageInfo("com.amazon.mp3", PackageManager.GET_ACTIVITIES);
			result = true;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}
	
	public void onCreate(Bundle icicle) { 
	    super.onCreate(icicle); 
		requestWindowFeature(Window.FEATURE_NO_TITLE);

	    mArtistName = getIntent().getStringExtra("lastfm.artist");
	    mTrackName = getIntent().getStringExtra("lastfm.track");
		String[] actions = new String[isAmazonInstalled()?5:4];
		actions[0] = getString(R.string.action_viewinfo);
		actions[1] = getString(R.string.action_share);
		actions[2] = getString(R.string.action_tag);
		actions[3] = getString(R.string.action_addplaylist);
	    if(isAmazonInstalled()) {
	    	actions[4] = getString(R.string.action_amazon);
	    }
	    this.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, actions)); 
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent;
		
		switch(position) {
		case 0:
	        intent = new Intent( this, Metadata.class );
	        //TODO: this is inconsistant
	        intent.putExtra("artist", mArtistName);
	        intent.putExtra("track", mTrackName);
	        startActivity( intent );
			break;
		case 1:
	        intent = new Intent( this, ShareResolverActivity.class );
	        intent.putExtra("lastfm.artist", mArtistName);
	        intent.putExtra("lastfm.track", mTrackName);
	        startActivity( intent );
			break;
		case 2:
	        intent = new Intent( this, Tag.class );
	        intent.putExtra("lastfm.artist", mArtistName);
	        intent.putExtra("lastfm.track", mTrackName);
	        startActivity( intent );
			break;
		case 3:
	        intent = new Intent( this, AddToPlaylist.class );
	        intent.putExtra("lastfm.artist", mArtistName);
	        intent.putExtra("lastfm.track", mTrackName);
	        startActivity( intent );
			break;
		case 4:
            String query = mArtistName + " " + mTrackName;
            int searchType = 0;
            try {
                intent = new Intent( Intent.ACTION_SEARCH );
                intent.setComponent(new ComponentName("com.amazon.mp3","com.amazon.mp3.android.client.SearchActivity"));
                intent.putExtra("actionSearchString", query);
                intent.putExtra("actionSearchType", searchType);
                startActivity( intent );
            } catch (Exception e) {
				LastFMApplication.getInstance().presentError(this, getString(R.string.ERROR_AMAZON_TITLE),
						getString(R.string.ERROR_AMAZON));
            }
			break;
		default:
			break;
		}
		finish();
	} 
}
