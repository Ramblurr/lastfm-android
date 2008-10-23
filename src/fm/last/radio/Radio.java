/* request example:
   ws.audioscrobbler.com/radio/handshake.php?version=0.1&platform=android&platformversion=beta&username=jonocole&passwordmd5=myhashedpassword&language=en
 */
package fm.last.radio;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.ws.*;
import fm.last.ws.Request.RequestType;

import android.net.Uri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.w3c.dom.*;

import fm.last.EasyElement;
import fm.last.LastFmApplication;
import fm.last.Log;
import fm.last.TrackInfo;

public class Radio 
{
	private ArrayList<RadioEventHandler> m_handlers = new ArrayList<RadioEventHandler>();
	private LinkedList<TrackInfo> m_playlist = new LinkedList<TrackInfo>();
	private TrackInfo m_nowPlaying;
	private RadioStream m_radioStream = new RadioStream();
	private LastFmServer server;
	
	public Radio()
	{
		Log.i( "Starting last.fm radio" );
		server = AndroidLastFmServerFactory.getServer();
	}
	
	public void addRadioHandler( RadioEventHandler handler )
	{
		m_handlers.add( handler );
	}

	/** @returns station pretty name */
	public String tuneToSimilarArtist( String artist ) 
	{
		String station = "lastfm://artist/" + Uri.encode( artist ) + "/similarartists";
		Session session = LastFmApplication.instance().getSession();
		String sk = session.getKey();
		try {
			Station radioStation = server.tuneToSimilarArtist(station, sk);
			if (radioStation != null) {
				return radioStation.getName();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		RequestParameters params = new RequestParameters();
		params.add( "station", "lastfm://artist/" + Uri.encode( Uri.encode( artist ) ) + "/similarartists" );
		Response response = RequestManager.version2().callMethod( "radio.tune", params, RequestType.POST_REQUEST);

		if( !response.hasError() )
		{
			EasyElement e = new EasyElement( response.xmlDocument().getDocumentElement() );
			return e.e( "lfm" ).e( "station" ).e( "name" ).value();
		}
		else
			//TODO proper error handling
			return response.error();
	}
	

	/** @returns station pretty name */
	public String tuneToTag( String tag ) 
	{
		RequestParameters params = new RequestParameters();
		params.add( "station", "lastfm://globaltags/" + Uri.encode( Uri.encode( tag ) )  );
		Response response = RequestManager.version2().callMethod( "radio.tune", params, RequestType.POST_REQUEST);

		if( !response.hasError() )
		{
			EasyElement e = new EasyElement( response.xmlDocument().getDocumentElement() );
			return e.e( "lfm" ).e( "station" ).e( "name" ).value();
		}
		else
			//TODO proper error handling
			return response.error();
	}

	public LinkedList<TrackInfo> playlist()
	{
		return m_playlist;
	}
	
	/** fetches 5 new tracks for the playlist, valid while the session is valid */
	public void fetch()
	{
		RequestParameters params = new RequestParameters();
	
		Response response = RequestManager.version2().callMethod( "radio.getPlaylist", params );
		
		Element trackList =
					  (Element)response.xmlDocument().getDocumentElement()
				   			  		   .getElementsByTagName( "trackList" )
				   			  		   .item( 0 );
		
		NodeList tracks = ((Element) trackList).getElementsByTagName( "track" );

		for (int i = 0; i < tracks.getLength(); i++)
		{
			m_playlist.add( new TrackInfo( (Element) tracks.item( i ) ) );
		}
	}
	
	public void play()
	{
		if( playlist().size() == 0 )
			fetch();
		
		if( playlist().size() == 0 )
		{
			//TODO: handle not enough content error correctly
			return;
		}
		
		TrackInfo track = playlist().remove();
		
		m_radioStream.play( track.location() );
		
		onTrackStarted( track );		
	}
	
	public void stop()
	{
		m_radioStream.stop();
		onTrackEnded( m_nowPlaying );	
	}
	
	public void skip()
	{
		stop();
		play();
	}
	
	private void onTrackStarted( TrackInfo track)
	{
		m_nowPlaying = track;
		for( RadioEventHandler handler : m_handlers )
		{
			handler.onTrackStarted( track );
		}
	}
	private void onTrackEnded( TrackInfo track)
	{
		m_nowPlaying = null;
		for( RadioEventHandler handler : m_handlers )
		{
			handler.onTrackEnded( track );
		}
	}
}
