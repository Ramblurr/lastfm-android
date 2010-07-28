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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import android.app.AlarmManager;
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
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.util.Log;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.LastFMMediaButtonHandler;
import fm.last.android.MusicFocusable;
import fm.last.android.MusicPlayerFocusHelper;
import fm.last.android.R;
import fm.last.android.RadioWidgetProvider;
import fm.last.android.activity.Player;
import fm.last.android.activity.Profile;
import fm.last.android.db.RecentStationsDao;
import fm.last.android.scrobbler.ScrobblerService;
import fm.last.android.utils.UserTask;
import fm.last.api.LastFmServer;
import fm.last.api.RadioPlayList;
import fm.last.api.RadioTrack;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.api.WSError;

public class RadioPlayerService extends Service implements MusicFocusable {

	private MediaPlayer mp = null;
	private MediaPlayer next_mp = null;
	private boolean mNextPrepared = false;
	private boolean mNextFullyBuffered = false;
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
	public static final int STATE_NODATA = 6;
	public static final int STATE_ERROR = -1;
	private int mState = STATE_STOPPED;
	private int mPlaylistRetryCount = 0;
	private int mAutoSkipCount = 0;
	private boolean mDoHasWiFi = false;
	private long mStationStartTime = 0;
	private long mTrackStartTime = 0;
	private PendingIntent mPreBufferIntent = null;
	private boolean pauseButtonPressed = false;

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

	private Logger logger;

    private final float DUCK_VOLUME = 0.1f;
    private MusicPlayerFocusHelper mFocusHelper;

