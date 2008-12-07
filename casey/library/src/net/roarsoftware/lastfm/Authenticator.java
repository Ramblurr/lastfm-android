package net.roarsoftware.lastfm;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static net.roarsoftware.util.StringUtilities.map;
import static net.roarsoftware.util.StringUtilities.md5;

/**
 * Provides bindings for the authentication methods of the last.fm API.
 * See <a href="http://www.last.fm/api/authentication">http://www.last.fm/api/authentication</a> for
 * authentication methods.
 *
 * @author Janni Kovacs
 * @see Session
 */
public class Authenticator {

	private Authenticator() {
	}

	/**
	 * Create a web service session for a user. Used for authenticating a user when the password can be inputted by the user.
	 *
	 * @param username last.fm username
	 * @param password last.fm password
	 * @param apiKey The API key
	 * @param secret Your last.fm API secret
	 * @return a Session instance
	 * @see Session
	 */
	public static Session getMobileSession(String username, String password, String apiKey, String secret) {
		String passwordHash = md5(password);
		String authToken = md5(username + passwordHash);
		Map<String, String> params = map("api_key", apiKey, "username", username, "authToken", authToken);
		String sig = createSignature("auth.getMobileSession", params, secret);
		Result result = Caller.getInstance()
				.call("auth.getMobileSession", apiKey, "username", username, "authToken", authToken, "api_sig", sig);
		return Session.sessionFromResult(result, apiKey, secret, passwordHash);
	}

	/**
	 * Fetch an unathorized request token for an API account.
	 *
	 * @param apiKey A last.fm API key.
	 * @return a token
	 */
	public static String getToken(String apiKey) {
		Result result = Caller.getInstance().call("auth.getToken", apiKey);
		return result.getContentElement().getText();
	}

	/**
	 * Fetch a session key for a user.
	 *
	 * @param token A token returned by {@link #getToken(String)}
	 * @param apiKey A last.fm API key
	 * @param secret Your last.fm API secret
	 * @return a Session instance
	 * @see Session
	 */
	public static Session getSession(String token, String apiKey, String secret) {
		String m = "auth.getSession";
		Map<String, String> params = new HashMap<String, String>();
		params.put("api_key", apiKey);
		params.put("token", token);
		params.put("api_sig", createSignature(m, params, secret));
		Result result = Caller.getInstance().call(m, apiKey, params);
		return Session.sessionFromResult(result, apiKey, secret);
	}

	static String createSignature(String method, Map<String, String> params, String secret) {
		params = new TreeMap<String, String>(params);
		params.put("method", method);
		StringBuilder b = new StringBuilder(100);
		for (Entry<String, String> entry : params.entrySet()) {
			b.append(entry.getKey());
			b.append(entry.getValue());
		}
		b.append(secret);
		return md5(b.toString());
	}
}
