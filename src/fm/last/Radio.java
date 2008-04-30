/* request example:
   ws.audioscrobbler.com/radio/handshake.php?version=0.1&platform=android&platformversion=beta&username=jonocole&passwordmd5=myhashedpassword&language=en
 */
package fm.last;

import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.net.Uri;

import fm.last.Log;



public class Radio 
{
	private String m_sessionId = null;
	private String m_streamUrl = null;
	private boolean m_subscriber = false;
	private String m_baseUrl = null;
	private String m_basePath = null;

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
	public String tuneToSimilarArtist( String artistName ) 
	{
		String urlString;
		urlString = "http://";
		urlString += m_baseUrl;
		urlString += "/radio/adjust.php?";
		urlString += "session=" + m_sessionId + "&";
		urlString += "url=lastfm://artist/" + artistName + "/similarartists&";
		urlString += "lang=en";
		
		try
		{
			URL radioAdjust = new URL( urlString );

			//TODO use Utils function
			Log.i( "ADJUST OUTPUT" );
			String stationName = "", line;
			BufferedReader reader = new BufferedReader( new InputStreamReader( radioAdjust.openStream(), "UTF-8" ) );
			while( (line = reader.readLine()) != null )
			{
				Log.i( line );
				
				if( line.startsWith( "stationname=" ) )
				{
					Log.d( "line startswith stationame" );
					stationName = Uri.decode( line.substring( 12 ) );
				}
			}	
			
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

	public TrackInfo[] getPlaylist()
	{
		String urlString;
		urlString = "http://";
		urlString += m_baseUrl;
		urlString += "/radio/xspf.php?";
		urlString += "sk=" + m_sessionId + "&";
		urlString += "discovery=0&";
		urlString += "&desktop=0.1";
	
		ArrayList<TrackInfo> tracks = new ArrayList<TrackInfo>();
		try 
		{
			URL xspfRequest = new URL(urlString);
			try 
			{
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = db.parse( new InputSource( xspfRequest.openStream() ) );
				Element rootElement = doc.getDocumentElement();
				Element trackListElement = (Element) rootElement.getElementsByTagName("trackList").item(0);
				NodeList trackNodes = trackListElement.getElementsByTagName("track");

				for (int i = 0; i < trackNodes.getLength(); i++) 
				{
					tracks.add( new TrackInfo( (Element) trackNodes.item( i ) ) );
				}
			}
			catch (org.xml.sax.SAXException e) 
			{}
			catch (java.io.IOException e) 
			{}
			catch (ParserConfigurationException e) 
			{}
		}
		catch (java.net.MalformedURLException e) 
		{
			Log.e( "Error: malformed URL: " + e.toString() );
			return new TrackInfo[] {};
		}
		
		TrackInfo[] array = new TrackInfo[ tracks.size() ];
		tracks.toArray( array );
		return array;
	}
}
