package fm.last;

import org.w3c.dom.Text;
import org.w3c.dom.Element;

public class TrackInfo 
{
	private String m_title, m_artist, m_album, m_location = null;
	private long m_duration;

	public TrackInfo( Element track )
	{
		Element creatorElement = (Element) track.getElementsByTagName("creator").item(0);
		if( creatorElement.getFirstChild() != null )
			m_artist = ((Text) creatorElement.getFirstChild()).getData();

		Element titleElement = (Element) track.getElementsByTagName("title").item(0);
		if( titleElement.getFirstChild() != null )
			m_title = ((Text) titleElement.getFirstChild()).getData();

		Element albumElement = (Element) track.getElementsByTagName("album").item(0);
		if( albumElement.getFirstChild() != null )
			m_album = ((Text) albumElement.getFirstChild()).getData();

		Element durationElement = (Element) track.getElementsByTagName("duration").item(0);
		if( durationElement.getFirstChild() != null )
			m_duration = Long.valueOf(((Text) durationElement.getFirstChild()).getData());
		
		Element locationElement = (Element) track.getElementsByTagName("location").item(0);
		if( locationElement.getFirstChild() != null )
			m_location = ((Text) locationElement.getFirstChild()).getData();
	}

	public String toString() 
	{
		return m_title + " - " + m_artist + " - " + m_album;
	}

	public String location() { return m_location; }
	public long duration() { return m_duration; }
	public String artist() { return m_artist; }
	public String title() { return m_title; }
	public String album() { return m_album; }
}
