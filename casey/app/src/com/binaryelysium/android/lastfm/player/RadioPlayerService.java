package com.binaryelysium.android.lastfm.player;

import java.io.IOException;
import java.net.ResponseCache;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;

import net.roarsoftware.lastfm.ImageSize;
import net.roarsoftware.lastfm.Playlist;
import net.roarsoftware.lastfm.Radio;
import net.roarsoftware.lastfm.Result;
import net.roarsoftware.lastfm.Session;
import net.roarsoftware.lastfm.Track;
import net.roarsoftware.lastfm.Tuner;
import net.roarsoftware.lastfm.Result.Status;
import net.roarsoftware.lastfm.scrobble.Rating;
import net.roarsoftware.lastfm.scrobble.ResponseStatus;
import net.roarsoftware.lastfm.scrobble.Scrobbler;
import net.roarsoftware.lastfm.scrobble.Source;
import net.roarsoftware.lastfm.scrobble.SubmissionData;

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

import com.binaryelysium.android.lastfm.LastFMPlayer;
import com.binaryelysium.android.lastfm.R;

public class RadioPlayerService extends Service
{

    private MediaPlayer mp = new MediaPlayer();
    private Radio currentTuner;
    private Session currentSession;
    private Scrobbler scrobbler;
    private boolean handshaked = false;
    private int failCounter;
    private Track currentTrack;
    private long currentStartTime;
    private Rating currentRating;
    private ArrayBlockingQueue<Track> currentQueue;
    private ArrayBlockingQueue<SubmissionData> scrobbleBacklog;
    private boolean mPlaying = false;
    private NotificationManager nm = null;
    private int bufferPercent;

    /**
     * Tracks whether there are activities currently bound to the service so
     * that we can determine when it would be safe to call stopSelf().
     */
    boolean mActive = false;

    private static final int NOTIFY_ID = 1337;
    private static final int SCROBBLE_HANDSHAKE = 1;

