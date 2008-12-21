package fm.last.api.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Node;

import fm.last.api.Event;
import fm.last.api.ImageUrl;
import fm.last.api.Venue;
import fm.last.util.XMLUtil;
import fm.last.xml.XMLBuilder;

/**
 * @author Lukasz Wisniewski
 */
public class EventBuilder extends XMLBuilder<Event> {

	private final DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
	private final DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
	private ImageUrlBuilder imageBuilder = new ImageUrlBuilder();

	@Override
	public Event build(Node eventNode) {
		node = eventNode;
		int id = new Integer(getText("id"));
		String title = getText("title");

		// artists
		Node artistsNode = getChildNode("artists");
		List<Node> artistNodes = XMLUtil.findNamedElementNodes(artistsNode, "artist");
		String[] artists = new String[artistNodes.size()];
		int i=0;
		for(Node artist : artistNodes){
			artists[i++] = artist.getFirstChild().getNodeValue();
		}

		// headliner
		String headliner = null;
		headliner = XMLUtil.findNamedElementNode(artistsNode, "headliner").getFirstChild().getNodeValue();

		// venue
		Node venueNode = getChildNode("venue");
		VenueBuilder venueBuilder = new VenueBuilder();
		Venue venue = venueBuilder.build(venueNode);

		// startDate
		Date startDate = null;
		try {
			String text = getText("startDate");
			if(text != null){
				startDate = dateFormat.parse(text);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// startTime 
		Date startTime = null;
		try {
			String text = getText("startTime");
			if(text != null){
				startTime = timeFormat.parse(text);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// description
		// FIXME String description = getText("description");
		String description = null;
		Node descriptionNode = getChildNode("description").getFirstChild();
		if(descriptionNode != null){
			description = descriptionNode.getNodeValue();
		}

		// images
		List<Node> imageNodes = getChildNodes("image");
		ImageUrl[] images = new ImageUrl[imageNodes.size()];
		i = 0;
		for (Node imageNode : imageNodes) {
			images[i++] = imageBuilder.build(imageNode);
		}

		// attendance
		int attendance = 0;
		try {
			attendance = Integer.parseInt(getText("attendance"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		// reviews
		int reviews = 0;
		try {
			reviews = Integer.parseInt(getText("reviews"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		// tag
		String tag = getText("tag");
		
		// url
		String url = getText("url");

		return new Event(id, title, artists, headliner,
				venue, 
				startDate, startTime, description,
				images, attendance, reviews, tag, url);
	}

}
