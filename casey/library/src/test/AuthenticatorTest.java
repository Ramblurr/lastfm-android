package test;


import android.util.Log;
import net.roarsoftware.lastfm.Authenticator;
import net.roarsoftware.lastfm.Credentials;
import net.roarsoftware.lastfm.Session;


public class AuthenticatorTest {
	String apiKey = Credentials.API_KEY;
	String secret = Credentials.SECRET;
	String user = Credentials.USER;
	String pass = Credentials.PASS;

	public void testGetMobileSession() {
		Session session = Authenticator.getMobileSession(user, pass, apiKey, secret);
		if( session == null ) {
			Log.d("Test", "SESSION Null Aborting Tests");
			return;
		}
		System.out.println("Username: '" + session.getUsername() + "'");
		System.out.println("Username: '" + session.getUsername() + "'");
		System.out.println("ApiKey: '" + session.getApiKey() + "'");
		System.out.println("SKey: '" + session.getKey() + "'");
		System.out.println("Secret: '" + session.getSecret() + "'");
		System.out.println("Subscriber: '" + session.isSubscriber() + "'");
	}
	
	

}
