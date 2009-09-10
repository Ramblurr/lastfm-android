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
package fm.last.android.player;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.api.RadioTrack;
import fm.last.api.RadioPlayList;
import fm.last.api.WSError;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.RadioWidgetProvider;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import fm.last.android.R;
import fm.last.android.activity.Player;
import fm.last.android.utils.UserTask;

public class RadioPlayerService extends Service
{

	private MediaPlayer mp = new MediaPlayer();
	private MediaPlayer next_mp = null;
	private boolean mNextPrepared = false;
	private Station currentStation;
	private Session currentSession;
	private RadioTrack currentTrack;
	private ArrayBlockingQueue<RadioTrack> currentQueue;
	private NotificationManager nm = null;
	private int bufferPercent;
	private WSError mError = null;
	private String currentStationURL = null;
	private PowerManager.WakeLock wakeLock;
	private WifiManager.WifiLock wifiLock;
	public static final int STATE_STOPPED = 0;
	public static final int STATE_TUNING = 1;
	public static final int STATE_PREPARING = 2;
	public static final int STATE_PLAYING = 3;
	public static final int STATE_SKIPPING = 4;
	public static final int STATE_PAUSED = 5;
	private int mState = STATE_STOPPED;
	private int mPlaylistRetryCount = 0;
	private int mAutoSkipCount = 0;
	private Bitmap mAlbumArt;

	private static final int NOTIFY_ID = 1337;

	public static final String META_CHANGED = "fm.last.android.metachanged";
	public static final String PLAYBACK_FINISHED = "fm.last.android.playbackcomplete";
	public static final String PLAYBACK_STATE_CHANGED = "fm.last.android.playstatechanged";
	public static final String STATION_CHANGED = "fm.last.android.stationchanged";
	public static final String PLAYBACK_ERROR = "fm.last.android.playbackerror";
	public static final String UNKNOWN = "fm.last.android.unknown";


	/**
	 * Used for pausing on incoming call
	 */
	private TelephonyManager mTelephonyManager;

