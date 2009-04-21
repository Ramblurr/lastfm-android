/**
 * 
 */
package fm.last.android.scrobbler;

import java.io.IOException;

import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.User;
import fm.last.api.WSError;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

/**
 * @author sam
 *
 */
public class ScrobblerApplication extends Application {
	private static ScrobblerApplication _instance = null;
    public static String PREFS = "fm.last.android.scrobbler.preferences";

    public static ScrobblerApplication the() {
		if(_instance == null) {
			_instance = new ScrobblerApplication();
		}
		return _instance;
	}
	
    public ScrobblerApplication() {
    	_instance = this;
    }
    
	public Session getSession() {
		SharedPreferences settings = getSharedPreferences( PREFS, 0 );
        String username = settings.getString( "lastfm_user", "" );
        String session_key = settings.getString( "lastfm_session_key", "" );
        String subscriber = settings.getString( "lastfm_subscriber", "0" );
        LastFmServer server = AndroidLastFmServerFactory.getServer();

        if ( !username.equals( "" ) && !session_key.equals( "" ) )
        {
            try {
				User user = server.getUserInfo(session_key);
				if(user != null) {
					subscriber = user.getSubscriber();
		            SharedPreferences.Editor editor = settings.edit();
		            editor.putString( "lastfm_subscriber", subscriber);
		            editor.commit();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	Session session = new Session(username, session_key, subscriber);
	    	return session;
        }
        return null;
	}

    public void presentError(Context ctx, WSError error) {
    	int title = 0;
    	int description = 0;
    	
    	System.out.printf("Received a webservice error during method: %s: %s\n", error.getMethod(), error.getMessage());
    	
    	if(error.getMethod().startsWith("radio.")) {
    		title = R.string.ERROR_STATION_TITLE;
    		switch(error.getCode()) {
	    		case WSError.ERROR_NotEnoughContent:
	    			title = R.string.ERROR_INSUFFICIENT_CONTENT_TITLE;
	    			description = R.string.ERROR_INSUFFICIENT_CONTENT;
	    			break;

	    		case WSError.ERROR_NotEnoughFans:
	    			description = R.string.ERROR_INSUFFICIENT_FANS;
	    			break;

	    		case WSError.ERROR_NotEnoughMembers:
	    			description = R.string.ERROR_INSUFFICIENT_MEMBERS;
	    			break;

	    		case WSError.ERROR_NotEnoughNeighbours:
	    			description = R.string.ERROR_INSUFFICIENT_NEIGHBOURS;
	    			break;
    		}
    	}
    	
    	if(error.getMethod().equals("user.signUp")) {
    		title = R.string.ERROR_SIGNUP_TITLE;
    		switch(error.getCode()) {
	    		case WSError.ERROR_InvalidParameters:
	    			presentError(ctx, getResources().getString(title), error.getMessage());
	    			return;

    		}
    	}
    	
    	if(title == 0)
    		title = R.string.ERROR_SERVER_UNAVAILABLE_TITLE;
    	
    	if(description == 0) {
    		switch(error.getCode()) {
				case WSError.ERROR_AuthenticationFailed:
				case WSError.ERROR_InvalidSession:
					title = R.string.ERROR_SESSION_TITLE;
					description = R.string.ERROR_SESSION;
					break;
				case WSError.ERROR_InvalidAPIKey:
					title = R.string.ERROR_UPGRADE_TITLE;
					description = R.string.ERROR_UPGRADE;
					break;
				case WSError.ERROR_SubscribersOnly:
					title = R.string.ERROR_SUBSCRIPTION_TITLE;
					description = R.string.ERROR_SUBSCRIPTION;
					break;
				default:
					description = R.string.ERROR_SERVER_UNAVAILABLE;
					break;
    		}
    	}
    	
    	presentError(ctx, getResources().getString(title), getResources().getString(description));
    }
    
    public void presentError(Context ctx, String title, String description) {
		AlertDialog.Builder d = new AlertDialog.Builder(ctx);
		d.setTitle(title);
		d.setMessage(description);
		d.setIcon(android.R.drawable.ic_dialog_alert);
		d.setNeutralButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton)
					{
					}
				});
		d.show();
    }
}
