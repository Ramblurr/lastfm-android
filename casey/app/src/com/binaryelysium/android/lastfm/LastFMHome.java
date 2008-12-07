package com.binaryelysium.android.lastfm;

import java.text.DecimalFormat;
import java.text.NumberFormat;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import net.roarsoftware.lastfm.Session;
import net.roarsoftware.lastfm.Radio;
import net.roarsoftware.lastfm.User;
import net.roarsoftware.lastfm.Radio.RadioStation;

public class LastFMHome extends Activity
{

    private SeparatedListAdapter mMainAdapter;
    private LastFMStreamAdapter mMyStationsAdapter;
    private LastFMStreamAdapter mMyRecentAdapter;
    private Worker mProfileImageWorker;
    private RemoteImageHandler mProfileImageHandler;
    private RemoteImageView mProfileImage;
    private User mUser;

    @Override
    public void onCreate( Bundle icicle )
    {

        super.onCreate( icicle );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.home );
        Session session = ( Session ) LastFMApplication.getInstance().map
                .get( "lastfm_session" );
        TextView tv = ( TextView ) findViewById( R.id.home_usersname );
        tv.setText( session.getUsername() );

        ListView mainlistview = ( ListView ) findViewById( R.id.home_main_list );
        mMainAdapter = new SeparatedListAdapter(this); 
        
        SetupMyStations( session );
        SetupRecentStations();

        mainlistview.setAdapter( mMainAdapter );
        mainlistview.setOnItemClickListener( mStationsClickListener );

        Button b = ( Button ) findViewById( R.id.home_startnewstation );
        b.setOnClickListener( mNewStationListener );

        /*
         * b = ( Button ) findViewById( R.id.logout ); b.setOnClickListener(
         * mLogoutListener );
         */

        mProfileImage = ( RemoteImageView ) findViewById( R.id.home_profileimage );

        mProfileImageWorker = new Worker( "profile image worker" );
        mProfileImageHandler = new RemoteImageHandler( mProfileImageWorker
                .getLooper(), mHandler );
        SetupProfile( session );
    }

    private void SetupProfile( final Session session )
    {

        final Handler uiThreadCallback = new Handler();
        final Runnable runInUIThread = new Runnable()
        {

            public void run()
            {

                FinishSetupProfile();
            }
        };

        new Thread()
        {

            @Override
            public void run()
            {

                mUser = User.getInfo( session );
                uiThreadCallback.post( runInUIThread );
            }
        }.start();
    }

    private void FinishSetupProfile()
    {
        if( mUser == null)
            return; //TODO HANDLE
        TextView tv = ( TextView ) findViewById( R.id.home_songsplayed );
        int playcount = mUser.getPlaycount();
        NumberFormat format = NumberFormat.getNumberInstance();
        String count = format.format( playcount );
        tv.setText( count + " " + getString( R.string.home_tracksplayed ) );
        if ( mUser.get64sImageURL() != null )
        {
            mProfileImageHandler
                    .removeMessages( RemoteImageHandler.GET_REMOTE_IMAGE );
            mProfileImageHandler
                    .obtainMessage( RemoteImageHandler.GET_REMOTE_IMAGE,
                            mUser.get64sImageURL() ).sendToTarget();
        }
    }

    private void SetupRecentStations()
    {
        mMyRecentAdapter = new LastFMStreamAdapter( this );
        SQLiteDatabase db = null;
        try
        {
            db = this.openOrCreateDatabase( LastFm.DB_NAME, MODE_PRIVATE, null );
            Cursor c = db.rawQuery( "SELECT Url,Name" + " FROM "
                    + LastFm.DB_TABLE_RECENTSTATIONS + " LIMIT 4;", null );
            int urlColumn = c.getColumnIndex( "Url" );
            int nameColumn = c.getColumnIndex( "Name" );
            if ( c.getCount() > 0 )
            {
                c.moveToFirst();
                int i = 0;
                // Loop through all Results
                do
                {
                    i++;
                    String name = c.getString( nameColumn );
                    String url = c.getString( urlColumn );
                    mMyRecentAdapter.putStation( name, new Radio.RadioStation(
                            url ) );
                }
                while ( c.moveToNext() );
            }
            c.close();
            db.close();
            mMyRecentAdapter.updateModel();
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
        }
        mMainAdapter.addSection( getString(R.string.home_recentstations), mMyRecentAdapter );

    }

    private void SetupMyStations( final Session session )
    {
        mMyStationsAdapter = new LastFMStreamAdapter( this );
        Radio.RadioStation personal = RadioStation.personal( session
                .getUsername() );
        Radio.RadioStation recommended = RadioStation.recommended( session
                .getUsername() );
        Radio.RadioStation neighbours = RadioStation.neighbours( session
                .getUsername() );
        Radio.RadioStation loved = RadioStation.lovedTracks( session
                .getUsername() );
        mMyStationsAdapter.putStation( getString(R.string.home_mylibrary), personal );
        mMyStationsAdapter.putStation( getString(R.string.home_myloved), loved );
        mMyStationsAdapter.putStation( getString(R.string.home_myrecs), recommended );
        mMyStationsAdapter.putStation( getString(R.string.home_myneighborhood), neighbours );
        mMyStationsAdapter.updateModel();
        mMainAdapter.addSection( getString(R.string.home_mystations), mMyStationsAdapter );
    }

    public OnItemClickListener mStationsClickListener = new OnItemClickListener()
    {

        public void onItemClick( AdapterView parent, View v, int position,
                long id )
        {

            final Session session = ( Session ) LastFMApplication.getInstance().map
                    .get( "lastfm_session" );
            Radio.RadioStation r = mMainAdapter.getStation( position );
            Radio t = Radio.newRadio( "AndroidClient", "devel" );
            t.handshake( session.getUsername(), session.getPasswordHash() );
            t.changeStation( r );
            Intent intent = new Intent( LastFMHome.this, LastFMPlayer.class );
            intent.putExtra( "radiostation", t );
            startActivity( intent );
        }
    };

    private OnClickListener mLogoutListener = new OnClickListener()
    {

        public void onClick( View v )
        {

            logout();
        }
    };

    private OnClickListener mNewStationListener = new OnClickListener()
    {

        public void onClick( View v )
        {

            Intent intent = new Intent( LastFMHome.this, LastFMNewStation.class );
            startActivity( intent );
        }
    };

    private final Handler mHandler = new Handler()
    {

        public void handleMessage( Message msg )
        {

            switch ( msg.what )
            {
            case RemoteImageHandler.REMOTE_IMAGE_DECODED:
                mProfileImage.setArtwork( ( Bitmap ) msg.obj );
                mProfileImage.invalidate();
                break;

            default:
                break;
            }
        }
    };
    
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        // Parameters for menu.add are:
        // group -- Not used here.
        // id -- Used only when you want to handle and identify the click yourself.
        // title
        menu.add(Menu.NONE, 0, Menu.NONE, "Logout");
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
        case 0:
            logout();
            return true;
        }
        return false;
    }
    
    private void logout()
    {
        SharedPreferences settings = getSharedPreferences( LastFm.PREFS, 0 );
        SharedPreferences.Editor editor = settings.edit();
        editor.remove( "lastfm_user" );
        editor.remove( "lastfm_pass" );
        editor.commit();
        Intent intent = new Intent( LastFMHome.this, LastFm.class );
        startActivity( intent );
        finish();
    }


}
