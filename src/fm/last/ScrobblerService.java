package fm.last;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import fm.last.Track.Rating;

class Utils {
	static long now() {
		// TODO check this is UTC
		return System.currentTimeMillis() / 1000;
	}
}

class Track {
	enum Source {
		Player, LastFm
	}

	enum Rating {
		Unrated, Scrobbled, Skipped, Loved, Banned
	}

	// in order of submission parameters
	String artist;
	String title;
	long timestamp;
	Source source;
	Rating rating;
	int duration;
	String album;
	int trackNumber;
	String mbid;

	String auth; // Last.fm Radio tracks come with auth codes

	Track() {
		source = Source.Player;
		rating = Rating.Unrated;
		mbid = "";
	}

	// TODO stupid function, bearing in mind Unrated possibility
	public boolean requiresScrobble() {
		return rating != Rating.Unrated;
	}

	public boolean isValid() {
		return !(artist == null && title == null);
	}

	public void setPlaybackEnded() {
		if (!isValid())
			return;

		// TODO better system where skipped/scrobbled is determined by list of
		// stop/start times
		// TODO as this overwrites the love ban info
		if (timestamp + duration / 2 <= Utils.now())
			rating = Rating.Scrobbled;
		else if (source == Source.LastFm)
			rating = Rating.Skipped;
	}
}

class SanitisedTrack extends Track {
	private Track t;

	SanitisedTrack(Track tt) {
		t = tt;
	}

	private String formUrlEncoded(String in) {
		try {
			return URLEncoder.encode(in, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		} catch (NullPointerException e) {
			return "";
		}
	}

	String artist() {
		return formUrlEncoded(t.artist);
	}

	String title() {
		return formUrlEncoded(t.title);
	}

	String timestamp() {
		return new Long(t.timestamp).toString();
	}

	String source() {
		switch (t.source) {
		case LastFm:
			return "L" + t.auth;
		default:
			return "P";
		}
	}

	String rating() {
		// precedence order
		switch (t.rating) {
		case Banned:
			return "B";
		case Loved:
			return "L";
			// case Scrobbled: return "";
		case Skipped:
			return "S";
		}

		// prevent compiler error -- lame
		return "";
	}

	String duration() {
		return new Integer(t.duration).toString();
	}

	String album() {
		return formUrlEncoded(t.album).toString();
	}

	String trackNumber() {
		return t.trackNumber == 0 ? "" : new Integer(t.trackNumber).toString();
	}

	// TODO sanitise
	String mbid() {
		return t.mbid;
	}
}

public class ScrobblerService extends Service {
	private static final String TAG = "Last.fm";

	private String httpConnectionOutput(HttpURLConnection http)
			throws IOException {
		InputStream in = http.getInputStream();
		String out = "";
		while (in.available() > 0) {
			out += (char) in.read(); // FIXME inefficient!
		}
		in.close();
		return out;
	}

	private void notify(String text) {
		// NOTE this is wrong ui wise, but cool for now
		Notification n = new Notification();
		n.icon = R.drawable.status_bar_icon;
		n.tickerText = text;
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(
				0, n);
	}

	private String md5(String in) throws NoSuchAlgorithmException {
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(in.getBytes(), 0, in.length());
		BigInteger bi = new BigInteger(1, m.digest());
		return bi.toString(16);
	}

	private String m_session_id;
	private String m_now_playing_url;
	private String m_submission_url;

	public String sessionId() {
		return m_session_id;
	}

	public URL nowPlayingUrl() throws MalformedURLException {
		return new URL(m_now_playing_url);
	}

	public URL submissionUrl() throws MalformedURLException {
		return new URL(m_submission_url);
	}

	private void handshake(String username, String password)
			throws IOException, NoSuchAlgorithmException {
		notify("Handshaking");

		// TODO percent encode username
		// TODO toLower the md5 of the password
		String timestamp = new Long(Utils.now()).toString();
		String authToken = md5(password + timestamp);
		String query = "?hs=true" + "&p=1.2" + "&c=ass" + "&v=1.5" + "&u="
				+ URLEncoder.encode(username, "UTF-8") + "&t=" + timestamp
				+ "&a=" + authToken;

		URL url = new URL("http://post.audioscrobbler.com/" + query);

		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setRequestMethod("GET");
		String out = httpConnectionOutput(http);

		Log.i(TAG, out);

		String[] tokens = out.split("\n");
		if (tokens[0].equals("OK")) {
			m_session_id = tokens[1];
			m_now_playing_url = tokens[2];
			m_submission_url = tokens[3];
		}
		//TODO else

		notify("Handshaken");
	}

