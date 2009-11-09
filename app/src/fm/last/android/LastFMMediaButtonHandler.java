package fm.last.android;

import fm.last.android.player.IRadioPlayer;
import fm.last.android.player.RadioPlayerService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;

public class LastFMMediaButtonHandler extends BroadcastReceiver{
	private final static String TAG = "LastFMMediaButtonHandler"; 
	@Override
	public void onReceive(Context context, Intent intent) {
		IBinder service = peekService(context, new Intent(context,RadioPlayerService.class));
		if (service == null){
			Log.i(TAG, "LastFM-Player not active, don't handling media keys.");
			return;
		}
		
        KeyEvent event = (KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        
		if (event == null) {
			return;
		}

		int keycode = event.getKeyCode();
		try {
			IRadioPlayer player = fm.last.android.player.IRadioPlayer.Stub.asInterface( service );
			
			// handling only down events if the player is playing
			if (event.getAction() == KeyEvent.ACTION_DOWN &&
					player.isPlaying()){
				
				switch (keycode){
				case KeyEvent.KEYCODE_MEDIA_NEXT:
					Log.i(TAG,"Next-Button => Skipping '"+player.getTrackName()+"'");
					player.skip();
					abortBroadcast();
					return;
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
					Log.i(TAG,"Pause-Button => Stopping '"+player.getTrackName()+"'");
					player.stop();
					abortBroadcast();
					return;				
				}
			}
		} catch (RemoteException e) {
			Log.e(TAG,"Couldn't reach player service.",e);
		}
	}
}
