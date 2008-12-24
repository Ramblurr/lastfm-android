package fm.last.android;

import java.util.WeakHashMap;

import fm.last.android.activity.Home;
import fm.last.android.activity.Player;
import fm.last.android.player.RadioPlayerService;
import fm.last.android.utils.UserTask;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.api.WSError;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class LastFMApplication extends Application
{

    public WeakHashMap map;
	public fm.last.android.player.IRadioPlayer player = null;

    private static LastFMApplication instance;
    private ProgressDialog mProgress;

    public static LastFMApplication getInstance()
    {

        return instance;
    }

    public void onCreate()
    {

        super.onCreate();
        instance = this;

        // construct an 'application global' object
        this.map = new WeakHashMap();

        // start our media player service
		Intent mpIntent = new Intent(
				this,
				fm.last.android.player.RadioPlayerService.class );
		startService( mpIntent );
		boolean b = bindService( mpIntent, mConnection, 0 );
		if ( !b )
		{
			// something went wrong
			// mHandler.sendEmptyMessage(QUIT);
			System.out.println( "Binding to service failed " + mConnection );
		}
    }
    
    private BroadcastReceiver mStatusListener = new BroadcastReceiver()
    {

        @Override
        public void onReceive( Context context, Intent intent )
        {

            String action = intent.getAction();
            if ( action.equals( RadioPlayerService.STATION_CHANGED ) )
            {
       			Intent i = new Intent( LastFMApplication.getInstance(), Player.class );
       			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       			startActivity( i );
            }
        }
    };

	private ServiceConnection mConnection = new ServiceConnection()
	{

		public void onServiceConnected( ComponentName className, IBinder service )
		{
			player = fm.last.android.player.IRadioPlayer.Stub.asInterface( service );
		}

		public void onServiceDisconnected( ComponentName className )
		{
			player = null;
		}
	};
    
	public void playRadioStation(Context ctx, String url)
	{

		if ( LastFMApplication.getInstance().player == null )
			return;

		mProgress = ProgressDialog.show(ctx, "", "Tuning Radio Station", true, false);
		new TuneRadioTask().execute(new String[] {url});
	}
	
	private void appendRecentStation( String url, String name )
	{

		SQLiteDatabase db = null;
		try
		{
			db = this.openOrCreateDatabase( LastFm.DB_NAME, MODE_PRIVATE, null );
			db.execSQL( "CREATE TABLE IF NOT EXISTS "
					+ LastFm.DB_TABLE_RECENTSTATIONS
					+ " (Url VARCHAR UNIQUE NOT NULL PRIMARY KEY, Name VARCHAR NOT NULL, Timestamp INTEGER NOT NULL);" );
			db.execSQL( "DELETE FROM " + LastFm.DB_TABLE_RECENTSTATIONS
					+ " WHERE Url = '" + url + "'" );
			db.execSQL( "INSERT INTO " + LastFm.DB_TABLE_RECENTSTATIONS
					+ "(Url, Name, Timestamp) " + "VALUES ('" + url + "', '" + name
					+ "', " + System.currentTimeMillis() + ")" );
			db.close();
		}
		catch ( Exception e )
		{
			System.out.println( e.getMessage() );
		}
	}

	
    public void onTerminate()
    {

        // clean up application global
        this.map.clear();
        this.map = null;

        instance = null;
        super.onTerminate();
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

    private class TuneRadioTask extends UserTask<String, Integer, Boolean> {
    	public void onPreExecute() {
            IntentFilter f = new IntentFilter();
            f.addAction( RadioPlayerService.STATION_CHANGED );
            registerReceiver( mStatusListener, f );
    	}
    	
        public Boolean doInBackground(String... urls) {
            boolean success = false;
    		try
    		{
    			Session session = ( Session ) LastFMApplication.getInstance().map.get( "lastfm_session" );
    			LastFMApplication.getInstance().player.setSession( session );
    			if(LastFMApplication.getInstance().player.tune( urls[0], session )) {
    				LastFMApplication.getInstance().player.startRadio();
        			appendRecentStation( LastFMApplication.getInstance().player.getStationUrl(), LastFMApplication.getInstance().player.getStationName() );
        			success = true;
    			} else {
    				success = false;
    			}
    		}
    		catch ( Exception e )
    		{
    			Log.d( "LastFMPlayer", "couldn't start playback: " + e );
				e.printStackTrace();
    			success = false;
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	unregisterReceiver( mStatusListener );
        	Context ctx = mProgress.getContext();
            mProgress.dismiss();
            if (!result) {
				try {
					WSError error = LastFMApplication.getInstance().player.getError();
					if(error != null)
						LastFMApplication.getInstance().presentError(ctx, error);
					else
						LastFMApplication.getInstance().presentError(ctx, getResources().getString(R.string.ERROR_SERVER_UNAVAILABLE_TITLE),
								getResources().getString(R.string.ERROR_SERVER_UNAVAILABLE));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    }
}
