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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

import java.io.IOException;
import java.util.Collection;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.R.id;
import fm.last.android.R.layout;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Artist;
import fm.last.api.Tag;

public class NewStation extends ListActivity
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
    private View previousSelectedView;

    @Override
    public void onCreate( Bundle icicle )
    {

        super.onCreate( icicle );
        setContentView( R.layout.newstation );

        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        searchBar = ( EditText ) findViewById( R.id.station_editbox );
        searchBar.setOnKeyListener( mNewStation );

        RadioGroup rgroup = ( RadioGroup ) findViewById( R.id.station_typegroup );
        rgroup.setOnCheckedChangeListener( mRGroup );
        if ( searching == null )
            rgroup.check( R.id.station_type_artist );

        mAdapter = new LastFMStreamAdapter( this );
        setListAdapter( mAdapter );
		getListView().setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> adapter, View view,
					int position, long id) {
				if(previousSelectedView != null) {
					if(previousSelectedView.getTag() == "bottom")
						previousSelectedView.setBackgroundResource(R.drawable.list_item_rest_rounded_bottom);
					else
						previousSelectedView.setBackgroundResource(R.drawable.list_item_rest);
					((ImageView)previousSelectedView.findViewById(R.id.icon)).setImageResource(R.drawable.list_radio_icon_rest);
					((TextView)previousSelectedView.findViewById(R.id.label)).setTextColor(0xFF000000);
				}
				if(position >= 0 && getListView().isFocused()) {
					if(view.getTag() == "bottom")
						view.setBackgroundResource(R.drawable.list_item_focus_rounded_bottom);
					else
						view.setBackgroundResource(R.drawable.list_item_focus);
					((ImageView)view.findViewById(R.id.icon)).setImageResource(R.drawable.list_radio_icon_focus);
					((TextView)view.findViewById(R.id.label)).setTextColor(0xFFFFFFFF);
					previousSelectedView = view;
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				if(previousSelectedView != null) {
					if(previousSelectedView.getTag() == "bottom")
						previousSelectedView.setBackgroundResource(R.drawable.list_item_rest_rounded_bottom);
					else
						previousSelectedView.setBackgroundResource(R.drawable.list_item_rest);
					((ImageView)previousSelectedView.findViewById(R.id.icon)).setImageResource(R.drawable.list_radio_icon_rest);
					((TextView)previousSelectedView.findViewById(R.id.label)).setTextColor(0xFF000000);
				}
				previousSelectedView = null;
			}
	    });
		getListView().setOnFocusChangeListener(new View.OnFocusChangeListener() {

			public void onFocusChange(View v, boolean hasFocus) {
				if(v == getListView()) {
					if(hasFocus)
						getListView().getOnItemSelectedListener().onItemSelected(getListView(), getListView().getSelectedView(), getListView().getSelectedItemPosition(), getListView().getSelectedItemId());
					else
						getListView().getOnItemSelectedListener().onNothingSelected(null);
				}
			}
			
		});
        mDoingSearch = false;
    }

    @Override
    public void onStop()
    {
    	super.onStop();
        mAdapter.updateModel();
    }
    
    private OnCheckedChangeListener mRGroup = new OnCheckedChangeListener()
    {

        public void onCheckedChanged( RadioGroup group, int checked )
        {

            if ( checked == R.id.station_type_artist )
            {
                searching = SearchType.Artist;
                searchBar.setHint( "Enter an Artist" );
            }
            else if ( checked == R.id.station_type_tag )
            {
                searching = SearchType.Tag;
                searchBar.setHint( "Enter a Tag" );
            }
            else if ( checked == R.id.station_type_user )
            {
                searching = SearchType.User;
                searchBar.setHint( "Enter a Username" );
            }
        }
    };

    private OnKeyListener mNewStation = new OnKeyListener()
    {

        public boolean onKey( View v, int i, KeyEvent e )
        {

            if ( e.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && e.getAction() == KeyEvent.ACTION_DOWN )
            {
                if ( mDoingSearch )
                    return true;
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
                        return false;
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
                return true;
            }
            return false;
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
    	}
    };

    public void onListItemClick( ListView l, View v, int position, long id ) {
    	l.getOnItemSelectedListener().onItemSelected(l, v, position, id);
    	ViewSwitcher switcher = (ViewSwitcher)v.findViewById(R.id.row_view_switcher);
    	switcher.showNext();
    	LastFMApplication.getInstance().playRadioStation(this, mAdapter.getStation(position));
    }

}
