/***************************************************************************
 *   Copyright 2005-2009 Last.fm Ltd.                                      *
 *   Portions contributed by Casey Link, Lukasz Wisniewski,                *
 *   Mike Jennings, and Michael Novak Jr.                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
 ***************************************************************************/
package fm.last.api;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import fm.last.util.UrlUtil;

/**
 * This is super basic, we know, we'll improve as necessary Also it is
 * syncronous, which can be painful for sure, but generally, will not hang the
 * GUI So it'll do until 1.0.1... I'm sorry it is named service but isn't an
 * android service.
 * 
 * @author <max@last.fm>
 */
public class AudioscrobblerService extends Object {
	/** would be more useful at fm.last package level */
	private static class Log {
		final private static String TAG = "AudioscrobblerService";

		public static void d(String s) {
			android.util.Log.d(TAG, s);
		}

		public static void i(String s) {
			android.util.Log.i(TAG, s);
		}

		public static void e(String s) {
			android.util.Log.e(TAG, s);
		}

		public static void e(Throwable e) {
			android.util.Log.e(TAG, e.toString());
		}
	}

	// used by handshake
	private String mUsername;
	private String mSessionKey;
	private String mSharedSecret;
	private String mApiKey;
	private String mClientVersion;

	// responses from handshake
	public String sessionId;
	public URL npUrl;
	public URL subsUrl;
	public String player = "";

	public AudioscrobblerService(Session session, String apiKey, String sharedSecret, String clientVersion) {
		mUsername = session.getName();
		mSessionKey = session.getKey();
		mSharedSecret = sharedSecret;
		mClientVersion = clientVersion;
		mApiKey = apiKey;
	}

	private static String timestamp() {
		return new Long(System.currentTimeMillis() / 1000).toString();
	}

	public synchronized void handshake() throws IOException {
		sessionId = null;

		String timestamp = timestamp();

		Map<String, String> params = new HashMap<String, String>();
		params.put("hs", "true");
		params.put("p", "1.2.1");
		params.put("c", "lnd");
		params.put("v", mClientVersion);
		params.put("u", mUsername);
		params.put("t", timestamp);
		params.put("a", MD5.getInstance().hash(mSharedSecret + timestamp));
		params.put("api_key", mApiKey);
		params.put("sk", mSessionKey);

		String response = UrlUtil.doGet("http://post.audioscrobbler.com/", params);
		Log.d("handshake response: " + response);

		String lines[] = response.split("\n");
		if (lines.length < 4)
			throw new IOException();

		sessionId = lines[1];
		npUrl = new URL(lines[2]);
		subsUrl = new URL(lines[3]);
	}

	public synchronized String nowPlaying(RadioTrack t) throws IOException {
		if (sessionId == null)
			handshake();

		if(player.length() > 0)
			return "OK";

		Map<String, String> params = new HashMap<String, String>();
		params.put("s", sessionId);
		params.put("a", t.getCreator());
		params.put("t", t.getTitle());
		params.put("b", t.getAlbum());
		
		if(t.getDuration() > 0)
			params.put("l", new Integer(t.getDuration() / 1000).toString());
		else
			params.put("l", "");

		String response = UrlUtil.doPost(npUrl, UrlUtil.buildQuery(params));
		Log.i("np query: " + UrlUtil.buildQuery(params));
		Log.i("np response: " + response);

		return response.trim();
	}

	public synchronized String submit(RadioTrack t, long timestamp) throws IOException {
		return submit(t, timestamp, "");
	}

	/**
	 * valid ratings are, L for love, B for banned and S for skip, you can only
	 * specify one!
	 */
	public synchronized String submit(RadioTrack t, long timestamp, String ratingCharacter) throws IOException {
		if (sessionId == null)
			handshake();

		Map<String, String> params = new HashMap<String, String>();
		params.put("s", sessionId);
		params.put("a[0]", t.getCreator());
		params.put("t[0]", t.getTitle());
		params.put("b[0]", t.getAlbum());
		if(t.getDuration() > 0)
			params.put("l[0]", new Integer(t.getDuration() / 1000).toString());
		else
			params.put("l[0]", "");
		params.put("i[0]", new Long(timestamp).toString());
		if (t.getTrackAuth().length() > 0)
			params.put("o[0]", "L" + t.getTrackAuth());
		else
			params.put("o[0]", "P" + player);
		params.put("r[0]", ratingCharacter);
		params.put("m[0]", "");
		params.put("n[0]", "");

		String response = UrlUtil.doPost(subsUrl, UrlUtil.buildQuery(params));
		Log.i("submit query: " + UrlUtil.buildQuery(params));

		Log.i("submit response: " + response);
		return response.trim();
	}
}
