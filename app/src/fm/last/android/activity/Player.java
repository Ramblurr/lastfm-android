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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Player extends Activity
{

	private ImageButton mInfoButton;
	private ImageButton mBackButton;
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
	private IntentFilter mIntentFilter;

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
		mBackButton = ( ImageButton ) findViewById( R.id.player_backBtn );
		mBackButton.setOnClickListener( mBackListener );
		mInfoButton = ( ImageButton ) findViewById( R.id.player_infoBtn );
		mInfoButton.setOnClickListener( mInfoListener );
		mStopButton = ( ImageButton ) findViewById( R.id.stop );
		mStopButton.requestFocus();
		mStopButton.setOnClickListener( mStopListener );
		mNextButton = ( ImageButton ) findViewById( R.id.skip );
		mNextButton.setOnClickListener( mNextListener );

		mAlbumArtWorker = new Worker( "album art worker" );
		mAlbumArtHandler = new RemoteImageHandler( mAlbumArtWorker.getLooper(), mHandler );

		if ( icicle != null )
		{
			mRelaunchAfterConfigChange = icicle.getBoolean( "configchange" );
		}

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction( RadioPlayerService.META_CHANGED );
		mIntentFilter.addAction( RadioPlayerService.PLAYBACK_FINISHED );
		mIntentFilter.addAction( RadioPlayerService.PLAYBACK_STATE_CHANGED );
		mIntentFilter.addAction( RadioPlayerService.STATION_CHANGED );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.player, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.tag_menu_item:
			fireTagActivity();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void fireTagActivity(){
		String artist = null;
		String track = null;

		try {
			artist = LastFMApplication.getInstance().player.getArtistName();
			track = LastFMApplication.getInstance().player.getTrackName();
			Intent myIntent = new Intent(this, Tag.class);
			myIntent.putExtra("lastfm.artist", artist);
			myIntent.putExtra("lastfm.track", track);
			startActivity(myIntent);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void fireMetadataActivity(){
		try {
			String artist = LastFMApplication.getInstance().player.getArtistName();
			String track = LastFMApplication.getInstance().player.getTrackName();
			String album = LastFMApplication.getInstance().player.getAlbumName();
			Intent myIntent = new Intent(this, Metadata.class);
			myIntent.putExtra("lastfm.artist", artist);
			myIntent.putExtra("lastfm.album", album);
			myIntent.putExtra("lastfm.track", track);
			startActivity(myIntent);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		paused = false;
		try {
			mStationName.setText( LastFMApplication.getInstance().player.getStationName() );
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateTrackInfo();
		long next = refreshNow();
		queueNextRefresh( next );
	}

	@Override
	public void onStop()
	{

		paused = true;
		mHandler.removeMessages( REFRESH );

		
		super.onStop();
	}

	@Override
	public void onSaveInstanceState( Bundle outState )
	{

		outState.putBoolean( "configchange", getChangingConfigurations() != 0 );
		super.onSaveInstanceState( outState );
	}

	@Override
	protected void onPause() {
		unregisterReceiver( mStatusListener );
		super.onPause();
	}

	@Override
	public void onResume()
	{
		registerReceiver( mStatusListener, mIntentFilter );
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

            if ( LastFMApplication.getInstance().player == null )
                return;
            try
            {
                LastFMApplication.getInstance().player.love();
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

            if ( LastFMApplication.getInstance().player == null )
                return;
            try
            {
                LastFMApplication.getInstance().player.ban();
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

			if ( LastFMApplication.getInstance().player == null )
				return;
			try
			{
				LastFMApplication.getInstance().player.skip();
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
	
	private View.OnClickListener mInfoListener = new View.OnClickListener()
	{
		public void onClick( View v )
		{
			fireMetadataActivity();
		}
	};

	private View.OnClickListener mStopListener = new View.OnClickListener()
	{

		public void onClick( View v )
		{

			if ( LastFMApplication.getInstance().player == null )
				return;
			try
			{
				LastFMApplication.getInstance().player.stop();
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
					mStationName.setText( LastFMApplication.getInstance().player.getStationName() );
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
		if ( LastFMApplication.getInstance().player == null )
		{
			return;
		}
		try
		{
			/*
			 * if (LastFMApplication.getInstance().player.getPath() == null) { finish(); return; }
			 */// TODO if player is done finish()
			String artistName = LastFMApplication.getInstance().player.getArtistName();
			mArtistName.setText( artistName );
			mTrackName.setText( LastFMApplication.getInstance().player.getTrackName() );
			String artUrl = LastFMApplication.getInstance().player.getArtUrl();
			if ( artUrl != RadioPlayerService.UNKNOWN )
			{
				mAlbumArtHandler.removeMessages( RemoteImageHandler.GET_REMOTE_IMAGE );
				mAlbumArtHandler.obtainMessage( RemoteImageHandler.GET_REMOTE_IMAGE, artUrl )
				.sendToTarget();
			}

			mDuration = LastFMApplication.getInstance().player.getDuration();
			System.out.println( "Setting track duration to: " + mDuration );
			mTotalTime.setText( makeTimeString( this, mDuration / 1000 ) );
		}
		catch ( RemoteException ex )
		{
			finish();
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

		if ( LastFMApplication.getInstance().player == null )
			return 500;
		try
		{
			long pos = mPosOverride < 0 ? LastFMApplication.getInstance().player.getPosition() : mPosOverride;
			long remaining = 1000 - ( pos % 1000 );
			if ( ( pos >= 0 ) && ( mDuration > 0 )  && ( pos <= mDuration ))
			{
				mCurrentTime.setText( makeTimeString( this, pos / 1000 ) );

				if ( LastFMApplication.getInstance().player.isPlaying() )
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

				//mProgress
				//.setSecondaryProgress( LastFMApplication.getInstance().player.getBufferPercent() * 10 );
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