	public static boolean radioAvailable(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm == null || tm.getNetworkCountryIso() == null|| tm.getNetworkCountryIso().length() == 0 || tm.getNetworkCountryIso().equals("us") || tm.getNetworkCountryIso().equals("uk") || tm.getNetworkCountryIso().equals("de")) {
			context.getPackageManager().setComponentEnabledSetting(new ComponentName("fm.last.android", "fm.last.android.activity.Player"), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
			return true;
		}
		Log.i("Last.fm", "Radio is unavailable in this region: " + tm.getNetworkCountryIso());
		context.getPackageManager().setComponentEnabledSetting(new ComponentName("fm.last.android", "fm.last.android.activity.Player"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
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
		} catch (IOException e) {
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
				private FadeVolumeTask mFadeVolumeTask = null;
	
				@Override
				public void onCallStateChanged(int state, String incomingNumber) {
					if (mState != STATE_STOPPED) {
						if (mFadeVolumeTask != null)
							mFadeVolumeTask.cancel();
	
						if (state == TelephonyManager.CALL_STATE_IDLE) // fade music
																		// in to
																		// 100%
						{
							logger.info("Call ended, fading music back in");
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
						} else { // fade music out to silence
							logger.info("Incoming call, fading music out");
							if (mState == STATE_PAUSED) {
								// this particular state of affairs should be
								// impossible, seeing as we are the only
								// component that dares the pause the radio. But we
								// cater to it just in case
								if(mp != null && mp.isPlaying())
									mp.setVolume(0.0f, 0.0f);
								return;
							}
	
							// fade out faster if making a call, this feels more
							// natural
							int duration = state == TelephonyManager.CALL_STATE_RINGING ? 3000 : 1500;
	
							mFadeVolumeTask = new FadeVolumeTask(FadeVolumeTask.FADE_OUT, duration) {
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
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(connectivityListener, intentFilter);

		intentFilter = new IntentFilter();
		intentFilter.addAction("fm.last.android.player.PREBUFFER");
		registerReceiver(prebufferListener, intentFilter);
	}

	BroadcastReceiver prebufferListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mPreBufferIntent = null;
			if(mState == STATE_PLAYING)
				new PreBufferTask().execute();
		}
	};

	BroadcastReceiver connectivityListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			NetworkInfo ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

			if (ni.getState() == NetworkInfo.State.DISCONNECTED || ni.getState() == NetworkInfo.State.SUSPENDED) {
				if (mState != STATE_STOPPED && mState != STATE_ERROR) {
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
					if (mp != null && bufferPercent < 100) {
						try {
							mp.stop();
						} catch (Exception e) {
							e.printStackTrace();
						}
						clearNotification();
						mState = STATE_NODATA;
						currentQueue.clear();
					}
					if (next_mp != null && !mNextFullyBuffered) {
						try {
							next_mp.stop();
						} catch (Exception e) {
							e.printStackTrace();
						}
						next_mp.release();
						next_mp = null;
						mNextPrepared = false;
						mNextFullyBuffered = false;
					}
				}
			} else if (ni.getState() == NetworkInfo.State.CONNECTED && mState != STATE_STOPPED && mState != STATE_PAUSED && mState != STATE_ERROR) {
				if (mState == STATE_NODATA || ni.isFailover() || ni.getType() == ConnectivityManager.TYPE_WIFI) {
					if (ni.getType() == ConnectivityManager.TYPE_WIFI) {
						if (!mDoHasWiFi)
							mDoHasWiFi = true;
						else
							return;
					}
					logger.info("New data connection attached! Type: " + ni.getTypeName() + " Subtype: " + ni.getSubtypeName() + "Extra Info: "
							+ ni.getExtraInfo() + " Reason: " + ni.getReason());
					mState = STATE_TUNING;
					new NextTrackTask().execute();
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
		if (intent.getAction().equals("fm.last.android.PLAY")) {
			String stationURL = intent.getStringExtra("station");
			Session session = intent.getParcelableExtra("session");
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
			if (next_mp != null) {
				next_mp.release();
			}
		} catch (Exception e) {
			
		}
		clearNotification();
		unregisterReceiver(connectivityListener);
		unregisterReceiver(prebufferListener);
		if (mPreBufferIntent != null) {
			AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			am.cancel(mPreBufferIntent);
			mPreBufferIntent = null;
		}
		if(wakeLock != null && wakeLock.isHeld())
			wakeLock.release();
		
		if(wifiLock != null && wifiLock.isHeld())
			wifiLock.release();
		
        unregisterMediaButtonEventReceiverCompat((AudioManager) getSystemService(Context.AUDIO_SERVICE), 
        		new ComponentName(getApplicationContext(), LastFMMediaButtonHandler.class));

	}

	public IBinder getBinder() {

		return mBinder;
	}

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
			} catch (SQLiteException e) {
				//Google Analytics doesn't appear to be thread safe
			}
			mStationStartTime = 0;
		}
	}

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

		public void onCompletion(MediaPlayer mp) {
			logger.info("Track completed normally (bye, laurie!)");
			new NextTrackTask().execute((Void) null);
		}
	};

	private OnBufferingUpdateListener mOnBufferingUpdateListener = new OnBufferingUpdateListener() {

		public void onBufferingUpdate(MediaPlayer p, int percent) {
			if (p == mp) {
				bufferPercent = percent;
				if (mPreBufferIntent == null && percent == 100 && PreferenceManager.getDefaultSharedPreferences(RadioPlayerService.this).getBoolean("prebuffer", true)) {
					Intent intent = new Intent("fm.last.android.player.PREBUFFER");
					mPreBufferIntent = PendingIntent.getBroadcast(RadioPlayerService.this, 0, intent, 0);
					AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					long delay = (mp.getDuration() - mp.getCurrentPosition() - 30000);
					if (delay < 1000)
						delay = 1000;
					am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, mPreBufferIntent);
					logger.info("Prebuffering in " + delay / 1000 + " seconds");
				}
			}
			if (p == next_mp && percent == 100) {
				mNextFullyBuffered = true;
			}
		}
	};

