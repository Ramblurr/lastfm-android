package fm.last.Android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class ScrobblerActivity extends Activity
{
	private ServiceConnection m_connection = new ServiceConnection()
	{
		public void onServiceConnected( ComponentName className, IBinder service )
		{}
		
		public void onServiceDisconnected(ComponentName className) 
		{}
	};	
	
	@Override
	protected void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		
		bindService( new Intent( this, 
					 ScrobblerService.class ),
					 m_connection, 
					 Context.BIND_AUTO_CREATE );
	}
}
