package fm.last;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.net.Uri;
import android.widget.Toast;

import fm.last.Utils;

class Track
{
	enum State { Stopped, Playing, Paused }
	enum Source { Player, LastFm }
	enum Rating { Unrated, Scrobbled, Skipped, Loved, Banned }
	enum Transition { Started, Ended, Paused, Resumed }
	
	// in order of submission parameters
	String artist;
	String title;
	Source source;
	Rating rating;
	int duration;
	String album;
	int trackNumber;
	String mbid;

	private State state;
	
	//FIXME is this a reference too?
	State getState() { return state; }
	
	String auth; // Last.fm Radio tracks come with auth codes

	Track() {
		source = Source.Player;
		rating = Rating.Unrated;
		state = State.Stopped;
		mbid = "";
	}

	// TODO stupid function, bearing in mind Unrated possibility
	public boolean requiresScrobble() {
		return rating != Rating.Unrated;
	}

	public boolean isValid() {
		return !(artist == null && title == null);
	}

	// used to calculate duration, adjacent timestamps are regions of playback
	// ie 300 -> 400 is 100 seconds of playback, if another timestamp occurs, it 
	// is assumed playback resumed, a final timestamp will be required for this
	// region to be counted. It's up to you to ensure you don't timestamp incorrectly
	// eg. adding to pause start timestamps will break the eventual playTime() 
	// calculation
	protected List<Long> timestamps = new ArrayList<Long>( 2 /*initial capacity*/ );

	private void stamp()
	{
		timestamps.add( Utils.now() );
	}

	void handleTransition( Transition t )
	{
		switch (t)
		{
			case Started:
				if (state == State.Stopped)
					stamp();
				state = State.Playing;
				break;
			
			case Ended:
				if (state == State.Playing)
					stamp();
				
				switch (rating)
				{
					case Unrated:
					case Skipped:   // in case we fucked up
					case Scrobbled: // in case we fucked up
						long l = playTime();
						if (playTime() >= duration / 2)
							rating = Rating.Scrobbled;
						else if (source == Source.LastFm)
							rating = Rating.Skipped;
					
					case Loved:
					case Banned:
						// these implicitly scrobble too
						break;
				}
				
				state = State.Stopped;
				break;
				
			case Paused:
				if (state == State.Playing)
					stamp();
				state = State.Paused;
				break;
				
			case Resumed:
				if (state == State.Paused)
					stamp();
				state = State.Playing;
				break;
		}
	}

	Long playTime()
	{
		Iterator<Long> i = timestamps.iterator();
		
		long playTime = 0;
		while (i.hasNext())
		{
			long start = (Long) i.next();
			if (i.hasNext())
				playTime += ((Long) i.next()) - start;
		}
		return playTime;
	}
}

class SanitisedTrack extends Track {
	private Track t;

	SanitisedTrack(Track tt) 
	{
		t = tt;
	}
	
	String artist() { return Uri.encode( t.artist ); }
	String title() { return Uri.encode( t.title ); }
	String timestamp() { return ((Long)t.timestamps.get( 0 )).toString(); } //FIXME throw if not enough elements in timestamps?
	String source()
	{
		switch (t.source)
		{
			case LastFm: return "L" + t.auth;
			default: return "P";
		}
	}

	String rating()
	{
		// precedence order
		switch (t.rating) 
		{
			case Banned: return "B";
			case Loved: return "L";
			case Skipped: return "S";
		}

		// prevent compiler error -- lame
		return "";
	}

	String duration() {
		return new Integer(t.duration).toString();
	}

	String album() {
		return Uri.encode(t.album).toString();
	}

	String trackNumber() {
		return t.trackNumber == 0 ? "" : new Integer(t.trackNumber).toString();
	}

	// TODO sanitise
	String mbid() {
		return t.mbid;
	}
}

public class ScrobblerService extends Service
{
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

	private String m_session_id;
	private String m_now_playing_url;
	private String m_submission_url;
	
	public String sessionId() { return m_session_id; }
	public URL nowPlayingUrl() throws MalformedURLException { return new URL( m_now_playing_url ); }
	public URL submissionUrl() throws MalformedURLException { return new URL( m_submission_url ); }
	