	//NOTE dunno what this is
	public class ScrobblerBinder extends Binder {
		ScrobblerService getService() {
			return ScrobblerService.this;
		}
	}

	@Override
	public void onCreate() {
		Log.e(TAG, "Oh Hai!");

		notify("Initialising AudioScrobbler");

		try {
			handshake("2girls1cup", "77e3c764678e809f1e72727c1f26e3f3");
		} catch (NoSuchAlgorithmException e) {
			//TODO error handling
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			//TODO error handling
			Log.e(TAG, e.toString());
		}
	}

	private Track m_track = new Track();

	enum Command {
		Invalid, Start, Love, Pause, Resume, Stop
	}

	private Command commandFromString(String s) {
		s = s.toLowerCase();

		if (s.equals("start"))
			return Command.Start;
		if (s.equals("love"))
			return Command.Love;
		if (s.equals("pause"))
			return Command.Pause;
		if (s.equals("resume"))
			return Command.Resume;
		if (s.equals("stop"))
			return Command.Stop;

		return Command.Invalid;
	}

	@Override
	public void onStart(int startId, Bundle args) {
		Log.i(TAG, args.toString());

		Command command = commandFromString(args.getString("command"));

		switch (command) {
		case Start:
		case Stop:
			m_track.setPlaybackEnded();

			if (m_track.requiresScrobble())
				scrobble(new SanitisedTrack(m_track));
		}

		switch (command) {
		case Start: {
			Track t = new Track();
			t.artist = args.getString("artist");
			t.title = args.getString("title");
			t.duration = args.getInt("duration");
			t.auth = args.getString("authorisation-code");
			t.mbid = args.getString("mbid");
			t.timestamp = Utils.now();
			t.trackNumber = args.getInt("track-number");
			t.album = args.getString("album");

			t.source = args.getString("source") == "Last.fm" ? Track.Source.LastFm
					: Track.Source.Player;

			m_track = t;
		}
			break;

		case Love:
			//TODO verify we're loving the right track
			m_track.rating = Rating.Loved;
			break;

		case Pause:

		case Resume:

		case Stop:
			m_track = new Track();

		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		Toast.makeText(this, "Scrobbler stopped", Toast.LENGTH_SHORT).show();
	}

	private final IBinder m_binder = new ScrobblerBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return m_binder;
	}

	private String post(URL url, String parameters) throws IOException {
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setRequestMethod("POST");
		http.setDoOutput(true);
		http.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");

		byte[] utf8 = parameters.getBytes("UTF8");

		DataOutputStream o = new DataOutputStream(http.getOutputStream());
		o.write(utf8, 0, utf8.length);
		o.flush();
		o.close();

		return httpConnectionOutput(http);
	}

	void nowPlaying(SanitisedTrack t) {
		try {
			String data = "s=" + sessionId() + "&a=" + t.artist() + "&t="
					+ t.title() + "&b=" + t.album() + "&l=" + t.duration()
					+ "&n=" + t.trackNumber() + "&m=" + t.mbid();

			String out = post(nowPlayingUrl(), data);

			Log.i(TAG, "nowPlaying() result: " + out);
		} catch (IOException e) {
			Log.e(TAG, "nowPlaying() error: " + e.toString());
		}
	}

	void scrobble(SanitisedTrack t) {
		try {
			String data = "s=" + sessionId();
			String N = "0";

			data += "&a[" + N + "]=" + t.artist() + "&t[" + N + "]="
					+ t.title() + "&i[" + N + "]=" + t.timestamp() + "&o[" + N
					+ "]=" + t.source() + "&r[" + N + "]=" + t.rating() + "&l["
					+ N + "]=" + t.duration() + "&b[" + N + "]=" + t.album()
					+ "&n[" + N + "]=" + t.trackNumber() + "&m[" + N + "]="
					+ t.mbid();

			Log.d(TAG, data);

			String out = post(submissionUrl(), data);
			Log.i(TAG, "scrobble() result: " + out);
		} catch (IOException e) {
			Log.e(TAG, "scrobble() error: " + e.toString());
		}
	}
}