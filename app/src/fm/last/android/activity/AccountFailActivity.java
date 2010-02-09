/**
 * 
 */
package fm.last.android.activity;

import android.accounts.AccountAuthenticatorActivity;
import android.os.Bundle;
import android.widget.Toast;

/**
 * @author sam
 *
 */
public class AccountFailActivity extends AccountAuthenticatorActivity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Toast.makeText(this, "Only one Last.fm account is supported.", Toast.LENGTH_LONG).show();
		finish();
	}
}
