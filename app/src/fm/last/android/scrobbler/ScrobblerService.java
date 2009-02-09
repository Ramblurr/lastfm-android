/**
 * 
 */
package fm.last.android.scrobbler;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;

import com.android.music.IMediaPlaybackService;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFm;
import fm.last.android.player.RadioPlayerService;
import fm.last.android.utils.UserTask;
import fm.last.api.AudioscrobblerService;
import fm.last.api.LastFmServer;
import fm.last.api.RadioTrack;
import fm.last.api.Session;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

/**
 * A Last.fm scrobbler for Android
 * 
 * @author Sam Steele <sam@last.fm>
 * 
 * This is a scrobbler that can scrobble both our radio player as well as the built-in media player
 * and other 3rd party apps that broadcast fm.last.android.metachanged notifications.  We can't
 * rely on com.android.music.metachanged due to a bug in the built-in media player that does not
 * broadcast this notification when playing the first track, only when starting the next track.
 * 
 * Scrobbles and Now Playing data are serialized between launches, and will be sent when the track or
 * network state changes.  This service has a very short lifetime and is only started for a few seconds at
 * a time when there's work to be done.  This server is started when music state or network state change.
 * 
 * Scrobbles are submitted to the server after Now Playing info is sent, or when a network connection becomes
 * available.
 * 
 * Sample code for a 3rd party to integrate with us:
 * 
 * Intent i = new Intent("fm.last.android.metachanged");
 * i.putExtra("artist", {artist name});
 * i.putExtra("album", {album name});
 * i.putExtra("track", {track name});
 * i.putExtra("duration", {track duration in milliseconds});
 * sendBroadcast(i);
 * 
 * To love the currently-playing track:
 * Intent i = new Intent("fm.last.android.LOVE");
 * sendBroadcast(i);
 * 
 * To ban the currently-playing track:
 * Intent i = new Intent("fm.last.android.BAN");
 * sendBroadcast(i);
 *
 */
public class ScrobblerService extends Service {
	private Session mSession;
	public static final String LOVE = "fm.last.android.LOVE";
	public static final String BAN = "fm.last.android.BAN";
	AudioscrobblerService mScrobbler;
	SubmitTracksTask mSubmissionTask = null;
	NowPlayingTask mNowPlayingTask = null;
	ArrayBlockingQueue<ScrobblerQueueEntry> mQueue;
	ScrobblerQueueEntry mCurrentTrack = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		LastFmServer server = AndroidLastFmServerFactory.getServer();
        SharedPreferences settings = getSharedPreferences( LastFm.PREFS, 0 );
        String user = settings.getString( "lastfm_user", "" );
        String session_key = settings.getString( "lastfm_session_key", "" );
        String subscriber = settings.getString( "lastfm_subscriber", "0" );
        if ( !user.equals( "" ) && !session_key.equals( "" ) ) {
	    	mSession = new Session(user, session_key, subscriber);
			String version = "0.1";
			try {
				version = getPackageManager().getPackageInfo("fm.last.android", 0).versionName;
			} catch (NameNotFoundException e) {
			}
			mScrobbler = server.createAudioscrobbler( mSession, version );
        } else {
        	//User not authenticated, shutting down...
        	stopSelf();
        }
        
        try {
            FileInputStream fileStream = openFileInput("currentTrack.dat");
            ObjectInputStream objectStream = new ObjectInputStream(fileStream);
            Object obj = objectStream.readObject();
            if(obj instanceof ScrobblerQueueEntry) {
            	mCurrentTrack = (ScrobblerQueueEntry)obj;
            }
            objectStream.close();
            fileStream.close();
        } catch (Exception e) {
        	mCurrentTrack = null;
        }

