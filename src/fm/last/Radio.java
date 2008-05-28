/* request example:
   ws.audioscrobbler.com/radio/handshake.php?version=0.1&platform=android&platformversion=beta&username=jonocole&passwordmd5=myhashedpassword&language=en
 */
package fm.last;

import fm.last.ws.*;

import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;
import org.w3c.dom.*;

import android.net.Uri;

import fm.last.Log;


public class Radio 
{
	private String m_sessionId = null;
	private String m_streamUrl = null;
	private boolean m_subscriber = false;
	private String m_baseUrl = null;
	private String m_basePath = null;

	private Stack<TrackInfo> m_playlist = new Stack<TrackInfo>();
	
	Radio( String username, String md5password )
	{
		Log.i( "Starting last.fm radio" );
		
		RadioHandshake handshake = new RadioHandshake( username, md5password );
		handshake.connect();
		
		m_sessionId = handshake.getValue("session");
		m_streamUrl = handshake.getValue("stream_url");
		m_subscriber = Integer.valueOf( handshake.getValue("subscriber") ).intValue() == 1;
		m_baseUrl = handshake.getValue("base_url");
		m_basePath = handshake.getValue("base_path");
	}

	/** @returns station pretty name */
	public String tuneToSimilarArtist( String artist ) 
	{
		RequestParameters params = new RequestParameters();
		params.add( "session", m_sessionId )
			  .add( "url", "lastfm://artist/" + Uri.encode( artist ) + "/similarartists")
			  .add( "lang", "en" );
		
		Response response = RequestManager.version1().callMethod( "radio/adjust.php", params );
		
		
		String stationName = "", line;
		BufferedReader reader = response.dataReader();
		
		Log.i( "ADJUST OUTPUT" );
		try
		{
			while( (line = reader.readLine()) != null )
			{
				Log.i( line );
				
				if( line.startsWith( "stationname=" ) )
				{
					stationName = Uri.decode( line.substring( 12 ) );
				}
			}
		}
		catch (java.io.IOException e) 
		{
			Log.e( "tuneToSimilarArtist(): " + e.toString() );
		}
		
		
		fetch();
		
		return stationName;

	}

	public Stack<TrackInfo> playlist()
	{
		return m_playlist;
	}
	
	/** fetches 5 new tracks for the playlist, valid while the session is valid */
	public void fetch()
	{
		RequestParameters params = new RequestParameters();
		params.add( "sk", m_sessionId )
			  .add( "discovery", "0" )
			  .add( "desktop", "0.1" );

		Response response = RequestManager.version1().callMethod( "radio/xspf.php", params );
		
		Element trackList =
					  (Element)response.xmlDocument().getDocumentElement()
				   			  		   .getElementsByTagName( "trackList" )
				   			  		   .item( 0 );
		
		NodeList tracks = ((Element) trackList).getElementsByTagName( "track" );

		for (int i = 0; i < tracks.getLength(); i++)
		{
			m_playlist.push( new TrackInfo( (Element) tracks.item( i ) ) );
		}
	}
}
