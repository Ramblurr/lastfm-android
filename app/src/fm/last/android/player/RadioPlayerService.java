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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.LastFMMediaButtonHandler;
import fm.last.android.LastFm;
import fm.last.android.MusicFocusable;
import fm.last.android.MusicPlayerFocusHelper;
import fm.last.android.R;
import fm.last.android.RadioWidgetProvider;
import fm.last.android.activity.Player;
import fm.last.android.activity.Profile;
import fm.last.android.db.RecentStationsDao;
import fm.last.android.scrobbler.ScrobblerService;
import fm.last.android.utils.AsyncTaskEx;
import fm.last.api.LastFmServer;
import fm.last.api.RadioPlayList;
import fm.last.api.RadioTrack;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;

public class RadioPlayerService extends Service implements MusicFocusable {

	private MediaPlayer mp = null;
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
	private boolean mUpdatedTrialCount;
	public static final int STATE_STOPPED = 0;
	public static final int STATE_TUNING = 1;
	public static final int STATE_PREPARING = 2;
	public static final int STATE_PLAYING = 3;
	public static final int STATE_SKIPPING = 4;
	public static final int STATE_PAUSED = 5;
	public static final int STATE_ERROR = -1;
	private int mState = STATE_STOPPED;
	private int mPlaylistRetryCount = 0;
	private int mAutoSkipCount = 0;
	private boolean mDoHasWiFi = false;
	private long mStationStartTime = 0;
	private long mTrackStartTime = 0;
	private int mTrackPosition = 0;
	private boolean pauseButtonPressed = false;
	private boolean focusLost = false;
	private boolean lostDataConnection = false;
	private static final int NOTIFY_ID = 1337;
	private FadeVolumeTask mFadeVolumeTask = null;

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

	private Logger logger;

    private final float DUCK_VOLUME = 0.1f;
    private MusicPlayerFocusHelper mFocusHelper;

