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

public class Event {
	private String m_title, m_headliner;
	private ArrayList<String> m_artists;

	private long m_duration;

	public Event() {
	}

	public static EventList getByLocation(String location) {
		EventList eventList = null;
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
			eventList = new EventList();
			
			//eventList.m_totalCount = rootElement.getAttribute(name)
			eventList.m_totalCount = 100;
			for (int i = 0; i < eventCount; i++) {
				Event event = new Event();
				event.read((Element) events.item(i));
				eventList.add(event);
			}
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
		return eventList;
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

	public static class EventList extends ArrayList<Event> {
		private static final long serialVersionUID = -3362761671731676362L;
		private int m_totalCount;
		int totalCount() {
			return m_totalCount;
		}
	}
}
