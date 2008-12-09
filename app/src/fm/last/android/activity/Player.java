package fm.last.android.activity;

import java.util.Formatter;
import java.util.Locale;

import fm.last.android.LastFMApplication;
import fm.last.android.LastFm;
import fm.last.android.R;
import fm.last.android.RemoteImageHandler;
import fm.last.android.RemoteImageView;
import fm.last.android.Worker;
import fm.last.android.player.RadioPlayerService;
import fm.last.api.Session;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;

public class Player extends Activity
{

    private fm.last.android.player.IRadioPlayer mService = null;
    private Button mBackButton;
    private ImageButton mStopButton;
    private ImageButton mNextButton;
    private RemoteImageView mAlbum;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private TextView mArtistName;
    private TextView mTrackName;
    private TextView mStationName;
    private ProgressBar mProgress;
    private long mPosOverride = -1;
    private long mDuration;
    private boolean paused;
    private Intent mpIntent;
    private boolean mRelaunchAfterConfigChange;

    private static final int REFRESH = 1;


    private Worker mAlbumArtWorker;
    private RemoteImageHandler mAlbumArtHandler;

    @Override
    public void onCreate( Bundle icicle )
    {

        super.onCreate( icicle );
        requestWindowFeature( Window.FEATURE_NO_TITLE );

        setContentView( R.layout.audio_player );

        mCurrentTime = ( TextView ) findViewById( R.id.currenttime );
        mTotalTime = ( TextView ) findViewById( R.id.totaltime );
        mProgress = ( ProgressBar ) findViewById( android.R.id.progress );
        mProgress.setMax( 1000 );
        mAlbum = ( RemoteImageView ) findViewById( R.id.album );
        mStationName = ( TextView ) findViewById( R.id.station_name );
        mArtistName = ( TextView ) findViewById( R.id.track_artist );
        mTrackName = ( TextView ) findViewById( R.id.track_title );
        mBackButton = ( Button ) findViewById( R.id.player_backBtn );
        mBackButton.setOnClickListener( mBackListener );
        mStopButton = ( ImageButton ) findViewById( R.id.stop );
        mStopButton.requestFocus();
        mStopButton.setOnClickListener( mStopListener );
        mNextButton = ( ImageButton ) findViewById( R.id.skip );
        mNextButton.setOnClickListener( mNextListener );
        mpIntent = new Intent(
                this,
                fm.last.android.player.RadioPlayerService.class );
        startService( mpIntent );
        boolean b = bindService( mpIntent, mConnection, 0 );
        if ( !b )
        {
            // something went wrong
            // mHandler.sendEmptyMessage(QUIT);
            System.out.println( "Binding to service failed " + mConnection );
        }

        mAlbumArtWorker = new Worker( "album art worker" );
        mAlbumArtHandler = new RemoteImageHandler( mAlbumArtWorker.getLooper(), mHandler );

        if ( icicle != null )
        {
            mRelaunchAfterConfigChange = icicle.getBoolean( "configchange" );
        }
    }

    @Override
    public void onStart()
    {

        super.onStart();
        paused = false;
        IntentFilter f = new IntentFilter();
        f.addAction( RadioPlayerService.META_CHANGED );
        f.addAction( RadioPlayerService.PLAYBACK_FINISHED );
        f.addAction( RadioPlayerService.PLAYBACK_STATE_CHANGED );
        f.addAction( RadioPlayerService.STATION_CHANGED );
        registerReceiver( mStatusListener, new IntentFilter( f ) );
        long next = refreshNow();
        queueNextRefresh( next );
    }

