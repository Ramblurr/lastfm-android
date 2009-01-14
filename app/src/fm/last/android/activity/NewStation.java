/**
 * TODO Complete the Service
 *	Port "Player"
 *	Integrate Player with service
 */
package fm.last.android.activity;

import android.app.ListActivity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.OnListRowSelectedListener;
import fm.last.android.R;
import fm.last.android.R.id;
import fm.last.android.R.layout;
import fm.last.android.adapter.LastFMStreamAdapter;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.UserTask;
import fm.last.android.widget.TabBar;
import fm.last.android.widget.TabBarListener;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Artist;
import fm.last.api.Tag;
import fm.last.api.User;

public class NewStation extends ListActivity implements TabBarListener
{

    private enum SearchType
    {
        Artist, Tag, User
    };

    Artist[] mArtists;
    Tag[] mTags;
    
    private SearchType searching;
    private EditText searchBar;
    private ListAdapter mAdapter;
    private boolean mDoingSearch;
    private TabBar mTabBar;
	private ImageCache mImageCache;
	private TextView mHint;
    
    private final int TAB_ARTIST = 0;
    private final int TAB_TAG = 1;
    private final int TAB_USER = 2;

    @Override
    public void onCreate( Bundle icicle )
    {

        super.onCreate( icicle );
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView( R.layout.newstation );

        searchBar = ( EditText ) findViewById( R.id.station_editbox );
        findViewById( R.id.search ).setOnClickListener( mNewStation );
        
        mHint = (TextView)findViewById(R.id.search_hint);

		mTabBar = (TabBar) findViewById(R.id.TabBar);
		mTabBar.setListener(this);
		mTabBar.addTab("Artist", TAB_ARTIST);
		mTabBar.addTab("Tag", TAB_TAG);
		mTabBar.addTab("User", TAB_USER);
		mTabBar.setActive(TAB_ARTIST);
		tabChanged(TAB_ARTIST);
        
        if ( searching == null )
            mTabBar.setActive(TAB_ARTIST);

        mImageCache = new ImageCache();
        
		getListView().setOnItemSelectedListener(new OnListRowSelectedListener(getListView()));
        mDoingSearch = false;
    }

    @Override
    public void onResume()
    {
    	if(mAdapter != null) {
    		mAdapter.enableLoadBar(-1);
    		mAdapter.notifyDataSetInvalidated();
    	}
    	super.onResume();
    }
    
	public void tabChanged(int index) {
        if ( index == TAB_ARTIST )
        {
            searching = SearchType.Artist;
            searchBar.setHint( "Enter an Artist" );
            mHint.setText(getResources().getString(R.string.newstation_hint_artist));
        }
        else if ( index == TAB_TAG )
        {
            searching = SearchType.Tag;
            searchBar.setHint( "Enter a Tag" );
            mHint.setText(getResources().getString(R.string.newstation_hint_tag));
        }
        else if ( index == TAB_USER )
        {
            searching = SearchType.User;
            searchBar.setHint( "Enter a Username" );
            mHint.setText(getResources().getString(R.string.newstation_hint_user));
        }
	}

    private OnClickListener mNewStation = new OnClickListener()
    {

        public void onClick( View v )
        {
			if(searching == SearchType.Artist ) {
				new SearchArtistsTask().execute((Void)null);
			}
			if(searching == SearchType.Tag ) {
				new SearchTagsTask().execute((Void)null);
			}
			if(searching == SearchType.User ) {
				new SearchUsersTask().execute((Void)null);
			}
        }
    };
    
    private class SearchTagsTask extends UserTask<Void, Void, Boolean> {
    	
