/**
 * 
 */
package fm.last.android.sync;

import fm.last.android.LastFm;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * @author sam
 *
 */
public class AccountAuthenticatorService extends Service {
	private static final String TAG = "AccountAuthenticatorService";
	private static AccountAuthenticatorImpl sAccountAuthenticator = null;

	public AccountAuthenticatorService() {
		super();
	}

	private static class AccountAuthenticatorImpl extends AbstractAccountAuthenticator {
		private Context mContext;

		public AccountAuthenticatorImpl(Context context) {
			super(context);
			mContext = context;
		}

		/* (non-Javadoc)
		 * @see android.accounts.AbstractAccountAuthenticator#addAccount(android.accounts.AccountAuthenticatorResponse, java.lang.String, java.lang.String, java.lang.String[], android.os.Bundle)
		 */
		@Override
		public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)
				throws NetworkErrorException {
			Bundle reply = new Bundle();
			
			Intent i = new Intent(mContext, LastFm.class);
			i.setAction("fm.last.android.sync.LOGIN");
			i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
			reply.putParcelable(AccountManager.KEY_INTENT, i);
			
			return reply;
		}

		/* (non-Javadoc)
		 * @see android.accounts.AbstractAccountAuthenticator#confirmCredentials(android.accounts.AccountAuthenticatorResponse, android.accounts.Account, android.os.Bundle)
		 */
		@Override
		public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
			// TODO Auto-generated method stub
			Log.i(TAG, "confirmCredentials");
			return null;
		}

		/* (non-Javadoc)
		 * @see android.accounts.AbstractAccountAuthenticator#editProperties(android.accounts.AccountAuthenticatorResponse, java.lang.String)
		 */
		@Override
		public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
			// TODO Auto-generated method stub
			Log.i(TAG, "editProperties");
			return null;
		}

		/* (non-Javadoc)
		 * @see android.accounts.AbstractAccountAuthenticator#getAuthToken(android.accounts.AccountAuthenticatorResponse, android.accounts.Account, java.lang.String, android.os.Bundle)
		 */
		@Override
		public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
			// TODO Auto-generated method stub
			Log.i(TAG, "getAuthToken");
			return null;
		}

		/* (non-Javadoc)
		 * @see android.accounts.AbstractAccountAuthenticator#getAuthTokenLabel(java.lang.String)
		 */
		@Override
		public String getAuthTokenLabel(String authTokenType) {
			// TODO Auto-generated method stub
			Log.i(TAG, "getAuthTokenLabel");
			return null;
		}

		/* (non-Javadoc)
		 * @see android.accounts.AbstractAccountAuthenticator#hasFeatures(android.accounts.AccountAuthenticatorResponse, android.accounts.Account, java.lang.String[])
		 */
		@Override
		public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
			// TODO Auto-generated method stub
			Log.i(TAG, "hasFeatures: " + features);
			return null;
		}

		/* (non-Javadoc)
		 * @see android.accounts.AbstractAccountAuthenticator#updateCredentials(android.accounts.AccountAuthenticatorResponse, android.accounts.Account, java.lang.String, android.os.Bundle)
		 */
		@Override
		public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
			// TODO Auto-generated method stub
			Log.i(TAG, "updateCredentials");
			return null;
		}
	}

	public IBinder onBind(Intent intent) { 
		IBinder ret = null;
		if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)) 
			ret = getAuthenticator().getIBinder();
		return ret;
	}
	
	private AccountAuthenticatorImpl getAuthenticator() { 
		if (sAccountAuthenticator == null)
			sAccountAuthenticator = new AccountAuthenticatorImpl(this);
		return sAccountAuthenticator;
	}
}
