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

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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
    private ListAdapter mListAdapters[] = new ListAdapter[3];
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
		mTabBar.addTab("Artist", R.drawable.similar_artists).setId(TAB_ARTIST);
		mTabBar.addTab("Tag", R.drawable.tags).setId(TAB_TAG);
		mTabBar.addTab("User", R.drawable.top_listeners).setId(TAB_USER);
		tabChanged(TAB_ARTIST, TAB_ARTIST);
        

        mImageCache = new ImageCache();
		
		getListView().setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> l, View v,
					int position, long id) {
				
				((ListAdapter) getListAdapter()).enableLoadBar(position);
				
				if( getListAdapter() == mListAdapters[TAB_ARTIST])
				{
					Artist artist = (Artist)getListAdapter().getItem(position);
					LastFMApplication.getInstance().playRadioStation(NewStation.this,"lastfm://artist/"+Uri.encode(artist.getName())+"/similarartists", true);
				}
				else if( getListAdapter() == mListAdapters[TAB_TAG])
				{
					Tag tag = (Tag)getListAdapter().getItem(position);
					LastFMApplication.getInstance().playRadioStation(NewStation.this,"lastfm://globaltags/"+Uri.encode(tag.getName()), true);
				}
				else if( getListAdapter() == mListAdapters[TAB_USER])
				{
					User user = (User)getListAdapter().getItem(position);
	                Intent profileIntent = new Intent(NewStation.this, fm.last.android.activity.Profile.class);
	                profileIntent.putExtra("lastfm.profile.username", user.getName());
	                startActivity(profileIntent);
				}
					
				
			}
			
		});
    }

    @Override
    public void onResume()
    {
    	for( int tabIndex = 0; tabIndex < 3; tabIndex++ )
    	if(mListAdapters[ tabIndex ] != null) {
    		mListAdapters[ tabIndex ].enableLoadBar(-1);
    		mListAdapters[ tabIndex ].notifyDataSetInvalidated();
    	}
    	
    	super.onResume();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) 
    {
    	if( isFinishing() )
    		return;
    	
    	outState.putInt( "selected_tab", mTabBar.getActive());
    	if( mListAdapters != null )
    		outState.putSerializable( "results", mListAdapters );
    	
    	super.onSaveInstanceState(outState);
    }
    
       
    @Override
    protected void onRestoreInstanceState(Bundle icicle)
    {
		if( icicle == null )
			return;
		
		int selectedTab = icicle.getInt( "selected_tab", -1 );
		if( selectedTab >= 0)
		{
			mTabBar.setActive( selectedTab );
			if( icicle.containsKey( "results" ))
			{
				Object[] results = (Object[]) icicle.getSerializable( "results" );
				for (int i = 0; i < results.length; i++) {
					if( results[i] != null ) {
						mListAdapters[ i ] = (ListAdapter)results[i];
						mListAdapters[ i ].setContext( this );
						mListAdapters[ i ].setImageCache( mImageCache );
					}
				}

				if( mListAdapters[selectedTab] == null )
					return;
				
				setListAdapter(mListAdapters[selectedTab]);
			}
		
			tabChanged(selectedTab, TAB_ARTIST );
		}
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
        }
        else if ( index == TAB_TAG )
        {
            searching = SearchType.Tag;
            searchBar.setHint( "eg. Rock" );
            mHint.setText(getResources().getString(R.string.newstation_hint_tag));
        }
        else if ( index == TAB_USER )
        {
            searching = SearchType.User;
            searchBar.setHint( "eg. Last.hq" );
            mHint.setText(getResources().getString(R.string.newstation_hint_user));
        }
        
        setListAdapter(mListAdapters[index]);
        if( mListAdapters[index] == null || mListAdapters[index].isEmpty() )
        {
        	getListView().setVisibility( View.GONE );
        	findViewById(R.id.search_hint).setVisibility(View.VISIBLE);
        }
        else
        {        	
        	getListView().setVisibility( View.VISIBLE );
        	findViewById(R.id.search_hint).setVisibility(View.GONE);
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
    			for(int i=0; i< ((mTags.length < 6) ? mTags.length : 6); i++) {
	            	Artist[] similar = server.topArtistsForTag(mTags[i].getName());
	            	String artistSample = similar[0].getName() + ", " + similar[1].getName() + ", and " + similar[2].getName();
    				ListEntry entry = new ListEntry(mTags[i], 
    						-1, 
    						mTags[i].getName() + " Tag Radio", 
    						"",
    						R.drawable.list_icon_station,
    						artistSample);
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
                mListAdapters[TAB_TAG] = new ListAdapter(NewStation.this, mImageCache);
    			mListAdapters[TAB_TAG].setSourceIconified(iconifiedEntries);
                setListAdapter( mListAdapters[TAB_TAG] );
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
    			for(int i=0; i< ((mArtists.length < 10) ? mArtists.length : 10); i++) {
    	            if ( mArtists[i].getStreamable().equals("1") ) {
    	            	Artist[] similar = server.getSimilarArtists(mArtists[i].getName(), "3");
    	            	String artistSample = similar[0].getName() + ", " + similar[1].getName() + ", and " + similar[2].getName();
	    				ListEntry entry = new ListEntry(mArtists[i], 
	    						R.drawable.artist_icon, 
	    						mArtists[i].getName() + " Radio", 
	    						mArtists[i].getImages()[0].getUrl(),
	    						R.drawable.list_icon_station,
	    						artistSample);
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
                mListAdapters[TAB_ARTIST] = new ListAdapter(NewStation.this, mImageCache);
				mListAdapters[TAB_ARTIST].setSourceIconified(iconifiedEntries);
                setListAdapter( mListAdapters[TAB_ARTIST] );
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
                mListAdapters[TAB_USER] = new ListAdapter(NewStation.this, mImageCache);
				mListAdapters[TAB_USER].setSourceIconified(iconifiedEntries);
                setListAdapter( mListAdapters[TAB_USER] );
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
