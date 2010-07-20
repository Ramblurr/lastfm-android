/**
 * 
 */
package fm.last.android;

import fm.last.android.player.RadioPlayerService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * @author sam
 *
 */
public class LocaleReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(RadioPlayerService.radioAvailable(context)) {
			context.getPackageManager().setComponentEnabledSetting(new ComponentName("fm.last.android", "fm.last.android.activity.Player"), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
		} else {
			context.getPackageManager().setComponentEnabledSetting(new ComponentName("fm.last.android", "fm.last.android.activity.Player"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		}
	}

}
