package fm.last.android.player;

import java.io.IOException;
import java.net.ResponseCache;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;

import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.api.RadioTrack;
import fm.last.api.RadioPlayList;
import fm.last.api.WSError;
import fm.last.android.AndroidLastFmServerFactory;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import fm.last.android.R;
import fm.last.android.activity.Player;

public class RadioPlayerService extends Service
{

    private MediaPlayer mp = new MediaPlayer();
    private Station currentStation;
    private Session currentSession;
    private boolean handshaked = false;
    private int failCounter;
    private RadioTrack currentTrack;
    private long currentStartTime;
    private ArrayBlockingQueue<RadioTrack> currentQueue;
    private boolean mPlaying = false;
    private NotificationManager nm = null;
    private int bufferPercent;
    private WSError mError = null;
    private String currentStationURL = null;

    /**
     * Tracks whether there are activities currently bound to the service so
     * that we can determine when it would be safe to call stopSelf().
     */
    boolean mActive = false;

    private static final int NOTIFY_ID = 1337;
    private static final int SCROBBLE_HANDSHAKE = 1;

    public static final String META_CHANGED = "fm.last.android.metachanged";
    public static final String PLAYBACK_FINISHED = "fm.last.android.playbackfinished";
    public static final String PLAYBACK_STATE_CHANGED = "fm.last.android.playbackstatechanged";
    public static final String STATION_CHANGED = "fm.last.android.stationchanged";
    public static final String PLAYBACK_ERROR = "fm.last.android.playbackerror";
    public static final String UNKNOWN = "fm.last.android.unknown";

    @Override
    public void onCreate()
    {

        super.onCreate();
        nm = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
        failCounter = 0;
        handshaked = false;
        bufferPercent = 0;
        setForeground( true ); // we dont want the service to be killed while
        // playing
        mp.setScreenOnWhilePlaying( true ); // we dont want to sleep while we're
        // playing
        currentQueue = new ArrayBlockingQueue<RadioTrack>(20);
    }

