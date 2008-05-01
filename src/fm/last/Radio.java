/* request example:
   ws.audioscrobbler.com/radio/handshake.php?version=0.1&platform=android&platformversion=beta&username=jonocole&passwordmd5=myhashedpassword&language=en
 */
package fm.last;

import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
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
		try
		{
			URL url = new URL( "http://" + m_baseUrl + "/radio/adjust.php" +
					    	   "?session=" + m_sessionId +
					 		   "&url=lastfm://artist/" + Uri.encode( artist ) + "/similarartists" +
					 		   "&lang=en" );

			String stationName = "", line;
			BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream(), "UTF-8" ) );
			
			Log.i( "ADJUST OUTPUT" );
			while( (line = reader.readLine()) != null )
			{
				Log.i( line );
				
				if( line.startsWith( "stationname=" ) )
				{
					stationName = Uri.decode( line.substring( 12 ) );
				}
			}
			
			fetch();
			
			return stationName;
		}
		catch (java.net.MalformedURLException e) 
		{
			Log.e( "tuneToSimilarArtist(): " + e.toString() );
		}
		catch (java.io.IOException e) 
		{
			Log.e( "tuneToSimilarArtist(): " + e.toString() );
		}
		
		return "";
	}

	public Stack<TrackInfo> playlist()
	{
		return m_playlist;
	}
	
	/** fetches 5 new tracks for the playlist, valid while the session is valid */
	public void fetch()
	{
		try 
		{
			URL url = new URL( "http://" + m_baseUrl + "/radio/xspf.php" +
					    	   "?sk=" + m_sessionId + 
							   "&discovery=0" +
							   "&desktop=0.1" );
			
			Node n = DocumentBuilderFactory.newInstance()
					   					   .newDocumentBuilder()
					   					   .parse( new InputSource( url.openStream() ) )
					   					   .getDocumentElement()
					   					   .getElementsByTagName( "trackList" )
					   					   .item( 0 );
			
			NodeList tracks = ((Element) n).getElementsByTagName( "track" );

			for (int i = 0; i < tracks.getLength(); i++)
			{
				m_playlist.push( new TrackInfo( (Element) tracks.item( i ) ) );
			}
		}
		catch (org.xml.sax.SAXException e) 
		{}
		catch (java.net.MalformedURLException e) 
		{
			Log.e( "Error: malformed URL: " + e.toString() );
		}
		catch (java.io.IOException e) 
		{}
		catch (ParserConfigurationException e) 
		{}

	}
}
