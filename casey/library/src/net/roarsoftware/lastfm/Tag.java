package net.roarsoftware.lastfm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xmlpull.v1.XmlPullParser;

import net.roarsoftware.xml.DomElement;

/**
 * Provides nothing more than a namespace for the API methods starting with tag.
 *
 * @author Janni Kovacs
 */
public class Tag {

	private Tag() {
	}

	public static Collection<String> getSimilar(String tag, String apiKey) {
		Result result = Caller.getInstance().call("tag.getSimilar", apiKey, "tag", tag);
		if (!result.isSuccessful())
			return Collections.emptyList();
		List<String> tags = new ArrayList<String>();
		for (DomElement domElement : result.getContentElement().getChildren("tag")) {
			tags.add(domElement.getChildText("name"));
		}
		return tags;
	}

	public static SortedMap<Integer, String> getTopTags(String apiKey) {
		Result result = Caller.getInstance().call("tag.getTopTags", apiKey);
		if (!result.isSuccessful())
			return new TreeMap<Integer, String>();
		SortedMap<Integer, String> tags = new TreeMap<Integer, String>(Collections.reverseOrder());
		for (DomElement domElement : result.getContentElement().getChildren("tag")) {
			tags.put(Integer.valueOf(domElement.getChildText("count")), domElement.getChildText("name"));
		}
		return tags;
	}

	public static Collection<Album> getTopAlbums(String tag, String apiKey) {
		Result result = Caller.getInstance().call("tag.getTopAlbums", apiKey, "tag", tag);
		if (!result.isSuccessful())
			return Collections.emptyList();
		List<Album> albums = new ArrayList<Album>();
		for (DomElement domElement : result.getContentElement().getChildren("album")) {
			albums.add(Album.albumFromElement(domElement));
		}
		return albums;
	}

	public static Collection<Track> getTopTracks(String tag, String apiKey) {
		Result result = Caller.getInstance().call("tag.getTopTracks", apiKey, "tag", tag);
		if (!result.isSuccessful())
			return Collections.emptyList();
		List<Track> tracks = new ArrayList<Track>();
		for (DomElement domElement : result.getContentElement().getChildren("track")) {
			tracks.add(Track.trackFromElement(domElement));
		}
		return tracks;
	}

	public static Collection<Artist> getTopArtists(String tag, String apiKey) {
		Result result = Caller.getInstance().call("tag.getTopArtists", apiKey, "tag", tag);
		if (!result.isSuccessful())
			return Collections.emptyList();
		List<Artist> artists = new ArrayList<Artist>();
		for (DomElement domElement : result.getContentElement().getChildren("artist")) {
			artists.add(Artist.artistFromElement(domElement));
		}
		return artists;
	}

	public static Collection<String> search(String tag, String apiKey) {
		return search(tag, 30, apiKey);
	}

	public static Collection<String> search(String tag, int limit, String apiKey) {
		try {
		Result result = Caller.getInstance().call("tag.search", apiKey, "tag", tag, "limit", String.valueOf(limit));
		
		result.getParser().nextTag();
		if (!result.getParser().getName().equals("results"))
			return null;
		int event = result.getParser().nextTag();
		List<String> tags = new ArrayList<String>();
		boolean loop = true;
		while (loop) {
			String n = result.getParser().getName();
			switch(event)
			{
			case XmlPullParser.START_TAG:
				if(n.equals("name"))
					tags.add(result.getParser().nextText());
				break;
			case XmlPullParser.END_TAG:
				if(n.equals("tagmatches"))
					loop = false;
				break;
			case XmlPullParser.TEXT:
			default:
				break;
			}
			event = result.getParser().next();
		}
		return tags;
		} catch (Exception e) {}
		return null;
	}

	public static Chart<Artist> getWeeklyArtistChart(String tag, String apiKey) {
		return getWeeklyArtistChart(tag, null, null, -1, apiKey);
	}

	public static Chart<Artist> getWeeklyArtistChart(String tag, int limit, String apiKey) {
		return getWeeklyArtistChart(tag, null, null, limit, apiKey);
	}

	public static Chart<Artist> getWeeklyArtistChart(String tag, String from, String to, int limit, String apiKey) {
		return Chart.getChart("tag.getWeeklyArtistChart", "tag", tag, "artist", from, to, limit, apiKey);
	}

	public static LinkedHashMap<String, String> getWeeklyChartList(String tag, String apiKey) {
		return Chart.getWeeklyChartList("tag", tag, apiKey);
	}

	public static Collection<Chart> getWeeklyChartListAsCharts(String tag, String apiKey) {
		return Chart.getWeeklyChartListAsCharts("tag", tag, apiKey);
	}
}
