/**
 * 
 */
package fm.last.android.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
public class TicketProviderPopup extends ListActivity {
	private HashMap<String, String> mTicketUrls;

	@Override
	public void onCreate(Bundle icicle) {
		ListEntry entry;
		
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.popup);
		
		mTicketUrls = (HashMap<String, String>)(getIntent().getSerializableExtra("ticketurls"));
		
		ListAdapter adapter = new ListAdapter(this, new ImageCache());
		ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();

		if(mTicketUrls != null && mTicketUrls.size() > 0) {
			Iterator<Entry<String, String>> itr = mTicketUrls.entrySet().iterator();
			
			while(itr.hasNext()) {
				HashMap.Entry pairs = (HashMap.Entry)itr.next();
				
				entry = new ListEntry(R.drawable.shopping_cart_dark, R.drawable.shopping_cart_dark, (String)pairs.getKey());
				iconifiedEntries.add(entry);
			}
		}

		adapter.setSourceIconified(iconifiedEntries);
		adapter.setIconsUnscaled();
		setListAdapter(adapter);
		getListView().setDivider(new ColorDrawable(0xffd9d7d7));
	}

	@Override
	public void onResume() {
		((ListAdapter)getListAdapter()).disableLoadBar();
		super.onResume();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String provider = ((ListAdapter)getListAdapter()).getEntry(position).text;
		
		final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(mTicketUrls.get(provider)));
		startActivity(myIntent);
		finish();
	}
}
