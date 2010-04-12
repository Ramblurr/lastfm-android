/**
 * 
 */
package fm.last.android;

import java.util.Formatter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.RemoteViews;
import fm.last.android.activity.PopupActionActivity;
import fm.last.android.activity.Profile;
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

	public static boolean isAndroidMusicInstalled(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			pm.getPackageInfo("com.android.music", 0);
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
						LastFMApplication.getInstance().bindService(new Intent().setClassName("com.android.music", "com.android.music.MediaPlaybackService"), new ServiceConnection() {
							public void onServiceConnected(ComponentName comp, IBinder binder) {
								com.android.music.IMediaPlaybackService s = com.android.music.IMediaPlaybackService.Stub.asInterface(binder);
		
								try {
									if (s.isPlaying()) {
										String track = s.getTrackName();
										String artist = s.getArtistName();
										Intent i = new Intent(LastFMApplication.getInstance(), PopupActionActivity.class);
										i.putExtra("lastfm.artist", artist);
										i.putExtra("lastfm.track", track);
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
				LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
						"widget-skip", // Action
						"", // Label
						0); // Value

				if(mediaPlayerPlaying) {
					if(isAndroidMusicInstalled(context)) {
						LastFMApplication.getInstance().bindService(new Intent().setClassName("com.android.music", "com.android.music.MediaPlaybackService"), new ServiceConnection() {
							public void onServiceConnected(ComponentName comp, IBinder binder) {
								com.android.music.IMediaPlaybackService s = com.android.music.IMediaPlaybackService.Stub.asInterface(binder);
		
								try {
									if (s.isPlaying()) {
										s.next();
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
						if(isHTCMusicInstalled(context)) {
							LastFMApplication.getInstance().bindService(new Intent().setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"), new ServiceConnection() {
								public void onServiceConnected(ComponentName comp, IBinder binder) {
									com.htc.music.IMediaPlaybackService s = com.htc.music.IMediaPlaybackService.Stub.asInterface(binder);
					
									try {
										if (s.isPlaying()) {
											s.next();
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
											if (LastFMApplication.getInstance().getLastStation() == null) {
												LastFMApplication.getInstance().playRadioStation(ctx, "lastfm://user/" + session.getName() + "/personal", false);
												updateAppWidget_idle(LastFMApplication.getInstance(), null, true);
											} else {
												LastFMApplication.getInstance().playRadioStation(ctx, LastFMApplication.getInstance().getLastStation().getUrl(),
														false);
												updateAppWidget_idle(LastFMApplication.getInstance(), LastFMApplication.getInstance().getLastStation().getName(),
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
				LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
						"widget-stop", // Action
						"", // Label
						0); // Value
				
				if(mediaPlayerPlaying) {
					if(isAndroidMusicInstalled(context)) {
						LastFMApplication.getInstance().bindService(new Intent().setClassName("com.android.music", "com.android.music.MediaPlaybackService"), new ServiceConnection() {
							public void onServiceConnected(ComponentName comp, IBinder binder) {
								com.android.music.IMediaPlaybackService s = com.android.music.IMediaPlaybackService.Stub.asInterface(binder);
		
								try {
									if (s.isPlaying()) {
										s.stop();
										mediaPlayerPlaying = false;
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
						if(isHTCMusicInstalled(context)) {
							LastFMApplication.getInstance().bindService(new Intent().setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"), new ServiceConnection() {
								public void onServiceConnected(ComponentName comp, IBinder binder) {
									com.htc.music.IMediaPlaybackService s = com.htc.music.IMediaPlaybackService.Stub.asInterface(binder);
					
									try {
										if (s.isPlaying()) {
											s.stop();
											mediaPlayerPlaying = false;
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
											if (LastFMApplication.getInstance().getLastStation() == null) {
												LastFMApplication.getInstance().playRadioStation(ctx, "lastfm://user/" + session.getName() + "/personal", false);
												updateAppWidget_idle(LastFMApplication.getInstance(), null, true);
											} else {
												LastFMApplication.getInstance().playRadioStation(ctx, LastFMApplication.getInstance().getLastStation().getUrl(),
														false);
												updateAppWidget_idle(LastFMApplication.getInstance(), LastFMApplication.getInstance().getLastStation().getName(),
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
				LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
						"widget-love", // Action
						"", // Label
						0); // Value
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
				Intent i = new Intent("fm.last.android.BAN");
				context.sendBroadcast(i);
				LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
						"widget-ban", // Action
						"", // Label
						0); // Value
				
				if(mediaPlayerPlaying) {
					if(isAndroidMusicInstalled(context)) {
						LastFMApplication.getInstance().bindService(new Intent().setClassName("com.android.music", "com.android.music.MediaPlaybackService"), new ServiceConnection() {
							public void onServiceConnected(ComponentName comp, IBinder binder) {
								com.android.music.IMediaPlaybackService s = com.android.music.IMediaPlaybackService.Stub.asInterface(binder);
		
								try {
									if (s.isPlaying()) {
										s.next();
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
						if(isHTCMusicInstalled(context)) {
							LastFMApplication.getInstance().bindService(new Intent().setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"), new ServiceConnection() {
								public void onServiceConnected(ComponentName comp, IBinder binder) {
									com.htc.music.IMediaPlaybackService s = com.htc.music.IMediaPlaybackService.Stub.asInterface(binder);
					
									try {
										if (s.isPlaying()) {
											s.next();
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
					}
				} else {
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
			AppWidgetManager.getInstance(context);
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
			LastFMApplication.getInstance().bindService(new Intent().setClassName("com.android.music", "com.android.music.MediaPlaybackService"), new ServiceConnection() {
				public void onServiceConnected(ComponentName comp, IBinder binder) {
					com.android.music.IMediaPlaybackService s = com.android.music.IMediaPlaybackService.Stub.asInterface(binder);
	
					try {
						if (s.isPlaying()) {
							mediaPlayerPlaying = true;
							updateAppWidget_playing(ctx, s.getTrackName(), s.getArtistName(), s.position(), s.duration(), false, false);
						} else {
							mediaPlayerPlaying = false;
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
							mediaPlayerPlaying = true;
							updateAppWidget_playing(ctx, s.getTrackName(), s.getArtistName(), s.position(), s.duration(), false, false);
						} else {
							mediaPlayerPlaying = false;
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

		if(!mediaPlayerPlaying) {
			LastFMApplication.getInstance().bindService(new Intent(context, fm.last.android.player.RadioPlayerService.class), new ServiceConnection() {
				public void onServiceConnected(ComponentName comp, IBinder binder) {
					IRadioPlayer player = IRadioPlayer.Stub.asInterface(binder);
					try {
						if (player.isPlaying()) {
							long duration = player.getDuration();
							long pos = player.getPosition();
							boolean buffering = true;
							boolean loved = player.getLoved();
							if ((pos >= 0) && (duration > 0) && (pos <= duration)) {
								buffering = false;
							}
							if (player.getTrackName().equals(RadioPlayerService.UNKNOWN))
								updateAppWidget_idle(ctx, player.getStationName(), true);
							else
								updateAppWidget_playing(ctx, player.getTrackName(), player.getArtistName(), pos, duration, buffering, loved);
						} else if (!mediaPlayerPlaying) {
							String stationName = player.getStationName();
							if (stationName == null) {
								Station station = LastFMApplication.getInstance().getLastStation();
								if (station != null)
									stationName = station.getName();
							}
							updateAppWidget_idle(ctx, stationName, player.getState() == RadioPlayerService.STATE_TUNING);
						}
					} catch (RemoteException ex) {
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

}
