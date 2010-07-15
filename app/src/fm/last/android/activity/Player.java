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
package fm.last.android.activity;

import java.io.IOException;
import java.util.Formatter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteException;
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
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import fm.last.android.Amazon;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.player.IRadioPlayer;
import fm.last.android.player.RadioPlayerService;
import fm.last.android.utils.UserTask;
import fm.last.android.widget.AdArea;
import fm.last.android.widget.AlbumArt;
import fm.last.api.Album;
import fm.last.api.Event;
import fm.last.api.ImageUrl;
import fm.last.api.LastFmServer;
import fm.last.api.Station;
import fm.last.api.WSError;

public class Player extends Activity {

	private ImageButton mLoveButton;
	private ImageButton mBanButton;
	private ImageButton mStopButton;
	private ImageButton mNextButton;
	private ImageButton mOntourButton;
	private AlbumArt mAlbum;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private TextView mArtistName;
	private TextView mTrackName;
	private ProgressBar mProgress;
	private long mDuration;
	private boolean paused;
	private boolean loved = false;

	private ProgressDialog mTuningDialog;

	private String mCachedArtist = null;
	private String mCachedTrack = null;
	private Bitmap mCachedBitmap = null;

	private static final int REFRESH = 1;

	private boolean tuning = false;
	
	LastFmServer mServer = AndroidLastFmServerFactory.getServer();