	@Override
	public void onCreate()
	{
		super.onCreate();

		nm = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
		bufferPercent = 0;
		setForeground( true ); // we dont want the service to be killed while
		// playing
		mp.setScreenOnWhilePlaying( true ); // we dont want to sleep while we're
		// playing
		currentQueue = new ArrayBlockingQueue<RadioTrack>(20);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Last.fm Player");

		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiLock = wm.createWifiLock("Last.fm Player");

		mTelephonyManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(new PhoneStateListener()
		{
			private FadeVolumeTask mFadeVolumeTask = null;
			
			@Override
			public void onCallStateChanged(int state, String incomingNumber) 
			{
				if (mState != STATE_STOPPED) {
					if (mFadeVolumeTask != null)
						mFadeVolumeTask.cancel();
					
					if (state == TelephonyManager.CALL_STATE_IDLE)  // fade music in to 100%
					{
						mFadeVolumeTask = new FadeVolumeTask(FadeVolumeTask.FADE_IN, 5000)
						{
							@Override
							public void onPreExecute() {
								if (mState == STATE_PAUSED)
									RadioPlayerService.this.pause();
							}
	
							@Override
							public void onPostExecute() {
								mFadeVolumeTask = null;
							}
						};
					} else { // fade music out to silence
						if (mState == STATE_PAUSED) {
							// this particular state of affairs should be impossible, seeing as we are the only
							// component that dares the pause the radio. But we cater to it just in case
							mp.setVolume( 0.0f, 0.0f );
							return;
						}
	
						// fade out faster if making a call, this feels more natural
						int duration = state == TelephonyManager.CALL_STATE_RINGING ? 3000 : 1500;
						
						mFadeVolumeTask = new FadeVolumeTask(FadeVolumeTask.FADE_OUT, duration)
						{
							@Override
							public void onPreExecute() {
							}
	
							@Override
							public void onPostExecute() {
								RadioPlayerService.this.pause();
								mFadeVolumeTask = null;
							}
						};
					}
				}
				super.onCallStateChanged(state, incomingNumber);
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);

	}
    @Override
    public void onStart(Intent intent, int startId) {
    	if(intent.getAction().equals("fm.last.android.PLAY")) {
    		String stationURL = intent.getStringExtra("station");
    		Session session = intent.getParcelableExtra("session");
    		if(stationURL.length() > 0 && session != null) {
				try {
					tune(stationURL, session);
					currentTrack = null;
					nextSong();
				} catch (Exception e) {
					if(mError != null)
						LastFMApplication.getInstance().presentError(this, mError);
					else
						LastFMApplication.getInstance().presentError(this, getResources().getString(R.string.ERROR_SERVER_UNAVAILABLE_TITLE),
								getResources().getString(R.string.ERROR_SERVER_UNAVAILABLE));
				}
    		}
    	}
    }

	@Override
	public void onDestroy()
	{
		Log.i("Last.fm", "Player service shutting down");
		if(mp.isPlaying())
			mp.stop();
		mp.release();
		nm.cancel( NOTIFY_ID );
	}

	public IBinder getBinder()
	{

		return mBinder;
	}

	private void playingNotify()
	{

		if ( currentTrack == null )
			return;
		Notification notification = new Notification(
				R.drawable.as_statusbar, "Streaming: "
				+ currentTrack.getTitle() + " by "
				+ currentTrack.getCreator(), System.currentTimeMillis() );
		PendingIntent contentIntent = PendingIntent.getActivity( this, 0,
				new Intent( this, Player.class ), 0 );
		String info = currentTrack.getTitle() + " - " + currentTrack.getCreator();
		notification.setLatestEventInfo( this, currentStation.getName(),
				info, contentIntent );
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		RadioWidgetProvider.updateAppWidget(this);
		nm.notify( NOTIFY_ID, notification );
	}

	private OnCompletionListener mOnCompletionListener = new OnCompletionListener()
	{

		public void onCompletion( MediaPlayer mp )
		{
			new NextTrackTask().execute((Void)null);
		}
	};

	private OnBufferingUpdateListener mOnBufferingUpdateListener = new OnBufferingUpdateListener()
	{

		public void onBufferingUpdate( MediaPlayer mp, int percent )
		{

			bufferPercent = percent;
			if(next_mp == null && percent == 100) {
				// Check if we're running low on tracks
				if ( currentQueue.size() < 2 )
				{
					mPlaylistRetryCount = 0;
					try {
						//Please to be working?
						refreshPlaylist();
					} catch (Exception e) {
					}
				}
				if(currentQueue.size() > 1) {
					mNextPrepared = false;
					next_mp = new MediaPlayer();
					playTrack((RadioTrack)(currentQueue.peek()), next_mp);
				}
			}
		}
	};

	private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			if(mp == RadioPlayerService.this.mp) {
				if (mState == STATE_PREPARING) {
					mp.start();
					playingNotify();
					mState = STATE_PLAYING;
					mAutoSkipCount = 0;
				} else {
					mp.stop();
				}
			} else {
				mNextPrepared = true;
			}
		}
	};
	
	private OnErrorListener mOnErrorListener = new OnErrorListener() {
		public boolean onError(MediaPlayer p, int what, int extra) {
			if(mp == p) {
				if(mAutoSkipCount++ > 4) {
					//If we weren't able to start playing after 3 attempts, bail out and notify
					//the user.  This will bring us into a stopped state.
					mState = STATE_STOPPED;
					notifyChange(PLAYBACK_ERROR);
					nm.cancel( NOTIFY_ID );
					if( wakeLock.isHeld())
						wakeLock.release();
					
					if( wifiLock.isHeld())
						wifiLock.release();
					stopSelf();
				} else {
					Log.i("LastFm", "Sadface: " + what + ", " + extra);
					//Enter a state that will allow nextSong to do its thang
					mState = STATE_PREPARING;
					new NextTrackTask().execute((Void)null);
				}
			} else {
				next_mp = null;
			}
			return true;
		}
	};
	
	private void playTrack( RadioTrack track, MediaPlayer p )
	{
		try
		{
			if (mState == STATE_STOPPED || mState == STATE_PREPARING) {
				Log.e("Last.fm", "playTrack() called from wrong state!");
				return;
			}
			
			if(p == mp) {
				currentTrack = track;
				mAlbumArt = null;
			}
			Log.i("Last.fm", "Streaming: " + track.getLocationUrl());
			RadioWidgetProvider.updateAppWidget_playing(this, track.getTitle(), track.getCreator(), 0, 0, true);
			p.reset();
			p.setDataSource( track.getLocationUrl() );
			p.setOnCompletionListener( mOnCompletionListener );
			p.setOnBufferingUpdateListener( mOnBufferingUpdateListener );
			p.setOnPreparedListener( mOnPreparedListener );
			p.setOnErrorListener( mOnErrorListener );
			
			// We do this because there has been bugs in our phonecall fade code
			// that resulted in the music never becoming audible again after a call.
			// Leave this precaution here please.
			p.setVolume( 1.0f, 1.0f );
			
			if(p == mp)
				mState = STATE_PREPARING;
			p.prepareAsync();
		}
		catch ( IllegalStateException e )
		{
			Log.e( getString( R.string.app_name ), e.toString() );
		}
		catch ( IOException e )
		{
			Log.e( getString( R.string.app_name ), e.getMessage() );
		}
	}

