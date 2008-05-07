package fm.last;

import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.util.ArrayList;
import java.net.URL;
import java.net.URLEncoder;
import java.io.InputStream;
import javax.xml.parsers.*;

import android.database.DataSetObserver;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import fm.last.Log;


//TODO Read the following from the webservice:
//		Venue information
//		Start Date
//		Description

public class Event 
{
	private String m_title, m_headliner, m_description = null;
	private String m_url, m_imageUrl = null;
	private ArrayList<String> m_artists = new ArrayList<String>();
	private long m_duration;
	private String m_xmlString, m_venue;
	private int m_latitude, m_longitude;
	
	private Element getFirstElementNamed( Element e, String s )
	{
		return (Element) e.getElementsByTagName( s ).item( 0 );
	}
	
	private String getValueOfFirstElementNamed( Element e, String s )
	{
		return getFirstElementNamed( e, s ).getFirstChild().getNodeValue();
	}
	
	private Element getFirstElementNamed( Element e, String ns, String s )
	{
		return (Element) e.getElementsByTagNameNS( ns, s ).item( 0 );
	}	
	
	private int microDegrees( String degrees )
	{
		double tmp = Double.valueOf( degrees ) * 1E6;
		return new Double( tmp ).intValue();
	}
	
	public String toString()
	{
		return m_title;
	}
	
	Event( Element e )
	{
		m_xmlString = Utils.toString( e );
		
		Element titleElement = (Element) e.getElementsByTagName("title").item( 0 );
		m_title = ((Text) titleElement.getFirstChild()).getData();

		Element artistsElement = getFirstElementNamed( e, "artists" );
		populateArtists( artistsElement );
		
		Element headlinerElement = (Element) artistsElement.getElementsByTagName("headliner").item( 0 );
		m_headliner = ((Text) headlinerElement.getFirstChild()).getData();
		
		Element urlElement = (Element) e.getElementsByTagName("url").item( 0 );
		m_url = ((Text)urlElement.getFirstChild()).getData();
		
		Element imageUrlElement = (Element) e.getElementsByTagName("image").item( 0 );
		if (imageUrlElement.hasChildNodes())
			m_imageUrl = ((Text)imageUrlElement.getFirstChild()).getData();
		
		Element descriptionElement = (Element) e.getElementsByTagName("description").item( 0 );
		if (descriptionElement.hasChildNodes())
			m_description = ((Text)descriptionElement.getFirstChild()).getData();
		
		m_venue = getValueOfFirstElementNamed( e, "name" );
		m_latitude = microDegrees( getValueOfFirstElementNamed( e, "geo:lat" ) );
		m_longitude = microDegrees( getValueOfFirstElementNamed( e, "geo:long" ) );
	}
	
	public static EventResult getPagesByLocation( String location, int pageOffset ) 
	{
		EventResult r = new EventResult();
		r.m_pageOffset = pageOffset;
		
		try 
		{
			URL url = new URL( "http://ws.audioscrobbler.com/2.0/" +
					    	   "?method=geo.getEvents" +
					  		   "&location=" + Uri.encode( location ) );

			NodeList nodes = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse( new InputSource( url.openStream() ) )
					.getDocumentElement()
					.getElementsByTagName("event");
					
			ArrayList<Event> events = new ArrayList<Event>();
			for (int i = 0; i < nodes.getLength(); i++) 
			{
				Element e = (Element) nodes.item( i );
				events.add( new Event( e ) );
			}
			
			r.m_totalCount = 100; //FIXME
			r.m_events = new Event[ events.size() ];
			
			events.toArray( r.m_events );
		} 
		catch (java.net.MalformedURLException e) 
		{
			Log.e( "Malformed events lookup URL: " + e );
		} 
		catch (java.io.IOException e) 
		{
			Log.e( "Could not read from http stream: " + e );
		} 
		catch (FactoryConfigurationError e) 
		{
			Log.e( "DocumentBuilder Factory configuration error: " + e );
		} 
		catch (ParserConfigurationException e) 
		{
			Log.e( "Parser Configuration error: " + e );
		}
		catch (org.xml.sax.SAXException e) 
		{
			Log.e( "Sax Error: " + e );
		}
		
		
		return r;
	}
	
    private void populateArtists( Element artists ) 
	{
		NodeList nodes = artists.getElementsByTagName( "artist" );
		for (int i = 0; i < nodes.getLength(); i++) 
		{
			m_artists.add( nodes.item( i ).getFirstChild().getNodeValue() );
		}
	}

	public String title() { return m_title;	}
	public String headliner() {	return m_headliner;	}
	public ArrayList<String> artists() { return m_artists; }
	public String url() { return m_url;	}
	public String imageUrl() { return m_imageUrl; }
	public String description() { return m_description;	}
	public String venue() { return m_venue; }
	public int latitude() { return m_latitude; }
	public int longitude() { return m_longitude; }
	public String xml()	{ return m_xmlString; }

	public static class EventResult
	{
		//Total number of events in result
		private int m_totalCount = 0;
		
		//Page offset (based on webservice pages - currently 10 events pp)
		private int m_pageOffset;
		
		//Array of events in this result
		private Event[] m_events;
		
		Event[] events() { return m_events; }
		int totalCount() { return m_totalCount; }
	}
}
