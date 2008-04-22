package fm.last.Android;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Integer;
import java.lang.Long;
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
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import fm.last.Android.R;
import fm.last.Android.Track.Source;


class Track
{
	enum Source { Player, LastFmRadio }
	enum Rating { Scrobbled, Skipped, Loved, Banned }
	
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
	
	String authCode; // Last.fm Radio tracks come with auth codes

	Track()
	{
		source = Source.Player;
		rating = Rating.Scrobbled;
	}
}


public class ScrobblerService extends Service 
{	
	private static final String TAG = "Last.fm";
	private NotificationManager m_nm;
	
	private class SanitisedTrack extends Track
	{
		private Track t;
		
		SanitisedTrack( Track tt )
		{
			t = tt;
		}
		
		private String formUrlEncoded( String in )
		{
			try
			{
				return URLEncoder.encode( in, "UTF-8" );
			}
			catch (UnsupportedEncodingException e)
			{
				return "";
			}
			catch (NullPointerException e)
			{
				return "";
			}
		}
		
		String artist() { return formUrlEncoded( t.artist ); }
		String title() { return formUrlEncoded( t.title ); }
		String timestamp() { return new Long( t.timestamp ).toString(); }
		String source()
		{
			switch (t.source)
			{
				case LastFmRadio: return "L" + t.authCode;
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
				case Scrobbled: return "";
				case Skipped: return "S";
			}
			
			// prevent compiler error -- lame
			return "";
		}
		String duration() { return new Integer( t.duration ).toString(); }
		String album() { return formUrlEncoded( t.album ).toString(); }
		String trackNumber() { return t.trackNumber == 0 ? "" : new Integer( t.trackNumber ).toString(); }
		
		//TODO sanitise
		String mbid() { return t.mbid; }
	}
	
	private String httpConnectionOutput( HttpURLConnection http ) throws IOException
	{
		InputStream in = http.getInputStream();
		String out = "";
		while (in.available() > 0)
		{
			out += (char) in.read(); //FIXME inefficient!
		}
		in.close();
		return out;
	}	
	
	private long now()
	{
		//TODO check this is UTC
		return System.currentTimeMillis() / 1000;
	}	
	
	private class Handshake
	{
		private String md5( String in ) throws NoSuchAlgorithmException
		{
			MessageDigest m = MessageDigest.getInstance( "MD5" );
			m.update( in.getBytes(), 0, in.length() );
			BigInteger bi = new BigInteger( 1, m.digest() );
			return bi.toString(16);
		}
				
		public Handshake( String username, String password ) throws IOException
		{
			//TODO percent encode username
			//TODO toLower the md5 of the password
			
			try 
			{
				String timestamp = new Long( now() ).toString();
				String authToken = md5( password + timestamp );
				String query = "?hs=true" +
							   "&p=1.2" +
							   "&c=ass" +
							   "&v=0.1" +
							   "&u=" + URLEncoder.encode( username ) +
							   "&t=" + timestamp +
							   "&a=" + authToken;
				
				URL url = new URL( "http://post.audioscrobbler.com/" + query );
				
				HttpURLConnection http = (HttpURLConnection) url.openConnection();
				http.setRequestMethod( "GET" );
				String out = httpConnectionOutput( http );
				onReturn( out );
			}
			catch (NoSuchAlgorithmException e)
			{}
		}
		
		private void onReturn( String out )
		{
			String[] tokens = out.split( "\n" );
			
			if (tokens[0].equals( "OK" ))
			{
				m_session_id = tokens[1];
				m_now_playing_url = tokens[2];
				m_submission_url = tokens[3];
			}
		}
		
		private String m_session_id;
		private String m_now_playing_url;
		private String m_submission_url;
		
		public String sessionId() { return m_session_id; }
		public URL nowPlayingUrl() throws MalformedURLException { return new URL( m_now_playing_url ); }
		public URL submissionUrl() throws MalformedURLException { return new URL( m_now_playing_url ); }
	}
	
