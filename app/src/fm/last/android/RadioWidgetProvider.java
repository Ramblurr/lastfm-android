/**
 * 
 */
package fm.last.android;

import java.util.Formatter;

import fm.last.android.activity.Metadata;
import fm.last.android.player.IRadioPlayer;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * @author sam
 *
 */
public class RadioWidgetProvider extends AppWidgetProvider {
    // log tag
    private static final String TAG = "LastFmRadioWidgetProvider";
    
    static final ComponentName THIS_APPWIDGET =
        new ComponentName("fm.last.android",
                "fm.last.android.RadioWidgetProvider");
    
    private static RadioWidgetProvider sInstance;
    
    public static synchronized RadioWidgetProvider getInstance() {
        if (sInstance == null) {
            sInstance = new RadioWidgetProvider();
        }
        return sInstance;
    }
    
    @Override 
    public void onReceive(Context context, Intent intent) { 
        final String action = intent.getAction();
        if (action.equals("fm.last.android.SKIP")) {
    		if (LastFMApplication.getInstance().player == null)
    			LastFMApplication.getInstance().bindPlayerService();
    		if (LastFMApplication.getInstance().player != null) {
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
        } else if (action.equals("fm.last.android.STOP")) {
       		if (LastFMApplication.getInstance().player == null)
       			LastFMApplication.getInstance().bindPlayerService();
       		if (LastFMApplication.getInstance().player != null) {
   				try {
   					// If the player is in a stopped state, call startRadio instead
   					// of stop (we should change the icon to "Play")
   					if (LastFMApplication.getInstance().player.isPlaying())
   						LastFMApplication.getInstance().player.stop();
   					else
   						LastFMApplication.getInstance().player.startRadio();
   				} catch (RemoteException e) {
   					// TODO Auto-generated catch block
   					e.printStackTrace();
   				}
            } 
        } else { 
        	super.onReceive(context, intent);
        }
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

        //Hook up the buttons (this should really be done eslewhere but doesn't hurt here)
        intent = new Intent("fm.last.android.LOVE");
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.love, pendingIntent);

        intent = new Intent("fm.last.android.BAN");
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.ban, pendingIntent);

        intent = new Intent("fm.last.android.SKIP");
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.skip, pendingIntent);

        intent = new Intent("fm.last.android.STOP");
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.stop, pendingIntent);

        intent = new Intent("fm.last.android.ACTION");
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.menu, pendingIntent);

		if (LastFMApplication.getInstance().player == null)
			LastFMApplication.getInstance().bindPlayerService();
		if (LastFMApplication.getInstance().player != null) {
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
					}
			        views.setTextViewText(R.id.widgettext, player.getArtistName() + " - " + player.getTrackName());
				} else {
					views.setViewVisibility(R.id.totaltime, View.GONE);
			        views.setTextViewText(R.id.widgettext, player.getStationName());
			        views.setProgressBar(android.R.id.progress, 1, 0, false);
					views.setProgressBar(R.id.spinner, 1, 0, false);
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
