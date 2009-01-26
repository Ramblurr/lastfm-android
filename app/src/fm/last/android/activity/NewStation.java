/**
 * TODO Complete the Service
 *	Port "Player"
 *	Integrate Player with service
 */
package fm.last.android.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.ArrayList;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.OnListRowSelectedListener;
import fm.last.android.R;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.UserTask;
import fm.last.android.widget.TabBar;
import fm.last.android.widget.TabBarListener;
import fm.last.api.LastFmServer;
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
    private ListAdapter mListAdapter[] = new ListAdapter[3];
    private Button mSearchButton;
    private TabBar mTabBar;
	private ImageCache mImageCache;
	private TextView mHint;
	
	private String mSearchText[] = new String[3];
    
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
        searchBar.setOnKeyListener( new View.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch( event.getKeyCode() )
				{
					case KeyEvent.KEYCODE_ENTER:
						mSearchButton.performClick();
						return true;
					default:
						return false;
				}
			}
			
		});

		mSearchButton = (Button)findViewById(R.id.search);
        mSearchButton.setOnClickListener( mNewStation );
        
        mHint = (TextView)findViewById(R.id.search_hint);

		mTabBar = (TabBar) findViewById(R.id.TabBar);
		mTabBar.setListener(this);
		mTabBar.addTab( "Artist", R.drawable.similar_artists, R.drawable.similar_artists, R.drawable.similar_artists, TAB_ARTIST);
		mTabBar.addTab("Tag", R.drawable.tags, R.drawable.tags, R.drawable.tags, TAB_TAG);
		mTabBar.addTab("User", R.drawable.top_listeners, R.drawable.top_listeners, R.drawable.top_listeners, TAB_USER);
		mTabBar.setActive(TAB_ARTIST);
		tabChanged(TAB_ARTIST, TAB_ARTIST);
        
        if ( searching == null )
            mTabBar.setActive(TAB_ARTIST);

        mImageCache = new ImageCache();
        
		getListView().setOnItemSelectedListener(new OnListRowSelectedListener(getListView()));
		
		if( icicle == null )
			return;
		int selectedTab = icicle.getInt( "selected_tab", -1 );
		if( selectedTab >= 0)
		{
			mTabBar.setActive( selectedTab );
			tabChanged(selectedTab, TAB_ARTIST );
			mListAdapter = (ListAdapter[]) icicle.getSerializable( "results" );
			if( mListAdapter[selectedTab] == null )
				return;
			getListView().setAdapter(mListAdapter[selectedTab]);
			getListView().setVisibility(View.VISIBLE);
    		findViewById(R.id.search_hint).setVisibility(View.GONE);
		}
    }

    @Override
    public void onResume()
    {
    	for( int tabIndex = 0; tabIndex < 3; tabIndex++ )
    	if(mListAdapter[ tabIndex ] != null) {
    		mListAdapter[ tabIndex ].enableLoadBar(-1);
    		mListAdapter[ tabIndex ].notifyDataSetInvalidated();
    	}
    	
    	super.onResume();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) 
    {
    	if( isFinishing() )
    		return;
    	
    	outState.putInt( "selected_tab", mTabBar.getActive());
    	if( mListAdapter != null )
    		outState.putSerializable( "results", mListAdapter );
    	
    }
    
	public void tabChanged(int index, int previousIndex) {
		mSearchText[previousIndex] = searchBar.getText().toString();
        if( mSearchText[index] != null )
        {
        	searchBar.setText( mSearchText[index] );
        	searchBar.setSelection( mSearchText[index].length() );
        } else {
        	searchBar.setText( "" );
        }
		
        if ( index == TAB_ARTIST )
        {
            searching = SearchType.Artist;
            searchBar.setHint( "Enter an Artist" );
            mHint.setText(getResources().getString(R.string.newstation_hint_artist));
            setListAdapter(mListAdapter[index]);
        }
        else if ( index == TAB_TAG )
        {
            searching = SearchType.Tag;
            searchBar.setHint( "Enter a Tag" );
            mHint.setText(getResources().getString(R.string.newstation_hint_tag));
            setListAdapter(mListAdapter[index]);
        }
        else if ( index == TAB_USER )
        {
            searching = SearchType.User;
            searchBar.setHint( "Enter a Username" );
            mHint.setText(getResources().getString(R.string.newstation_hint_user));
            setListAdapter(mListAdapter[index]);
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
        	searchBar.setEnabled(false);
            Toast.makeText( NewStation.this, "Searching...",
                    Toast.LENGTH_LONG ).show();
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
	        final String txt = ( ( EditText ) findViewById( R.id.station_editbox ) )
            	.getText().toString();
	        LastFmServer server = AndroidLastFmServerFactory.getServer();
            boolean success = false;
            mListAdapter[TAB_TAG] = new ListAdapter(NewStation.this, mImageCache);
			getListView().setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> l, View v,
						int position, long id) {
					Tag tag = (Tag)mListAdapter[TAB_TAG].getItem(position);
					mListAdapter[TAB_TAG].enableLoadBar(position);
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
    			mListAdapter[TAB_TAG].setSourceIconified(iconifiedEntries);
    			success = true;
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	if(result) {
                setListAdapter( mListAdapter[TAB_TAG] );
        		getListView().setVisibility(View.VISIBLE);
        		findViewById(R.id.search_hint).setVisibility(View.GONE);
        	} else {
        		Toast.makeText(NewStation.this, "No tags found", Toast.LENGTH_SHORT).show();
        		getListView().setVisibility(View.GONE);
        		findViewById(R.id.search_hint).setVisibility(View.VISIBLE);
        	}
        	searchBar.setEnabled(true);
        }
    }

    private class SearchArtistsTask extends UserTask<Void, Void, Boolean> {
    	
        @Override
    	public void onPreExecute() {
        	searchBar.setEnabled(false);
            Toast.makeText( NewStation.this, "Searching...",
                    Toast.LENGTH_LONG ).show();
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
	        final String txt = ( ( EditText ) findViewById( R.id.station_editbox ) )
            	.getText().toString();
	        LastFmServer server = AndroidLastFmServerFactory.getServer();
            boolean success = false;
            mListAdapter[TAB_ARTIST] = new ListAdapter(NewStation.this, mImageCache);
			getListView().setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> l, View v,
						int position, long id) {
					Artist artist = (Artist)mListAdapter[TAB_ARTIST].getItem(position);
					mListAdapter[TAB_ARTIST].enableLoadBar(position);
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
    				mListAdapter[TAB_ARTIST].setSourceIconified(iconifiedEntries);
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
                setListAdapter( mListAdapter[TAB_ARTIST] );
        		getListView().setVisibility(View.VISIBLE);
        		findViewById(R.id.search_hint).setVisibility(View.GONE);
        	} else {
        		Toast.makeText(NewStation.this, "No artists found", Toast.LENGTH_SHORT).show();
        		getListView().setVisibility(View.GONE);
        		findViewById(R.id.search_hint).setVisibility(View.VISIBLE);
        	}
        	searchBar.setEnabled(true);
        }
    }

    private class SearchUsersTask extends UserTask<Void, Void, Boolean> {
    	
        @Override
    	public void onPreExecute() {
        	searchBar.setEnabled(false);
            Toast.makeText( NewStation.this, "Searching...",
                    Toast.LENGTH_LONG ).show();
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
	        final String txt = ( ( EditText ) findViewById( R.id.station_editbox ) )
            	.getText().toString();
	        LastFmServer server = AndroidLastFmServerFactory.getServer();
            boolean success = false;
            mListAdapter[TAB_USER] = new ListAdapter(NewStation.this, mImageCache);
			getListView().setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> l, View v,
						int position, long id) {
					User user = (User)mListAdapter[TAB_USER].getItem(position);
					mListAdapter[TAB_USER].enableLoadBar(position);
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
							R.drawable.profile_unknown, 
							user.getName(),
							user.getImages().length == 0 ? "" : user.getImages()[0].getUrl(),
							R.drawable.list_item_rest_arrow);
					iconifiedEntries.add(entry);
					mListAdapter[TAB_USER].setSourceIconified(iconifiedEntries);
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
                setListAdapter( mListAdapter[TAB_USER] );
        		getListView().setVisibility(View.VISIBLE);
        		findViewById(R.id.search_hint).setVisibility(View.GONE);
        	} else {
        		Toast.makeText(NewStation.this, "No users found", Toast.LENGTH_SHORT).show();
        		getListView().setVisibility(View.GONE);
        		findViewById(R.id.search_hint).setVisibility(View.VISIBLE);
        	}
        	searchBar.setEnabled(true);
        }
    }

}
