package net.roarsoftware.lastfm;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Contains Session data relevant for making API calls which require
 * authentication. A <code>Session</code> instance is passed to all methods
 * requiring previous authentication.
 * 
 * @author Janni Kovacs
 * @see net.roarsoftware.lastfm.Authenticator
 */
public class Session implements Parcelable {

	private String apiKey;
	private String secret;
	private String username;
	private String key;
	private String passwordHash;
	private boolean subscriber;

	public String getSecret() {
		return secret;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getKey() {
		return key;
	}
	
	public String getPasswordHash()
	{
		return passwordHash;
	}

	public boolean isSubscriber() {
		return subscriber;
	}

	public String getUsername() {
		return username;
	}

	/**
	 * Convenience function for storing the user's hash in the session.
	 * Useful if this session will be used to play legacy radio
	 * @param result
	 * @param apiKey
	 * @param secret
	 * @param passwordHash
	 * @return a session, or null if parsing failed.
	 */
	static Session sessionFromResult(Result result, String apiKey, String secret, String passwordHash)
	{
		Session s = sessionFromResult(result, apiKey, secret);
		if( s != null )
			s.passwordHash = passwordHash;
		return s;
	}

	private Session(){}
	/**
	 * Create a session object from an XML result
	 * @param result
	 * @param apiKey
	 * @param secret
	 * @return an authenticated session, or null if parsing failed.
	 */
	static Session sessionFromResult(Result result, String apiKey, String secret) {
		try {
			if (result.getParser() == null)
				return null;

			result.getParser().nextTag();
			String str = result.getParser().getName();
			if (!result.getParser().getName().equals("session"))
				return null;
			Session s = new Session();
			int event = result.getParser().nextTag();
			while (event != XmlPullParser.END_DOCUMENT && !result.getParser().getName().equals("session")) {
				switch (event) {
				case XmlPullParser.START_TAG:
					String name = result.getParser().getName();
					if (name.equals("name")) {
						s.username = result.getParser().nextText();
					} else if (name.equals("key")) {
						s.key = result.getParser().nextText();
					} else if (name.equals("subscriber")) {
						s.subscriber = result.getParser().nextText().equals("1");
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				event = result.getParser().nextTag();
			}
			s.apiKey = apiKey;
			s.secret = secret;
			return s;
		} catch (Exception e) {
		}
		return null;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(apiKey);
		dest.writeString(key);
		dest.writeString(passwordHash);
		dest.writeString(secret);
		dest.writeString(username);
		dest.writeInt(subscriber ? 1 : 0);
	}
	
	public static final Parcelable.Creator<Session> CREATOR = new Parcelable.Creator<Session>() {
		public Session createFromParcel(Parcel in) {
			return new Session(in);
		}

		public Session[] newArray(int size) {
			return new Session[size];
		}	
	};
	
	private Session(Parcel in) {
		apiKey = in.readString();
		key = in.readString();
		passwordHash = in.readString();
		secret = in.readString();
		username = in.readString();
		subscriber = in.readInt() == 1;
	}

	public int describeContents() {
		// I have no idea what this is for
		return 0;
	}
}