    public static final String META_CHANGED = "com.binaryelysium.android.lastfm.metachanged";
    public static final String PLAYBACK_FINISHED = "com.binaryelysium.android.lastfm.playbackfinished";
    public static final String PLAYBACK_STATE_CHANGED = "com.binaryelysium.android.lastfm.playbackstatechanged";
    public static final String STATION_CHANGED = "com.binaryelysium.android.lastfm.stationchanged";
    public static final String UNKNOWN = "com.binaryelysium.android.lastfm.unknown";

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
                        + currentTrack.getName() + " by "
                        + currentTrack.getArtist(), System.currentTimeMillis() );
        PendingIntent contentIntent = PendingIntent.getActivity( this, 0,
                new Intent( this, LastFMPlayer.class ), 0 );
        String info = currentTrack.getName() + "\n" + currentTrack.getArtist();
        notification.setLatestEventInfo( this, currentTuner.getStationName(),
                info, contentIntent );
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        nm.notify( NOTIFY_ID, notification );
    }

    private long handshake()
    {

        if ( currentSession == null )
        {
            handshaked = false;
            return -1;
        }
        // if (handshaked)
        // return 0; // no need to handshake again.
        try
        {
            ResponseStatus r = scrobbler.handshake( currentSession );
            if ( r.getStatus() == ResponseStatus.BADAUTH )
            { // authentication
                // failed
                System.out.println( "Scrobbler authentication failed." );
                handshaked = false;
                return -1;
            }
            else if ( r.getStatus() == ResponseStatus.FAILED )
            {
                System.out.println( "Scrobbler handshake failed: "
                        + r.getStatus() + " " + r.getMessage() );
                handshaked = false;
                // increments failCounter, multiples ONE_MINUTE by
                // 2^(failCounter)
                long delay = Scrobbler.ONE_MINUTE << ++failCounter;
                if ( delay >= Scrobbler.MAX_DELAY )
                {
                    return -1; // stop trying to handshake
                }
                return delay;
            }
            failCounter = 0;
            handshaked = true;
            return 0;
        }
        catch ( IOException e )
        {
        }
        handshaked = false;
        return -1;
    }

    private void updateNowPlaying( int tryNumber )
    {

        if ( tryNumber > 3 || !handshaked )
            return; // limit to 3 tries
        System.out.println( "Updating Now Playing:" + tryNumber );
        try
        {
            String mbid = currentTrack.getMbid();
            if ( mbid == null )
                mbid = "";
            ResponseStatus r = scrobbler.nowPlaying( currentTrack.getArtist(),
                    currentTrack.getName(), currentTrack.getAlbum(), mp
                            .getDuration(), -1, mbid );

            if ( r.getStatus() == ResponseStatus.BADSESSION )
            {
                handshaked = false;
                handshake();
                updateNowPlaying( tryNumber + 1 );
            }
            else if ( r.getStatus() == ResponseStatus.BADTIME
                    || r.getStatus() == ResponseStatus.FAILED )
            {
                updateNowPlaying( tryNumber + 1 );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private void scrobbleCurrentTrack()
    {

        if ( mp.getDuration() < Scrobbler.MIN_LENGTH )
            return; // don't scrobble tracks < 30 seconds
        long now = System.currentTimeMillis() / 1000; // convert to seconds
        long diff = now - currentStartTime;
        long half = mp.getDuration() >> 1; // divide by 2
        // if (diff > Scrobbler.MIN_PLAYTIME || mp.getCurrentPosition() >= half)
        // {
        Source s = Source.LASTFM;
        Rating r = currentRating;
        s.setAuthKey( currentTrack.getTrackAuth() );
        SubmissionData data = new SubmissionData( currentTrack.getArtist(),
                currentTrack.getName(), currentTrack.getAlbum(), currentTrack
                        .getDuration(), -1, s, r, currentStartTime );
        scrobbleTrack( data, 1 );
        // }
    }

    private void scrobbleTrack( SubmissionData data, int tryNumber )
    {

        if ( !handshaked )
            return;
        if ( tryNumber > 3 )
        {
            // three hard failures, fall back to handshake
            long delay = handshake();
            queueNextHandshake( delay );
            scrobbleBacklog.add( data );
        }
        System.out.println( "Scrobbling:" + currentTrack.getName() );
        try
        {
            ResponseStatus r = scrobbler.submit( data );
            if ( r.getStatus() == ResponseStatus.BADSESSION )
            {
                handshaked = false;
                handshake();
                scrobbleTrack( data, tryNumber + 1 );
            }
            else if ( r.getStatus() == ResponseStatus.FAILED )
            {
                scrobbleTrack( data, tryNumber + 1 );
            }

        }
        catch ( Exception e )
        {
            System.out.println( "Scrobble failed " + e.getMessage() );
            e.printStackTrace();
        }
    }

    private void playTrack( Track track )
    {

        try
        {
            currentTrack = track;
            currentRating = Rating.UNKNOWN;
            mp.reset();
            mp.setDataSource( track.getLocation() );
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
            updateNowPlaying( 1 );

        }
        catch ( IOException e )
        {
            Log.e( getString( R.string.app_name ), e.getMessage() );
        }
    }

    private void nextSong()
    {

        // Check if last song or not
        if ( currentQueue.size() == 0 )
        {
            refreshPlaylist();
        }
        // Check again, if size still == 0 then the playlist is empty.
        if ( currentQueue.size() > 0 )
        {
            scrobbleCurrentTrack();
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

        if ( mPlaying )
        {
            if ( mActive == false )
                mDeferredStopHandler.deferredStopSelf();
            Notification notification = new Notification(
                    R.drawable.media_playback_pause, "Last.fm Paused", System
                            .currentTimeMillis() );
            PendingIntent contentIntent = PendingIntent.getActivity( this, 0,
                    new Intent( this, LastFMPlayer.class ), 0 );
            String info;
            String name;
            if ( currentTrack != null )
            {
                info = currentTrack.getName() + " by \n"
                        + currentTrack.getArtist();
                name = currentTuner.getStationName();
            }
            else
            {
                info = "Paused";
                name = currentTuner.getStationName();
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

    private void refreshPlaylist()
    {

        if ( currentTuner == null )
            return;
        Playlist playlist = currentTuner.fetchPlaylist();
        if ( playlist != null )
            currentQueue = playlist.getTracksQueue();
        else
        {
            System.out.println( "PLAYLIST IS NULL ZOMG" );
            notifyChange( PLAYBACK_FINISHED );
            nm.cancel( NOTIFY_ID );
        }
    }

    private void notifyChange( String what )
    {

        Intent i = new Intent( what );
        if ( currentTrack != null )
        {
            i.putExtra( "artist", currentTrack.getArtist() );
            i.putExtra( "album", currentTrack.getAlbum() );
            i.putExtra( "track", currentTrack.getName() );
        }
        sendBroadcast( i );
    }

    private void setTuner( Radio t )
    {

        currentTuner = t;
        notifyChange( STATION_CHANGED );
    }

    private final Handler mHandler = new Handler()
    {

        public void handleMessage( Message msg )
        {

            switch ( msg.what )
            {
            case SCROBBLE_HANDSHAKE:
                System.out.println( "Got Scrobble handshake message" );
                long next = handshake();
                queueNextHandshake( next );
                break;

            default:
                break;
            }
        }
    };

    private void queueNextHandshake( long delay )
    {

        if ( delay <= 0 )
            return;
        Message msg = mHandler.obtainMessage( SCROBBLE_HANDSHAKE );
        mHandler.removeMessages( SCROBBLE_HANDSHAKE );
        mHandler.sendMessageDelayed( msg, delay );
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

        public void setTuner( Radio tuner ) throws DeadObjectException
        {

            RadioPlayerService.this.setTuner( tuner );
        }

        public void startRadio() throws RemoteException
        {

            if ( currentTuner == null )
                return;
            refreshPlaylist();
            if ( currentQueue == null )
                return;
            if ( currentQueue.size() > 0 )
                playTrack( currentQueue.poll() );

        }

        public void ban() throws RemoteException
        {

            currentRating = Rating.BAN;
            Result r = Track.ban( currentTrack.getArtist(), currentTrack
                    .getName(), currentSession );
            nextSong();

        }

        public void love() throws RemoteException
        {

            currentRating = Rating.LOVE;
            Result r = Track.love( currentTrack.getArtist(), currentTrack
                    .getName(), currentSession );
            if ( r.getStatus() == Status.OK )
            {
                System.out.println( currentTrack.getName() + " loved" );
            }

        }

        public void skip() throws RemoteException
        {

            currentRating = Rating.SKIP;
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
                return currentTrack.getArtist();
            else
                return UNKNOWN;
        }

        public long getDuration() throws RemoteException
        {

            return mp.getDuration();
        }

        public String getTrackName() throws RemoteException
        {

            if ( currentTrack != null )
                return currentTrack.getName();
            else
                return UNKNOWN;
        }

        public boolean isPlaying() throws RemoteException
        {

            return mp.isPlaying();
        }

        public long getPosition() throws RemoteException
        {

            return mp.getCurrentPosition();
        }

        public String getArtUrl() throws RemoteException
        {

            if ( currentTrack != null )
            {
                String url = currentTrack.getImageURL( ImageSize.MEDIUM );
                return url;
            }
            else
                return UNKNOWN;
        }

        public String getStationName() throws RemoteException
        {

            return currentTuner.getStationName();
        }

        public void setSession( Session session ) throws RemoteException
        {

            currentSession = session;
            scrobbler = Scrobbler.newScrobbler( "tst", "1.0", currentSession
                    .getUsername() );
            long delay = handshake();
            queueNextHandshake( delay );
        }

        public int getBufferPercent() throws RemoteException
        {

            return bufferPercent;
        }

        public String getStationUrl() throws RemoteException
        {

            if ( currentTuner != null )
                return currentTuner.getStationUrl();
            return null;
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
