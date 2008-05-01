package fm.last;

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

import android.util.Log;

//TODO Read the following from the webservice:
//		Venue information
//		Start Date
//		Description

public class Event {
	private String m_title, m_headliner, m_description = null;
	private String m_url, m_imageUrl = null;
	private EventVenue m_venue;
	private ArrayList<String> m_artists;

	private long m_duration;

	public Event() {
	}

	public static EventResult getPagesByLocation(String location, int pageOffset) {
		EventResult eventResult = new EventResult();
		eventResult.m_pageOffset = pageOffset;
		try {
			String eventRequestlocation = "http://ws.audioscrobbler.com/2.0/?method=geo.getEvents&location="
					+ URLEncoder.encode(location, "UTF-8");
			URL eventRequest = new URL(eventRequestlocation);

			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();

			final InputStream eventIStream = eventRequest.openStream();

			Document doc = db.parse(new InputSource(eventIStream));
			Element rootElement = doc.getDocumentElement();
			NodeList events = rootElement.getElementsByTagName("event");
			final int eventCount = events.getLength();
			ArrayList<Event> eventList = new ArrayList<Event>();
			
			//eventList.m_totalCount = rootElement.getAttribute(name)
			eventResult.m_totalCount = 100;
			for (int i = 0; i < eventCount; i++) {
				Event event = new Event();
				event.read((Element) events.item(i));
				eventList.add(event);
			}
			eventResult.m_events = new Event[eventList.size()];
			eventList.toArray(eventResult.m_events);
		} catch (java.net.MalformedURLException e) {
			Log.e("Last.fm", "Malformed events lookup URL: " + e);
		} catch (java.io.IOException e) {
			Log.e("Last.fm", "Could not read from http stream: " + e);
		} catch (FactoryConfigurationError e) {
			Log.e("Last.fm", "DocumentBuilder Factory configuration error: "
					+ e);
		} catch (ParserConfigurationException e) {
			Log.e("Last.fm", "Parser Configuration error: " + e);
		} catch (org.xml.sax.SAXException e) {
			Log.e("Last.fm", "Sax Error: " + e);
		}
		return eventResult;
	}

	public void read(Element event) {
		Element titleElement = (Element) event.getElementsByTagName("title")
				.item(0);
		m_title = ((Text) titleElement.getFirstChild()).getData();

		Element artistsElement = (Element) event
				.getElementsByTagName("artists").item(0);
		readArtists(artistsElement);

		Element headlinerElement = (Element) artistsElement
				.getElementsByTagName("headliner").item(0);
		m_headliner = ((Text) headlinerElement.getFirstChild()).getData();
		
		Element urlElement = (Element) event.getElementsByTagName("url").item(0);
			m_url = ((Text)urlElement.getFirstChild()).getData();
		
		Element imageUrlElement = (Element) event.getElementsByTagName("image").item(0);
		if(imageUrlElement.hasChildNodes())
			m_imageUrl = ((Text)imageUrlElement.getFirstChild()).getData();
		
		Element descriptionElement = (Element) event.getElementsByTagName("description").item(0);
		if(descriptionElement.hasChildNodes())
			m_description = ((Text)descriptionElement.getFirstChild()).getData();
		
	}

	private void readArtists(Element artists) {
		NodeList artistNodes = artists.getElementsByTagName("artist");
		final int artistCount = artistNodes.getLength();
		m_artists = new ArrayList<String>();
		for (int artistIndex = 0; artistIndex < artistCount; artistIndex++) {

			Element artistElement = (Element) artistNodes.item(artistIndex);
			final String artistName = ((Text) artistElement.getFirstChild())
					.getData();

			m_artists.add(artistName);
		}
	}

	public String title() {
		return m_title;
	}
	
	public String headliner() {
		return m_headliner;
	}
	
	public ArrayList<String> artists() {
		return m_artists;
	}
	
	public String url() {
		return m_url;
	}
	
	public String imageUrl() {
		return m_imageUrl;
	}
	
	public String description() {
		return m_description;
	}

	public static class EventResult {
		
		//Total number of events in result
		private int m_totalCount;
		
		//Page offset (based on webservice pages - currently 10 events pp)
		private int m_pageOffset;
		
		//Array of events in the result
		private Event[] m_events;
		
		
		Event[] events() {
			return m_events;
		}
		
		int totalCount() {
			return m_totalCount;
		}
	}
}
