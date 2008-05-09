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
import fm.last.Utils;


public class Event 
{
	private String m_title, m_headliner, m_description = null;
	private String m_url, m_imageUrl = null;
	private ArrayList<String> m_artists = new ArrayList<String>();
	private String m_xmlString, m_venue;
	private int m_latitude, m_longitude;
	
	public String toString()
	{
		return m_title;
	}	
	
	Event( Element eventElement )
	{
		m_xmlString = Utils.toString( eventElement );
		
		EasyElement e = new EasyElement( eventElement );
		m_title       = e.e( "title" ).value();
		m_headliner   = e.e( "artists" ).e( "headliner" ).value();
		m_url         = e.e( "url" ).value();
		m_imageUrl    = e.e( "image" ).value();
		m_description = e.e( "description" ).value(); 
		m_venue       = e.e( "venue" ).e( "name" ).value();
		m_latitude    = e.e( "venue" ).e( "location" ).e( "geo:point" ).e( "geo:lat" ).microDegrees();
		m_longitude   = e.e( "venue" ).e( "location" ).e( "geo:point" ).e( "geo:long" ).microDegrees();
		
		NodeList nodes = e.e( "artists" ).e().getElementsByTagName( "artist" );
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
	public String xml()	{ return m_xmlString; }
	public int latitude() { return m_latitude; }
	public int longitude() { return m_longitude; }

	
	public static class EventResult extends ArrayList<Event>
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -1327152640298137275L;

		//Total number of events in result
		private int m_totalCount = 0;
		
		//Page offset (based on webservice pages - currently 10 events pp)
		
		int totalCount() { return m_totalCount; }
	}
	
	public static EventResult getPagesByLocation( String location, int pageOffset ) 
	{
		EventResult r = new EventResult();
		
		//imo page offsets /should/ start at 0 but the webservice starts with page 1
		pageOffset += 1;
		
		try 
		{
			URL url = new URL( "http://ws.audioscrobbler.com/2.0/" +
					    	   "?method=geo.getEvents" +
					    	   "&location=" + Uri.encode( location ) +
					    	   "&page=" + pageOffset);

			Log.i("Loading event information from: " + url.toString() );
			NodeList nodes = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse( new InputSource( url.openStream() ) )
					.getDocumentElement()
					.getElementsByTagName("event");
					
			for (int i = 0; i < nodes.getLength(); i++) 
			{
				Element e = (Element) nodes.item( i );
				r.add( new Event( e ) );
			}
			
			r.m_totalCount = 100; //FIXME
			
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
}