	private void stop()
	{
		if(mp != null) {
			try {
				mp.stop();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		if(next_mp != null) {
			try {
				next_mp.stop();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
			next_mp.release();
		}
		next_mp = null;
		nm.cancel( NOTIFY_ID );
		mState = STATE_STOPPED;
		RadioPlayerService.this.notifyChange(PLAYBACK_FINISHED);
		if( wakeLock.isHeld())
			wakeLock.release();
		
		if( wifiLock.isHeld())
			wifiLock.release();
		currentQueue.clear();
		RadioWidgetProvider.updateAppWidget_idle(this, currentStation.getName(), false);
		stopSelf();
	}
	
	private void nextSong()
	{
		if(mState == STATE_SKIPPING || mState == STATE_STOPPED) {
			Log.e("Last.fm", "nextSong() called in wrong state: " + mState);
			return;
		}
		
		if(mState == STATE_PLAYING || mState == STATE_PREPARING) {
			currentTrack = null;
			mp.stop();
		}
		
		mState = STATE_SKIPPING;
		// Check if we're running low on tracks
		if ( currentQueue.size() < 2 )
		{
			mPlaylistRetryCount = 0;
			try {
				refreshPlaylist();
			} catch (WSError e) {
				mError = e;
				notifyChange( PLAYBACK_ERROR );
				return;
			} catch (Exception e) {
				return;
			}
		}
		
		if(next_mp != null) {
			Log.i("Last.fm", "Skipping to pre-buffered track");
			mp.stop();
			mp.release();
			mp = next_mp;
			next_mp = null;
			mState = STATE_PREPARING;
			currentTrack = currentQueue.poll();
			mAlbumArt = null;
			if(mNextPrepared) {
				mOnPreparedListener.onPrepared(mp);
			}
			notifyChange( META_CHANGED );
			return;
		}
		
		// Check again, if size still == 0 then the playlist is empty.
		if ( currentQueue.size() > 0 )
		{
			//playTrack will check if mStopping is true, and stop us if the user has
			//pressed stop while we were fetching the playlist
			playTrack( currentQueue.poll(), mp );
			notifyChange( META_CHANGED );
		}
		else
		{
			// radio finished
			notifyChange( PLAYBACK_FINISHED );
			nm.cancel( NOTIFY_ID );
			wakeLock.release();
			wifiLock.release();
			mState = STATE_STOPPED;
			stopSelf();
		}
	}

	private void pause()
	{
		if ( mState == STATE_STOPPED )
			return;
		
		//TODO: This should not be exposed in the UI, only used to pause
		//during a phone call or similar interruption

		if ( mState != STATE_PAUSED)
		{
			Notification notification = new Notification(
					R.drawable.stop, "Last.fm Paused", System
					.currentTimeMillis() );
			PendingIntent contentIntent = PendingIntent.getActivity( this, 0,
					new Intent( this, Player.class ), 0 );
			String info;
			String name;
			if ( currentTrack != null )
			{
				info = currentTrack.getTitle() + " by \n"
				+ currentTrack.getCreator();
				name = currentStation.getName();
			}
			else
			{
				info = "Paused";
				name = currentStation.getName();
			}
			notification.setLatestEventInfo( this, name, info, contentIntent );
			// notification.flags |= Notification.FLAG_ONGOING_EVENT;
			nm.cancel( NOTIFY_ID );
			nm.notify( NOTIFY_ID, notification );
			notifyChange( PLAYBACK_STATE_CHANGED );
			mp.pause();
			mState = STATE_PAUSED;
		}
		else
		{
			playingNotify();
			mp.start();
			mState = STATE_PLAYING;
		}
	}

	private void refreshPlaylist() throws Exception
	{
		if ( currentStation == null )
			return;
		LastFmServer server = AndroidLastFmServerFactory.getServer();
		RadioPlayList playlist;
		try {
			String bitrate;
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if(ni.getType() == ConnectivityManager.TYPE_MOBILE)
				bitrate = "64";
			else
				bitrate = "128";
			
			if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("highquality", true))
				bitrate = "128";
			Log.i("Last.fm", "Requesting bitrate: " + bitrate);
			playlist = server.getRadioPlayList( bitrate, currentSession.getKey() );
			if(playlist == null || playlist.getTracks().length == 0)
				throw new WSError("radio.getPlaylist", "insufficient content", WSError.ERROR_NotEnoughContent);

			RadioTrack[] tracks = playlist.getTracks();
			for( int i=0; i < tracks.length; i++ ) {
				currentQueue.add(tracks[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if(e.getMessage().contains("code 503")) {
				if(mPlaylistRetryCount++ < 4 ) {
					Log.i("Last.fm", "Playlist service unavailable, retrying...");
					Thread.currentThread().sleep(2000);
					refreshPlaylist();
				} else {
					notifyChange( PLAYBACK_ERROR );
					nm.cancel( NOTIFY_ID );
				}
			}
		} catch (WSError e) {
			notifyChange( PLAYBACK_ERROR );
			nm.cancel( NOTIFY_ID );
			mError = e;
			throw e;
		}
	}

	private void notifyChange( String what )
	{

		Intent i = new Intent( what );
		if ( currentTrack != null )
		{
			i.putExtra( "artist", currentTrack.getCreator() );
			i.putExtra( "album", currentTrack.getAlbum() );
			i.putExtra( "track", currentTrack.getTitle() );
			i.putExtra( "duration", currentTrack.getDuration());
			i.putExtra( "trackAuth", currentTrack.getTrackAuth());
		}
		i.putExtra( "station", currentStation );
		sendBroadcast( i );
	}

	private void tune(String url, Session session) throws Exception, WSError
	{
		wakeLock.acquire();
		wifiLock.acquire();
		
		currentStationURL = url;
		
		Log.i("Last.fm","Tuning to station: " + url);
		if(mState == STATE_PLAYING) {
			nm.cancel( NOTIFY_ID );
			mp.stop();
		}
		mState = STATE_TUNING;
		currentQueue.clear();
		currentSession = session;
		LastFmServer server = AndroidLastFmServerFactory.getServer();
		currentStation = server.tuneToStation(url, session.getKey());
		RadioWidgetProvider.updateAppWidget_idle(RadioPlayerService.this, currentStation.getName(), true);
		if(currentStation != null) {
			Log.i("Last.fm","Station name: " + currentStation.getName());
			mPlaylistRetryCount = 0;
			refreshPlaylist();
			currentStationURL = url;
			notifyChange( STATION_CHANGED );
			LastFMApplication.getInstance().appendRecentStation(currentStation.getUrl(), currentStation.getName());
		} else {
			currentStationURL = null;
			wakeLock.release();
			wifiLock.release();
		}
	}

	private class NextTrackTask extends UserTask<Void, Void, Boolean> {

		public Boolean doInBackground(Void... input) {
			boolean success = false;
			try
			{
				nextSong();
				success = true;
			}
			catch ( WSError e ) {
				mError = e;
				success = false;
			}
			catch ( Exception e )
			{
				success = false;
			}
			return success;
		}

		@Override
		public void onPostExecute(Boolean result) {
			if(!result) {
				notifyChange( PLAYBACK_ERROR );
			}
		}
	}

	private final IRadioPlayer.Stub mBinder = new IRadioPlayer.Stub()
	{
		public int getState() throws DeadObjectException
		{
			return mState;
		}
		
		public void pause() throws DeadObjectException
		{
			RadioPlayerService.this.pause();
		}

		public void stop() throws DeadObjectException
		{
			RadioPlayerService.this.stop();
		}

		public boolean tune( String url, Session session ) throws DeadObjectException, WSError
		{
			mError = null;

			try {
				RadioPlayerService.this.tune( url, session );
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WSError e) {
				mError = e;
			}
			notifyChange(RadioPlayerService.PLAYBACK_ERROR);
			return false;
		}

		public void startRadio() throws RemoteException
		{
			if(Looper.myLooper() == null)
				Looper.prepare();
			//Enter a TUNING state if the user presses the skip button when the player is in a
			//STOPPED state
			if(mState == STATE_STOPPED)
				mState = STATE_TUNING;
			currentTrack = null;
			RadioWidgetProvider.updateAppWidget(RadioPlayerService.this);
			new NextTrackTask().execute((Void)null);
		}

		public void skip() throws RemoteException
		{
			if(Looper.myLooper() == null)
				Looper.prepare();
			
			new NextTrackTask().execute((Void)null);
		}

		public String getAlbumName() throws RemoteException
		{

			if ( currentTrack != null )
				return currentTrack.getAlbum();
			else
				return UNKNOWN;
		}

		public String getArtistName() throws RemoteException
		{

			if ( currentTrack != null )
				return currentTrack.getCreator();
			else
				return UNKNOWN;
		}

		public long getDuration() throws RemoteException
		{
			if( mp != null && mp.isPlaying() )
				return mp.getDuration();
			else
				return 0;
		}

		public String getTrackName() throws RemoteException
		{

			if ( currentTrack != null )
				return currentTrack.getTitle();
			else
				return UNKNOWN;
		}

		public boolean isPlaying() throws RemoteException
		{

			return mState != STATE_STOPPED;
		}

		public long getPosition() throws RemoteException
		{
			if( mp != null && mp.isPlaying() )
				return mp.getCurrentPosition();
			else
				return 0;
		}

		public String getArtUrl() throws RemoteException
		{
			if ( currentTrack != null )
			{
				return currentTrack.getImageUrl();
			}
			else
				return UNKNOWN;
		}

		public String getStationName() throws RemoteException
		{
			if ( currentStation != null )
				return currentStation.getName();
			return null;
		}

		public void setSession( Session session ) throws RemoteException
		{

			currentSession = session;
		}

		public int getBufferPercent() throws RemoteException
		{

			return bufferPercent;
		}

		public String getStationUrl() throws RemoteException
		{

			if ( currentStation != null )
				return currentStationURL;
			return null;
		}

		public WSError getError() throws RemoteException {
			WSError error = mError;
			mError = null;
			return error;
		}
		public Bitmap getAlbumArt() throws RemoteException {
			return mAlbumArt;
		}

		public void setAlbumArt(Bitmap art) throws RemoteException {
			mAlbumArt = art;
		}
	};

	@Override
	public IBinder onBind( Intent intent )
	{
		return mBinder;
	}

	/**
	 * Class responsible for fading in/out volume,
	 * for instance when a phone call arrives 
	 * 
	 * @author Lukasz Wisniewski
	 * 
	 * TODO if volume is not at 1.0 or 0.0 when this starts (eg. old fade task didn't finish)
	 * then this sounds broken. Hard to fix though as you have to recalculate the fade duration etc.
	 * 
	 * TODO setVolume is not logarithmic, and the ear is. We need a natural log scale
	 * see: http://stackoverflow.com/questions/207016/how-to-fade-out-volume-naturally
	 * see: http://code.google.com/android/reference/android/media/MediaPlayer.html#setVolume(float,%20float)
	 */
	private abstract class FadeVolumeTask extends TimerTask {

		public static final int FADE_IN = 0;
		public static final int FADE_OUT = 1;

		private int mCurrentStep = 0;
		private int mSteps;
		private int mMode;

		/**
		 * Constructor, launches timer immediately
		 * 
		 * @param mode Volume fade mode <code>FADE_IN</code> or <code>FADE_OUT</code>
		 * @param millis Time the fade process should take
		 * @param steps Number of volume gradations within given fade time
		 */
		public FadeVolumeTask(int mode, int millis){
			this.mMode = mode;
			this.mSteps = millis / 20; //20 times per second
			this.onPreExecute();
			new Timer().scheduleAtFixedRate(this, 0, millis/mSteps);
		}

		@Override
		public void run() {
			float volumeValue = 1.0f;
			
			if(mMode == FADE_OUT){
				volumeValue *= (float)(mSteps-mCurrentStep)/(float)mSteps;
			}
			else{
				volumeValue *= (float)(mCurrentStep)/(float)mSteps;
			}

			mp.setVolume(volumeValue, volumeValue);

			if(mCurrentStep >= mSteps){
				this.onPostExecute();
				this.cancel();
			}

			mCurrentStep++;
		}

		/**
		 * Task executed before launching timer
		 */
		public abstract void onPreExecute();

		/**
		 * Task executer after timer finished working
		 */
		public abstract void onPostExecute();
	}

}
