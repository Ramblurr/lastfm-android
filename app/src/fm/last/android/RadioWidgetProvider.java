/**
 * 
 */
package fm.last.android;

import java.util.Formatter;
import java.util.concurrent.RejectedExecutionException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.RemoteViews;
import fm.last.android.activity.PopupActionActivity;
import fm.last.android.activity.Profile;
import fm.last.android.db.RecentStationsDao;
import fm.last.android.player.IRadioPlayer;
import fm.last.android.player.RadioPlayerService;
import fm.last.api.Session;
import fm.last.api.Station;

/**
 * @author sam
 * 
 */
public class RadioWidgetProvider extends AppWidgetProvider {
	static final ComponentName THIS_APPWIDGET = new ComponentName("fm.last.android", "fm.last.android.RadioWidgetProvider");

	private static RadioWidgetProvider sInstance;
	private static PendingIntent mAlarmIntent = null;
	private static boolean mediaPlayerPlaying = false;

	public static synchronized RadioWidgetProvider getInstance() {
		if (sInstance == null) {
			sInstance = new RadioWidgetProvider();
		}
		return sInstance;
	}

	public static boolean isHTCMusicInstalled(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			pm.getPackageInfo("com.htc.music", 0);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public static String getAndroidMusicPackageName(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			pm.getPackageInfo("com.google.android.music", 0);
			return "com.google.android.music";
		} catch (Exception e) {
		}
		return "com.android.music";
	}

