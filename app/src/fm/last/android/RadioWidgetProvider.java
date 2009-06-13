/**
 * 
 */
package fm.last.android;

import java.util.Formatter;

import fm.last.android.activity.Metadata;
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
        Log.d(TAG, action);
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
        Log.d(TAG, "onUpdate");
		if (LastFMApplication.getInstance().player == null)
			LastFMApplication.getInstance().bindPlayerService();
		if (LastFMApplication.getInstance().player != null) {
			try {
		        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

		        long duration = LastFMApplication.getInstance().player.getDuration();
				long pos = LastFMApplication.getInstance().player.getPosition();
				if ((pos >= 0) && (duration > 0) && (pos <= duration)) {
			        views.setTextViewText(R.id.currenttime,makeTimeString(pos / 1000));
			        views.setTextViewText(R.id.totaltime,makeTimeString(duration / 1000));
					//mProgress.setProgress((int) (1000 * pos / mDuration));
				} else {
			        views.setTextViewText(R.id.currenttime,"--:--");
			        views.setTextViewText(R.id.totaltime,"--:--");
			        views.setTextViewText(R.id.widgettext, "");
					//mProgress.setProgress(0);
				}
		        appWidgetManager.updateAppWidget(THIS_APPWIDGET, views);
			} catch (RemoteException ex) {
			}
		}
    }

    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "onDeleted");
    }

    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled");
		if (LastFMApplication.getInstance().player == null)
			LastFMApplication.getInstance().bindPlayerService();
		if (LastFMApplication.getInstance().player != null) {
			try {
				updateAppWidget(context, LastFMApplication.getInstance().player.getTrackName(), LastFMApplication.getInstance().player.getArtistName());
			} catch (RemoteException ex) {
			}
		}
    }

    public void onDisabled(Context context) {
        Log.d(TAG, "onDisabled");
    }

    public static void updateAppWidget(Context context, String title, String artist) {
        Log.d(TAG, "updateAppWidget");
        Intent intent;
        PendingIntent pendingIntent;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);        
        // Construct the RemoteViews object.  It takes the package name (in our case, it's our
        // package, but it needs this because on the other side it's the widget host inflating
        // the layout from our package).
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.widgettext, artist + " - " + title);

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

        intent = new Intent(context, Metadata.class);
        intent.putExtra("artist", artist);
        intent.putExtra("track", title);
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.menu, pendingIntent);

        appWidgetManager.updateAppWidget(THIS_APPWIDGET, views);
    }

    public static String makeTimeString(long secs) {
		return new Formatter().format("%02d:%02d", secs / 60, secs % 60)
				.toString();
	}

}