    @Override
    public void onStop()
    {

        paused = true;
        mHandler.removeMessages( REFRESH );
        unregisterReceiver( mStatusListener );
        unbindService( mConnection );
        super.onStop();
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {

        outState.putBoolean( "configchange", getChangingConfigurations() != 0 );
        super.onSaveInstanceState( outState );
    }

    @Override
    public void onResume()
    {

        super.onResume();
        updateTrackInfo();
    }

    @Override
    public void onDestroy()
    {

        mAlbumArtWorker.quit();
        super.onDestroy();
    }

    /*private View.OnClickListener mLoveListener = new View.OnClickListener()
    {

        public void onClick( View v )
        {

            if ( mService == null )
                return;
            try
            {
                mService.love();
            }
            catch ( RemoteException ex )
            {
                System.out.println( ex.getMessage() );
            }
        }
    };*/

    /*private View.OnClickListener mBanListener = new View.OnClickListener()
    {

        public void onClick( View v )
        {

            if ( mService == null )
                return;
            try
            {
                mService.ban();
            }
            catch ( RemoteException ex )
            {
                System.out.println( ex.getMessage() );
            }
        }
    };*/

    private View.OnClickListener mNextListener = new View.OnClickListener()
    {

        public void onClick( View v )
        {

            if ( mService == null )
                return;
            try
            {
                mService.skip();
            }
            catch ( RemoteException ex )
            {
                System.out.println( ex.getMessage() );
            }
        }
    };

    private View.OnClickListener mBackListener = new View.OnClickListener()
    {
    	public void onClick( View v )
    	{
    		finish();
    	}
    };
    
    private View.OnClickListener mStopListener = new View.OnClickListener()
    {

        public void onClick( View v )
        {

            if ( mService == null )
                return;
            try
            {
                mService.stop();
            }
            catch ( RemoteException ex )
            {
                System.out.println( ex.getMessage() );
            }
            finish();
        }
    };

    private BroadcastReceiver mStatusListener = new BroadcastReceiver()
    {

        @Override
        public void onReceive( Context context, Intent intent )
        {

            String action = intent.getAction();
            if ( action.equals( RadioPlayerService.META_CHANGED ) )
            {
                // redraw the artist/title info and
                // set new max for progress bar
                updateTrackInfo();
            }
            else if ( action.equals( RadioPlayerService.PLAYBACK_FINISHED ) )
            {
                finish();
            }
            else if ( action.equals( RadioPlayerService.STATION_CHANGED ) )
            {
                try
                {
                    mStationName.setText( mService.getStationName() );
                }
                catch ( RemoteException e )
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };

    private void updateTrackInfo()
    {

        System.out.println( "Updating track info" );
        if ( mService == null )
        {
            return;
        }
        try
        {
            /*
             * if (mService.getPath() == null) { finish(); return; }
             */// TODO if player is done finish()
            String artistName = mService.getArtistName();
            mArtistName.setText( artistName );
            mTrackName.setText( mService.getTrackName() );
            String artUrl = mService.getArtUrl();
            if ( artUrl != RadioPlayerService.UNKNOWN )
            {
                mAlbumArtHandler.removeMessages( RemoteImageHandler.GET_REMOTE_IMAGE );
                mAlbumArtHandler.obtainMessage( RemoteImageHandler.GET_REMOTE_IMAGE, artUrl )
                        .sendToTarget();
            }

            mDuration = mService.getDuration();
            System.out.println( "Setting track duration to: " + mDuration );
            mTotalTime.setText( makeTimeString( this, mDuration / 1000 ) );
        }
        catch ( RemoteException ex )
        {
            finish();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection()
    {

        public void onServiceConnected( ComponentName className, IBinder service )
        {

            mService = fm.last.android.player.IRadioPlayer.Stub
                    .asInterface( service );
            startPlayback();
        }

        public void onServiceDisconnected( ComponentName className )
        {

            mService = null;
        }
    };

    private void startPlayback()
    {

        if ( mService == null )
            return;
        
        Intent intent = getIntent();
        if ( intent.hasExtra( "radiostation" ) )
        {
            String url = intent.getExtras().getString( "radiostation" );
            try
            {
                Session session = ( Session ) LastFMApplication.getInstance().map
                        .get( "lastfm_session" );
                if ( !mRelaunchAfterConfigChange )
                {
                    mService.setSession( session );
                    mService.tune( url, session );
                    mService.startRadio();
                    appendRecentStation( url, mService.getStationName() );
                }
            }
            catch ( Exception e )
            {
                Log.d( "LastFMPlayer", "couldn't start playback: " + e );
            }
        }
        updateTrackInfo();
        //long next = refreshNow();
        //queueNextRefresh( next );
    }

    private void appendRecentStation( String url, String name )
    {

        SQLiteDatabase db = null;
        try
        {
            db = this.openOrCreateDatabase( LastFm.DB_NAME, MODE_PRIVATE, null );
            db
                    .execSQL( "CREATE TABLE IF NOT EXISTS "
                            + LastFm.DB_TABLE_RECENTSTATIONS
                            + " (Url VARCHAR UNIQUE NOT NULL, Name VARCHAR NOT NULL, Id INTEGER PRIMARY KEY NOT NULL);" );
            Cursor c = db.rawQuery( "SELECT Id" + " FROM "
                    + LastFm.DB_TABLE_RECENTSTATIONS + ";", null );
            if ( c.getCount() > 4 )
            {
                c.moveToFirst();
                int max = -1;
                do
                {
                    int foo = c.getInt( 0 );
                    if ( foo > max )
                        max = foo;
                }
                while ( c.moveToNext() );
                int min = max - 3;
                db.execSQL( "DELETE FROM " + LastFm.DB_TABLE_RECENTSTATIONS
                        + " WHERE Id < " + min + ";" );
            }
            c.close();
            db.execSQL( "INSERT INTO " + LastFm.DB_TABLE_RECENTSTATIONS
                    + "(Url, Name) " + "VALUES ('" + url + "', '" + name
                    + "');" );
            db.close();
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
        }
    }

    private void queueNextRefresh( long delay )
    {

        if ( !paused )
        {
            Message msg = mHandler.obtainMessage( REFRESH );
            mHandler.removeMessages( REFRESH );
            mHandler.sendMessageDelayed( msg, delay );
        }
    }

    private long refreshNow()
    {

        if ( mService == null )
            return 500;
        try
        {
            long pos = mPosOverride < 0 ? mService.getPosition() : mPosOverride;
            long remaining = 1000 - ( pos % 1000 );
            if ( ( pos >= 0 ) && ( mDuration > 0 )  && ( pos <= mDuration ))
            {
                mCurrentTime.setText( makeTimeString( this, pos / 1000 ) );

            	if ( mService.isPlaying() )
                {
                    mCurrentTime.setVisibility( View.VISIBLE );
                }
                else
                {
                    // blink the counter
                    int vis = mCurrentTime.getVisibility();
                    mCurrentTime
                            .setVisibility( vis == View.INVISIBLE ? View.VISIBLE
                                    : View.INVISIBLE );
                    remaining = 500;
                }

                mProgress.setProgress( ( int ) ( 1000 * pos / mDuration ) );

                mProgress
                        .setSecondaryProgress( mService.getBufferPercent() * 10 );
            }
            else
            {
                mCurrentTime.setText( "--:--" );
                mProgress.setProgress( 1000 );
            }
            // return the number of milliseconds until the next full second, so
            // the counter can be updated at just the right time
            return remaining;
        }
        catch ( RemoteException ex )
        {
        }
        return 500;
    }

    private final Handler mHandler = new Handler()
    {

        public void handleMessage( Message msg )
        {

            switch ( msg.what )
            {
            case RemoteImageHandler.REMOTE_IMAGE_DECODED:
                mAlbum.setArtwork( ( Bitmap ) msg.obj );
                mAlbum.invalidate();
                break;

            case REFRESH:
                long next = refreshNow();
                queueNextRefresh( next );
                break;

            /*
             * case QUIT: // This can be moved back to onCreate once the bug
             * that prevents // Dialogs from being started from
             * onCreate/onResume is fixed. new
             * AlertDialog.Builder(MediaPlaybackActivity.this)
             * .setTitle(R.string.service_start_error_title)
             * .setMessage(R.string.service_start_error_msg)
             * .setPositiveButton(R.string.service_start_error_button, new
             * DialogInterface.OnClickListener() { public void
             * onClick(DialogInterface dialog, int whichButton) { finish(); } })
             * .setCancelable(false) .show(); break;
             */

            default:
                break;
            }
        }
    };

    /*
     * Try to use String.format() as little as possible, because it creates a
     * new Formatter every time you call it, which is very inefficient. Reusing
     * an existing Formatter more than tripled the speed of makeTimeString().
     * This Formatter/StringBuilder are also used by makeAlbumSongsLabel()
     */
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter( sFormatBuilder, Locale
            .getDefault() );
    private static final Object[] sTimeArgs = new Object[5];

    public static String makeTimeString( Context context, long secs )
    {

        String durationformat = context.getString( R.string.durationformat );

        /*
         * Provide multiple arguments so the format can be changed easily by
         * modifying the xml.
         */
        sFormatBuilder.setLength( 0 );

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = ( secs / 60 ) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        return sFormatter.format( durationformat, timeArgs ).toString();
    }

}