        try {
            FileInputStream fileStream = openFileInput("queue.dat");
            ObjectInputStream objectStream = new ObjectInputStream(fileStream);
            Object obj = objectStream.readObject();
            if(obj instanceof ArrayBlockingQueue) {
            	mQueue = (ArrayBlockingQueue<ScrobblerQueueEntry>)obj;
            }
            objectStream.close();
            fileStream.close();
        } catch (Exception e) {
            mQueue = new ArrayBlockingQueue<ScrobblerQueueEntry>(200);
        }
	}

	public void onDestroy() {
		super.onDestroy();

		try {
			if(getFileStreamPath("currentTrack.dat").exists())
				deleteFile("currentTrack.dat");
			if(mCurrentTrack != null) {
				FileOutputStream filestream = openFileOutput("currentTrack.dat", 0);
				ObjectOutputStream objectstream = new ObjectOutputStream(filestream);
				objectstream.writeObject(mCurrentTrack);
				objectstream.close();
				filestream.close();
			}
		} catch (Exception e) {
			if(getFileStreamPath("currentTrack.dat").exists())
				deleteFile("currentTrack.dat");
			Log.e("LastFm", "Unable to save current track state");
			e.printStackTrace();
		}

		try {
			if(getFileStreamPath("queue.dat").exists())
				deleteFile("queue.dat");
			if(mQueue.size() > 0) {
				FileOutputStream filestream = openFileOutput("queue.dat", 0);
				ObjectOutputStream objectstream = new ObjectOutputStream(filestream);
				objectstream.writeObject(mQueue);
				objectstream.close();
				filestream.close();
			}
		} catch (Exception e) {
			if(getFileStreamPath("queue.dat").exists())
				deleteFile("queue.dat");
			Log.e("LastFm", "Unable to save queue state");
			e.printStackTrace();
		}
	}
	
	/*
	 * This will check the distance between the start time and the current time to determine
	 * whether this is a skip or a played track, and will add it to our scrobble queue.
	 */
	public void enqueueCurrentTrack() {
		long playTime = (System.currentTimeMillis() / 1000) - mCurrentTrack.startTime;
		boolean played = playTime > (mCurrentTrack.duration / 2000) || playTime > 240;
		Log.i("LastFm", "Playtime: " + playTime + " / duration: " + mCurrentTrack.duration/1000);
		if(!played && mCurrentTrack.rating == "" && mCurrentTrack.trackAuth != "") {
			mCurrentTrack.rating = "S";
		}
		if(played || mCurrentTrack.rating != "") {
			Log.i("LastFm", "Enqueuing track");
	   		mQueue.add(mCurrentTrack);
	   		mCurrentTrack = null;
		}
	}
	
    @Override
    public void onStart(Intent intent, int startId) {
    	final Intent i = intent;

    	/*
         * The Android media player doesn't send a META_CHANGED notification for the first track,
         * so we'll have to catch PLAYBACK_STATE_CHANGED and check to see whether the player
         * is currently playing.  We'll then send our own META_CHANGED intent to the scrobbler.
         */
		if(intent.getAction().equals("com.android.music.playstatechanged") &&
				intent.getIntExtra("id", -1) != -1) {
			Log.i("LastFm", "Got PLAYBACK_STATE_CHANGED from Andriod media player, checking playing status...");
            bindService(new Intent().setClassName("com.android.music", "com.android.music.MediaPlaybackService"),
                    new ServiceConnection() {
                    public void onServiceConnected(ComponentName comp, IBinder binder) {
                            IMediaPlaybackService s =
                                    IMediaPlaybackService.Stub.asInterface(binder);
                            
                            try {
								if(s.isPlaying()) {
									i.setAction(RadioPlayerService.META_CHANGED);
									i.putExtra("position", s.position());
									i.putExtra("duration", (int)s.duration());
									handleIntent(i);
								} else { //Media player was paused
									mCurrentTrack = null;
									stopSelf();
								}
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							unbindService(this);
                    }

                    public void onServiceDisconnected(ComponentName comp) {}
            }, 0);			
		} else {
			handleIntent(i);
		}
    }
    
    public void handleIntent(Intent intent) {
        if(intent.getAction().equals(RadioPlayerService.META_CHANGED)) {
        	if(mCurrentTrack != null) {
        		enqueueCurrentTrack();
        	}
        	mCurrentTrack = new ScrobblerQueueEntry();
        	
        	mCurrentTrack.startTime = System.currentTimeMillis() / 1000;
        	long position = intent.getLongExtra("position", 0) / 1000;
        	if(position > 0) {
        		Log.i("LastFm", "Resuming from position: " + position);
        		mCurrentTrack.startTime -= position;
        	}
        	mCurrentTrack.title = intent.getStringExtra("track");
        	mCurrentTrack.artist = intent.getStringExtra("artist");
        	mCurrentTrack.album = intent.getStringExtra("album");
        	mCurrentTrack.duration = intent.getIntExtra("duration", 0);
        	String auth = intent.getStringExtra("trackAuth");
			if(auth != null && auth.length() > 0) {
				mCurrentTrack.trackAuth = auth;
			}
			mNowPlayingTask = new NowPlayingTask(mCurrentTrack.toRadioTrack());
			mNowPlayingTask.execute(mScrobbler);
		}
		if(intent.getAction().equals(RadioPlayerService.PLAYBACK_FINISHED)) {
			enqueueCurrentTrack();
		}
		if(intent.getAction().equals(LOVE)) {
			mCurrentTrack.rating = "L";
		}
		if(intent.getAction().equals(BAN)) {
			mCurrentTrack.rating = "B";
		}
		if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
				Log.i("LastFm", "Network connection lost, stopping scrobbler service");
				stopSelf();
			} else {
				Log.i("LastFm", "Network connection available!");
				if(mCurrentTrack != null && !mCurrentTrack.postedNowPlaying && mNowPlayingTask == null) {
					mNowPlayingTask = new NowPlayingTask(mCurrentTrack.toRadioTrack());
					mNowPlayingTask.execute(mScrobbler);
				}
		   		if(mQueue.size() > 0 && mSubmissionTask == null) {
			   		mSubmissionTask = new SubmitTracksTask();
			   		mSubmissionTask.execute(mScrobbler);
		   		}
			}
		}
		stopIfReady();
    }
    
    public void stopIfReady() {
		if(mSubmissionTask == null && mNowPlayingTask == null)
			stopSelf();
    }
    
    /* We don't currently offer any bindable functions.  Perhaps in the future we can add
     * a function to get the queue size / last scrobbler result / etc.
     */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private class NowPlayingTask extends UserTask<AudioscrobblerService, Void, Boolean> {
		RadioTrack mTrack;
		
		public NowPlayingTask(RadioTrack track) {
			mTrack = track;
		}

		public Boolean doInBackground(AudioscrobblerService... scrobbler) {
			boolean success = false;
			try
			{
				scrobbler[0].nowPlaying(mTrack);
				success = true;
			}
			catch ( Exception e )
			{
				success = false;
			}
			return success;
		}

		@Override
		public void onPostExecute(Boolean result) {
			mCurrentTrack.postedNowPlaying = result;
			mNowPlayingTask = null;
			/* If we have any scrobbles in the queue, try to send them now */
	   		if(mSubmissionTask == null && mQueue.size() > 0) {
		   		mSubmissionTask = new SubmitTracksTask();
		   		mSubmissionTask.execute(mScrobbler);
	   		}
	   		stopIfReady();
		}
	}

	private class SubmitTracksTask extends UserTask<AudioscrobblerService, Void, Boolean> {

		public Boolean doInBackground(AudioscrobblerService... scrobbler) {
			boolean success = false;
			try
			{
				Log.i("LastFm", "Going to submit " + mQueue.size() + " tracks");
				LastFmServer server = AndroidLastFmServerFactory.getServer();
				while(mQueue.size() > 0) {
					ScrobblerQueueEntry e = mQueue.element();
					if(e.rating.equals("L")) {
						server.loveTrack(e.artist, e.title, mSession.getKey());
					}
					if(e.rating.equals("B")) {
						server.banTrack(e.artist, e.title, mSession.getKey());
					}
					scrobbler[0].submit(e.toRadioTrack(), e.startTime, e.rating);
					mQueue.remove(e);
				}
				success = true;
			}
			catch ( Exception e )
			{
				success = false;
			}
			return success;
		}

		@Override
		public void onPostExecute(Boolean result) {
			mSubmissionTask = null;
			stopIfReady();
		}
	}
}