	private void handshake( String username, String password ) throws IOException, NoSuchAlgorithmException
	{
		notify( "Handshaking" );
		
		//TODO percent encode username
		//TODO toLower the md5 of the password
		String timestamp = new Long( Utils.now() ).toString();
		String authToken = Utils.md5( password + timestamp );
		String query = "?hs=true" +
					   "&p=1.2" +
					   "&c=ass" +
					   "&v=" + Utils.version() +
					   "&u=" + Uri.encode( username ) +
					   "&t=" + timestamp +
					   "&a=" + authToken;
		
		URL url = new URL( "http://post.audioscrobbler.com/" + query );
		
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
    public void onCreate()
    {
		Log.e( TAG, "Oh Hai!" );
    	
    	notify( "Initialising AudioScrobbler" );
    	
		try
    	{
			SharedPreferences p = getSharedPreferences( "Last.fm", MODE_PRIVATE );
			String user = p.getString( "username", "" );
			String pass = p.getString( "md5Password", "" );

	    	handshake( user, pass );
    	}
    	catch (NoSuchAlgorithmException e)
    	{
    		//TODO error handling
    		Log.e( TAG, e.toString() );
    	}
		catch (IOException e)
    	{
    		//TODO error handling
    		Log.e( TAG, e.toString() );
    	}
    }
	
    
    private Track m_track = new Track();
    
    enum Event { Unknown, TrackStarted, TrackLoved, TrackBanned, PlaybackPaused, PlaybackResumed, PlaybackEnded }
    
    private Event eventFromString( String s )
    {
    	s = s.toLowerCase();
    	
    	if (s.equals( "track-started" )) return Event.TrackStarted;
    	if (s.equals( "track-loved" )) return Event.TrackLoved;
    	if (s.equals( "track-banned" )) return Event.TrackBanned;
    	if (s.equals( "playback-paused" )) return Event.PlaybackPaused;
    	if (s.equals( "playback-resumed" )) return Event.PlaybackResumed;
    	if (s.equals( "playback-ended" )) return Event.PlaybackEnded;
    	
    	return Event.Unknown;
    }
    
    
    @Override
    public void onStart( Intent intent, int startId )
    {
    	Bundle args = intent.getExtras();
    	Log.i( TAG, args.toString());

    	Event e = eventFromString( args.getString( "event" ) );

    	switch (e)
    	{
	    	case TrackStarted:
	    		if (m_track.getState() != Track.State.Playing)
	    			break;
	    		
	    		// scrobble the previous track
	    		
	    	case PlaybackEnded:
				m_track.handleTransition( Track.Transition.Ended );
				if (m_track.requiresScrobble())
					scrobble( new SanitisedTrack( m_track ) );
				break;
		}
    	
    	switch (e)
    	{
    		case TrackStarted:
    		{
    			Track t = new Track();
    			t.artist = args.getString( "artist" );
    			t.title = args.getString( "title" );
    			t.duration = args.getInt( "duration" );
    			t.auth = args.getString( "authorisation-code" );
    			t.mbid = args.getString( "mbid" );
    			t.trackNumber = args.getInt( "track-number" );
    			t.album = args.getString( "album" );
    			
    			t.source = args.getString( "source" ) == "Last.fm"
     					 ? Track.Source.LastFm
    					 : Track.Source.Player;
    			
    			t.handleTransition( Track.Transition.Started );
    			
    			m_track = t;
    			
    			nowPlaying( new SanitisedTrack( m_track ) );
    		}
    		break;
    		
    		case TrackLoved:
    			//TODO verify we're loving the right track
    			m_track.rating = Track.Rating.Loved;
    			break;
    			
    		case TrackBanned:
    			m_track.rating = Track.Rating.Banned;
    			break;
    		
    		case PlaybackPaused:
    			m_track.handleTransition( Track.Transition.Paused );
    			break;
    			
    		case PlaybackResumed:
    			m_track.handleTransition( Track.Transition.Resumed );
    			break;
    			
    		case PlaybackEnded:
    			m_track.handleTransition( Track.Transition.Ended );
    			m_track = new Track();
    			
    		default:
    			break;
    	}
    }

	@Override
	public void onDestroy() {
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