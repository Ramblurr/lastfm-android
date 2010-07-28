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
package fm.last.android.scrobbler;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.widget.Toast;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.LastFm;
import fm.last.android.R;
import fm.last.android.RadioWidgetProvider;
import fm.last.android.db.ScrobblerQueueDao;
import fm.last.api.AudioscrobblerService;
import fm.last.api.LastFmServer;
import fm.last.api.RadioTrack;
import fm.last.api.Session;

/**
 * A Last.fm scrobbler for Android
 *
 * @author Sam Steele <sam@last.fm>
 *
 *         This is a scrobbler that can scrobble both our radio player as well
 *         as the built-in media player and other 3rd party apps that broadcast
 *         fm.last.android.metachanged notifications. We can't rely on
 *         com.android.music.metachanged due to a bug in the built-in media
 *         player that does not broadcast this notification when playing the
 *         first track, only when starting the next track.
 *
 *         Scrobbles and Now Playing data are serialized between launches, and
 *         will be sent when the track or network state changes. This service
 *         has a very short lifetime and is only started for a few seconds at a
 *         time when there's work to be done. This server is started when music
 *         state or network state change.
 *
 *         Scrobbles are submitted to the server after Now Playing info is sent,
 *         or when a network connection becomes available.
 *
 *         Sample code for a 3rd party to integrate with us is located at
 *         http://wiki.github.com/c99koder/lastfm-android/scrobbler-interface
 *
 */
public class ScrobblerService extends Service {
	private Session mSession;
	public static final String LOVE = "fm.last.android.LOVE";
	public static final String BAN = "fm.last.android.BAN";
	AudioscrobblerService mScrobbler;
	private Lock mScrobblerLock = new ReentrantLock();
	SubmitTracksTask mSubmissionTask = null;
	NowPlayingTask mNowPlayingTask = null;
	ScrobblerQueueEntry mCurrentTrack = null;

	public static final String META_CHANGED = "fm.last.android.metachanged";
	public static final String PLAYBACK_FINISHED = "fm.last.android.playbackcomplete";
	public static final String PLAYBACK_STATE_CHANGED = "fm.last.android.playstatechanged";
	public static final String STATION_CHANGED = "fm.last.android.stationchanged";
	public static final String PLAYBACK_ERROR = "fm.last.android.playbackerror";
	public static final String PLAYBACK_PAUSED = "fm.last.android.playbackpaused";
	public static final String UNKNOWN = "fm.last.android.unknown";

	private Logger logger;
	
