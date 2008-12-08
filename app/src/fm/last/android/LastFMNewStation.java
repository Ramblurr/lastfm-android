/**
 * TODO Complete the Service
 *	Port "Player"
 *	Integrate Player with service
 */
package fm.last.android;

import android.app.Activity;
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
import android.widget.RadioGroup;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

import java.io.IOException;
import java.util.Collection;

import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Artist;

public class LastFMNewStation extends Activity
{

    private enum SearchType
    {
        Artist, Tag, User
    };

    private SearchType searching;
    private EditText searchBar;
    private LastFMStreamAdapter mAdapter;
    private boolean mDoingSearch;

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

        ListView listView = ( ListView ) findViewById( R.id.station_list );
        mAdapter = new LastFMStreamAdapter( this );
        listView.setAdapter( mAdapter );
        listView.setOnItemClickListener( mListClickListener );

        mDoingSearch = false;
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
                    Toast.makeText( LastFMNewStation.this, "Searching...",
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
                    Toast.makeText( LastFMNewStation.this, "Searching...",
                            Toast.LENGTH_LONG ).show();
                }
                else if ( searching == SearchType.User )
                {
                    boolean exists = false;//User.userExists( txt, session.getApiKey() );
                    if ( !exists )
                    {
                        AlertDialog.Builder error = new AlertDialog.Builder(
                                LastFMNewStation.this );
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

        /*Collection<String> results = Tag.search( atag, session.getApiKey() );
        mAdapter.resetList();
        for ( String tag : results )
        {
            Radio.RadioStation r = Radio.RadioStation.globalTag( tag );
            mAdapter.putStation( tag, r );
        }*/
    }

    private void _searchArtist( String name, Session session )
    {
        LastFmServer server = AndroidLastFmServerFactory.getServer();
        Artist[] results;
		try {
			results = server.searchForArtist( name );
	        mAdapter.resetList();
	        for ( Artist artist : results )
	        {
	            if ( artist.getStreamable() != "1" )
	                continue;
	            System.out.printf("Search match: %s\n", artist.getName());
	            mAdapter.putStation( artist.getName(), "lastfm://artist/" + Uri.encode(artist.getName()) + "/similar" );
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public OnItemClickListener mListClickListener = new OnItemClickListener()
    {

        public void onItemClick( AdapterView parent, View v, int position,
                long id )
        {

            final Session session = ( Session ) LastFMApplication.getInstance().map
            .get( "lastfm_session" );
		    Intent intent = new Intent( LastFMNewStation.this, LastFMPlayer.class );
		    intent.putExtra( "radiostation", mAdapter.getStation( position ) );
		    startActivity( intent );
        }
    };

}
