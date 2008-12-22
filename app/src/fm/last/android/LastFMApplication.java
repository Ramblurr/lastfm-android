package fm.last.android;

import java.util.WeakHashMap;

import fm.last.android.activity.Home;
import fm.last.android.activity.Player;
import fm.last.android.player.RadioPlayerService;
import fm.last.android.utils.UserTask;
import fm.last.api.Session;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
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

    private class TuneRadioTask extends UserTask<String, Integer, Boolean> {
        public Boolean doInBackground(String... urls) {
            boolean success = false;
    		try
    		{
    			Session session = ( Session ) LastFMApplication.getInstance().map.get( "lastfm_session" );
    			LastFMApplication.getInstance().player.setSession( session );
    			LastFMApplication.getInstance().player.tune( urls[0], session );
    			LastFMApplication.getInstance().player.startRadio();
    			appendRecentStation( urls[0], LastFMApplication.getInstance().player.getStationName() );
    			success = true;
    		}
    		catch ( Exception e )
    		{
    			Log.d( "LastFMPlayer", "couldn't start playback: " + e );
    			success = false;
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
            if (result) {
    			Intent intent = new Intent( LastFMApplication.getInstance(), Player.class );
    			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			startActivity( intent );
            }
            mProgress.dismiss();
        }
    }
}
