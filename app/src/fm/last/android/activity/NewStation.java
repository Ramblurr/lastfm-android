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
import java.io.Serializable;
import java.util.ArrayList;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
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

public class NewStation extends ListActivity implements TabBarListener, Serializable
{
	private static final long serialVersionUID = 2513501434143727293L;

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
		mTabBar.addTab("Artist", R.drawable.similar_artists);
		mTabBar.addTab("Tag", R.drawable.tags);
		mTabBar.addTab("User", R.drawable.top_listeners);
		tabChanged(TAB_ARTIST, TAB_ARTIST);
        

        mImageCache = new ImageCache();
		
		getListView().setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> l, View v,
					int position, long id) {
				
				((ListAdapter) getListAdapter()).enableLoadBar(position);
				
				if( getListAdapter() == mListAdapter[TAB_ARTIST])
				{
					Artist artist = (Artist)getListAdapter().getItem(position);
					LastFMApplication.getInstance().playRadioStation(NewStation.this, "lastfm://artist/"+Uri.encode(artist.getName())+"/similarartists");
				}
				else if( getListAdapter() == mListAdapter[TAB_TAG])
				{
					Tag tag = (Tag)getListAdapter().getItem(position);
					LastFMApplication.getInstance().playRadioStation(NewStation.this, "lastfm://globaltags/"+Uri.encode(tag.getName()));
				}
				else if( getListAdapter() == mListAdapter[TAB_USER])
				{
					User user = (User)getListAdapter().getItem(position);
	                Intent profileIntent = new Intent(NewStation.this, fm.last.android.activity.Profile.class);
	                profileIntent.putExtra("lastfm.profile.username", user.getName());
	                startActivity(profileIntent);
				}
					
				
			}
			
		});
		
		
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
			setListAdapter(mListAdapter[selectedTab]);
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
    	
    	super.onSaveInstanceState(outState);
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
            searchBar.setHint( "eg. Nirvana" );
            mHint.setText(getResources().getString(R.string.newstation_hint_artist));
            setListAdapter(mListAdapter[index]);
        }
        else if ( index == TAB_TAG )
        {
            searching = SearchType.Tag;
            searchBar.setHint( "eg. Rock" );
            mHint.setText(getResources().getString(R.string.newstation_hint_tag));
            setListAdapter(mListAdapter[index]);
        }
        else if ( index == TAB_USER )
        {
            searching = SearchType.User;
            searchBar.setHint( "eg. Last.hq" );
            mHint.setText(getResources().getString(R.string.newstation_hint_user));
            setListAdapter(mListAdapter[index]);
        }
	}

    private OnClickListener mNewStation = new OnClickListener()
    {

        public void onClick( View v )
        {
	        String searchTxt = ( ( EditText ) findViewById( R.id.station_editbox ) ).getText().toString();
        	if (searchTxt == null || searchTxt.length() == 0)
        		return;
	        
            if (searching == SearchType.Artist) {
            	new SearchArtistsTask().execute(searchTxt);
            } else if (searching == SearchType.Tag) {
            	new SearchTagsTask().execute(searchTxt);
            } else if (searching == SearchType.User) {
            	new SearchUsersTask().execute(searchTxt);
            } else {
            	return;
            }
            
        	searchBar.setEnabled(false);
        	mSearchButton.setEnabled(false);
            Toast.makeText( NewStation.this, "Searching...", Toast.LENGTH_LONG ).show();
        }
    };
    
    private class SearchTagsTask extends UserTask<String, Void, ArrayList<ListEntry>> {
    	
        @Override
        public ArrayList<ListEntry> doInBackground(String...params) {
    		try {
    	        LastFmServer server = AndroidLastFmServerFactory.getServer();
   				mTags = server.searchForTag( params[0] );
   				ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
    			for(int i=0; i< ((mTags.length < 10) ? mTags.length : 10); i++){
    				ListEntry entry = new ListEntry(mTags[i], 
    						R.drawable.tag_dark, 
    						mTags[i].getName(), 
    						R.drawable.list_icon_station);
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
        	if(iconifiedEntries != null) {
                mListAdapter[TAB_TAG] = new ListAdapter(NewStation.this, mImageCache);
    			mListAdapter[TAB_TAG].setSourceIconified(iconifiedEntries);
                setListAdapter( mListAdapter[TAB_TAG] );
        		findViewById(R.id.search_hint).setVisibility(View.GONE);
        	} else {
        		Toast.makeText(NewStation.this, "No tags found", Toast.LENGTH_SHORT).show();
        		getListView().setVisibility(View.GONE);
        		findViewById(R.id.search_hint).setVisibility(View.VISIBLE);
        	}
        	searchBar.setEnabled(true);
        	mSearchButton.setEnabled(true);
        	mTabBar.setActive(TAB_TAG);
        }
    }

    private class SearchArtistsTask extends UserTask<String, Void, ArrayList<ListEntry>> {
    	
        @Override
        public ArrayList<ListEntry> doInBackground(String...params) {
    		try {
    	        LastFmServer server = AndroidLastFmServerFactory.getServer();
   				mArtists = server.searchForArtist( params[0] );
   				ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
    			for(int i=0; i< ((mArtists.length < 10) ? mArtists.length : 10); i++){
    	            if ( mArtists[i].getStreamable().equals("1") ) {
	    				ListEntry entry = new ListEntry(mArtists[i], 
	    						R.drawable.tag_dark, 
	    						mArtists[i].getName(), 
	    						mArtists[i].getImages()[0].getUrl(),
	    						R.drawable.list_icon_station);
	    				iconifiedEntries.add(entry);
    	            }
    			}
    			if(iconifiedEntries.size() > 0) {
	    			return iconifiedEntries;
    			}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
            return null;
        }

        @Override
        public void onPostExecute(ArrayList<ListEntry> iconifiedEntries) {
        	if(iconifiedEntries != null) {
                mListAdapter[TAB_ARTIST] = new ListAdapter(NewStation.this, mImageCache);
				mListAdapter[TAB_ARTIST].setSourceIconified(iconifiedEntries);
                setListAdapter( mListAdapter[TAB_ARTIST] );
        		findViewById(R.id.search_hint).setVisibility(View.GONE);
        	} else {
        		Toast.makeText(NewStation.this, "No artists found", Toast.LENGTH_SHORT).show();
        		getListView().setVisibility(View.GONE);
        		findViewById(R.id.search_hint).setVisibility(View.VISIBLE);
        	}
        	searchBar.setEnabled(true);
        	mSearchButton.setEnabled(true);
        	mTabBar.setActive(TAB_ARTIST);
        }
    }

    private class SearchUsersTask extends UserTask<String, Void, ArrayList<ListEntry>> {
    	
        @Override
        public ArrayList<ListEntry> doInBackground(String...params) {
    		try {
    	        LastFmServer server = AndroidLastFmServerFactory.getServer();
   				User user = server.getAnyUserInfo( params[0] );
   				if(user != null) {
	    			ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
					ListEntry entry = new ListEntry(user, 
							R.drawable.profile_unknown, 
							user.getName(),
							user.getImages().length == 0 ? "" : user.getImages()[0].getUrl(),
							R.drawable.list_item_rest_arrow);
					iconifiedEntries.add(entry);
	    			return iconifiedEntries;
   				}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
            return null;
        }

        @Override
        public void onPostExecute(ArrayList<ListEntry> iconifiedEntries) {
        	if(iconifiedEntries != null) {
                mListAdapter[TAB_USER] = new ListAdapter(NewStation.this, mImageCache);
				mListAdapter[TAB_USER].setSourceIconified(iconifiedEntries);
                setListAdapter( mListAdapter[TAB_USER] );
        		findViewById(R.id.search_hint).setVisibility(View.GONE);
        	} else {
        		Toast.makeText(NewStation.this, "No users found", Toast.LENGTH_SHORT).show();
        		getListView().setVisibility(View.GONE);
        		findViewById(R.id.search_hint).setVisibility(View.VISIBLE);
        	}
        	searchBar.setEnabled(true);
        	mSearchButton.setEnabled(true);
        	mTabBar.setActive(TAB_USER);
        }
    }

}
