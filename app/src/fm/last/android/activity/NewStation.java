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
import java.util.Collection;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.OnListRowSelectedListener;
import fm.last.android.R;
import fm.last.android.R.id;
import fm.last.android.R.layout;
import fm.last.android.adapter.LastFMStreamAdapter;
import fm.last.android.widget.TabBar;
import fm.last.android.widget.TabBarListener;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Artist;
import fm.last.api.Tag;

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
    private LastFMStreamAdapter mAdapter;
    private boolean mDoingSearch;
    private TabBar mTabBar;
    
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

		mTabBar = (TabBar) findViewById(R.id.TabBar);
		mTabBar.setListener(this);
		mTabBar.addTab("Artist", TAB_ARTIST);
		mTabBar.addTab("Tag", TAB_TAG);
		mTabBar.addTab("User", TAB_USER);
		mTabBar.setActive(TAB_ARTIST);
		//tabChanged("Artist", TAB_ARTIST);
        
        if ( searching == null )
            mTabBar.setActive(TAB_ARTIST);

        mAdapter = new LastFMStreamAdapter( this );
        setListAdapter( mAdapter );
		getListView().setOnItemSelectedListener(new OnListRowSelectedListener(getListView()));
        mDoingSearch = false;
    }

    @Override
    public void onStop()
    {
    	super.onStop();
        mAdapter.updateModel();
    }
    
	public void tabChanged(int index) {
        if ( index == TAB_ARTIST )
        {
            searching = SearchType.Artist;
            searchBar.setHint( "Enter an Artist" );
        }
        else if ( index == TAB_TAG )
        {
            searching = SearchType.Tag;
            searchBar.setHint( "Enter a Tag" );
        }
        else if ( index == TAB_USER )
        {
            searching = SearchType.User;
            searchBar.setHint( "Enter a Username" );
        }
	}

    private OnClickListener mNewStation = new OnClickListener()
    {

        public void onClick( View v )
        {
	        final String txt = ( ( EditText ) findViewById( R.id.station_editbox ) )
	                .getText().toString();
	        final Handler uiThreadCallback = new Handler();
	        final Runnable runInUIThread = new Runnable()
	        {
	
	            public void run()
	            {
	
	                updateList();
	            }
	        };
	        final Session session = ( Session ) LastFMApplication
	                .getInstance().map.get( "lastfm_session" );
	        if ( searching == SearchType.Artist )
	        {
	            mDoingSearch = true;
	            new Thread()
	            {
	
	                @Override
	                public void run()
	                {
	
	                    _searchArtist( txt, session );
	                    uiThreadCallback.post( runInUIThread );
	                }
	            }.start();
	            Toast.makeText( NewStation.this, "Searching...",
	                    Toast.LENGTH_LONG ).show();
	        }
	        else if ( searching == SearchType.Tag )
	        {
	            mDoingSearch = true;
	            new Thread()
	            {
	
	                @Override
	                public void run()
	                {
	
	                    _searchTag( txt, session );
	                    uiThreadCallback.post( runInUIThread );
	                }
	            }.start();
	            Toast.makeText( NewStation.this, "Searching...",
	                    Toast.LENGTH_LONG ).show();
	        }
	        else if ( searching == SearchType.User )
	        {
	            boolean exists = false;//User.userExists( txt, session.getApiKey() );
	            if ( !exists )
	            {
	                AlertDialog.Builder error = new AlertDialog.Builder(
	                        NewStation.this );
	                error.setTitle( "Error" ).setMessage(
	                        "No such user '" + txt + "'." )
	                        .setPositiveButton( "OK", null ).show();
	                return;
	            }
	            else
	            {
	                mAdapter.resetList();
	                /*Radio.RadioStation r = Radio.RadioStation
	                        .personal( txt );
	                mAdapter.putStation( txt + "'s Library", r );*/
	                updateList();
	            }
	        }
        }
    };

    private void updateList()
    {

        mDoingSearch = false;
        mAdapter.updateModel();
    }

    private void _searchTag( String atag, Session session )
    {
        LastFmServer server = AndroidLastFmServerFactory.getServer();
        Tag[] results;
		try {
			mTags = server.searchForTag( atag );

	        runOnUiThread(updateSearchResults);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void _searchArtist( String name, Session session )
    {
        LastFmServer server = AndroidLastFmServerFactory.getServer();
        Artist[] results;
		try {
			mArtists = server.searchForArtist( name );
	        
	        runOnUiThread(updateSearchResults);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private Runnable updateSearchResults = new Runnable() {
    	
    	public void run() {
    		mAdapter.resetList();
    	    if( searching == SearchType.Artist) {
    	        for ( Artist artist : mArtists )
	            {
    	            System.out.println("Artist can stream? " + artist.getStreamable());
    	            if ( artist.getStreamable().equals("1") ) {
    	                System.out.printf("Search match: %s\n", artist.getName());
    	                mAdapter.putStation( artist.getName(), "lastfm://artist/" + Uri.encode(artist.getName()) + "/similarartists" );
	                }
	            }
	        } else if( searching == SearchType.Tag) {
	            for ( Tag tag : mTags )
                {
	                mAdapter.putStation( tag.getName(), "lastfm://globaltags/" + Uri.encode(tag.getName()) );
                }
    		} else if( searching == SearchType.User) {
    			// TODO search result parsing for users
    		}
    		mAdapter.updateModel();
    		getListView().setVisibility(View.VISIBLE);
    		findViewById(R.id.search_hint).setVisibility(View.GONE);
    	}
    };

    public void onListItemClick( ListView l, View v, int position, long id ) {
    	l.getOnItemSelectedListener().onItemSelected(l, v, position, id);
    	ViewSwitcher switcher = (ViewSwitcher)v.findViewById(R.id.row_view_switcher);
    	switcher.showNext();
    	LastFMApplication.getInstance().playRadioStation(this, mAdapter.getStation(position));
    }

}
