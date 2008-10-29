package fm.last;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class ScrobblerTestActivity extends Activity {
	private ServiceConnection m_connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
		}

		public void onServiceDisconnected(ComponentName className) {
		}
	};

	@Override
	protected void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
        
		Intent intent = new Intent( ScrobblerTestActivity.this, ScrobblerService.class);

        intent.putExtra( "event", "track-started" );
        intent.putExtra( "artist", "Foo" );
        intent.putExtra( "title", "Bar" );
        intent.putExtra( "duration", 300 );
		

        startService( intent );
	}
}