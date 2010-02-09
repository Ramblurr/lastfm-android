package fm.last.android.authtest;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AuthTestActivity extends Activity {
	private TextView mUsername;
	private TextView mSessionKey;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mUsername = (TextView)findViewById(R.id.username);
        mSessionKey = (TextView)findViewById(R.id.session_key);
        
        Button getSessionBtn = (Button)findViewById(R.id.getsession);
        getSessionBtn.setOnClickListener(mOnGetSessionKey);
        
        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccountsByType("fm.last.android.account");
        if(accounts.length > 0) {
        	mUsername.setText("Last.fm account: " + accounts[0].name);
        } else {
        	mUsername.setText("No Last.fm account configured!");
        }
    }
    
    private OnClickListener mOnGetSessionKey = new OnClickListener() {
		public void onClick(View v) {
	        AccountManager am = AccountManager.get(AuthTestActivity.this);
	        Account[] accounts = am.getAccountsByType("fm.last.android.account");
	        if(accounts.length > 0) {
	        	Bundle options = new Bundle();
	        	//This is a test key. Register your own at http://www.last.fm/api
	        	options.putString("api_key", "8a733e4c0a9f0df84cc745446b6b2f10");
	        	options.putString("api_secret", "750d8398452da9ac008ade1015404f96");
	        	am.getAuthToken(accounts[0], "", options, AuthTestActivity.this, new AccountManagerCallback<Bundle>() {
					public void run(AccountManagerFuture<Bundle> arg0) {
						try {
							String key = arg0.getResult().getString(AccountManager.KEY_AUTHTOKEN);
							mSessionKey.setText("Session key: " + key);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
	        	}, null);
	        }
		}
    };
}