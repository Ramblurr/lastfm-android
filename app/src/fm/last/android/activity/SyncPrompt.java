/**
 * 
 */
package fm.last.android.activity;

import fm.last.android.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author sam
 *
 */
public class SyncPrompt extends Activity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sync_prompt);
		
		Button yes = (Button)findViewById(R.id.yes);
		yes.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				AccountManager am = AccountManager.get(SyncPrompt.this);
				Account[] accounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));
				ContentResolver.setIsSyncable(accounts[0], ContactsContract.AUTHORITY, 1);
	            ContentResolver.setSyncAutomatically(accounts[0], ContactsContract.AUTHORITY, true);
	            finish();
			}
		});

		Button no = (Button)findViewById(R.id.no);
		no.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
			
		});
	}
}
