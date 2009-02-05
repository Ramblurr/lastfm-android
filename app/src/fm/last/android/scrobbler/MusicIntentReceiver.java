/**
 * 
 */
package fm.last.android.scrobbler;

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
        final Intent out = new Intent(context, ScrobblerService.class);
        out.setAction(intent.getAction());
        out.putExtras(intent);
        context.startService(out);
	}
}
