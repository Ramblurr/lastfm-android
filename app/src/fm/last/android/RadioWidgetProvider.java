/**
 * 
 */
package fm.last.android;

import java.util.Formatter;

import fm.last.android.activity.Metadata;
import fm.last.android.activity.PopupActionActivity;
import fm.last.android.activity.Share;
import fm.last.android.player.IRadioPlayer;
import fm.last.android.player.RadioPlayerService;
import fm.last.api.Session;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * @author sam
 *
 */
public class RadioWidgetProvider extends AppWidgetProvider {
    static final ComponentName THIS_APPWIDGET =
        new ComponentName("fm.last.android",
                "fm.last.android.RadioWidgetProvider");
    
    private static RadioWidgetProvider sInstance;
    private static PendingIntent mAlarmIntent = null;
    
    public static synchronized RadioWidgetProvider getInstance() {
        if (sInstance == null) {
            sInstance = new RadioWidgetProvider();
        }
        return sInstance;
    }
    
    @Override 
    public void onReceive(Context context, Intent intent) { 
        final String action = intent.getAction();
		Session session = LastFMApplication.getInstance().map.get("lastfm_session");
		if(session != null) {
	        if (action.equals("fm.last.android.widget.ACTION")) {
	       		if (LastFMApplication.getInstance().player == null)
	    			LastFMApplication.getInstance().bindPlayerService();
	    		if (LastFMApplication.getInstance().player != null) {
					try {
						// If the player is in a stopped state, call startRadio instead
						// of skip
						String track = LastFMApplication.getInstance().player.getTrackName();
						String artist = LastFMApplication.getInstance().player.getArtistName();
						if(!track.equals(RadioPlayerService.UNKNOWN)) {
							Intent i = new Intent( context, PopupActionActivity.class );
					        i.putExtra("lastfm.artist", artist);
					        i.putExtra("lastfm.track", track);
					        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					        context.startActivity( i );
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	        } else if (action.equals("fm.last.android.widget.SKIP")) {
	    		if (LastFMApplication.getInstance().player == null)
	    			LastFMApplication.getInstance().bindPlayerService();
	    		if (LastFMApplication.getInstance().player != null) {
					try {
						// If the player is in a stopped state, call startRadio instead
						// of skip
						if (LastFMApplication.getInstance().player.isPlaying())
							LastFMApplication.getInstance().player.skip();
						else {
							if(LastFMApplication.getInstance().player.getStationName() == null) {
								LastFMApplication.getInstance().player.tune("lastfm://user/"+session.getName()+"/personal", session);
							}
							LastFMApplication.getInstance().player.startRadio();
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	        } else if (action.equals("fm.last.android.widget.STOP")) {
	       		if (LastFMApplication.getInstance().player == null)
	       			LastFMApplication.getInstance().bindPlayerService();
	       		if (LastFMApplication.getInstance().player != null) {
	   				try {
	   					// If the player is in a stopped state, call startRadio instead
	   					// of stop (we should change the icon to "Play")
	   					if (LastFMApplication.getInstance().player.isPlaying())
	   						LastFMApplication.getInstance().player.stop();
	   					else {
							if(LastFMApplication.getInstance().player.getStationName() == null) {
								LastFMApplication.getInstance().player.tune("lastfm://user/"+session.getName()+"/personal", session);
							}
							LastFMApplication.getInstance().player.startRadio();
	   					}
	   				} catch (RemoteException e) {
	   					// TODO Auto-generated catch block
	   					e.printStackTrace();
	   				}
	            }
	        } else if (action.equals("fm.last.android.widget.LOVE")) {
				Intent i = new Intent("fm.last.android.LOVE");
				context.sendBroadcast(i);
	        } else if (action.equals("fm.last.android.widget.BAN")) {
				Intent i = new Intent("fm.last.android.BAN");
				context.sendBroadcast(i);
	    		if (LastFMApplication.getInstance().player == null)
	    			LastFMApplication.getInstance().bindPlayerService();
	    		if (LastFMApplication.getInstance().player != null) {
					try {
						if (LastFMApplication.getInstance().player.isPlaying())
							LastFMApplication.getInstance().player.skip();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	        } else if (action.equals("fm.last.android.widget.UPDATE")) {
	        	updateAppWidget(context);
	        }
		}
    	super.onReceive(context, intent);
    } 

    
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	updateAppWidget(context);
    }

    public void onDeleted(Context context, int[] appWidgetIds) {
    }

    public void onEnabled(Context context) {
		updateAppWidget(context);
    }

    public void onDisabled(Context context) {
    }

    public static void updateAppWidget(Context context) {
        Intent intent;
        PendingIntent pendingIntent;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);        
        // Construct the RemoteViews object.  It takes the package name (in our case, it's our
        // package, but it needs this because on the other side it's the widget host inflating
        // the layout from our package).
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        //Hook up the buttons (this should really be done elsewhere but doesn't hurt here)
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

		if (LastFMApplication.getInstance().player == null) {
			LastFMApplication.getInstance().bindPlayerService();
			//Try again in 1 second if the player service wasn't running
	        intent = new Intent("fm.last.android.widget.UPDATE");
	        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	        am.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, PendingIntent.getBroadcast(context, 0, intent, 0));
		} else {
			IRadioPlayer player = LastFMApplication.getInstance().player;
			try {
				if(player.isPlaying()) {
			        long duration = player.getDuration();
					long pos = player.getPosition();
					if ((pos >= 0) && (duration > 0) && (pos <= duration)) {
						views.setViewVisibility(R.id.totaltime, View.VISIBLE);
				        views.setTextViewText(R.id.totaltime,makeTimeString((duration - pos) / 1000));
						views.setProgressBar(R.id.spinner, 1, 0, false);
				        views.setProgressBar(android.R.id.progress, (int)duration, (int)pos, false);
					} else {
						views.setViewVisibility(R.id.totaltime, View.GONE);
						views.setProgressBar(R.id.spinner, 1, 0, true);
				        views.setProgressBar(android.R.id.progress, 1, 0, false);
					}
					if(player.getTrackName().equals(RadioPlayerService.UNKNOWN))
				        views.setTextViewText(R.id.widgettext, player.getStationName());
					else
						views.setTextViewText(R.id.widgettext, player.getArtistName() + " - " + player.getTrackName());
					views.setImageViewResource(R.id.stop, R.drawable.stop);
					if(mAlarmIntent == null) {
				        intent = new Intent("fm.last.android.widget.UPDATE");
				        mAlarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
				        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 1000, 1000, mAlarmIntent);
					}
				} else {
					views.setViewVisibility(R.id.totaltime, View.GONE);
					if(player.getStationName() != null) {
						views.setTextViewText(R.id.widgettext, player.getStationName());
					} else {
						Session session = LastFMApplication.getInstance().map.get("lastfm_session");
						if(session != null)
							views.setTextViewText(R.id.widgettext, session.getName() + "'s Library");
					}
			        views.setProgressBar(android.R.id.progress, 1, 0, false);
					views.setProgressBar(R.id.spinner, 1, 0, false);
					views.setImageViewResource(R.id.stop, R.drawable.play);
					if(mAlarmIntent != null) {
				        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				        am.cancel(mAlarmIntent);
				        mAlarmIntent = null;
					}
				}
			} catch (RemoteException ex) {
			}
		}        
        appWidgetManager.updateAppWidget(THIS_APPWIDGET, views);
    }

    public static String makeTimeString(long secs) {
		return new Formatter().format("%02d:%02d", secs / 60, secs % 60)
				.toString();
	}

}
