/***************************************************************************
 *   Copyright 2005-2009 Last.fm Ltd.                                      *
 *   Portions contributed by Casey Link, Lukasz Wisniewski,                *
 *   Mike Jennings, and Michael Novak Jr.                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
 ***************************************************************************/
package fm.last.api.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Node;

import android.util.Log;

import fm.last.api.Event;
import fm.last.api.ImageUrl;
import fm.last.api.Venue;
import fm.last.util.XMLUtil;
import fm.last.xml.XMLBuilder;

/**
 * @author Lukasz Wisniewski
 */
public class EventBuilder extends XMLBuilder<Event> {

	private final DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
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
		int i = 0;
		for (Node artist : artistNodes) {
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
			if (text != null) {
				startDate = dateFormat.parse(text);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// description
		// FIXME String description = getText("description");
		String description = null;
		Node descriptionNode = getChildNode("description").getFirstChild();
		if (descriptionNode != null) {
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

		// status
		String status = this.getAttribute("status");

		// ticket providers
		Node ticketsNode = getChildNode("tickets");
		List<Node> ticketNodes = XMLUtil.findNamedElementNodes(ticketsNode, "ticket");
		HashMap<String, String> ticketUrls = new HashMap<String, String>();
		for (Node ticket : ticketNodes) {
			Log.i("LastFm", "Supplier: " + ticket.getAttributes().getNamedItem("supplier").getNodeValue());
			Log.i("LastFm", "URL: " + ticket.getFirstChild().getNodeValue());
			ticketUrls.put(ticket.getAttributes().getNamedItem("supplier").getNodeValue(),ticket.getFirstChild().getNodeValue());
		}

		return new Event(id, title, artists, headliner, venue, startDate, description, images, attendance, reviews, tag, url, status, ticketUrls);
	}

}
