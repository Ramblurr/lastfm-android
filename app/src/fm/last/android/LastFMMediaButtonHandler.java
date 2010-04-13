package fm.last.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import fm.last.android.player.IRadioPlayer;
import fm.last.android.player.RadioPlayerService;

public class LastFMMediaButtonHandler extends BroadcastReceiver {
	private final static String TAG = "LastFMMediaButtonHandler";

	@Override
	public void onReceive(Context context, Intent intent) {
		IBinder service = peekService(context, new Intent(context, RadioPlayerService.class));
		if (service == null || !PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).getBoolean("headset_controls", true)) {
			Log.i(TAG, "LastFM-Player not active, don't handling media keys.");
			return;
		}
		try {
			IRadioPlayer player = fm.last.android.player.IRadioPlayer.Stub.asInterface(service);

			if (player != null && player.isPlaying()) {
				if (intent.getAction().equals("com.smartmadsoft.openwatch.command.BUTTON_FF")) {
					player.skip();
				}
				if (intent.getAction().equals("com.smartmadsoft.openwatch.command.BUTTON_PLAYPAUSE")
						|| (intent.getAction().equals("android.media.AUDIO_BECOMING_NOISY") && PreferenceManager.getDefaultSharedPreferences(
								LastFMApplication.getInstance()).getBoolean("handle_noisy", true))) {
					player.stop();
				}

				if (intent.getAction().equals("android.intent.action.MEDIA_BUTTON")) {
					KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

					if (event == null) {
						return;
					}

					int keycode = event.getKeyCode();

					if (event.getAction() == KeyEvent.ACTION_DOWN) {

						switch (keycode) {
						case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
							// ignore previous button as accepting it would start the built-in player
							abortBroadcast();
							return;
						case KeyEvent.KEYCODE_MEDIA_NEXT:
							Log.i(TAG, "Next-Button => Skipping '" + player.getTrackName() + "'");
							player.skip();
							abortBroadcast();
							return;
						case KeyEvent.KEYCODE_HEADSETHOOK:
						case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
							Log.i(TAG, "Pause-Button => Stopping '" + player.getTrackName() + "'");
							player.stop();
							abortBroadcast();
							return;
						}
					}
				}
			}
		} catch (RemoteException e) {
			Log.e(TAG, "Couldn't reach player service.", e);
		}
	}
}