	private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
		public void onPrepared(MediaPlayer p) {
			if (p == mp) {
				if (mState == STATE_PREPARING) {
					p.start();
					try {
						playingNotify();
					} catch (NullPointerException e) {
					}
					mState = STATE_PLAYING;
					mAutoSkipCount = 0;
					logger.info("Ready to produce packets (Hi, Laurie!)");
				} else {
					p.stop();
				}
			} else {
				mNextPrepared = true;
			}
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Radio", // Category
						"Buffering", // Action
						currentStation.getType(), // Label
						(int) ((System.currentTimeMillis() - mTrackStartTime) / 1000)); // Value
			} catch (SQLiteException e) {
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
						mState = STATE_NODATA;
					}
				}
			} else {
				logger.info("Encountered an error during pre-buffer");
				if(next_mp != null)
					next_mp.release();
				next_mp = null;
				mNextPrepared = false;
				mNextFullyBuffered = false;
			}
			return true;
		}
	};

	private void playTrack(RadioTrack track, MediaPlayer p) {
		try {
			if (mState == STATE_STOPPED || mState == STATE_PREPARING || mState == STATE_NODATA) {
				logger.severe("playTrack() called from wrong state!");
				return;
			}

			if (p == mp) {
				currentTrack = track;
				RadioWidgetProvider.updateAppWidget_playing(this, track.getTitle(), track.getCreator(), 0, 0, true, track.getLoved());
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
		if (next_mp != null) {
			try {
				next_mp.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			next_mp.release();
		}
		next_mp = null;
		mNextPrepared = false;
		mNextFullyBuffered = false;
		if (mPreBufferIntent != null) {
			AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			am.cancel(mPreBufferIntent);
			mPreBufferIntent = null;
		}
		clearNotification();
		notifyChange(PLAYBACK_FINISHED);
		if (wakeLock.isHeld())
			wakeLock.release();

		if (wifiLock.isHeld())
			wifiLock.release();
		
		currentQueue.clear();
		if(currentStation != null)
			RadioWidgetProvider.updateAppWidget_idle(this, currentStation.getName(), false);
		
        if (mFocusHelper.isSupported())
            mFocusHelper.abandonMusicFocus();
        
		stopSelf();
	}

	private void nextSong() {
		pauseButtonPressed = false;
		
		if (mState == STATE_SKIPPING || mState == STATE_STOPPED || mState == STATE_NODATA) {
			logger.severe("nextSong() called in wrong state: " + mState);
			return;
		}

		if (mState == STATE_PLAYING || mState == STATE_PREPARING) {
			currentTrack = null;
			if (mp.isPlaying()) {
				mp.stop();
				mp.release();
				mp = null;
			}
		}

		if (mPreBufferIntent != null) {
			AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			am.cancel(mPreBufferIntent);
			mPreBufferIntent = null;
		}

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

		if (next_mp != null) {
			logger.info("Skipping to pre-buffered track");
			if(mp != null)
				mp.release();
			mp = next_mp;
			next_mp = null;
			mState = STATE_PREPARING;
			currentTrack = currentQueue.poll();
			if (mNextPrepared) {
				mOnPreparedListener.onPrepared(mp);
			}
			if (mNextFullyBuffered) {
				mOnBufferingUpdateListener.onBufferingUpdate(mp, 100);
			}
			mNextPrepared = false;
			mNextFullyBuffered = false;
			notifyChange(META_CHANGED);
			return;
		}

		// Check again, if size still == 0 then the playlist is empty.
		if (currentQueue.size() > 0) {
			// playTrack will check if mStopping is true, and stop us if the
			// user has
			// pressed stop while we were fetching the playlist
			if(mp == null) {
				mp = new MediaPlayer();
			}
			playTrack(currentQueue.poll(), mp);
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
		if (mState == STATE_STOPPED || mState == STATE_NODATA || mState == STATE_ERROR || currentStation == null)
			return;

		// TODO: This should not be exposed in the UI, only used to pause
		// during a phone call or similar interruption

		if (mState != STATE_PAUSED) {
			Notification notification = new Notification(R.drawable.stop, getString(R.string.playerservice_paused_ticker_text), System.currentTimeMillis());
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Player.class), 0);
			String info;
			String name;
			if (currentTrack != null) {
				info = getString(R.string.playerservice_paused_info, currentTrack.getTitle(), currentTrack.getCreator());
				name = currentStation.getName();
			} else {
				info = getString(R.string.playerservice_paused);
				name = currentStation.getName();
			}
			notification.setLatestEventInfo(this, name, info, contentIntent);
			// notification.flags |= Notification.FLAG_ONGOING_EVENT;
			clearNotification();
			nm.notify(NOTIFY_ID, notification);
			notifyChange(PLAYBACK_STATE_CHANGED);
			mp.pause();
			mState = STATE_PAUSED;
			if (mPreBufferIntent != null) {
				AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				am.cancel(mPreBufferIntent);
				mPreBufferIntent = null;
			}
		} else {
			playingNotify();
			mp.start();
			mState = STATE_PLAYING;
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
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			logger.info("Current network type: " + ni.getTypeName());
			if (ni.getType() == ConnectivityManager.TYPE_MOBILE)
				bitrate = "64";
			else
				bitrate = "128";

			mDoHasWiFi = (ni.getType() == ConnectivityManager.TYPE_WIFI);

			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("highquality", false))
				bitrate = "128";

			if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("scrobble", true))
				rtp = "0";

			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("discovery", false))
				discovery = "1";

			logger.info("Requesting bitrate: " + bitrate);
			playlist = server.getRadioPlayList(bitrate, rtp, discovery, currentSession.getKey());
			if (playlist == null || playlist.getTracks().length == 0) {
				try {
					LastFMApplication.getInstance().tracker.trackEvent("Radio", // Category
							"Error", // Action
							"NotEnoughContent", // Label
							0); // Value
				} catch (SQLiteException e) {
					//Google Analytics doesn't appear to be thread safe
				}
				throw new WSError("radio.getPlaylist", "insufficient content", WSError.ERROR_NotEnoughContent);
			}

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
	
	private class TuneRadioTask extends UserTask<Void, Void, Void> {
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
				clearNotification();
				stopSelf();
			} catch (Exception e) {
				currentStationURL = null;
				Intent i = new Intent("fm.last.android.ERROR");
				sendBroadcast(i);
				logger.severe("Tuning error: " + e.getMessage());
				clearNotification();
				stopSelf();
			}
			return null;
		}
	}
	
	private class PreBufferTask extends UserTask<Void, Void, Void> {

		@Override
		public Void doInBackground(Void... input) {
			// Check if we're running low on tracks
			if (currentQueue.size() < 2) {
				mPlaylistRetryCount = 0;
				try {
					// Please to be working?
					refreshPlaylist();
				} catch (WSError e) {
					logger.info("Got a webservice error during soft-skip, ignoring: " + e.getMessage());
				} catch (Exception e) {
				}
			}
			if (currentQueue.size() > 1) {
				mNextPrepared = false;
				mNextFullyBuffered = false;
				next_mp = new MediaPlayer();
				playTrack((currentQueue.peek()), next_mp);
			}
			return null;
		}
	}

	private class NextTrackTask extends UserTask<Void, Void, Boolean> {

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

			return mState != STATE_STOPPED && mState != STATE_ERROR;
		}

		public long getPosition() throws RemoteException {
			try {
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

			mp.setVolume(volumeValue, volumeValue);

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
		if(mState == STATE_PAUSED) {
			pause();
		}

		if(mp != null && mp.isPlaying())
			mp.setVolume(1.0f, 1.0f);
	}

	public void focusLost(boolean isTransient, boolean canDuck) {
        if (mp == null)
            return;

        if (canDuck) {
            mp.setVolume(DUCK_VOLUME, DUCK_VOLUME);
        } else if(isTransient) {
            pause();
        } else {
        	stop();
        }
	}
}
