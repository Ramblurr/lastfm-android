package fm.last.events;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

import android.net.Uri;

import fm.last.EasyElement;
import fm.last.Utils;
import fm.last.ws.RequestManager;
import fm.last.ws.RequestParameters;
import fm.last.ws.Response;


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
	
	public Event( Element eventElement )
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
		private static final long serialVersionUID = -1327152640298137275L;

		//Total number of events in result
		private int m_pageCount = 0;
		
		//Page offset (based on webservice pages - currently 10 events pp)
		
		int pageCount() { return m_pageCount; }
	}
	
	public static EventResult getEventsByLocation( String location, int pageOffset )
	{
		int id = getEventsByLocation( location, pageOffset, null );
		Response response = RequestManager.version2().waitForRequestResponse( id );
		
		if( response.hasError() )
			return new EventResult();
		
		return resultFromResponse( response );
	}
	
	public static int getEventsByLocation( String location, int pageOffset, final EventHandler handler ) 
	{
		//imo page offsets /should/ start at 0 but the webservice starts with page 1
		pageOffset += 1;
		
		RequestParameters params = new RequestParameters();
		params.add( "location", Uri.encode( location ) )
			  .add( "page", String.valueOf( pageOffset) );

		int requestId = 
			RequestManager.version2().callMethod( "geo.Events", params, new fm.last.ws.EventHandler()
			{
	
				@Override
				public void onError( int id, String error )
				{
					// TODO Auto-generated method stub
					
				}
	
				@Override
				public void onMethodComplete( int id, Response response )
				{
	
					if( response.hasError() )
					{
						if( handler != null )
							handler.onError( response.error() );
						return;
					}
					
					EventResult r = resultFromResponse( response );
					if( handler != null )
						handler.onSuccess( r );
				}
				
			} );
		return requestId;
	}
	
	private static EventResult resultFromResponse( Response response )
	{
		EventResult r = new EventResult();
		
		Document xmlDom = response.xmlDocument();
		
		Element events = (Element)xmlDom.getDocumentElement().getElementsByTagName("events").item(0);
		r.m_pageCount = Integer.parseInt( events.getAttribute("totalpages") );
		NodeList nodes = events.getElementsByTagName("event");

		for (int i = 0; i < nodes.getLength(); i++) {
			Element e = (Element) nodes.item(i);
			r.add(new Event(e));
		}
		return r;
	}
}