	public static boolean isAndroidMusicInstalled(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			pm.getPackageInfo(getAndroidMusicPackageName(ctx), 0);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final Context ctx = context;
		final String action = intent.getAction();
		final Session session = LastFMApplication.getInstance().session;
		if (session != null) {
			if (action.equals("fm.last.android.widget.ACTION")) {
				if(mediaPlayerPlaying) {
					if(isAndroidMusicInstalled(context)) {
						LastFMApplication.getInstance().bindService(new Intent().setClassName(getAndroidMusicPackageName(context), "com.android.music.MediaPlaybackService"), new ServiceConnection() {
							public void onServiceConnected(ComponentName comp, IBinder binder) {
								com.android.music.IMediaPlaybackService s = com.android.music.IMediaPlaybackService.Stub.asInterface(binder);
		
								try {
									if (s.isPlaying()) {
										String track = s.getTrackName();
										String artist = s.getArtistName();
										Intent i = new Intent(LastFMApplication.getInstance(), PopupActionActivity.class);
										i.putExtra("lastfm.artist", artist);
										i.putExtra("lastfm.track", track);
										i.putExtra("lastfm.nowplaying", true);
										i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										LastFMApplication.getInstance().startActivity(i);
									}
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								LastFMApplication.getInstance().unbindService(this);
							}
		
							public void onServiceDisconnected(ComponentName comp) {
							}
						}, 0);
					}
					if(isHTCMusicInstalled(context)) {
						LastFMApplication.getInstance().bindService(new Intent().setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"), new ServiceConnection() {
							public void onServiceConnected(ComponentName comp, IBinder binder) {
								com.htc.music.IMediaPlaybackService s = com.htc.music.IMediaPlaybackService.Stub.asInterface(binder);
				
								try {
									if (s.isPlaying()) {
										String track = s.getTrackName();
										String artist = s.getArtistName();
										Intent i = new Intent(LastFMApplication.getInstance(), PopupActionActivity.class);
										i.putExtra("lastfm.artist", artist);
										i.putExtra("lastfm.track", track);
										i.putExtra("lastfm.nowplaying", true);
										i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										LastFMApplication.getInstance().startActivity(i);
									}
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								LastFMApplication.getInstance().unbindService(this);
							}
				
							public void onServiceDisconnected(ComponentName comp) {
							}
						}, 0);
					}
				} else {
					LastFMApplication.getInstance().bindService(new Intent(LastFMApplication.getInstance(), fm.last.android.player.RadioPlayerService.class),
							new ServiceConnection() {
								public void onServiceConnected(ComponentName comp, IBinder binder) {
									IRadioPlayer player = IRadioPlayer.Stub.asInterface(binder);
									try {
										String track = player.getTrackName();
										String artist = player.getArtistName();
										if (!track.equals(RadioPlayerService.UNKNOWN)) {
											Intent i = new Intent(LastFMApplication.getInstance(), PopupActionActivity.class);
											i.putExtra("lastfm.artist", artist);
											i.putExtra("lastfm.track", track);
											i.putExtra("lastfm.nowplaying", true);
											i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											LastFMApplication.getInstance().startActivity(i);
										} else {
											Intent i = new Intent(LastFMApplication.getInstance(), Profile.class);
											i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											LastFMApplication.getInstance().startActivity(i);
										}
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
			} else if (action.equals("fm.last.android.widget.SKIP")) {
				try {
					LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
							"widget-skip", // Action
							"", // Label
							0); // Value
				} catch (SQLiteException e) {
					//Google Analytics doesn't appear to be thread safe
				}

				if(mediaPlayerPlaying || !RadioPlayerService.radioAvailable(context)) {
					if(isAndroidMusicInstalled(context)) {
						LastFMApplication.getInstance().bindService(new Intent().setClassName(getAndroidMusicPackageName(context), "com.android.music.MediaPlaybackService"), new ServiceConnection() {
							public void onServiceConnected(ComponentName comp, IBinder binder) {
								com.android.music.IMediaPlaybackService s = com.android.music.IMediaPlaybackService.Stub.asInterface(binder);
		
								try {
									s.next();
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								LastFMApplication.getInstance().unbindService(this);
							}
		
							public void onServiceDisconnected(ComponentName comp) {
							}
						}, 0);
					}
					if(isHTCMusicInstalled(context)) {
						LastFMApplication.getInstance().bindService(new Intent().setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"), new ServiceConnection() {
							public void onServiceConnected(ComponentName comp, IBinder binder) {
								com.htc.music.IMediaPlaybackService s = com.htc.music.IMediaPlaybackService.Stub.asInterface(binder);
				
								try {
									s.next();
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								LastFMApplication.getInstance().unbindService(this);
							}
				
							public void onServiceDisconnected(ComponentName comp) {
							}
						}, 0);
					}
				} else {
					LastFMApplication.getInstance().bindService(new Intent(LastFMApplication.getInstance(), fm.last.android.player.RadioPlayerService.class),
							new ServiceConnection() {
								public void onServiceConnected(ComponentName comp, IBinder binder) {
									IRadioPlayer player = IRadioPlayer.Stub.asInterface(binder);
									try {
										// If the player is in a stopped state, call
										// startRadio instead
										// of skip
										if (player.isPlaying())
											player.skip();
										else {
											Station lastStation = RecentStationsDao.getInstance().getLastStation();
											if (lastStation == null) {
												LastFMApplication.getInstance().playRadioStation(ctx, "lastfm://user/" + session.getName() + "/personal", false);
												updateAppWidget_idle(LastFMApplication.getInstance(), null, true);
											} else {
												LastFMApplication.getInstance().playRadioStation(ctx, lastStation.getUrl(),
														false);
												updateAppWidget_idle(LastFMApplication.getInstance(), lastStation.getName(),
														true);
											}
										}
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
			} else if (action.equals("fm.last.android.widget.STOP")) {
				try {
					LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
							"widget-stop", // Action
							"", // Label
							0); // Value
				} catch (SQLiteException e) {
					//Google Analytics doesn't appear to be thread safe
				}
				
				if(mediaPlayerPlaying || !RadioPlayerService.radioAvailable(context)) {
					if(isAndroidMusicInstalled(context)) {
						LastFMApplication.getInstance().bindService(new Intent().setClassName(getAndroidMusicPackageName(context), "com.android.music.MediaPlaybackService"), new ServiceConnection() {
							public void onServiceConnected(ComponentName comp, IBinder binder) {
								com.android.music.IMediaPlaybackService s = com.android.music.IMediaPlaybackService.Stub.asInterface(binder);
		
								try {
									if (s.isPlaying()) {
										s.pause();
										mediaPlayerPlaying = false;
									} else {
										s.play();
										mediaPlayerPlaying = true;
									}
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								LastFMApplication.getInstance().unbindService(this);
							}
		
							public void onServiceDisconnected(ComponentName comp) {
							}
						}, 0);
					}
					if(isHTCMusicInstalled(context)) {
						LastFMApplication.getInstance().bindService(new Intent().setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"), new ServiceConnection() {
							public void onServiceConnected(ComponentName comp, IBinder binder) {
								com.htc.music.IMediaPlaybackService s = com.htc.music.IMediaPlaybackService.Stub.asInterface(binder);
				
								try {
									if (s.isPlaying()) {
										s.pause();
										mediaPlayerPlaying = false;
									} else {
										s.play();
										mediaPlayerPlaying = true;
									}
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								LastFMApplication.getInstance().unbindService(this);
							}
				
							public void onServiceDisconnected(ComponentName comp) {
							}
						}, 0);
					}
				} else {
					LastFMApplication.getInstance().bindService(new Intent(LastFMApplication.getInstance(), fm.last.android.player.RadioPlayerService.class),
							new ServiceConnection() {
								public void onServiceConnected(ComponentName comp, IBinder binder) {
									IRadioPlayer player = IRadioPlayer.Stub.asInterface(binder);
									try {
										// If the player is in a stopped state, call
										// startRadio instead
										// of stop
										if (player.isPlaying())
											player.stop();
										else {
											Station lastStation = RecentStationsDao.getInstance().getLastStation();
											if (lastStation == null) {
												LastFMApplication.getInstance().playRadioStation(ctx, "lastfm://user/" + session.getName() + "/personal", false);
												updateAppWidget_idle(LastFMApplication.getInstance(), null, true);
											} else {
												LastFMApplication.getInstance().playRadioStation(ctx, lastStation.getUrl(),
														false);
												updateAppWidget_idle(LastFMApplication.getInstance(), lastStation.getName(),
														true);
											}
										}
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
			} else if (action.equals("fm.last.android.widget.LOVE")) {
				Intent i = new Intent("fm.last.android.LOVE");
				context.sendBroadcast(i);
				try {
					LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
							"widget-love", // Action
							"", // Label
							0); // Value
				} catch (SQLiteException e) {
					//Google Analytics doesn't appear to be thread safe
				}
				LastFMApplication.getInstance().bindService(new Intent(context, fm.last.android.player.RadioPlayerService.class), new ServiceConnection() {
					public void onServiceConnected(ComponentName comp, IBinder binder) {
						IRadioPlayer player = IRadioPlayer.Stub.asInterface(binder);
						try {
							if (player.isPlaying())
								player.setLoved(true);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						LastFMApplication.getInstance().unbindService(this);
					}

					public void onServiceDisconnected(ComponentName arg0) {
					}
				}, 0);
			} else if (action.equals("fm.last.android.widget.BAN")) {
				if(mediaPlayerPlaying || !RadioPlayerService.radioAvailable(context)) {
					try {
						LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
								"widget-prev", // Action
								"", // Label
								0); // Value
					} catch (SQLiteException e) {
						//Google Analytics doesn't appear to be thread safe
					}

					if(isAndroidMusicInstalled(context)) {
						LastFMApplication.getInstance().bindService(new Intent().setClassName(getAndroidMusicPackageName(context), "com.android.music.MediaPlaybackService"), new ServiceConnection() {
							public void onServiceConnected(ComponentName comp, IBinder binder) {
								com.android.music.IMediaPlaybackService s = com.android.music.IMediaPlaybackService.Stub.asInterface(binder);
		
								try {
									s.prev();
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								LastFMApplication.getInstance().unbindService(this);
							}
		
							public void onServiceDisconnected(ComponentName comp) {
							}
						}, 0);
					}
					if(isHTCMusicInstalled(context)) {
						LastFMApplication.getInstance().bindService(new Intent().setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"), new ServiceConnection() {
							public void onServiceConnected(ComponentName comp, IBinder binder) {
								com.htc.music.IMediaPlaybackService s = com.htc.music.IMediaPlaybackService.Stub.asInterface(binder);
				
								try {
									s.prev();
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								LastFMApplication.getInstance().unbindService(this);
							}
				
							public void onServiceDisconnected(ComponentName comp) {
							}
						}, 0);
					}
				} else {
					Intent i = new Intent("fm.last.android.BAN");
					context.sendBroadcast(i);
					try {
						LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
								"widget-ban", // Action
								"", // Label
								0); // Value
					} catch (SQLiteException e) {
						//Google Analytics doesn't appear to be thread safe
					}

					LastFMApplication.getInstance().bindService(new Intent(context, fm.last.android.player.RadioPlayerService.class), new ServiceConnection() {
						public void onServiceConnected(ComponentName comp, IBinder binder) {
							IRadioPlayer player = IRadioPlayer.Stub.asInterface(binder);
							try {
								if (player.isPlaying())
									player.skip();
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							LastFMApplication.getInstance().unbindService(this);
						}
	
						public void onServiceDisconnected(ComponentName comp) {
						}
					}, 0);
				}
			} else if (action.equals("fm.last.android.widget.UPDATE") || action.startsWith("com.")) {
				updateAppWidget(context);
			}
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		updateAppWidget(context);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Intent intent = new Intent("fm.last.android.widget.UPDATE");
		mAlarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(mAlarmIntent);
		mAlarmIntent = null;
	}

	@Override
	public void onEnabled(Context context) {
		updateAppWidget(context);
	}

	@Override
	public void onDisabled(Context context) {
	}

	private static void bindButtonIntents(Context context, RemoteViews views) {
		AppWidgetManager appWidgetManager = null;
		
		if(views == null) {
			appWidgetManager = AppWidgetManager.getInstance(context);
			views = new RemoteViews(context.getPackageName(), R.layout.widget);
		}
		PendingIntent pendingIntent;
		Intent intent;

		intent = new Intent("fm.last.android.widget.LOVE");
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.love, pendingIntent);

		intent = new Intent("fm.last.android.widget.BAN");
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.ban, pendingIntent);

		intent = new Intent("fm.last.android.widget.SKIP");
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.skip, pendingIntent);

		intent = new Intent("fm.last.android.widget.STOP");
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.stop, pendingIntent);

		intent = new Intent("fm.last.android.widget.ACTION");
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.menu, pendingIntent);

		if(appWidgetManager != null)
			appWidgetManager.updateAppWidget(THIS_APPWIDGET, views);
	}

	public static void updateAppWidget_idle(Context context, String stationName, boolean tuning) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		bindButtonIntents(context, views);

		views.setViewVisibility(R.id.totaltime, View.GONE);
		if (stationName != null) {
			views.setTextViewText(R.id.widgettext, stationName);
		} else {
			Session session = LastFMApplication.getInstance().session;
			if (session != null)
				views.setTextViewText(R.id.widgettext, session.getName() + "'s Library");
		}
		views.setProgressBar(android.R.id.progress, 1, 0, false);
		if (tuning) {
			views.setProgressBar(R.id.spinner, 1, 0, true);
			views.setImageViewResource(R.id.stop, R.drawable.stop);
			if (mAlarmIntent == null) {
				Intent intent = new Intent("fm.last.android.widget.UPDATE");
				mAlarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
				AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 1000, 1000, mAlarmIntent);
			}
		} else {
			views.setProgressBar(R.id.spinner, 1, 0, false);
			views.setImageViewResource(R.id.stop, R.drawable.play);
			Intent intent = new Intent("fm.last.android.widget.UPDATE");
			mAlarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.cancel(mAlarmIntent);
			mAlarmIntent = null;
		}
		views.setImageViewResource(R.id.love, R.drawable.love);
		if(RadioPlayerService.radioAvailable(context))
			views.setImageViewResource(R.id.ban, R.drawable.ban);
		else
			views.setImageViewResource(R.id.ban, R.drawable.prev);
		appWidgetManager.updateAppWidget(THIS_APPWIDGET, views);
	}

	public static void updateAppWidget_playing(Context context, String title, String artist, long pos, long duration, boolean buffering, boolean loved) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		bindButtonIntents(context, views);

		if (buffering) {
			views.setViewVisibility(R.id.totaltime, View.GONE);
			views.setProgressBar(R.id.spinner, 1, 0, true);
			views.setProgressBar(android.R.id.progress, 1, 0, false);
		} else {
			views.setViewVisibility(R.id.totaltime, View.VISIBLE);
			views.setTextViewText(R.id.totaltime, makeTimeString((duration - pos) / 1000));
			views.setProgressBar(R.id.spinner, 1, 0, false);
			views.setProgressBar(android.R.id.progress, (int) duration, (int) pos, false);
		}

		views.setTextViewText(R.id.widgettext, artist + " - " + title);
		views.setImageViewResource(R.id.stop, R.drawable.stop);
		if(loved)
			views.setImageViewResource(R.id.love, R.drawable.loved);
		else
			views.setImageViewResource(R.id.love, R.drawable.love);

		if(mediaPlayerPlaying) {
			views.setImageViewResource(R.id.ban, R.drawable.prev);
		} else {
			views.setImageViewResource(R.id.ban, R.drawable.ban);
		}
		
		if (mAlarmIntent == null) {
			Intent intent = new Intent("fm.last.android.widget.UPDATE");
			mAlarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 1000, 1000, mAlarmIntent);
		}

		appWidgetManager.updateAppWidget(THIS_APPWIDGET, views);
	}

	public static void updateAppWidget(Context context) {
		final Context ctx = context;
		bindButtonIntents(context, null);

		if(isAndroidMusicInstalled(context)) {
			LastFMApplication.getInstance().bindService(new Intent().setClassName(getAndroidMusicPackageName(context), "com.android.music.MediaPlaybackService"), new ServiceConnection() {
				public void onServiceConnected(ComponentName comp, IBinder binder) {
					com.android.music.IMediaPlaybackService s = com.android.music.IMediaPlaybackService.Stub.asInterface(binder);
	
					try {
						new UpdateFromAndroidPlayerTask(ctx).execute(s);
					} catch (RejectedExecutionException e) { //try again in 1 second
						if (mAlarmIntent == null) {
							Intent intent = new Intent("fm.last.android.widget.UPDATE");
							mAlarmIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
							AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
							am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 1000, 1000, mAlarmIntent);
						}
					}
					LastFMApplication.getInstance().unbindService(this);
				}
	
				public void onServiceDisconnected(ComponentName comp) {
				}
			}, 0);
		}
		
		if(isHTCMusicInstalled(context)) {
			LastFMApplication.getInstance().bindService(new Intent().setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"), new ServiceConnection() {
				public void onServiceConnected(ComponentName comp, IBinder binder) {
					com.htc.music.IMediaPlaybackService s = com.htc.music.IMediaPlaybackService.Stub.asInterface(binder);
	
					try {
						new UpdateFromHTCPlayerTask(ctx).execute(s);
					} catch (RejectedExecutionException e) { //try again in 1 second
						if (mAlarmIntent == null) {
							Intent intent = new Intent("fm.last.android.widget.UPDATE");
							mAlarmIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
							AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
							am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 1000, 1000, mAlarmIntent);
						}
					}

					LastFMApplication.getInstance().unbindService(this);
				}
	
				public void onServiceDisconnected(ComponentName comp) {
				}
			}, 0);
		}

		if(!mediaPlayerPlaying && RadioPlayerService.radioAvailable(context)) {
			LastFMApplication.getInstance().bindService(new Intent(context, fm.last.android.player.RadioPlayerService.class), new ServiceConnection() {
				public void onServiceConnected(ComponentName comp, IBinder binder) {
					IRadioPlayer player = IRadioPlayer.Stub.asInterface(binder);
	
					try {
						new UpdateFromRadioPlayerTask(ctx).execute(player);
					} catch (RejectedExecutionException e) { //try again in 1 second
						if (mAlarmIntent == null) {
							Intent intent = new Intent("fm.last.android.widget.UPDATE");
							mAlarmIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
							AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
							am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 1000, 1000, mAlarmIntent);
						}
					}

					LastFMApplication.getInstance().unbindService(this);
				}
	
				public void onServiceDisconnected(ComponentName comp) {
				}
			}, Context.BIND_AUTO_CREATE);
		}
	}

	public static String makeTimeString(long secs) {
		return new Formatter().format("%02d:%02d", secs / 60, secs % 60).toString();
	}

	private static class UpdateFromAndroidPlayerTask extends AsyncTask<com.android.music.IMediaPlaybackService, Void, Boolean> {
		Context ctx = null;
		String trackName = "";
		String artistName = "";
		long position = 0L;
		long duration = 0L;

		UpdateFromAndroidPlayerTask(Context context) {
			super();
			ctx = context;
		}
		
		@Override
		public Boolean doInBackground(com.android.music.IMediaPlaybackService... s) {
			try {
				mediaPlayerPlaying = s[0].isPlaying();
				trackName = s[0].getTrackName();
				artistName = s[0].getArtistName();
				position = s[0].position();
				duration = s[0].duration();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return mediaPlayerPlaying;
		}

		@Override
		public void onPostExecute(Boolean result) {
			if(result) {
				updateAppWidget_playing(ctx, trackName, artistName, position, duration, false, false);
			} else if(trackName != null && !RadioPlayerService.radioAvailable(ctx)) {
				updateAppWidget_idle(ctx, artistName + " - " + trackName, false);
			} else if(!RadioPlayerService.radioAvailable(ctx)) {
				updateAppWidget_idle(ctx, "", false);
			}
		}
	}

	private static class UpdateFromRadioPlayerTask extends AsyncTask<IRadioPlayer, Void, Boolean> {
		Context ctx = null;
		String trackName = "";
		String artistName = "";
		long position = 0L;
		long duration = 0L;
		boolean buffering = true;
		boolean loved = false;
		String stationName = "";
		int state = 0;

		UpdateFromRadioPlayerTask(Context context) {
			super();
			ctx = context;
		}
		
		@Override
		public Boolean doInBackground(IRadioPlayer... s) {
			boolean playing = false;
			try {
				if (s[0].isPlaying()) {
					trackName = s[0].getTrackName();
					artistName = s[0].getArtistName();
					position = s[0].getPosition();
					duration = s[0].getDuration();
					loved = s[0].getLoved();
					if ((position >= 0) && (duration > 0) && (position <= duration)) {
						buffering = false;
					}
					playing = true;
				}
				state = s[0].getState();
				stationName = s[0].getStationName();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return playing;
		}

		@Override
		public void onPostExecute(Boolean result) {
			if(result) {
				if (trackName.equals(RadioPlayerService.UNKNOWN))
					updateAppWidget_idle(ctx, stationName, true);
				else
					updateAppWidget_playing(ctx, trackName, artistName, position, duration, buffering, loved);
			} else if (!mediaPlayerPlaying) {
				if (stationName == null || stationName.length() < 1) {
					Station station = RecentStationsDao.getInstance().getLastStation();
					if (station != null)
						stationName = station.getName();
				}
				updateAppWidget_idle(ctx, stationName, state == RadioPlayerService.STATE_TUNING);
			}
		}
	}

	private static class UpdateFromHTCPlayerTask extends AsyncTask<com.htc.music.IMediaPlaybackService, Void, Boolean> {
		Context ctx = null;
		String trackName = "";
		String artistName = "";
		long position = 0L;
		long duration = 0L;

		UpdateFromHTCPlayerTask(Context context) {
			super();
			ctx = context;
		}
		
		@Override
		public Boolean doInBackground(com.htc.music.IMediaPlaybackService... s) {
			try {
				if (s[0].isPlaying()) {
					mediaPlayerPlaying = true;
					trackName = s[0].getTrackName();
					artistName = s[0].getArtistName();
					position = s[0].position();
					duration = s[0].duration();
				} else {
					mediaPlayerPlaying = false;
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return mediaPlayerPlaying;
		}

		@Override
		public void onPostExecute(Boolean result) {
			if(result) {
				updateAppWidget_playing(ctx, trackName, artistName, position, duration, false, false);
			}
		}
	}
}