    @Override
    public void onDestroy()
    {

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
                R.drawable.stat_notify_musicplayer, "Streaming: "
                        + currentTrack.getTitle() + " by "
                        + currentTrack.getCreator(), System.currentTimeMillis() );
        PendingIntent contentIntent = PendingIntent.getActivity( this, 0,
                new Intent( this, Player.class ), 0 );
        String info = currentTrack.getTitle() + "\n" + currentTrack.getCreator();
        notification.setLatestEventInfo( this, currentStation.getName(),
                info, contentIntent );
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        nm.notify( NOTIFY_ID, notification );
    }

    private void playTrack( RadioTrack track )
    {
        try
        {
            currentTrack = track;
            System.out.printf("Streaming: %s\n", track.getLocationUrl());
            mp.reset();
            mp.setDataSource( track.getLocationUrl() );
            mp.setOnCompletionListener( new OnCompletionListener()
            {

                public void onCompletion( MediaPlayer mp )
                {

                    RadioPlayerService.this.nextSong();
                }
            } );

            mp.setOnBufferingUpdateListener( new OnBufferingUpdateListener()
            {

                public void onBufferingUpdate( MediaPlayer mp, int percent )
                {

                    bufferPercent = percent;
                }
            } );

            mp.prepare();
            mp.start();
            currentStartTime = System.currentTimeMillis() / 1000;
            mPlaying = true;
            mDeferredStopHandler.cancelStopSelf();
            playingNotify();
        }
        catch ( IOException e )
        {
            Log.e( getString( R.string.app_name ), e.getMessage() );
        }
    }

    private void nextSong()
    {

        // Check if we're running low on tracks
        if ( currentQueue.size() < 2 )
        {
            refreshPlaylist();
        }
        // Check again, if size still == 0 then the playlist is empty.
        if ( currentQueue.size() > 0 )
        {
            playTrack( currentQueue.poll() );
            notifyChange( META_CHANGED );
        }
        else
        {
            // radio finished
            notifyChange( PLAYBACK_FINISHED );
            nm.cancel( NOTIFY_ID );
        }
    }

    private void pause()
    {
    	//TODO: This should not be exposed in the UI, only used to pause
    	//during a phone call or similar interruption

        if ( mPlaying )
        {
            if ( mActive == false )
                mDeferredStopHandler.deferredStopSelf();
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
            mPlaying = false;
        }
        else
        {
            playingNotify();
            mp.start();
            mPlaying = true;
        }
    }

    private void prevSong()
    {

        // NOT IMPLEMENTED FOR LASTFM
    }

    private void refreshPlaylist() throws WSError
    {
        if ( currentStation == null )
            return;
        LastFmServer server = AndroidLastFmServerFactory.getServer();
        RadioPlayList playlist;
		try {
			playlist = server.getRadioPlayList( currentSession.getKey() );
			if(playlist == null || playlist.getTracks().length == 0)
				throw new WSError("radio.getPlaylist", "insufficient content", WSError.ERROR_NotEnoughContent);
			
        	RadioTrack[] tracks = playlist.getTracks();
        	for( int i=0; i < tracks.length; i++ ) {
        		currentQueue.add(tracks[i]);
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if(e.getMessage().contains("code 503")) {
				System.out.print("Server unavailable, retrying...");
				refreshPlaylist();
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
        }
        i.putExtra( "station", currentStation );
        sendBroadcast( i );
    }

	private void tune(String url, Session session) throws IOException, WSError
    {
		System.out.printf("Tuning to station: %s\n", url);
		if(mp.isPlaying()) {
            nm.cancel( NOTIFY_ID );
            mp.stop();
		}
		currentQueue.clear();
        currentSession = session;
        LastFmServer server = AndroidLastFmServerFactory.getServer();
		currentStation = server.tuneToStation(url, session.getKey());
		if(currentStation != null) {
			System.out.printf("Station name: %s\n", currentStation.getName());
			refreshPlaylist();
			currentStationURL = url;
	        notifyChange( STATION_CHANGED );
		}
    }

    /**
     * Deferred stop implementation from the five music player for android:
     * http://code.google.com/p/five/ (C) 2008 jasta00
     */
    private final DeferredStopHandler mDeferredStopHandler = new DeferredStopHandler();

    private class DeferredStopHandler extends Handler
    {

        /* Wait 1 minute before vanishing. */
        public static final long DEFERRAL_DELAY = 1 * ( 60 * 1000 );

        private static final int DEFERRED_STOP = 0;

        public void handleMessage( Message msg )
        {

            switch ( msg.what )
            {
            case DEFERRED_STOP:
                stopSelf();
                break;
            default:
                super.handleMessage( msg );
            }
        }

        public void deferredStopSelf()
        {

            Log.i( "Lastfm", "Service stop scheduled "
                    + ( DEFERRAL_DELAY / 1000 / 60 ) + " minutes from now." );
            sendMessageDelayed( obtainMessage( DEFERRED_STOP ), DEFERRAL_DELAY );
        }

        public void cancelStopSelf()
        {

            if ( hasMessages( DEFERRED_STOP ) == true )
            {
                Log.i( "Lastfm", "Service stop cancelled." );
                removeMessages( DEFERRED_STOP );
            }
        }
    };

    private final IRadioPlayer.Stub mBinder = new IRadioPlayer.Stub()
    {

        public void skipForward() throws DeadObjectException
        {

            nextSong();
        }

        public void pause() throws DeadObjectException
        {

            RadioPlayerService.this.pause();
        }

        public void stop() throws DeadObjectException
        {

            nm.cancel( NOTIFY_ID );
            mp.stop();
        }

        public boolean tune( String url, Session session ) throws DeadObjectException, WSError
        {
        	mError = null;
        	
			try {
				RadioPlayerService.this.tune( url, session );
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WSError e) {
				mError = e;
			}
			return false;
        }

        public void startRadio() throws RemoteException
        {
           	nextSong();            		
        }

        public void skip() throws RemoteException
        {

            //currentRating = Rating.SKIP;
            nextSong();
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

            return mp.isPlaying();
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

            return currentStation.getName();
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

		@Override
		public WSError getError() throws RemoteException {
			WSError error = mError;
			mError = null;
			return error;
		}

    };

    @Override
    public IBinder onBind( Intent intent )
    {

        mActive = true;
        return mBinder;
    }

    @Override
    public void onRebind( Intent intent )
    {

        mActive = true;
        mDeferredStopHandler.cancelStopSelf();
        super.onRebind( intent );
    }

    @Override
    public boolean onUnbind( Intent intent )
    {

        mActive = false;

        if ( mPlaying == true ) // || mResumeAfterCall == true)
            return true;

        mDeferredStopHandler.deferredStopSelf();
        return true;
    }

}
