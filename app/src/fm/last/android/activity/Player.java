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

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.LastFm;
import fm.last.android.R;
import fm.last.android.RemoteImageHandler;
import fm.last.android.RemoteImageView;
import fm.last.android.Worker;
import fm.last.android.player.IRadioPlayer;
import fm.last.android.player.RadioPlayerService;
import fm.last.android.utils.UserTask;
import fm.last.android.widget.AdArea;
import fm.last.api.Album;
import fm.last.api.Event;
import fm.last.api.ImageUrl;
import fm.last.api.LastFmServer;
import fm.last.api.WSError;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Player extends Activity {

	private ImageButton mLoveButton;
	private ImageButton mBanButton;
	private ImageButton mStopButton;
	private ImageButton mNextButton;
	private ImageButton mOntourButton;
	private RemoteImageView mAlbum;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private TextView mArtistName;
	private TextView mTrackName;
	private ProgressBar mProgress;
	private long mDuration;
	private boolean paused;
	private ProgressDialog mBufferingDialog;
	private ProgressDialog mTuningDialog;

	private static final int REFRESH = 1;

	LastFmServer mServer = AndroidLastFmServerFactory.getServer();

	private Worker mAlbumArtWorker;
	private RemoteImageHandler mAlbumArtHandler;
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
		mAlbum = (RemoteImageView) findViewById(R.id.album);
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

		mAlbumArtWorker = new Worker("album art worker");
		mAlbumArtHandler = new RemoteImageHandler(mAlbumArtWorker.getLooper(),
				mHandler);

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(RadioPlayerService.META_CHANGED);
		mIntentFilter.addAction(RadioPlayerService.PLAYBACK_FINISHED);
		mIntentFilter.addAction(RadioPlayerService.PLAYBACK_STATE_CHANGED);
		mIntentFilter.addAction(RadioPlayerService.STATION_CHANGED);
		mIntentFilter.addAction(RadioPlayerService.PLAYBACK_ERROR);

		Intent intent = getIntent();
        if(intent != null && intent.getData() != null && intent.getData().getScheme().equals("lastfm")) {
        	LastFMApplication.getInstance().playRadioStation(Player.this,intent.getData().toString(), false);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.player, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if( item.getItemId() == R.id.info_menu_item ) {
			showMetadataIntent();
			return true;
		}
		
		if( handleOptionItemSelected( this, item ) )
			return true;
		
		return super.onOptionsItemSelected(item);
	}
	
	public static boolean handleOptionItemSelected( Context c, MenuItem item ) {
		switch (item.getItemId()) {
		case R.id.buy_menu_item:
			try {
				if (LastFMApplication.getInstance().player == null)
					return false;
				Intent intent = new Intent(Intent.ACTION_SEARCH);
				intent.setComponent(new ComponentName("com.amazon.mp3",
						"com.amazon.mp3.android.client.SearchActivity"));
				intent
						.putExtra("actionSearchString", LastFMApplication
								.getInstance().player.getArtistName()
								+ " "
								+ LastFMApplication.getInstance().player
										.getTrackName());
				intent.putExtra("actionSearchType", 0);
				c.startActivity(intent);
			} catch (Exception e) {
				LastFMApplication
						.getInstance()
						.presentError(c, "Amazon Unavailable",
								"The Amazon MP3 store is not currently available on this device.");
			}
			break;
		case R.id.share_menu_item:
			try {
				if (LastFMApplication.getInstance().player == null)
					return false;
				Intent intent = new Intent(c, Share.class);
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
			fireTagActivity( c );
			break;

		default:
			break;
		}
		return false;
	}

	private static void fireTagActivity( Context c ) {
		String artist = null;
		String track = null;

		try {
			if (LastFMApplication.getInstance().player == null)
				return;
			artist = LastFMApplication.getInstance().player.getArtistName();
			track = LastFMApplication.getInstance().player.getTrackName();
			Intent myIntent = new Intent(c,
					fm.last.android.activity.Tag.class);
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

		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			LayoutParams params = mAlbum.getLayoutParams();
			if(AdArea.adsEnabled(this)) {
				params.width = 155;
				params.height = 155;
			} else {
				params.width = 212;
				params.height = 212;
			}
			mAlbum.setLayoutParams(params);
		} else {
			LayoutParams params = mAlbum.getLayoutParams();
			if(AdArea.adsEnabled(this)) {
				params.width = 240;
				params.height = 240;
			} else {
				params.width = 300;
				params.height = 300;
			}
			mAlbum.setLayoutParams(params);
		}
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
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mStatusListener);
		mHandler.removeMessages(REFRESH);
		if (LastFMApplication.getInstance().player != null)
			LastFMApplication.getInstance().unbindPlayerService();
		super.onPause();
	}

	@Override
	public void onResume() {
		registerReceiver(mStatusListener, mIntentFilter);
		if (LastFMApplication.getInstance().player == null)
			LastFMApplication.getInstance().bindPlayerService();
		updateTrackInfo();
		long next = refreshNow();
		queueNextRefresh(next);
		super.onResume();
	}

	@Override
	public void onDestroy() {
		mAlbumArtWorker.quit();
		super.onDestroy();
	}

	private View.OnClickListener mLoveListener = new View.OnClickListener() {

		public void onClick(View v) {

			if (LastFMApplication.getInstance().player == null)
				return;
			Intent i = new Intent("fm.last.android.LOVE");
			sendBroadcast(i);
		}
	};

	private View.OnClickListener mBanListener = new View.OnClickListener() {

		public void onClick(View v) {

			if (LastFMApplication.getInstance().player == null)
				return;
			Intent i = new Intent("fm.last.android.BAN");
			sendBroadcast(i);
			try {
				LastFMApplication.getInstance().player.skip();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private View.OnClickListener mNextListener = new View.OnClickListener() {

		public void onClick(View v) {

			if (LastFMApplication.getInstance().player == null)
				return;
			try {
				// If the player is in a stopped state, call startRadio instead
				// of skip
				if (LastFMApplication.getInstance().player.isPlaying())
					LastFMApplication.getInstance().player.skip();
				else
					LastFMApplication.getInstance().player.startRadio();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private void showMetadataIntent() {
		showMetadataIntent( false );
	}
	
	private void showEventsMetadataIntent() {
		showMetadataIntent( true );
	}
	
	private void showMetadataIntent( boolean gotoEventsTab ) {
		Intent metaIntent = new Intent(this, fm.last.android.activity.Metadata.class);
		metaIntent.putExtra("artist", mArtistName.getText());
		metaIntent.putExtra("track", mTrackName.getText());
		if( gotoEventsTab )
			metaIntent.putExtra("show_events", true );
		
		startActivity(metaIntent);
	}
	
	private View.OnClickListener mOntourListener = new View.OnClickListener() {

		public void onClick(View v) {
			showEventsMetadataIntent();
		}

	};

	private View.OnClickListener mStopListener = new View.OnClickListener() {

		public void onClick(View v) {

			if (LastFMApplication.getInstance().player != null) {
				try {
					if(LastFMApplication.getInstance().player.isPlaying())
						LastFMApplication.getInstance().player.stop();
					LastFMApplication.getInstance().unbindPlayerService();
				} catch (RemoteException ex) {
					System.out.println(ex.getMessage());
				}
			}
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
			} else if (action.equals(RadioPlayerService.PLAYBACK_ERROR)) {
				// TODO add a skip counter and try to skip 3 times before
				// display an error message
				try {
					if (LastFMApplication.getInstance().player == null)
						return;
					WSError error = LastFMApplication.getInstance().player
							.getError();
					if (error != null) {
						LastFMApplication.getInstance().presentError(context,
								error);
					} else {
						LastFMApplication.getInstance().presentError(
								context,
								getResources().getString(
										R.string.ERROR_PLAYBACK_FAILED_TITLE),
								getResources().getString(
										R.string.ERROR_PLAYBACK_FAILED));
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	private void updateTrackInfo() {
        LastFMApplication.getInstance().bindService(new Intent(LastFMApplication.getInstance(),fm.last.android.player.RadioPlayerService.class ),
                new ServiceConnection() {
                public void onServiceConnected(ComponentName comp, IBinder binder) {
                        IRadioPlayer player = IRadioPlayer.Stub.asInterface(binder);
    					try {
    						String artistName = player.getArtistName();
		    				String trackName = player.getTrackName();
		    				if(!mArtistName.getText().equals(artistName) || !mTrackName.getText().equals(trackName)) {
			    				if(artistName.equals(RadioPlayerService.UNKNOWN)) {
			    					mArtistName.setText("");
			    				} else {
			    					mArtistName.setText(artistName);
			    				}
			    				if(trackName.equals(RadioPlayerService.UNKNOWN)) {
			    					mTrackName.setText("");
			    				} else {
			    					mTrackName.setText(trackName);
			    				}
			    				
								if (mTuningDialog != null && player.getState() == RadioPlayerService.STATE_TUNING) {
									mTuningDialog = ProgressDialog.show(Player.this, "",
											"Tuning", true, false);
									mTuningDialog
											.setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);
									mTuningDialog.setCancelable(true);
								}
	
			    				// fetching artist events (On Tour indicator)
			    				new LoadEventsTask().execute((Void)null);
			
			    				Bitmap art = player.getAlbumArt();
			    				mAlbum.setArtwork(art);
			    				mAlbum.invalidate();
			    				if (art == null)
			    					 new LoadAlbumArtTask().execute((Void) null);
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
        LastFMApplication.getInstance().bindService(new Intent(LastFMApplication.getInstance(),fm.last.android.player.RadioPlayerService.class ),
                new ServiceConnection() {
                public void onServiceConnected(ComponentName comp, IBinder binder) {
                        IRadioPlayer player = IRadioPlayer.Stub.asInterface(binder);
    					try {
    						mDuration = player.getDuration();
    						long pos = player.getPosition();
    						if ((pos >= 0) && (mDuration > 0) && (pos <= mDuration)) {
    							mCurrentTime.setText(makeTimeString(Player.this, pos / 1000));
    							mTotalTime.setText(makeTimeString(Player.this, mDuration / 1000));
    							mProgress.setProgress((int) (1000 * pos / mDuration));
    							if (mBufferingDialog != null) {
    								mBufferingDialog.dismiss();
    								mBufferingDialog = null;
    							}
    							if (mTuningDialog != null) {
    								mTuningDialog.dismiss();
    								mTuningDialog = null;
    							}
    						} else {
    							mCurrentTime.setText("--:--");
    							mTotalTime.setText("--:--");
    							mProgress.setProgress(0);
    							if (mBufferingDialog == null && player.isPlaying()) {
        							if (mTuningDialog != null) {
        								mTuningDialog.dismiss();
        								mTuningDialog = null;
        							}
    								mBufferingDialog = ProgressDialog.show(Player.this, "",
    										"Buffering", true, false);
    								mBufferingDialog
    										.setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);
    								mBufferingDialog.setCancelable(true);
    							}
    						}
    						// return the number of milliseconds until the next full second, so
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

		public void handleMessage(Message msg) {
			final Message m = msg;
			switch (msg.what) {
			case RemoteImageHandler.REMOTE_IMAGE_DECODED:
				mAlbum.setArtwork((Bitmap) msg.obj);
				mAlbum.invalidate();
		        LastFMApplication.getInstance().bindService(new Intent(LastFMApplication.getInstance(),fm.last.android.player.RadioPlayerService.class ),
		                new ServiceConnection() {
		                public void onServiceConnected(ComponentName comp, IBinder binder) {
		                        IRadioPlayer player = IRadioPlayer.Stub.asInterface(binder);
		    					try {
		    						if(m.obj != null && m.obj.getClass() == Bitmap.class)
		    							player.setAlbumArt((Bitmap) m.obj);
		    					} catch (RemoteException e) {
		    						// TODO Auto-generated catch block
		    						e.printStackTrace();
		    					}
		    					LastFMApplication.getInstance().unbindService(this);
		                }

		                public void onServiceDisconnected(ComponentName comp) {
		                }
		        }, Context.BIND_AUTO_CREATE);
				break;

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

	private class LoadAlbumArtTask extends UserTask<Void, Void, Boolean> {
		String artUrl;

		@Override
		public void onPreExecute() {
		}

		@Override
		public Boolean doInBackground(Void... params) {
			Album album;
			boolean success = false;

			try {
				if (LastFMApplication.getInstance().player != null) {
					artUrl = LastFMApplication.getInstance().player.getArtUrl();
					String artistName = LastFMApplication.getInstance().player
							.getArtistName();
					String albumName = LastFMApplication.getInstance().player
							.getAlbumName();
					if (albumName != null && albumName.length() > 0) {
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
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return success;
		}
		

		@Override
		public void onPostExecute(Boolean result) {
			if (artUrl != RadioPlayerService.UNKNOWN) {
				mAlbumArtHandler
						.removeMessages(RemoteImageHandler.GET_REMOTE_IMAGE);
				mAlbumArtHandler.obtainMessage(
						RemoteImageHandler.GET_REMOTE_IMAGE, artUrl)
						.sendToTarget();
			}
		}
	}
	
	private class LoadEventsTask extends UserTask<Void, Void, Boolean> {
		String mArtist = null;
		
		@Override
		public void onPreExecute()
		{
			mArtist = mArtistName.getText().toString();
			mOntourButton.clearAnimation();
			mOntourButton.setVisibility(View.GONE);
			mOntourButton.invalidate();
		}
		
		@Override
		public Boolean doInBackground(Void...params) {
			boolean result = false;
			if( mArtist != null && 
				Player.this.mArtistName.getText().toString()
						.compareToIgnoreCase( mArtist ) != 0 )
				return false;

			try {
				Event[] events = mServer.getArtistEvents(mArtist);
				if(events.length > 0)
					result = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		public void onPostExecute(Boolean result) {
			
			//Check if this is a stale event request
			if( Player.this.mArtistName.getText().toString()
						.compareToIgnoreCase( mArtist ) != 0 )
				return;
			
			if(result) {
				
				Animation a = AnimationUtils.loadAnimation(Player.this, R.anim.tag_fadein);
				a.setAnimationListener(new AnimationListener(){

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