	public class ScrobblerBinder extends Binder
	{
		ScrobblerService getService()
		{
			return ScrobblerService.this;
		}
	}
	
	private Handshake m_handshake;
	
    @Override
    public void onCreate()
    {
		Log.e( TAG, "Oh Hai!" );
    	
    	try
    	{
			m_nm = (NotificationManager) getSystemService( NOTIFICATION_SERVICE );
	
	    	// Display a notification about us starting.  We put an icon in the status bar.
	    	onCreateShowNotification();
	    	
	    	m_handshake = new Handshake( "2girls1cup", "77e3c764678e809f1e72727c1f26e3f3" );
	    	
	    	{
	    		Track t = new Track();
	    		t.artist = "Moose";
	    		t.title = "I Am Not The Horse";
	    		t.duration = 300;
	    		t.timestamp = now() - t.duration;

	    		scrobble( new SanitisedTrack( t ) );	
	    	}
	    	
	    	Track t = new Track();
	    	t.artist = "Foo";
	    	t.title = "Bar";
	    	t.duration = 60;
	    	nowPlaying( new SanitisedTrack( t ) );
    	}
    	catch (IOException e)
    	{
    		//TODO error handling
    		Log.e( TAG, e.toString() );
    	}
    }
    
    @Override
    protected void onDestroy()
    {
    	m_nm.cancel( 0 );
    	
    	Toast.makeText( this, "Scrobbler stopped", Toast.LENGTH_SHORT ).show();
    }
    
    private void onCreateShowNotification()
    {
    	Notification n = new Notification();
    	
    	n.icon = R.drawable.status_bar_icon;
    	n.tickerText = "FooBar";
    	
    	m_nm.notify( 0, n );
    }

    private final IBinder m_binder = new ScrobblerBinder();
    
	@Override
	public IBinder onBind( Intent intent ) 
	{
		return m_binder;
	}
	
	private String post( URL url, String parameters ) throws IOException
	{
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setRequestMethod( "POST" );
		http.setDoOutput( true );
		http.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
		
		DataOutputStream o = new DataOutputStream( http.getOutputStream() );
		o.writeBytes( parameters );
		o.flush();
		o.close();
		
		return httpConnectionOutput( http );
	}
	
	void nowPlaying( SanitisedTrack t )
	{	
		//TODO at some point mbId needs to checked valid or blanked
		try
		{
			String data = "s=" + m_handshake.sessionId() +
			             "&a=" + t.artist() +
			             "&t=" + t.title() + 
			             "&b=" + t.album() +
			             "&l=" + t.duration() +
			             "&n=" + t.trackNumber() +
			             "&m=" + t.mbid();
			
			String out = post( m_handshake.nowPlayingUrl(), data );
						
			Log.i( TAG, "nowPlaying() result: " + out );
		}
		catch (IOException e)
		{
			Log.e( TAG, "nowPlaying() error: " + e.toString() );
		}
	}
	
	void scrobble( SanitisedTrack t )
	{	
		try
		{
			String data = "s=" + m_handshake.sessionId();
			String N = "0";
			
			data += "&a[" + N + "]=" + t.artist() +
	        		"&t[" + N + "]=" + t.title() +
	        		"&i[" + N + "]=" + t.timestamp() +
	        		"&o[" + N + "]=" + t.source() +
	        		"&r[" + N + "]=" + t.rating() +
	        		"&l[" + N + "]=" + t.duration() +
	        		"&b[" + N + "]=" + t.album() +
	        		"&n[" + N + "]=" + t.trackNumber() +
	        		"&m[" + N + "]=" + t.mbid();
			
			String out = post( m_handshake.submissionUrl(), data );
			Log.i( TAG, "scrobble() result: " + out );
		}
		catch (IOException e)
		{
			Log.e( TAG, "scrobble() error: " + e.toString() );
		}
	}
}