	public static boolean radioAvailable(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm == null || tm.getNetworkCountryIso() == null|| tm.getNetworkCountryIso().length() == 0 
				|| tm.getNetworkCountryIso().equals("us") || tm.getNetworkCountryIso().equals("310") || tm.getNetworkCountryIso().equals("311") || tm.getNetworkCountryIso().equals("312") || tm.getNetworkCountryIso().equals("313") || tm.getNetworkCountryIso().equals("314") || tm.getNetworkCountryIso().equals("315")
				|| tm.getNetworkCountryIso().equals("gb") || tm.getNetworkCountryIso().equals("234") || tm.getNetworkCountryIso().equals("235")
				|| tm.getNetworkCountryIso().equals("de") || tm.getNetworkCountryIso().equals("262")) {
			return true;
		}
		return false;
	}
    
	@Override
	public void onCreate() {
		super.onCreate();
		
		initializeStaticCompatMethods();
        mFocusHelper = new MusicPlayerFocusHelper(this, this);
	
		logger = Logger.getLogger("fm.last.android.player");
		try {
			if (logger.getHandlers().length < 1) {
				FileHandler handler = new FileHandler(getFilesDir().getAbsolutePath() + "/player.log", 4096, 1, true);
				handler.setFormatter(new SimpleFormatter());
				logger.addHandler(handler);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("Player service started");

		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		bufferPercent = 0;

		// playing
		currentQueue = new ArrayBlockingQueue<RadioTrack>(20);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Last.fm Player");

		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiLock = wm.createWifiLock("Last.fm Player");

		if(!mFocusHelper.isSupported()) {

			mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
			mTelephonyManager.listen(new PhoneStateListener() {
				@Override
				public void onCallStateChanged(int state, String incomingNumber) {
					if (mState != STATE_STOPPED) {
						if (state == TelephonyManager.CALL_STATE_IDLE) {
							focusGained();
						} else { // fade music out to silence
							focusLost(true, false);
						}
					}
					super.onCallStateChanged(state, incomingNumber);
				}
			}, PhoneStateListener.LISTEN_CALL_STATE);
		}
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(connectivityListener, intentFilter);
		try {
			if (getFileStreamPath("player.dat").exists()) {
				FileInputStream fileStream = openFileInput("player.dat");
				ObjectInputStream objectStream = new ObjectInputStream(fileStream);
				Object obj = objectStream.readObject();
				currentSession = (Session)obj;
				obj = objectStream.readObject();
				currentStation = (Station)obj;
				obj = objectStream.readObject();
				currentStationURL = (String)obj;
				obj = objectStream.readObject();
				currentTrack = (RadioTrack)obj;
				obj = objectStream.readObject();
				mStationStartTime = (Long)obj;
				obj = objectStream.readObject();
				mTrackStartTime = (Long)obj;
				obj = objectStream.readObject();
				mTrackPosition = (Integer)obj;
				objectStream.close();
				fileStream.close();
				logger.info("Loaded serialized state");
				mState = STATE_PAUSED;
			}
		} catch (Exception e) {
			logger.warning("Unable to load serialized state");
			currentStation = null;
			currentStationURL = null;
			currentTrack = null;
			mStationStartTime = 0;
			mTrackStartTime = 0;
			mTrackPosition = 0;
			e.printStackTrace();
		}
	}

	BroadcastReceiver connectivityListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			NetworkInfo ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

			if (ni.getState() == NetworkInfo.State.DISCONNECTED || ni.getState() == NetworkInfo.State.SUSPENDED) {
				if (mState != STATE_STOPPED && mState != STATE_ERROR && mState != STATE_PAUSED) {
					ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
					NetworkInfo activeni = cm.getActiveNetworkInfo();
					if(activeni != null && activeni.isConnected()) {
						logger.info("A network other than the active network has disconnected, ignoring");
						return;
					}
					// Ignore disconnections that don't change our WiFi / cell
					// state
					if ((ni.getType() == ConnectivityManager.TYPE_WIFI) != mDoHasWiFi) {
						return;
					}

					// We just lost the WiFi connection so update our state
					if (ni.getType() == ConnectivityManager.TYPE_WIFI)
						mDoHasWiFi = false;

					logger.info("Data connection lost! Type: " + ni.getTypeName() + " Subtype: " + ni.getSubtypeName() + "Extra Info: " + ni.getExtraInfo()
							+ " Reason: " + ni.getReason());
					lostDataConnection = true;
				}
			} else if (ni.getState() == NetworkInfo.State.CONNECTED && mState != STATE_STOPPED && mState != STATE_ERROR) {
				if (lostDataConnection || ni.isFailover() || ni.getType() == ConnectivityManager.TYPE_WIFI) {
					if (ni.getType() == ConnectivityManager.TYPE_WIFI) {
						if (!mDoHasWiFi)
							mDoHasWiFi = true;
						else
							return;
					}
					logger.info("New data connection attached! Type: " + ni.getTypeName() + " Subtype: " + ni.getSubtypeName() + "Extra Info: "
							+ ni.getExtraInfo() + " Reason: " + ni.getReason());
					if(lostDataConnection) {
						if(mState == STATE_PAUSED) {
							pause();
							lostDataConnection = false;
						}
					}
				}
			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onStart(intent, startId);
		return START_NOT_STICKY;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (intent != null && intent.getAction() != null && intent.getAction().equals("fm.last.android.PLAY")) {
			String stationURL = intent.getStringExtra("station");
			Session session = intent.getParcelableExtra("session");
			if(currentStationURL != null && currentStationURL.equals(stationURL)) {
				if(mState == STATE_PAUSED)
					pause();
				if(mState != STATE_STOPPED)
					return;
			}
			if (stationURL != null && stationURL.length() > 0 && session != null) {
				new TuneRadioTask(stationURL, session).execute();
			}
		}
	}

	@Override
	public void onDestroy() {
		logger.info("Player service shutting down");
		try {
			if (mp != null) {
				if (mp.isPlaying())
					mp.stop();
				mp.release();
			}
		} catch (Exception e) {
			
		}
		clearNotification();
		unregisterReceiver(connectivityListener);
		releaseLocks();
		
		if(mState == STATE_PAUSED) {
			serializeCurrentStation();
		}
	}

	private void releaseLocks() {
		if(wakeLock != null && wakeLock.isHeld())
			wakeLock.release();
		
		if(wifiLock != null && wifiLock.isHeld())
			wifiLock.release();
	}
	
	private void serializeCurrentStation() {
		try {
			if (getFileStreamPath("player.dat").exists())
				deleteFile("player.dat");
			if (mState == STATE_PAUSED && currentTrack != null) {
				logger.info("Serializing station info");
				FileOutputStream filestream = openFileOutput("player.dat", 0);
				ObjectOutputStream objectstream = new ObjectOutputStream(filestream);

				objectstream.writeObject(currentSession);
				objectstream.writeObject(currentStation);
				objectstream.writeObject(currentStationURL);
				objectstream.writeObject(currentTrack);
				objectstream.writeObject(new Long(mStationStartTime));
				objectstream.writeObject(new Long(mTrackStartTime));
				objectstream.writeObject(new Integer(mTrackPosition));
				objectstream.close();
				filestream.close();
			}
		} catch (Exception e) {
			if (getFileStreamPath("player.dat").exists())
				deleteFile("player.dat");
			logger.severe("Unable to save queue state");
			e.printStackTrace();
		}
	}
	
	public IBinder getBinder() {

		return mBinder;
	}

	@SuppressWarnings("rawtypes")
	private void clearNotification() {
		try {
			Class types[] = { boolean.class };
			Object args[] = { true };
			Method method = Service.class.getMethod("stopForeground", types);
			method.invoke(this, args);
		} catch (NoSuchMethodException e) {
			nm.cancel(NOTIFY_ID);
			setForeground(false);
		} catch (Exception e) {
		}
		if (currentStation != null && mStationStartTime > 0) {
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Radio", // Category
						"Stream", // Action
						currentStation.getType(), // Label
						(int) ((System.currentTimeMillis() - mStationStartTime) / 1000)); // Value
			} catch (Exception e) {
				//Google Analytics doesn't appear to be thread safe
			}
			mStationStartTime = 0;
		}
	}

	@SuppressWarnings("rawtypes")
	private void playingNotify() {

		if (currentTrack == null || currentTrack.getTitle() == null || currentTrack.getCreator() == null)
			return;
		Notification notification = new Notification(R.drawable.as_statusbar, getString(R.string.playerservice_streaming_ticker_text, currentTrack.getTitle(),
				currentTrack.getCreator()), System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Player.class), 0);
		String info = currentTrack.getTitle() + " - " + currentTrack.getCreator();
		notification.setLatestEventInfo(this, currentStation.getName(), info, contentIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		RadioWidgetProvider.updateAppWidget(this);
		try {
			Class types[] = { int.class, Notification.class };
			Object args[] = { NOTIFY_ID, notification };
			Method method = Service.class.getMethod("startForeground", types);
			method.invoke(this, args);
		} catch (NoSuchMethodException e) {
			nm.notify(NOTIFY_ID, notification);
			setForeground(true);
		} catch (Exception e) {
		}

		// Send the now playing info to an OpenWatch-enabled watch
		Intent i = new Intent("com.smartmadsoft.openwatch.action.TEXT");
		i.putExtra("line1", currentTrack.getTitle());
		i.putExtra("line2", currentTrack.getCreator());
		sendBroadcast(i);

	}

	@SuppressWarnings("rawtypes")
	private void tuningNotify() {
		String info = getString(R.string.playerservice_tuning);
		if (currentStation != null) {
			info = getString(R.string.playerservice_tuningwithstation, currentStation.getName());
		}
		Notification notification = new Notification(R.drawable.as_statusbar, getString(R.string.playerservice_tuning), System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Player.class), 0);
		notification.setLatestEventInfo(this, info, "", contentIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		RadioWidgetProvider.updateAppWidget(this);
		try {
			Class types[] = { int.class, Notification.class };
			Object args[] = { NOTIFY_ID, notification };
			Method method = Service.class.getMethod("startForeground", types);
			method.invoke(this, args);
		} catch (NoSuchMethodException e) {
			nm.notify(NOTIFY_ID, notification);
			setForeground(true);
		} catch (Exception e) {
		}
		mStationStartTime = System.currentTimeMillis();
	}

	private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {

		public void onCompletion(MediaPlayer p) {
			if(lostDataConnection && bufferPercent < 99) {
				logger.info("Track ran out of data, pausing");
				pause();
				mp.release();
				mp = null;
				ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				NetworkInfo activeni = cm.getActiveNetworkInfo();
				if(activeni != null && activeni.isConnected()) {
					logger.info("Another data connection is available, attempting to resume");
					pause();
					lostDataConnection = false;
				}
			} else {
				logger.info("Track completed normally (bye, laurie!)");
				new NextTrackTask().execute((Void) null);
			}
		}
	};

	private OnBufferingUpdateListener mOnBufferingUpdateListener = new OnBufferingUpdateListener() {

		public void onBufferingUpdate(MediaPlayer p, int percent) {
			if (p == mp) {
				bufferPercent = percent;
				if(percent > 50 && !mUpdatedTrialCount && getSharedPreferences(LastFm.PREFS, 0).getBoolean("lastfm_freetrial", false)) {
					int elapsed = getSharedPreferences(LastFm.PREFS, 0).getInt("lastfm_playselapsed", 0);
					int left = getSharedPreferences(LastFm.PREFS, 0).getInt("lastfm_playsleft", 30);
					elapsed++;
					left--;
					SharedPreferences.Editor editor = getSharedPreferences(LastFm.PREFS, 0).edit();
					editor.putInt("lastfm_playselapsed", elapsed);
					editor.putInt("lastfm_playsleft", left);
					editor.commit();
					mUpdatedTrialCount = true;
				}
			}
		}
	};

	private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
		public void onPrepared(MediaPlayer p) {
			logger.info("Prepared");
			if (p == mp) {
				logger.info("main player");
				if (mState == STATE_PREPARING) {
					logger.info("preparing state");
					if(mTrackPosition > 0)
						p.seekTo(mTrackPosition);
					mTrackPosition = 0;
					p.start();
					try {
						playingNotify();
					} catch (NullPointerException e) {
					}
					mState = STATE_PLAYING;
					mAutoSkipCount = 0;
					logger.info("Ready to produce packets (Hi, Laurie!)");
					mUpdatedTrialCount = false;
					if( getSharedPreferences(LastFm.PREFS, 0).getBoolean("lastfm_freetrial", false) && getSharedPreferences(LastFm.PREFS, 0).getInt("lastfm_playsleft", 30) <= 5 && !getSharedPreferences(LastFm.PREFS, 0).getBoolean("lastfm_freetrialexpirationwarning", false)) {
						Notification notification = new Notification(R.drawable.as_statusbar, getString(R.string.playerservice_trial_almost_expired_title), System.currentTimeMillis());
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse("http://www.last.fm/subscribe"));
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						PendingIntent contentIntent = PendingIntent.getActivity(RadioPlayerService.this, 0, i, 0);
						notification.setLatestEventInfo(RadioPlayerService.this, getString(R.string.playerservice_trial_almost_expired_title), getString(R.string.playerservice_trial_almost_expired, getSharedPreferences(LastFm.PREFS, 0).getInt("lastfm_playsleft", 30)), contentIntent);
						nm.notify(NOTIFY_ID+1, notification);
						SharedPreferences.Editor editor = getSharedPreferences(LastFm.PREFS, 0).edit();
						editor.putBoolean("lastfm_freetrialexpirationwarning", true);
						editor.commit();
					}
				} else {
					p.stop();
				}
			}
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Radio", // Category
						"Buffering", // Action
						currentStation.getType(), // Label
						(int) ((System.currentTimeMillis() - mTrackStartTime) / 1000)); // Value
			} catch (Exception e) {
				//Google Analytics doesn't appear to be thread safe
			}
		}
	};

	private OnErrorListener mOnErrorListener = new OnErrorListener() {
		public boolean onError(MediaPlayer p, int what, int extra) {
			if(mState == STATE_STOPPED)
				return true;
			
			if (mp == p) {
				if (mAutoSkipCount++ > 4) {
					// If we weren't able to start playing after 3 attempts,
					// bail out and notify
					// the user. This will bring us into a stopped state.
					logger.severe("Too many playback errors, entering ERROR state");
					mState = STATE_ERROR;
					notifyChange(PLAYBACK_ERROR);
					clearNotification();
					if (wakeLock.isHeld())
						wakeLock.release();

					if (wifiLock.isHeld())
						wifiLock.release();
					
			        if (mFocusHelper.isSupported())
			            mFocusHelper.abandonMusicFocus();
			        
					stopSelf();
				} else {
					if (mState == STATE_PLAYING || mState == STATE_PREPARING) {
						logger.severe("Playback error: " + what + ", " + extra);
						// ditch our playlist and fetch a new one, in case our
						// IP changed
						currentQueue.clear();
						// Enter a state that will allow nextSong to do its
						// thang
						mState = STATE_ERROR;
						new NextTrackTask().execute((Void) null);
					}
					if (mState == STATE_PAUSED) {
						logger.severe("Playback error while paused, data connection probably timed out.");
					}
				}
			}
			return true;
		}
	};

	private void playTrack(RadioTrack track, MediaPlayer p) {
		try {
			if (p == mp) {
				currentTrack = track;
				RadioWidgetProvider.updateAppWidget_playing(this, track.getTitle(), track.getCreator(), 0, 0, true, track.getLoved(), false);
			}
			if(track.getLocationUrl().contains("play.last.fm")) {
				URL newURL = UrlUtil.getRedirectedUrl(new URL(track.getLocationUrl()));
				track.setLocationUrl(newURL.toString());
			}
			if (mState == STATE_STOPPED || mState == STATE_PAUSED || mState == STATE_PREPARING) {
				logger.severe("playTrack() called from wrong state!");
				return;
			}
			logger.info("Streaming: " + track.getLocationUrl());
			p.reset();
			p.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
			p.setOnCompletionListener(mOnCompletionListener);
			p.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
			p.setOnPreparedListener(mOnPreparedListener);
			p.setOnErrorListener(mOnErrorListener);
			p.setAudioStreamType(AudioManager.STREAM_MUSIC);
			p.setDataSource(track.getLocationUrl());
			
	        if (mFocusHelper.isSupported())
	            mFocusHelper.requestMusicFocus();
	        
			// We do this because there has been bugs in our phonecall fade code
			// that resulted in the music never becoming audible again after a
			// call.
			// Leave this precaution here please.
			p.setVolume(1.0f, 1.0f);

			if (p == mp)
				mState = STATE_PREPARING;
			mTrackStartTime = System.currentTimeMillis();
			p.prepareAsync();
		} catch (IllegalStateException e) {
			logger.severe(e.toString());
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}
	}

	private void stop() {
		mState = STATE_STOPPED;
		if (mp != null) {
			try {
				mp.stop();
				mp.release();
				mp = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		clearNotification();
		notifyChange(PLAYBACK_FINISHED);
		releaseLocks();
		
		currentQueue.clear();
		if(currentStation != null)
			RadioWidgetProvider.updateAppWidget_idle(this, currentStation.getName(), false);
		
        if (mFocusHelper.isSupported())
            mFocusHelper.abandonMusicFocus();
        
		stopSelf();
	}

	private void nextSong() {
		pauseButtonPressed = false;
		
		if (mState == STATE_SKIPPING || mState == STATE_STOPPED) {
			logger.severe("nextSong() called in wrong state: " + mState);
			return;
		}

		if (mState == STATE_PLAYING || mState == STATE_PREPARING) {
			currentTrack = null;
			if (mp.isPlaying()) {
				mp.stop();
			}
		}

		mTrackPosition = 0;
		lostDataConnection = false;
		mState = STATE_SKIPPING;
		// Check if we're running low on tracks
		if (currentQueue.size() < 1) {
			mPlaylistRetryCount = 0;
			try {
				refreshPlaylist();
			} catch (WSError e) {
				mError = e;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Check again, if size still == 0 then the playlist is empty.
		if (currentQueue.size() > 0) {
			// playTrack will check if mStopping is true, and stop us if the
			// user has
			// pressed stop while we were fetching the playlist
			if(mp == null) {
				mp = new MediaPlayer();
			}
			if(mState == STATE_SKIPPING)
				playTrack(currentQueue.poll(), mp);
			if(mState == STATE_PREPARING)
				notifyChange(META_CHANGED);
		} else {
			// we ran out of tracks, display a NEC error and stop
			clearNotification();
			notifyChange(PLAYBACK_ERROR);
			mState = STATE_ERROR;
			Notification notification = new Notification(R.drawable.as_statusbar, getString(R.string.playerservice_error_ticker_text), System
					.currentTimeMillis());
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Profile.class), 0);
			notification.setLatestEventInfo(this, getString(R.string.ERROR_INSUFFICIENT_CONTENT_TITLE), getString(R.string.ERROR_INSUFFICIENT_CONTENT),
					contentIntent);
			nm.notify(NOTIFY_ID, notification);
			stopSelf();
		}
	}

	private void pause() {
		logger.info("Pause()" + mState);
		if (mState == STATE_STOPPED || mState == STATE_ERROR || currentStation == null)
			return;

		if (mState != STATE_PAUSED) {
			clearNotification();
			notifyChange(PLAYBACK_STATE_CHANGED);
			notifyChange(ScrobblerService.PLAYBACK_PAUSED);
			mp.setOnErrorListener(null);
			mp.setOnCompletionListener(null);
			try {
				mTrackPosition = mp.getCurrentPosition();
				if(mp.isPlaying()) {
					mp.pause();
				} else {
					mp.reset();
					mp.release();
					mp = null;
					mTrackPosition = 0;
				}
				mState = STATE_PAUSED;
			} catch (Exception e) { //Sometimes the MediaPlayer is in a state where it can't pause
				e.printStackTrace();
			}
			serializeCurrentStation();
			releaseLocks();
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Radio", // Category
						"Pause", // Action
						"", // Label
						0); // Value
			} catch (Exception e) {
				//Google Analytics doesn't appear to be thread safe
			}
		} else {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			mDoHasWiFi = (ni == null || ni.getType() == ConnectivityManager.TYPE_WIFI);
			playingNotify();
			notifyChange(ScrobblerService.META_CHANGED);
			try {
				if(currentTrack != null) {
					if(mp == null) {
						mState = STATE_SKIPPING;
						mp = new MediaPlayer();
						playTrack(currentTrack, mp);
					} else {
						mp.start();
						mState = STATE_PLAYING;
					}
				}
			} catch (Exception e) { //Sometimes the MediaPlayer is in a state where it can't resume
				mState = STATE_SKIPPING;
				nextSong();
				e.printStackTrace();
			}
			if (getFileStreamPath("player.dat").exists())
				deleteFile("player.dat");
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Radio", // Category
						"Resume", // Action
						"", // Label
						0); // Value
			} catch (Exception e) {
				//Google Analytics doesn't appear to be thread safe
			}
		}
	}

	private void refreshPlaylist() throws Exception {
		if (currentStation == null)
			return;
		LastFmServer server = AndroidLastFmServerFactory.getServer();
		RadioPlayList playlist;
		try {
			String bitrate;
			String rtp = "1";
			String discovery = "0";
			String multiplier = "2";
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if(ni != null)
				logger.info("Current network type: " + ni.getTypeName());
			
			if (ni != null && ni.getType() == ConnectivityManager.TYPE_MOBILE)
				bitrate = "64";
			else
				bitrate = "128";

			mDoHasWiFi = (ni == null || ni.getType() == ConnectivityManager.TYPE_WIFI);

			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("highquality", false))
				bitrate = "128";

			if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("scrobble", true))
				rtp = "0";

			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("discovery", false))
				discovery = "1";

			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("faststreaming", true))
				multiplier = "8";

			logger.info("Requesting bitrate: " + bitrate);
			playlist = server.getRadioPlayList(bitrate, rtp, discovery, multiplier, currentSession.getKey());
			if (playlist == null || playlist.getTracks().length == 0) {
				try {
					LastFMApplication.getInstance().tracker.trackEvent("Radio", // Category
							"Error", // Action
							"NotEnoughContent", // Label
							0); // Value
				} catch (Exception e) {
					//Google Analytics doesn't appear to be thread safe
				}
				throw new WSError("radio.getPlaylist", "insufficient content", WSError.ERROR_NotEnoughContent);
			}

			SharedPreferences.Editor editor = getSharedPreferences(LastFm.PREFS, 0).edit();
			editor.putInt("lastfm_playsleft", playlist.playLeft());
			editor.commit();

			RadioTrack[] tracks = playlist.getTracks();
			logger.info("Got " + tracks.length + " track(s)");
			for (int i = 0; i < tracks.length; i++) {
				currentQueue.add(tracks[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if (e.getMessage().contains("code 503")) {
				if (mPlaylistRetryCount++ < 4) {
					logger.warning("Playlist service unavailable, retrying...");
					Thread.sleep(2000);
					refreshPlaylist();
				} else {
					throw e;
				}
			}
		} catch (WSError e) {
			String message;
			if (e.getCode() == WSError.ERROR_NotEnoughContent)
				message = "NotEnoughContent";
			else
				message = e.getMessage();
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Radio", // Category
						"Error", // Action
						message, // Label
						0); // Value
			} catch (SQLiteException e1) {
				//Google Analytics doesn't appear to be thread safe
			}
			logger.severe("Web service error: " + e.getMessage());
			mError = e;
			throw e;
		}
	}

	private void notifyChange(String what) {

		Intent i = new Intent(what);
		if (currentTrack != null) {
			i.putExtra("artist", currentTrack.getCreator());
			i.putExtra("album", currentTrack.getAlbum());
			i.putExtra("track", currentTrack.getTitle());
			i.putExtra("duration", (long) currentTrack.getDuration());
			i.putExtra("trackAuth", currentTrack.getTrackAuth());
			i.putExtra("loved", currentTrack.getLoved());
			if(mTrackPosition > 0)
				i.putExtra("position",(long) mTrackPosition);
		}
		if (what.equals(PLAYBACK_ERROR) && mError != null) {
			i.putExtra("error", (Parcelable) mError);
		}
		i.putExtra("station", currentStation);
		sendBroadcast(i);
	}

	private void tune(String url, Session session) throws Exception, WSError {
		if(!radioAvailable(this)) {
			throw new WSError("radio.tune", "Last.fm radio is unavailable in this region", WSError.ERROR_RadioUnavailable);
		}
		
		wakeLock.acquire();
		wifiLock.acquire();

		currentStationURL = url;

		if(!mFocusHelper.isSupported()) {
			
			//Stop the standard media player
			if(RadioWidgetProvider.isAndroidMusicInstalled(this)) {
				try {
					bindService(new Intent().setClassName("com.android.music", "com.android.music.MediaPlaybackService"), new ServiceConnection() {
						public void onServiceConnected(ComponentName comp, IBinder binder) {
							com.android.music.IMediaPlaybackService s = com.android.music.IMediaPlaybackService.Stub.asInterface(binder);
			
							try {
								if (s.isPlaying()) {
									s.pause();
									sendBroadcast(new Intent(ScrobblerService.PLAYBACK_PAUSED));
								}
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								unbindService(this);
							} catch (Exception e) {
							}
						}
			
						public void onServiceDisconnected(ComponentName comp) {
						}
					}, 0);
				} catch (Exception e) {
				}
			}
			
			//Stop the HTC media player
			if(RadioWidgetProvider.isHTCMusicInstalled(this)) {
				bindService(new Intent().setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"), new ServiceConnection() {
					public void onServiceConnected(ComponentName comp, IBinder binder) {
						com.htc.music.IMediaPlaybackService s = com.htc.music.IMediaPlaybackService.Stub.asInterface(binder);
		
						try {
							if (s.isPlaying()) {
								s.pause();
								sendBroadcast(new Intent(ScrobblerService.PLAYBACK_PAUSED));
							}
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							unbindService(this);
						} catch (Exception e) {
						}
					}
		
					public void onServiceDisconnected(ComponentName comp) {
					}
				}, 0);
			}
		}
		
		tuningNotify();

		logger.info("Tuning to station: " + url);
		if (mState == STATE_PLAYING) {
			clearNotification();
			mp.stop();
		}
		mState = STATE_TUNING;
		currentQueue.clear();
		currentSession = session;
		LastFmServer server = AndroidLastFmServerFactory.getServer();
		String lang = Locale.getDefault().getLanguage();
		if (lang.equalsIgnoreCase("de")) {
			currentStation = server.tuneToStation(url, session.getKey(), lang);
		} else {
			currentStation = server.tuneToStation(url, session.getKey(), null);
		}
		RadioWidgetProvider.updateAppWidget_idle(RadioPlayerService.this, currentStation.getName(), true);
		if (currentStation != null) {
			logger.info("Station name: " + currentStation.getName());
			mPlaylistRetryCount = 0;
			tuningNotify();
			refreshPlaylist();
			currentStationURL = url;
			notifyChange(STATION_CHANGED);
			RecentStationsDao.getInstance().appendRecentStation(currentStationURL, currentStation.getName());
            registerMediaButtonEventReceiverCompat((AudioManager) getSystemService(Context.AUDIO_SERVICE), 
            		new ComponentName(getApplicationContext(), LastFMMediaButtonHandler.class));
		} else {
			clearNotification();
			currentStationURL = null;
			wakeLock.release();
			wifiLock.release();
			stopSelf();
		}
	}
	
	private class TuneRadioTask extends AsyncTaskEx<Void, Void, Void> {
		String mStationURL = "";
		Session mSession = null;
		
		public TuneRadioTask(String stationURL, Session session) {
			mStationURL = stationURL;
			mSession = session;
		}
		
		@Override
		public Void doInBackground(Void... input) {
			try {
				tune(mStationURL, mSession);
				currentTrack = null;
				nextSong();
			} catch (WSError e) {
				mError = e;
				currentStationURL = null;
				Intent i = new Intent("fm.last.android.ERROR");
				i.putExtra("error", (Parcelable) e);
				sendBroadcast(i);
				logger.severe("Tuning error: " + e.getMessage());
				e.printStackTrace();
				clearNotification();
				stopSelf();
			} catch (Exception e) {
				currentStationURL = null;
				Intent i = new Intent("fm.last.android.ERROR");
				i.putExtra("error", (Parcelable) new WSError(e.getClass().getSimpleName(), e.getMessage(), -1));
				sendBroadcast(i);
				logger.severe("Tuning error: " + e.getMessage());
				e.printStackTrace();
				clearNotification();
				stopSelf();
			}
			return null;
		}
	}
	
	private class NextTrackTask extends AsyncTaskEx<Void, Void, Boolean> {

		@Override
		public Boolean doInBackground(Void... input) {
			boolean success = false;
			try {
				nextSong();
				success = true;
			} catch (WSError e) {
				e.printStackTrace();
				mError = e;
				success = false;
			} catch (Exception e) {
				e.printStackTrace();
				success = false;
			}
			return success;
		}

		@Override
		public void onPostExecute(Boolean result) {
			if (!result) {
				notifyChange(PLAYBACK_ERROR);
				mState = STATE_ERROR;
			}
		}
	}

	private final IRadioPlayer.Stub mBinder = new IRadioPlayer.Stub() {
		public boolean getPauseButtonPressed() throws DeadObjectException {
			return pauseButtonPressed;
		}
		
		public void pauseButtonPressed() throws DeadObjectException {
			pauseButtonPressed = true;
		}
		
		public int getState() throws DeadObjectException {
			return mState;
		}

		public void pause() throws DeadObjectException {
			RadioPlayerService.this.pause();
		}

		public void stop() throws DeadObjectException {
			logger.info("Stop button pressed");
			RadioPlayerService.this.stop();
		}

		public boolean tune(String url, Session session) throws DeadObjectException, WSError {
			mError = null;

			try {
				RadioPlayerService.this.tune(url, session);
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WSError e) {
				mError = e;
			}
			notifyChange(PLAYBACK_ERROR);
			mState = STATE_ERROR;
			return false;
		}

		public void startRadio() throws RemoteException {
			if (Looper.myLooper() == null)
				Looper.prepare();
			// Enter a TUNING state if the user presses the skip button when the
			// player is in a
			// STOPPED state
			if (mState == STATE_STOPPED)
				mState = STATE_TUNING;
			currentTrack = null;
			RadioWidgetProvider.updateAppWidget(RadioPlayerService.this);
			new NextTrackTask().execute((Void) null);
		}

		public void skip() throws RemoteException {
			logger.info("Skip button pressed");
			if (Looper.myLooper() == null)
				Looper.prepare();
			
			new NextTrackTask().execute((Void) null);
		}

		public String getAlbumName() throws RemoteException {

			if (currentTrack != null)
				return currentTrack.getAlbum();
			else
				return UNKNOWN;
		}

		public String[] getContext() throws RemoteException {

			if (currentTrack != null)
				return currentTrack.getContext();
			else
				return null;
		}

		public boolean getLoved() throws RemoteException {
			return currentTrack != null ? currentTrack.getLoved() : false;
		}
		
		public void setLoved(boolean loved) throws RemoteException {
			if(currentTrack != null)
				currentTrack.setLoved(loved);
		}
		
		public String getArtistName() throws RemoteException {

			if (currentTrack != null)
				return currentTrack.getCreator();
			else
				return UNKNOWN;
		}

		public long getDuration() throws RemoteException {
			try {
				if (mState == STATE_PAUSED && currentTrack != null)
					return currentTrack.getDuration();
				if (mp != null && mp.isPlaying())
					return mp.getDuration();
			} catch (Exception e) {
			}
			return 0;
		}

		public String getTrackName() throws RemoteException {

			if (currentTrack != null)
				return currentTrack.getTitle();
			else
				return UNKNOWN;
		}

		public boolean isPlaying() throws RemoteException {
			return mState != STATE_STOPPED && mState != STATE_ERROR && mState != STATE_PAUSED;
		}

		public long getPosition() throws RemoteException {
			try {
				if (mState == STATE_PAUSED && mTrackPosition > 0)
					return mTrackPosition;
				if (mp != null && mp.isPlaying())
					return mp.getCurrentPosition();
			} catch (Exception e) {
			}
			return 0;
		}

		public String getArtUrl() throws RemoteException {
			if (currentTrack != null) {
				return currentTrack.getImageUrl();
			} else
				return UNKNOWN;
		}

		public String getStationName() throws RemoteException {
			if (currentStation != null)
				return currentStation.getName();
			return null;
		}

		public void setSession(Session session) throws RemoteException {

			currentSession = session;
		}

		public int getBufferPercent() throws RemoteException {

			return bufferPercent;
		}

		public String getStationUrl() throws RemoteException {

			if (currentStation != null)
				return currentStationURL;
			return null;
		}

		public WSError getError() throws RemoteException {
			WSError error = mError;
			mError = null;
			return error;
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * Class responsible for fading in/out volume, for instance when a phone
	 * call arrives
	 * 
	 * @author Lukasz Wisniewski
	 * 
	 *         TODO if volume is not at 1.0 or 0.0 when this starts (eg. old
	 *         fade task didn't finish) then this sounds broken. Hard to fix
	 *         though as you have to recalculate the fade duration etc.
	 * 
	 *         TODO setVolume is not logarithmic, and the ear is. We need a
	 *         natural log scale see:
	 *         http://stackoverflow.com/questions/207016/how
	 *         -to-fade-out-volume-naturally see:
	 *         http://code.google.com/android/
	 *         reference/android/media/MediaPlayer
	 *         .html#setVolume(float,%20float)
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
		 * @param mode
		 *            Volume fade mode <code>FADE_IN</code> or
		 *            <code>FADE_OUT</code>
		 * @param millis
		 *            Time the fade process should take
		 * @param steps
		 *            Number of volume gradations within given fade time
		 */
		public FadeVolumeTask(int mode, int millis) {
			this.mMode = mode;
			this.mSteps = millis / 20; // 20 times per second
			this.onPreExecute();
			new Timer().scheduleAtFixedRate(this, 0, millis / mSteps);
		}

		@Override
		public void run() {
			float volumeValue = 1.0f;

			if (mMode == FADE_OUT) {
				volumeValue *= (float) (mSteps - mCurrentStep) / (float) mSteps;
			} else {
				volumeValue *= (float) (mCurrentStep) / (float) mSteps;
			}

			try {
				mp.setVolume(volumeValue, volumeValue);
			} catch (Exception e) {
				return;
			}

			if (mCurrentStep >= mSteps) {
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
    // Backwards compatibility code (methods available as of SDK Level 8)

    static {
        initializeStaticCompatMethods();
    }

    static Method sMethodRegisterMediaButtonEventReceiver;
    static Method sMethodUnregisterMediaButtonEventReceiver;

    private static void initializeStaticCompatMethods() {
        try {
            sMethodRegisterMediaButtonEventReceiver = AudioManager.class.getMethod(
                    "registerMediaButtonEventReceiver",
                    new Class[] { ComponentName.class });
            sMethodUnregisterMediaButtonEventReceiver = AudioManager.class.getMethod(
                    "unregisterMediaButtonEventReceiver",
                    new Class[] { ComponentName.class });
        } catch (NoSuchMethodException e) {
            // Silently fail when running on an OS before SDK level 8.
        }
    }

    private static void registerMediaButtonEventReceiverCompat(AudioManager audioManager,
            ComponentName receiver) {
        if (sMethodRegisterMediaButtonEventReceiver == null)
            return;

        try {
            sMethodRegisterMediaButtonEventReceiver.invoke(audioManager, receiver);
        } catch (InvocationTargetException e) {
            // Unpack original exception when possible
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                // Unexpected checked exception; wrap and re-throw
                throw new RuntimeException(e);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static void unregisterMediaButtonEventReceiverCompat(AudioManager audioManager,
            ComponentName receiver) {
        if (sMethodUnregisterMediaButtonEventReceiver == null)
            return;

        try {
            sMethodUnregisterMediaButtonEventReceiver.invoke(audioManager, receiver);
        } catch (InvocationTargetException e) {
            // Unpack original exception when possible
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                // Unexpected checked exception; wrap and re-throw
                throw new RuntimeException(e);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

	public void focusGained() {
		if (mFadeVolumeTask != null)
			mFadeVolumeTask.cancel();

		if(mState == STATE_PAUSED && focusLost) {
			logger.info("fading music back in");
			mFadeVolumeTask = new FadeVolumeTask(FadeVolumeTask.FADE_IN, 5000) {
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
			focusLost = false;
		} else {
			try {
				if(mp != null && mp.isPlaying())
					mp.setVolume(1.0f, 1.0f);
			} catch (Exception e) { //Sometimes the MediaPlayer is in a state where isPlaying() or setVolume() will fail
				e.printStackTrace();
			}
		}
	}

	public void focusLost(boolean isTransient, boolean canDuck) {
		if (mFadeVolumeTask != null)
			mFadeVolumeTask.cancel();

		if (mp == null || mState == STATE_PAUSED)
            return;

        if (canDuck) {
    		try {
    			mp.setVolume(DUCK_VOLUME, DUCK_VOLUME);
    		} catch (Exception e) { //Sometimes the MediaPlayer is in a state where setVolume() will fail
    			e.printStackTrace();
    		}
        } else {
			logger.info("fading music out");

			mFadeVolumeTask = new FadeVolumeTask(FadeVolumeTask.FADE_OUT, 1500) {
				@Override
				public void onPreExecute() {
				}

				@Override
				public void onPostExecute() {
					RadioPlayerService.this.pause();
					mFadeVolumeTask = null;
				}
			};
            focusLost = isTransient;
        }
	}
}
