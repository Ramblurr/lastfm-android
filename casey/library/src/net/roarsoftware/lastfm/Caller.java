package net.roarsoftware.lastfm;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

//import org.apache.http.client.HttpClient;

import static net.roarsoftware.util.StringUtilities.encode;
import static net.roarsoftware.util.StringUtilities.map;
import static net.roarsoftware.util.StringUtilities.md5;

/**
 * The <code>Caller</code> class handles the low-level communication between the
 * client and last.fm.<br/>
 * Direct usage of this class should be unnecessary since all method calls are
 * available via the methods in the <code>Artist</code>, <code>Album</code>,
 * <code>User</code>, etc. classes. If specialized calls which are not covered
 * by the Java API are necessary this class may be used directly.<br/>
 * Supports the setting of a custom {@link Proxy} and a custom
 * <code>User-Agent</code> HTTP header.
 * 
 * @author Janni Kovacs
 */
public class Caller {

	private static final String PARAM_API_KEY = "api_key";
	private static final String PARAM_METHOD = "method";

	private static final String DEFAULT_API_ROOT = "http://ws.audioscrobbler.com/2.0/";
	private static final Caller instance = new Caller();

	private String apiRootUrl = DEFAULT_API_ROOT;

	private Proxy proxy;
	private String userAgent = "LastfmAndroid";

	private boolean debugMode = true;

	private Caller() {
	}

	/**
	 * Returns the single instance of the <code>Caller</code> class.
	 * 
	 * @return a <code>Caller</code>
	 */
	public static Caller getInstance() {
		return instance;
	}

	/**
	 * Set api root url.
	 * 
	 * @param apiRootUrl
	 *            new api root url
	 */
	public void setApiRootUrl(String apiRootUrl) {
		this.apiRootUrl = apiRootUrl;
	}

	/**
	 * Sets a {@link Proxy} instance this Caller will use for all upcoming HTTP
	 * requests. May be <code>null</code>.
	 * 
	 * @param proxy
	 *            A <code>Proxy</code> or <code>null</code>.
	 */
	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * Sets a User Agent this Caller will use for all upcoming HTTP requests.
	 * For testing purposes use "tst". If you distribute your application use an
	 * identifiable User-Agent.
	 * 
	 * @param userAgent
	 *            a User-Agent string
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * Sets the <code>debugMode</code> property. If <code>debugMode</code> is
	 * <code>true</code> all call() methods will print debug information and
	 * error messages on failure to stdout and stderr respectively.<br/>
	 * Default is <code>false</code>. Set this to <code>true</code> while in
	 * development and for troubleshooting.
	 * 
	 * @param debugMode
	 *            <code>true</code> to enable debug mode
	 */
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public Result call(String method, String apiKey, String... params)
			throws CallException {
		return call(method, apiKey, map(params));
	}

	public Result call(String method, String apiKey, Map<String, String> params)
			throws CallException {
		return call(method, apiKey, params, null);
	}

	public Result call(String method, Session session, String... params) {
		return call(method, session.getApiKey(), map(params), session);
	}

	public Result call(String method, Session session,
			Map<String, String> params) {
		return call(method, session.getApiKey(), params, session);
	}

	/**
	 * Performs the web-service call. If the <code>session</code> parameter is
	 * <code>non-null</code> then an authenticated call is made. If it's
	 * <code>null</code> then an unauthenticated call is made.<br/>
	 * The <code>apiKey</code> parameter is always required, even when a valid
	 * session is passed to this method.
	 * 
	 * @param method
	 *            The method to call
	 * @param apiKey
	 *            A Last.fm API key
	 * @param params
	 *            Parameters
	 * @param session
	 *            A Session instance or <code>null</code>
	 * @return the result of the operation
	 * @throws XmlPullParserException
	 */
	private Result call(String method, String apiKey,
			Map<String, String> params, Session session) {
		
		// create new Map in case params is an immutable Map
		params = new HashMap<String, String>(params); 
		params.put(PARAM_API_KEY, apiKey);
		if (session != null) {
			params.put("sk", session.getKey());
			String sig = Authenticator.createSignature(method, params, session
					.getSecret());
			params.put("api_sig", sig);
		}
		try {
			HttpURLConnection urlConnection = openConnection(apiRootUrl);
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			OutputStream outputStream = urlConnection.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					outputStream));
			String post = buildParameterQueue(method, params);
			if (debugMode) {
				System.out.println("body: " + post);
			}
			writer.write(post);
			writer.close();
			int responseCode = urlConnection.getResponseCode();
			InputStream httpInput;
			if (responseCode == HttpURLConnection.HTTP_FORBIDDEN
					|| responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
				httpInput = urlConnection.getErrorStream();
			} else if (responseCode != HttpURLConnection.HTTP_OK) {
				return Result.createHttpErrorResult(responseCode, urlConnection
						.getResponseMessage());
			} else {
				httpInput = urlConnection.getInputStream();
			}
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			if (debugMode) {
				String all = "";
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(httpInput));
				String response = reader.readLine();
				
				while (null != response) {
					all = all.concat(response + "\n");
					response = reader.readLine();
				}
				if (debugMode) {
					System.out.println(all);
				}
				xpp.setInput(new StringReader(all));
			} else {
				xpp.setInput(httpInput, "utf-8");	
			}
			return Result.createOkResult(xpp);
		} catch (IOException e) {
			return Result.createRestErrorResult(Result.FAILURE, e.getMessage());
		} catch (XmlPullParserException e) {
			return Result.createRestErrorResult(Result.FAILURE, e.getMessage());
		}
	}

	/**
	 * Creates a new {@link HttpURLConnection}, sets the proxy, if available,
	 * and sets the User-Agent property.
	 * 
	 * @param url
	 *            URL to connect to
	 * @return a new connection.
	 * @throws IOException
	 *             if an I/O exception occurs.
	 */
	public HttpURLConnection openConnection(String url) throws IOException {
		if (isDebugMode())
			System.out.println("open: " + url);
		URL u = new URL(url);
		HttpURLConnection urlConnection;
		if (proxy != null)
			urlConnection = (HttpURLConnection) u.openConnection(proxy);
		else
			urlConnection = (HttpURLConnection) u.openConnection();
		urlConnection.setRequestProperty("User-Agent", userAgent);
		return urlConnection;
	}

	private String buildParameterQueue(String method,
			Map<String, String> params, String... strings) {
		StringBuilder builder = new StringBuilder(100);
		builder.append("method=");
		builder.append(method);
		builder.append('&');
		for (Iterator<Entry<String, String>> it = params.entrySet().iterator(); it
				.hasNext();) {
			Entry<String, String> entry = it.next();
			builder.append(entry.getKey());
			builder.append('=');
			builder.append(encode(entry.getValue()));
			if (it.hasNext() || strings.length > 0)
				builder.append('&');
		}
		int count = 0;
		for (String string : strings) {
			builder.append(count % 2 == 0 ? string : encode(string));
			count++;
			if (count != strings.length) {
				if (count % 2 == 0) {
					builder.append('&');
				} else {
					builder.append('=');
				}
			}
		}
		return builder.toString();
	}

	private String createSignature(Map<String, String> params, String secret) {
		Set<String> sorted = new TreeSet<String>(params.keySet());
		StringBuilder builder = new StringBuilder(50);
		for (String s : sorted) {
			builder.append(s);
			builder.append(encode(params.get(s)));
		}
		builder.append(secret);
		return md5(builder.toString());
	}

	public Proxy getProxy() {
		return proxy;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public boolean isDebugMode() {
		return debugMode;
	}
}
