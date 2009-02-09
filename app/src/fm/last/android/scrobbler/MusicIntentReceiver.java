/**
 * 
 */
package fm.last.android.scrobbler;

import fm.last.android.LastFMApplication;
import fm.last.api.Session;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author sam
 * 
 */
public class MusicIntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Session s = LastFMApplication.getInstance().map.get("lastfm_session");
        if ( s != null && s.getKey().length() > 0 ) {
	        final Intent out = new Intent(context, ScrobblerService.class);
	        out.setAction(intent.getAction());
	        out.putExtras(intent);
	        context.startService(out);
        }
	}
}