        @Override
    	public void onPreExecute() {
            mDoingSearch = true;
            Toast.makeText( NewStation.this, "Searching...",
                    Toast.LENGTH_LONG ).show();
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
	        final String txt = ( ( EditText ) findViewById( R.id.station_editbox ) )
            	.getText().toString();
	        final Session session = ( Session ) LastFMApplication
            	.getInstance().map.get( "lastfm_session" );
	        LastFmServer server = AndroidLastFmServerFactory.getServer();
            boolean success = false;
            mAdapter = new ListAdapter(NewStation.this, mImageCache);
			getListView().setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> l, View v,
						int position, long id) {
					Tag tag = (Tag)mAdapter.getItem(position);
					mAdapter.enableLoadBar(position);
			    	LastFMApplication.getInstance().playRadioStation(NewStation.this, "lastfm://globaltags/"+Uri.encode(tag.getName()));
				}
				
			});
            
    		try {
   				mTags = server.searchForTag( txt );
    			ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
    			for(int i=0; i< ((mTags.length < 10) ? mTags.length : 10); i++){
    				ListEntry entry = new ListEntry(mTags[i], 
    						R.drawable.tag_dark, 
    						mTags[i].getName(), 
    						R.drawable.radio_icon);
    				iconifiedEntries.add(entry);
    			}
    			mAdapter.setSourceIconified(iconifiedEntries);
    			success = true;
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	if(result) {
                setListAdapter( mAdapter );
        		getListView().setVisibility(View.VISIBLE);
        		findViewById(R.id.search_hint).setVisibility(View.GONE);
        	} else {
        		Toast.makeText(NewStation.this, "No tags found", Toast.LENGTH_SHORT);
        		getListView().setVisibility(View.GONE);
        		findViewById(R.id.search_hint).setVisibility(View.VISIBLE);
        	}
        }
    }

    private class SearchArtistsTask extends UserTask<Void, Void, Boolean> {
    	
        @Override
    	public void onPreExecute() {
            mDoingSearch = true;
            Toast.makeText( NewStation.this, "Searching...",
                    Toast.LENGTH_LONG ).show();
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
	        final String txt = ( ( EditText ) findViewById( R.id.station_editbox ) )
            	.getText().toString();
	        final Session session = ( Session ) LastFMApplication
            	.getInstance().map.get( "lastfm_session" );
	        LastFmServer server = AndroidLastFmServerFactory.getServer();
            boolean success = false;
            mAdapter = new ListAdapter(NewStation.this, mImageCache);
			getListView().setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> l, View v,
						int position, long id) {
					Artist artist = (Artist)mAdapter.getItem(position);
					mAdapter.enableLoadBar(position);
			    	LastFMApplication.getInstance().playRadioStation(NewStation.this, "lastfm://artist/"+Uri.encode(artist.getName())+"/similarartists");
				}
				
			});
            
    		try {
   				mArtists = server.searchForArtist( txt );
    			ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
    			for(int i=0; i< ((mArtists.length < 10) ? mArtists.length : 10); i++){
    	            if ( mArtists[i].getStreamable().equals("1") ) {
	    				ListEntry entry = new ListEntry(mArtists[i], 
	    						R.drawable.tag_dark, 
	    						mArtists[i].getName(), 
	    						mArtists[i].getImages()[0].getUrl(),
	    						R.drawable.radio_icon);
	    				iconifiedEntries.add(entry);
    	            }
    			}
    			if(iconifiedEntries.size() > 0) {
	    			mAdapter.setSourceIconified(iconifiedEntries);
	    			success = true;
    			}
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	if(result) {
                setListAdapter( mAdapter );
        		getListView().setVisibility(View.VISIBLE);
        		findViewById(R.id.search_hint).setVisibility(View.GONE);
        	} else {
        		Toast.makeText(NewStation.this, "No artists found", Toast.LENGTH_SHORT);
        		getListView().setVisibility(View.GONE);
        		findViewById(R.id.search_hint).setVisibility(View.VISIBLE);
        	}
        }
    }

    private class SearchUsersTask extends UserTask<Void, Void, Boolean> {
    	
        @Override
    	public void onPreExecute() {
            mDoingSearch = true;
            Toast.makeText( NewStation.this, "Searching...",
                    Toast.LENGTH_LONG ).show();
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
	        final String txt = ( ( EditText ) findViewById( R.id.station_editbox ) )
            	.getText().toString();
	        final Session session = ( Session ) LastFMApplication
            	.getInstance().map.get( "lastfm_session" );
	        LastFmServer server = AndroidLastFmServerFactory.getServer();
            boolean success = false;
            mAdapter = new ListAdapter(NewStation.this, mImageCache);
			getListView().setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> l, View v,
						int position, long id) {
					User user = (User)mAdapter.getItem(position);
					mAdapter.enableLoadBar(position);
                    Intent profileIntent = new Intent(NewStation.this, fm.last.android.activity.Profile.class);
                    profileIntent.putExtra("lastfm.profile.username", user.getName());
                    startActivity(profileIntent);
				}
				
			});
            
    		try {
   				User user = server.getAnyUserInfo( txt );
   				if(user != null) {
	    			ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
					ListEntry entry = new ListEntry(user, 
							R.drawable.artist_icon, 
							user.getName(),
							user.getImages().length == 0 ? "" : user.getImages()[0].getUrl(),
							R.drawable.arrow);
					iconifiedEntries.add(entry);
	    			mAdapter.setSourceIconified(iconifiedEntries);
	    			success = true;
   				}
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	if(result) {
                setListAdapter( mAdapter );
        		getListView().setVisibility(View.VISIBLE);
        		findViewById(R.id.search_hint).setVisibility(View.GONE);
        	} else {
        		Toast.makeText(NewStation.this, "No users found", Toast.LENGTH_SHORT);
        		getListView().setVisibility(View.GONE);
        		findViewById(R.id.search_hint).setVisibility(View.VISIBLE);
        	}
        }
    }

}