	private IntentFilter mIntentFilter;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.audio_player);
		setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);

		mCurrentTime = (TextView) findViewById(R.id.currenttime);
		mTotalTime = (TextView) findViewById(R.id.totaltime);
		mProgress = (ProgressBar) findViewById(android.R.id.progress);
		mProgress.setMax(1000);
		mAlbum = (AlbumArt) findViewById(R.id.album);
		LayoutParams params = mAlbum.getLayoutParams();
		if (AdArea.adsEnabled(this)) {
			params.width -= 54;
			params.height -= 54;
		}
		mAlbum.setLayoutParams(params);
		mArtistName = (TextView) findViewById(R.id.track_artist);
		mTrackName = (TextView) findViewById(R.id.track_title);

		mLoveButton = (ImageButton) findViewById(R.id.love);
		mLoveButton.setOnClickListener(mLoveListener);
		mBanButton = (ImageButton) findViewById(R.id.ban);
		mBanButton.setOnClickListener(mBanListener);
		mStopButton = (ImageButton) findViewById(R.id.stop);
		mStopButton.requestFocus();
		mStopButton.setOnClickListener(mStopListener);
		mNextButton = (ImageButton) findViewById(R.id.skip);
		mNextButton.setOnClickListener(mNextListener);
		mOntourButton = (ImageButton) findViewById(R.id.ontour);
		mOntourButton.setOnClickListener(mOntourListener);

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(RadioPlayerService.META_CHANGED);
		mIntentFilter.addAction(RadioPlayerService.PLAYBACK_FINISHED);
		mIntentFilter.addAction(RadioPlayerService.PLAYBACK_STATE_CHANGED);
		mIntentFilter.addAction(RadioPlayerService.STATION_CHANGED);
		mIntentFilter.addAction(RadioPlayerService.PLAYBACK_ERROR);
		mIntentFilter.addAction("fm.last.android.ERROR");

		Intent intent = getIntent();
		if (intent != null) {
			if(intent.getAction() != null && intent.getAction().equals("android.media.action.MEDIA_PLAY_FROM_SEARCH")) {
				String query = intent.getStringExtra(SearchManager.QUERY);
				try {
					Station s = mServer.searchForStation(query);
					if(s != null) {
						LastFMApplication.getInstance().playRadioStation(Player.this, s.getUrl(), false);
						tuning = true;
					}
				} catch (NullPointerException e) {
					Intent i = new Intent(this, Profile.class);
					i.putExtra(SearchManager.QUERY, query);
					startActivity(i);
					finish();
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if(intent.getData() != null && intent.getData().getScheme() != null && intent.getData().getScheme().equals("lastfm")) {
				LastFMApplication.getInstance().playRadioStation(Player.this, intent.getData().toString(), false);
				tuning = true;
			}
		}
		if (icicle != null) {
			mCachedArtist = icicle.getString("artist");
			mCachedTrack = icicle.getString("track");
			mCachedBitmap = icicle.getParcelable("artwork");
			if (icicle.getBoolean("isOnTour", false))
				mOntourButton.setVisibility(View.VISIBLE);
			loved = icicle.getBoolean("loved", false);
			if (loved) {
				mLoveButton.setImageResource(R.drawable.loved);
			} else {
				mLoveButton.setImageResource(R.drawable.love);
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.buy_menu_item).setEnabled(
				Amazon.getAmazonVersion(this) > 0);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.player, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.info_menu_item) {
			showMetadataIntent();
			return true;
		}

		if (handleOptionItemSelected(this, item))
			return true;

		return super.onOptionsItemSelected(item);
	}

	public boolean handleOptionItemSelected(Context c, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.buy_menu_item:
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
						"player-buy", // Action
						"", // Label
						0); // Value
			} catch (SQLiteException e) {
				//Google Analytics doesn't appear to be thread safe
			}
			Amazon.searchForTrack(this, mArtistName.getText().toString(),
					mTrackName.getText().toString());
			break;
		case R.id.share_menu_item:
			try {
				if (LastFMApplication.getInstance().player == null)
					return false;
				Intent intent = new Intent(c, ShareResolverActivity.class);
				intent.putExtra(Share.INTENT_EXTRA_ARTIST, LastFMApplication
						.getInstance().player.getArtistName());
				intent.putExtra(Share.INTENT_EXTRA_TRACK, LastFMApplication
						.getInstance().player.getTrackName());
				c.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.playlist_menu_item:
			try {
				if (LastFMApplication.getInstance().player == null)
					return false;
				Intent intent = new Intent(c, AddToPlaylist.class);
				intent.putExtra(Share.INTENT_EXTRA_ARTIST, LastFMApplication
						.getInstance().player.getArtistName());
				intent.putExtra(Share.INTENT_EXTRA_TRACK, LastFMApplication
						.getInstance().player.getTrackName());
				c.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.tag_menu_item:
			fireTagActivity(c);
			break;

		default:
			break;
		}
		return false;
	}

	private static void fireTagActivity(Context c) {
		String artist = null;
		String track = null;

		try {
			if (LastFMApplication.getInstance().player == null)
				return;
			artist = LastFMApplication.getInstance().player.getArtistName();
			track = LastFMApplication.getInstance().player.getTrackName();
			Intent myIntent = new Intent(c, fm.last.android.activity.Tag.class);
			myIntent.putExtra("lastfm.artist", artist);
			myIntent.putExtra("lastfm.track", track);
			c.startActivity(myIntent);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		paused = false;
	}

	@Override
	public void onStop() {

		paused = true;
		mHandler.removeMessages(REFRESH);

		super.onStop();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("configchange", getChangingConfigurations() != 0);
		outState.putString("artist", mArtistName.getText().toString());
		outState.putString("track", mTrackName.getText().toString());
		outState.putParcelable("artwork", mAlbum.getBitmap());
		outState.putBoolean("isOnTour",
				mOntourButton.getVisibility() == View.VISIBLE);
		outState.putBoolean("loved",
				loved);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		try {
			unregisterReceiver(mStatusListener);
		} catch(IllegalArgumentException e) {
			//The listener wasn't registered yet
		}
		mHandler.removeMessages(REFRESH);
		if (LastFMApplication.getInstance().player != null)
			LastFMApplication.getInstance().unbindPlayerService();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		registerReceiver(mStatusListener, mIntentFilter);
		if (LastFMApplication.getInstance().player == null)
			LastFMApplication.getInstance().bindPlayerService();
		updateTrackInfo();
		long next = refreshNow();
		queueNextRefresh(next);

		try {
			LastFMApplication.getInstance().tracker.trackPageView("/Player");
		} catch (SQLiteException e) {
			//Google Analytics doesn't appear to be thread safe
		}

		if(!tuning) {
			bindService(new Intent(Player.this,
					fm.last.android.player.RadioPlayerService.class),
					new ServiceConnection() {
						public void onServiceConnected(ComponentName comp,
								IBinder binder) {
							IRadioPlayer player = IRadioPlayer.Stub
									.asInterface(binder);
							try {
								if (!player.isPlaying()) {
									Intent i = new Intent(Player.this, Profile.class);
									startActivity(i);
									finish();
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
		}
	}

	@Override
	public void onDestroy() {
		mAlbum.cancel();
		super.onDestroy();
	}

	private View.OnClickListener mLoveListener = new View.OnClickListener() {

		public void onClick(View v) {
			Intent i = new Intent("fm.last.android.LOVE");
			sendBroadcast(i);
			bindService(new Intent(Player.this,
					fm.last.android.player.RadioPlayerService.class),
					new ServiceConnection() {
						public void onServiceConnected(ComponentName comp,
								IBinder binder) {
							IRadioPlayer player = IRadioPlayer.Stub
									.asInterface(binder);
							try {
								if (player.isPlaying())
									player.setLoved(true);
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							unbindService(this);
						}

						public void onServiceDisconnected(ComponentName comp) {
						}
					}, 0);
			mLoveButton.setImageResource(R.drawable.loved);
			loved = true;
			
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
						"player-love", // Action
						"", // Label
						0); // Value
			} catch (SQLiteException e) {
				//Google Analytics doesn't appear to be thread safe
			}
		}
	};

	private View.OnClickListener mBanListener = new View.OnClickListener() {

		public void onClick(View v) {
			Intent i = new Intent("fm.last.android.BAN");
			sendBroadcast(i);
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
						"player-ban", // Action
						"", // Label
						0); // Value
			} catch (SQLiteException e) {
				//Google Analytics doesn't appear to be thread safe
			}
			bindService(new Intent(Player.this,
					fm.last.android.player.RadioPlayerService.class),
					new ServiceConnection() {
						public void onServiceConnected(ComponentName comp,
								IBinder binder) {
							IRadioPlayer player = IRadioPlayer.Stub
									.asInterface(binder);
							try {
								if (player.isPlaying())
									player.skip();
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							unbindService(this);
						}

						public void onServiceDisconnected(ComponentName comp) {
						}
					}, 0);
		}
	};

	private View.OnClickListener mNextListener = new View.OnClickListener() {

		public void onClick(View v) {
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
						"player-skip", // Action
						"", // Label
						0); // Value
			} catch (SQLiteException e) {
				//Google Analytics doesn't appear to be thread safe
			}
			bindService(new Intent(Player.this,
					fm.last.android.player.RadioPlayerService.class),
					new ServiceConnection() {
						public void onServiceConnected(ComponentName comp,
								IBinder binder) {
							IRadioPlayer player = IRadioPlayer.Stub
									.asInterface(binder);
							try {
								if (player.isPlaying())
									player.skip();
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							unbindService(this);
						}

						public void onServiceDisconnected(ComponentName comp) {
						}
					}, 0);
		}
	};

	private void showMetadataIntent() {
		showMetadataIntent(false);
	}

	private void showEventsMetadataIntent() {
		showMetadataIntent(true);
	}

	private void showMetadataIntent(boolean gotoEventsTab) {
		Intent metaIntent = new Intent(this,
				fm.last.android.activity.Metadata.class);
		metaIntent.putExtra("artist", mArtistName.getText());
		metaIntent.putExtra("track", mTrackName.getText());
		if (gotoEventsTab)
			metaIntent.putExtra("show_events", true);

		startActivity(metaIntent);
	}

	private View.OnClickListener mOntourListener = new View.OnClickListener() {

		public void onClick(View v) {
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
						"on-tour-badge", // Action
						"", // Label
						0); // Value
			} catch (SQLiteException e) {
				//Google Analytics doesn't appear to be thread safe
			}
			showEventsMetadataIntent();
		}

	};

	private View.OnClickListener mStopListener = new View.OnClickListener() {

		public void onClick(View v) {
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
						"player-stop", // Action
						"", // Label
						0); // Value
			} catch (SQLiteException e) {
				//Google Analytics doesn't appear to be thread safe
			}

			bindService(new Intent(Player.this,
					fm.last.android.player.RadioPlayerService.class),
					new ServiceConnection() {
						public void onServiceConnected(ComponentName comp,
								IBinder binder) {
							IRadioPlayer player = IRadioPlayer.Stub
									.asInterface(binder);
							try {
								if (player.isPlaying())
									player.stop();
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							unbindService(this);
						}

						public void onServiceDisconnected(ComponentName comp) {
						}
					}, 0);
			LastFMApplication.getInstance().unbindPlayerService();
			finish();
		}
	};

	private BroadcastReceiver mStatusListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (action.equals(RadioPlayerService.META_CHANGED)) {
				// redraw the artist/title info and
				// set new max for progress bar
				updateTrackInfo();
			} else if (action.equals(RadioPlayerService.PLAYBACK_FINISHED)) {
				finish();
			} else if (action.equals(RadioPlayerService.STATION_CHANGED)) {
				// FIXME: this *should* be handled by the metadata activity now
				// if(mDetailFlipper.getDisplayedChild() == 1)
				// mDetailFlipper.showPrevious();
			} else if (action.equals(RadioPlayerService.PLAYBACK_ERROR) || action.equals("fm.last.android.ERROR")) {
				// TODO add a skip counter and try to skip 3 times before
				// display an error message
				if (mTuningDialog != null) {
					mTuningDialog.dismiss();
					mTuningDialog = null;
				}
				WSError error = intent.getParcelableExtra("error");
				if (error != null) {
					LastFMApplication.getInstance().presentError(Player.this,
							error);
				} else {
					LastFMApplication.getInstance().presentError(
							Player.this,
							getResources().getString(
									R.string.ERROR_PLAYBACK_FAILED_TITLE),
							getResources().getString(
									R.string.ERROR_PLAYBACK_FAILED));
				}
			}
		}
	};

	private void updateTrackInfo() {
		LastFMApplication.getInstance().bindService(
				new Intent(LastFMApplication.getInstance(),
						fm.last.android.player.RadioPlayerService.class),
				new ServiceConnection() {
					public void onServiceConnected(ComponentName comp,
							IBinder binder) {
						IRadioPlayer player = IRadioPlayer.Stub
								.asInterface(binder);
						try {
							String artistName = player.getArtistName();
							String trackName = player.getTrackName();
							loved = player.getLoved();
							
							
							if (loved) {
								mLoveButton.setImageResource(R.drawable.loved);
							} else {
								mLoveButton.setImageResource(R.drawable.love);
							}

							if ((mArtistName != null && mArtistName.getText() != null && mTrackName != null && mTrackName.getText() != null) && (!mArtistName.getText().equals(artistName)
									|| !mTrackName.getText().equals(trackName))) {
								if (artistName
										.equals(RadioPlayerService.UNKNOWN)) {
									mArtistName.setText("");
								} else {
									mArtistName.setText(artistName);
								}
								if (trackName
										.equals(RadioPlayerService.UNKNOWN)) {
									mTrackName.setText("");
								} else {
									mTrackName.setText(trackName);
								}

								if (mTuningDialog != null
										&& player.getState() == RadioPlayerService.STATE_TUNING) {
									mTuningDialog = ProgressDialog.show(
											Player.this, "",
											getString(R.string.player_tuning),
											true, false);
									mTuningDialog
											.setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);
									mTuningDialog.setCancelable(true);
								}

								if (mCachedArtist != null
										&& mCachedArtist.equals(artistName)
										&& mCachedTrack != null
										&& mCachedTrack.equals(trackName)) {
									if (mCachedBitmap != null) {
										mAlbum.setImageBitmap(mCachedBitmap);
										mCachedBitmap = null;
									} else {
										new LoadAlbumArtTask().execute(player
												.getArtUrl(), player
												.getArtistName(), player
												.getAlbumName());
									}
								} else {
									new LoadEventsTask().execute((Void) null);
									new LoadAlbumArtTask().execute(player
											.getArtUrl(), player
											.getArtistName(), player
											.getAlbumName());
								}
							}
						} catch (java.util.concurrent.RejectedExecutionException e) {
							e.printStackTrace();
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						LastFMApplication.getInstance().unbindService(this);
					}

					public void onServiceDisconnected(ComponentName comp) {
					}
				}, Context.BIND_AUTO_CREATE);
	}

	private void queueNextRefresh(long delay) {
		if (!paused) {
			Message msg = mHandler.obtainMessage(REFRESH);
			mHandler.removeMessages(REFRESH);
			mHandler.sendMessageDelayed(msg, delay);
		}
	}

	private long refreshNow() {
		LastFMApplication.getInstance().bindService(
				new Intent(LastFMApplication.getInstance(),
						fm.last.android.player.RadioPlayerService.class),
				new ServiceConnection() {
					public void onServiceConnected(ComponentName comp,
							IBinder binder) {
						IRadioPlayer player = IRadioPlayer.Stub
								.asInterface(binder);
						try {
							mDuration = player.getDuration();
							long pos = player.getPosition();
							if ((pos >= 0) && (mDuration > 0)
									&& (pos <= mDuration)) {
								mCurrentTime.setText(makeTimeString(
										Player.this, pos / 1000));
								mTotalTime.setText(makeTimeString(Player.this,
										mDuration / 1000));
								mProgress
										.setProgress((int) (1000 * pos / mDuration));
								mProgress.setSecondaryProgress(player.getBufferPercent() * 10);
								if (mTuningDialog != null) {
									mTuningDialog.dismiss();
									mTuningDialog = null;
								}
							} else {
								mCurrentTime.setText("--:--");
								mTotalTime.setText("--:--");
								mProgress.setProgress(0);
								mProgress.setSecondaryProgress(player.getBufferPercent() * 10);
								if (player.isPlaying() && mTuningDialog != null) {
									mTuningDialog.dismiss();
									mTuningDialog = null;
								}
							}
							// return the number of milliseconds until the next
							// full second, so
							// the counter can be updated at just the right time
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						LastFMApplication.getInstance().unbindService(this);
					}

					public void onServiceDisconnected(ComponentName comp) {
					}
				}, Context.BIND_AUTO_CREATE);

		return 500;
	}

	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH:
				long next = refreshNow();
				queueNextRefresh(next);
				break;
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
	 * 
	 * Hi I changed this due to a bug I managed to make at time zero. But
	 * honestly, this kind of optimisation is a bit much. --mxcl
	 */

	public static String makeTimeString(Context context, long secs) {
		return new Formatter().format("%02d:%02d", secs / 60, secs % 60)
				.toString();
	}

	private class LoadAlbumArtTask extends UserTask<String, Void, Boolean> {
		String artUrl;

		@Override
		public void onPreExecute() {
			mAlbum.clear();
		}

		@Override
		public Boolean doInBackground(String... params) {
			Album album;
			boolean success = false;

			artUrl = params[0];
			Log.i("LastFm", "Art URL from playlist: " + artUrl);

			try {
				String artistName = params[1];
				String albumName = params[2];
				if (!artistName.equals(RadioPlayerService.UNKNOWN) && albumName != null && albumName.length() > 0) {
					album = mServer.getAlbumInfo(artistName, albumName);
					if (album != null) {
						for (ImageUrl image : album.getImages()) {
							if (image.getSize().contentEquals("extralarge")) {
								artUrl = image.getUrl();
								break;
							}
						}
					}
				}
				success = true;
			} catch (Exception e) {
				e.printStackTrace();
			} catch (WSError e) {
			}
			return success;
		}

		@Override
		public void onPostExecute(Boolean result) {
			if (artUrl != RadioPlayerService.UNKNOWN) {
				mAlbum.fetch(artUrl);
			} else {
				mAlbum.setDefaultImageResource(R.drawable.no_artwork);
			}
		}
	}

	private class LoadEventsTask extends UserTask<Void, Void, Boolean> {
		String mArtist = null;

		@Override
		public void onPreExecute() {
			mArtist = mArtistName.getText().toString();
			mOntourButton.clearAnimation();
			mOntourButton.setVisibility(View.GONE);
			mOntourButton.invalidate();
		}

		@Override
		public Boolean doInBackground(Void... params) {
			boolean result = false;
			if (mArtist != null
					&& (mArtist.equals(RadioPlayerService.UNKNOWN) || Player.this.mArtistName.getText().toString()
							.compareToIgnoreCase(mArtist) != 0))
				return false;

			try {
				Event[] events = mServer.getArtistEvents(mArtist);
				if (events.length > 0)
					result = true;

			} catch (Exception e) {
				e.printStackTrace();
			} catch (WSError e) {
			}
			return result;
		}

		@Override
		public void onPostExecute(Boolean result) {

			// Check if this is a stale event request
			if (Player.this.mArtistName.getText().toString()
					.compareToIgnoreCase(mArtist) != 0)
				return;

			if (result) {

				Animation a = AnimationUtils.loadAnimation(Player.this,
						R.anim.tag_fadein);
				a.setAnimationListener(new AnimationListener() {

					public void onAnimationEnd(Animation animation) {
					}

					public void onAnimationRepeat(Animation animation) {
					}

					public void onAnimationStart(Animation animation) {
						mOntourButton.setVisibility(View.VISIBLE);
					}

				});
				mOntourButton.startAnimation(a);
			} else {

			}
		}
	}

}