	@Override
	public void onCreate() {
		super.onCreate();

		logger = Logger.getLogger("fm.last.android.scrobbler");
		try {
			if (logger.getHandlers().length < 1) {
				FileHandler handler = new FileHandler(getFilesDir().getAbsolutePath() + "/scrobbler.log", 4096, 1, true);
				handler.setFormatter(new SimpleFormatter());
				logger.addHandler(handler);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mSession = LastFMApplication.getInstance().session;

		if (mSession != null && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("scrobble", true)) {
			mScrobbler = LastFMApplication.getInstance().scrobbler;
		} else {
			// User not authenticated, shutting down...
			stopSelf();
			return;
		}

		try {
			ScrobblerQueueEntry entry = ScrobblerQueueDao.getInstance().loadCurrentTrack();
			if (entry != null) {
				if (entry.startTime > System.currentTimeMillis()) {
					logger.info("Serialized start time is in the future! ignoring");
				}
				else {
					mCurrentTrack = entry;
				}
			}
		} catch (Exception e) {
			mCurrentTrack = null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		try {
			Intent intent = new Intent("fm.last.android.scrobbler.FLUSH");
			PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
			AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			am.cancel(alarmIntent); // cancel any pending alarm intents
			if (ScrobblerQueueDao.getInstance().getQueueSize() > 0) {
				// schedule an alarm to wake the device and try again in an hour
				logger.info("Scrobbles are pending, will retry in an hour");
				am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3600000, alarmIntent);
			}
			ScrobblerQueueDao.getInstance().saveCurrentTrack(mCurrentTrack);
		} catch (Exception e) {
			logger.severe("Unable to save current track state");
			e.printStackTrace();
		}
	}

	/*
	 * This will check the distance between the start time and the current time
	 * to determine whether this is a skip or a played track, and will add it to
	 * our scrobble queue.
	 */
	public void enqueueCurrentTrack() {
		if (mCurrentTrack != null) {
			long playTime = (System.currentTimeMillis() / 1000) - mCurrentTrack.startTime;

			int scrobble_perc = PreferenceManager.getDefaultSharedPreferences(this).getInt("scrobble_percentage", 50);
			int track_duration = (int) (mCurrentTrack.duration / 1000);

			scrobble_perc = (int)(track_duration * (scrobble_perc * 0.01));
			boolean played = (playTime > scrobble_perc) || (playTime > 240);
			if (!played && mCurrentTrack.rating.length() == 0 && mCurrentTrack.trackAuth.length() > 0) {
				mCurrentTrack.rating = "S";
			}
			if (played || mCurrentTrack.rating.length() > 0) {
				logger.info("Enqueuing track (Rating:" + mCurrentTrack.rating + ")");
				boolean queued = ScrobblerQueueDao.getInstance().addToQueue(mCurrentTrack);
				if (!queued) {			
					logger.severe("Scrobble queue is full!  Have " + ScrobblerQueueDao.MAX_QUEUE_SIZE + " scrobbles!");
				}
			}
			mCurrentTrack = null;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onStart(intent, startId);
		return START_NOT_STICKY;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		final Intent i = intent;
		if (mScrobbler == null) {
			stopIfReady();
			return;
		}

		/*
		 * The Android media player doesn't send a META_CHANGED notification for
		 * the first track, so we'll have to catch PLAYBACK_STATE_CHANGED and
		 * check to see whether the player is currently playing. We'll then send
		 * our own META_CHANGED intent to the scrobbler.
		 */
		if (intent.getAction().equals("com.android.music.playstatechanged") || intent.getAction().equals("com.android.music.metachanged")
				|| intent.getAction().equals("com.android.music.queuechanged")) {
			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("scrobble_music_player", true)) {
				bindService(new Intent().setClassName(RadioWidgetProvider.getAndroidMusicPackageName(this), "com.android.music.MediaPlaybackService"), new ServiceConnection() {
					public void onServiceConnected(ComponentName comp, IBinder binder) {
						com.android.music.IMediaPlaybackService s = com.android.music.IMediaPlaybackService.Stub.asInterface(binder);

						try {
							if (s.isPlaying()) {
								i.setAction(META_CHANGED);
								i.putExtra("position", s.position());
								i.putExtra("duration", s.duration());
								handleIntent(i);
							} else { // Media player was paused
								mCurrentTrack = null;
								NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
								nm.cancel(1338);
								stopSelf();
							}
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						unbindService(this);
					}

					public void onServiceDisconnected(ComponentName comp) {
					}
				}, 0);
			} else {
				// Clear the current track in case the user has disabled
				// scrobbling of the media player
				// during the middle of this track.
				mCurrentTrack = null;
				stopIfReady();
			}
		} else if ((intent.getAction().equals("com.htc.music.playstatechanged") && intent.getIntExtra("id", -1) != -1)
				|| intent.getAction().equals("com.htc.music.metachanged")) {
			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("scrobble_music_player", true)) {
				bindService(new Intent().setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"), new ServiceConnection() {
					public void onServiceConnected(ComponentName comp, IBinder binder) {
						com.htc.music.IMediaPlaybackService s = com.htc.music.IMediaPlaybackService.Stub.asInterface(binder);

						try {
							if (s.isPlaying()) {
								i.setAction(META_CHANGED);
								i.putExtra("position", s.position());
								i.putExtra("duration", s.duration());
								i.putExtra("track", s.getTrackName());
								i.putExtra("artist", s.getArtistName());
								i.putExtra("album", s.getAlbumName());
								handleIntent(i);
							} else { // Media player was paused
								mCurrentTrack = null;
								NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
								nm.cancel(1338);
								stopSelf();
							}
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						unbindService(this);
					}

					public void onServiceDisconnected(ComponentName comp) {
					}
				}, 0);
			} else {
				// Clear the current track in case the user has disabled
				// scrobbling of the media player
				// during the middle of this track.
				mCurrentTrack = null;
				stopIfReady();
			}
		} else if(intent.getAction().equals("com.adam.aslfms.notify.playstatechanged")) {
			int state = intent.getIntExtra("state", -1);
			if(state > -1) {
				if(state < 2) { //start or resume
					i.setAction(META_CHANGED);
					//convert the duration from int to long
					long duration = intent.getIntExtra("duration", 0);
					i.removeExtra("duration");
					i.putExtra("duration", duration * 1000);
				} else if(state == 2) { //pause
					i.setAction(PLAYBACK_PAUSED);
				} else if(state == 3) { //complete
					i.setAction(PLAYBACK_FINISHED);
				}
				handleIntent(i);
			}
		} else if(intent.getAction().equals("net.jjc1138.android.scrobbler.action.MUSIC_STATUS")) {
			boolean playing = intent.getBooleanExtra("playing", false);

			if(!playing) {
				i.setAction(PLAYBACK_FINISHED);
			} else {
				i.setAction(META_CHANGED);
				int id = intent.getIntExtra("id", -1);

				if(id != -1) {
					final String[] columns = new String[] {
						MediaStore.Audio.AudioColumns.ARTIST,
						MediaStore.Audio.AudioColumns.TITLE,
						MediaStore.Audio.AudioColumns.DURATION,
						MediaStore.Audio.AudioColumns.ALBUM,
						MediaStore.Audio.AudioColumns.TRACK, };
				
					Cursor cur = getContentResolver().query(
						ContentUris.withAppendedId(
							MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
							id), columns, null, null, null);
					
					if (cur == null) {
						logger.severe("could not open cursor to media in media store");
						return;
					}
		
		            try {
						if (!cur.moveToFirst()) {
						        logger.severe("no such media in media store");
						        cur.close();
						        return;
						}
						String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
						i.putExtra("artist", artist);
						
						String track = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
						i.putExtra("track", track);
						
						String album = cur.getString(cur.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
						i.putExtra("album", album);
						
						long duration = cur.getLong(cur.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));
						if (duration != 0) {
						    i.putExtra("duration", duration);
						}
		            } finally {
		                    cur.close();
		            }
	            } else {
					//convert the duration from int to long
					long duration = intent.getIntExtra("secs", 0);
					i.removeExtra("secs");
					i.putExtra("duration", duration * 1000);
				}
			}
			handleIntent(i);
		} else { //
			handleIntent(i);
		}
	}

	public void handleIntent(Intent intent) {
		if (intent.getAction().equals(META_CHANGED)) {
			long startTime = System.currentTimeMillis() / 1000;
			long position = intent.getLongExtra("position", 0) / 1000;
			if (position > 0) {
				startTime -= position;
			}

			String title = intent.getStringExtra("track");
			String artist = intent.getStringExtra("artist");

			if (mCurrentTrack != null) {
				int scrobble_perc = PreferenceManager.getDefaultSharedPreferences(this).getInt("scrobble_percentage", 50);
				long scrobblePoint = mCurrentTrack.duration * (scrobble_perc / 100);

				if (scrobblePoint > 240000)
					scrobblePoint = 240000;
				if (startTime < (mCurrentTrack.startTime + scrobblePoint) && mCurrentTrack.title.equals(title) && mCurrentTrack.artist.equals(artist)) {
					logger.warning("Ignoring duplicate scrobble");
					stopIfReady();
					return;
				}
				enqueueCurrentTrack();
			}
			mCurrentTrack = new ScrobblerQueueEntry();

			mCurrentTrack.startTime = startTime;
			mCurrentTrack.title = title;
			mCurrentTrack.artist = artist;
			mCurrentTrack.album = intent.getStringExtra("album");
			mCurrentTrack.duration = intent.getLongExtra("duration", 0);
			if (mCurrentTrack.title == null || mCurrentTrack.artist == null) {
				mCurrentTrack = null;
				stopIfReady();
				return;
			}
			String auth = intent.getStringExtra("trackAuth");
			if (auth != null && auth.length() > 0) {
				mCurrentTrack.trackAuth = auth;
			}
			boolean scrobbleRealtime = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("scrobble_realtime", true);
			if (scrobbleRealtime || auth != null) {
				ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				NetworkInfo ni = cm.getActiveNetworkInfo();
				if (ni != null) {
					boolean scrobbleWifiOnly = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("scrobble_wifi_only", false);
					if (cm.getBackgroundDataSetting() && (!scrobbleWifiOnly || (scrobbleWifiOnly && ni.getType() == ConnectivityManager.TYPE_WIFI) || auth != null && mNowPlayingTask == null)) {
						mNowPlayingTask = new NowPlayingTask(mCurrentTrack.toRadioTrack());
						mNowPlayingTask.execute(mScrobbler);
					}
				}
			}

			if (auth == null) {
				NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				nm.cancel(1338);

				Notification notification = new Notification(R.drawable.as_statusbar, getString(R.string.scrobbler_ticker_text, mCurrentTrack.title,
						mCurrentTrack.artist), System.currentTimeMillis());
				Intent metaIntent = new Intent(this, fm.last.android.activity.Metadata.class);
				metaIntent.putExtra("artist", mCurrentTrack.artist);
				metaIntent.putExtra("track", mCurrentTrack.title);
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, metaIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				String info = mCurrentTrack.title + " - " + mCurrentTrack.artist;
				notification.setLatestEventInfo(this, getString(R.string.scrobbler_info_title), info, contentIntent);
				notification.flags |= Notification.FLAG_ONGOING_EVENT;
				nm.notify(1338, notification);
			}
		}
		if (intent.getAction().equals(PLAYBACK_FINISHED) || intent.getAction().equals("com.android.music.playbackcomplete")
				|| intent.getAction().equals("com.htc.music.playbackcomplete")) {
			enqueueCurrentTrack();
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			nm.cancel(1338);
		}
		if (intent.getAction().equals(PLAYBACK_PAUSED) && mCurrentTrack != null) {
			mCurrentTrack = null;
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			nm.cancel(1338);
		}
		if (intent.getAction().equals(LOVE) && mCurrentTrack != null) {
			mCurrentTrack.rating = "L";
			Toast.makeText(this, getString(R.string.scrobbler_trackloved), Toast.LENGTH_SHORT).show();
		}
		if (intent.getAction().equals(BAN) && mCurrentTrack != null) {
			mCurrentTrack.rating = "B";
			Toast.makeText(this, getString(R.string.scrobbler_trackbanned), Toast.LENGTH_SHORT).show();
		}
		if (intent.getAction().equals("fm.last.android.scrobbler.FLUSH") || mNowPlayingTask == null) {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if(ni != null) {
				boolean scrobbleWifiOnly = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("scrobble_wifi_only", false);
				if (cm.getBackgroundDataSetting() && (!scrobbleWifiOnly || (scrobbleWifiOnly && ni.getType() == ConnectivityManager.TYPE_WIFI))) {
					int queueSize = ScrobblerQueueDao.getInstance().getQueueSize();
					if (queueSize > 0 && mSubmissionTask == null) {
						mSubmissionTask = new SubmitTracksTask();
						mSubmissionTask.execute(mScrobbler);
					}
				}
			}
		}
		stopIfReady();
	}

	public void stopIfReady() {
		if (mSubmissionTask == null && mNowPlayingTask == null)
			stopSelf();
	}

	/*
	 * We don't currently offer any bindable functions. Perhaps in the future we
	 * can add a function to get the queue size / last scrobbler result / etc.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class NowPlayingTask extends AsyncTask<AudioscrobblerService, Void, Boolean> {
		RadioTrack mTrack;

		public NowPlayingTask(RadioTrack track) {
			mTrack = track;
		}

		@Override
		public void onPreExecute() {
			/* If we have any scrobbles in the queue, try to send them now */
			if (mSubmissionTask == null && ScrobblerQueueDao.getInstance().getQueueSize() > 0) {
				mSubmissionTask = new SubmitTracksTask();
				mSubmissionTask.execute(mScrobbler);
			}
		}

		@Override
		public Boolean doInBackground(AudioscrobblerService... scrobbler) {
			boolean success = false;
			try {
				mScrobblerLock.lock();
				if(scrobbler[0].sessionId == null) {
					scrobbler[0].handshake();
					if(scrobbler[0].sessionId != null) {
						SharedPreferences settings = getSharedPreferences(LastFm.PREFS, 0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString("scrobbler_session", scrobbler[0].sessionId);
						editor.putString("scrobbler_npurl", scrobbler[0].npUrl.toString());
						editor.putString("scrobbler_subsurl", scrobbler[0].subsUrl.toString());
						editor.commit();
					}
				}
				String result = scrobbler[0].nowPlaying(mTrack);
				if(result.equals("BADSESSION")) {
					scrobbler[0].sessionId = null;
					doInBackground(scrobbler[0]);
				}
				success = true;
			} catch (Exception e) {
				success = false;
			} finally {
				mScrobblerLock.unlock();
			}
			return success;
		}

		@Override
		public void onPostExecute(Boolean result) {
			if (mCurrentTrack != null)
				mCurrentTrack.postedNowPlaying = result;
			mNowPlayingTask = null;
			stopIfReady();
		}
	}

	private class SubmitTracksTask extends AsyncTask<AudioscrobblerService, Void, Boolean> {

		@Override
		public Boolean doInBackground(AudioscrobblerService... scrobbler) {
			boolean success = false;
			mScrobblerLock.lock();
			logger.info("Going to submit " + ScrobblerQueueDao.getInstance().getQueueSize() + " tracks");
			LastFmServer server = AndroidLastFmServerFactory.getServer();
			
			ScrobblerQueueEntry e = null;
			while ((e = ScrobblerQueueDao.getInstance().nextQueueEntry()) != null) {
				try {
					success = false;
					if (e != null && e.title != null && e.artist != null && e.toRadioTrack() != null) {
						if (e.rating.equals("L")) {
							server.loveTrack(e.artist, e.title, mSession.getKey());
						}
						if (e.rating.equals("B")) {
							if(e.trackAuth.length() == 0) {
								//Local tracks can't be banned, so drop them
								logger.info("Removing banned local track from queue");
								ScrobblerQueueDao.getInstance().removeFromQueue(e);
								continue;
							}
							server.banTrack(e.artist, e.title, mSession.getKey());
						}
						String result = scrobbler[0].submit(e.toRadioTrack(), e.startTime, e.rating);
						if(result.equals("OK")) {
							success = true;
						} else if(result.equals("BADSESSION")) {
							scrobbler[0].handshake();
							if(scrobbler[0].sessionId != null) {
								SharedPreferences settings = getSharedPreferences(LastFm.PREFS, 0);
								SharedPreferences.Editor editor = settings.edit();
								editor.putString("scrobbler_session", scrobbler[0].sessionId);
								editor.putString("scrobbler_npurl", scrobbler[0].npUrl.toString());
								editor.putString("scrobbler_subsurl", scrobbler[0].subsUrl.toString());
								editor.commit();
								continue; //try again with our new session key
							}
						}
					}
				} 
				catch (Exception ex) {
					logger.severe("Unable to submit track: " + ex.toString());
					ex.printStackTrace();
					success = false;
				}
				if(success) {
					ScrobblerQueueDao.getInstance().removeFromQueue(e);
				} 
				else {
					logger.severe("Scrobble submission aborted");
					break;
				}
			}
			mScrobblerLock.unlock();
			return success;
		}

		@Override
		public void onPostExecute(Boolean result) {
			mSubmissionTask = null;
			stopIfReady();
		}
	}
}
