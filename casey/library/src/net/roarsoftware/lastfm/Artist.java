package net.roarsoftware.lastfm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.ParseConversionEvent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import net.roarsoftware.util.StringUtilities;
import net.roarsoftware.xml.DomElement;

/**
 * Bean that contains artist information.<br/>
 * This class contains static methods that executes API methods relating to
 * artists.<br/>
 * Method names are equivalent to the last.fm API method names.
 * 
 * @author Janni Kovacs
 */
public class Artist extends MusicEntry {

	private Collection<Artist> similar = new ArrayList<Artist>();

	private float similarityMatch;

	protected Artist(String name, String url) {
		super(name, url);
	}

	protected Artist(String name, String url, String mbid, int playcount,
			int listeners, boolean streamable) {
		super(name, url, mbid, playcount, listeners, streamable);
	}

	public float getSimilarityMatch() {
		return similarityMatch;
	}

	/**
	 * Returns a list of similar <code>Artist</code>s. Note that this method
	 * does not retrieve this list from the server but instead returns the
	 * result of an <code>artist.getInfo</code> call.<br/>
	 * If you need to retrieve similar artists to a specified artist use the
	 * {@link #getSimilar(String, String)} method.
	 * 
	 * @return list of similar artists
	 * @see #getSimilar(String, String)
	 * @see #getSimilar(String, int, String)
	 */
	public Collection<Artist> getSimilar() {
		return similar;
	}

	/**
	 * Retrieves detailed artist info for the given artist or mbid entry.
	 * 
	 * @param artistOrMbid
	 *            Name of the artist or an mbid
	 * @param apiKey
	 *            The API key
	 * @return detailed artist info
	 */
	public static Artist getInfo(String artistOrMbid, String apiKey) {
		Map<String, String> params;
		if (StringUtilities.isMbid(artistOrMbid)) {
			params = Collections.singletonMap("mbid", artistOrMbid);
		} else {
			params = Collections.singletonMap("artist", artistOrMbid);
		}
		Result result = Caller.getInstance().call("artist.getInfo", apiKey,
				params);
		if (!result.isSuccessful())
			return null;
		DomElement artistElement = result.getContentElement();
		return artistFromElement(artistElement);
	}

	/**
	 * Calls {@link #getSimilar(String, int, String)} with the default limit of
	 * 100.
	 * 
	 * @param artist
	 *            Artist's name
	 * @param apiKey
	 *            The API key
	 * @return similar artists
	 * @see #getSimilar(String, int, String)
	 */
	public static Collection<Artist> getSimilar(String artist, String apiKey) {
		return getSimilar(artist, 100, apiKey);
	}

	/**
	 * Returns <code>limit</code> similar artists to the given one.
	 * 
	 * @param artist
	 *            Artist's name
	 * @param limit
	 *            Number of maximum results
	 * @param apiKey
	 *            The API key
	 * @return similar artists
	 */
	public static Collection<Artist> getSimilar(String artist, int limit,
			String apiKey) {
		Result result = Caller.getInstance().call("artist.getSimilar", apiKey,
				"artist", artist, "limit", String.valueOf(limit));
		if (!result.isSuccessful())
			return Collections.emptyList();
		DomElement element = result.getContentElement();
		List<Artist> artists = new ArrayList<Artist>();
		for (DomElement e : element.getChildren("artist")) {
			artists.add(artistFromElement(e));
		}
		return artists;
	}

	/**
	 * Searches for an artist and returns a <code>Collection</code> of possible
	 * matches.
	 * 
	 * @param name
	 *            The artist name to look up
	 * @param apiKey
	 *            The API key
	 * @return a list of possible matches
	 */
	public static Collection<Artist> search(String name, String apiKey) {
		try {
			Result result = Caller.getInstance().call("artist.search", apiKey,
					"artist", name);
			result.getParser().nextTag();
			if (!result.getParser().getName().equals("results"))
				return null;
			int event = result.getParser().nextTag();
			List<Artist> list = new ArrayList<Artist>();
			boolean loop = true;
			while (loop) {
				String n = result.getParser().getName();
				switch (event) {
				case XmlPullParser.START_TAG:
					String a = result.getParser().getNamespace();
					if (n.equals("totalResults")) {
						int totalresults = 	Integer.parseInt(result.getParser().nextText());
					} else if (n.equals("artist")) {
						list.add( artistFromSearch(result.getParser()) );
					}
					break;
				case XmlPullParser.END_TAG:
					if(n.equals("artistmatches"))
						loop = false;
					break;
				case XmlPullParser.TEXT:
				default: 
					break;
				}
				event = result.getParser().next();
			}
			return list;
		} catch (Exception e) {
			System.out.println("Search failed:" + e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list of the given artist's top albums.
	 * 
	 * @param artist
	 *            Artist's name
	 * @param apiKey
	 *            The API key
	 * @return list of top albums
	 */
	public static Collection<Album> getTopAlbums(String artist, String apiKey) {
		Result result = Caller.getInstance().call("artist.getTopAlbums",
				apiKey, "artist", artist);
		if (!result.isSuccessful())
			return Collections.emptyList();
		DomElement element = result.getContentElement();
		String artistName = element.getAttribute("artist"); // may be different
															// from user input
		List<Album> albums = new ArrayList<Album>();
		for (DomElement e : element.getChildren("album")) {
			albums.add(Album.albumFromElement(e, artistName));
		}
		return albums;
	}

	/**
	 * Retrieves a list of the top fans of the given artist.
	 * 
	 * @param artist
	 *            Artist's name
	 * @param apiKey
	 *            The API key
	 * @return list of top fans
	 */
	public static Collection<User> getTopFans(String artist, String apiKey) {
		Result result = Caller.getInstance().call("artist.getTopFans", apiKey,
				"artist", artist);
		if (!result.isSuccessful())
			return Collections.emptyList();
		DomElement element = result.getContentElement();
		List<User> users = new ArrayList<User>();
		for (DomElement domElement : element.getChildren("user")) {
			//users.add(User.userFromElement(domElement));
		}
		return users;
	}

	/**
	 * Retrieves the top tags for the given artist.
	 * 
	 * @param artist
	 *            Artist's name
	 * @param apiKey
	 *            The API key
	 * @return list of top tags
	 */
	public static Collection<String> getTopTags(String artist, String apiKey) {
		Result result = Caller.getInstance().call("artist.getTopTags", apiKey,
				"artist", artist);
		if (!result.isSuccessful())
			return Collections.emptyList();
		DomElement element = result.getContentElement();
		List<String> tags = new ArrayList<String>();
		for (DomElement domElement : element.getChildren("tag")) {
			tags.add(domElement.getChildText("name"));
		}
		return tags;
	}

	/**
	 * Get the top tracks by an artist on Last.fm, ordered by popularity
	 * 
	 * @param artist
	 *            The artist name in question
	 * @param apiKey
	 *            A Last.fm API key.
	 * @return list of top tracks
	 */
	public static Collection<Track> getTopTracks(String artist, String apiKey) {
		Result result = Caller.getInstance().call("artist.getTopTracks",
				apiKey, "artist", artist);
		if (!result.isSuccessful())
			return Collections.emptyList();
		DomElement element = result.getContentElement();
		String artistName = element.getAttribute("artist"); // may be different
															// from user input
		List<Track> tracks = new ArrayList<Track>();
		for (DomElement e : element.getChildren("track")) {
			tracks.add(Track.trackFromElement(e, artistName));
		}
		return tracks;
	}

	/**
	 * Tag an artist with one or more user supplied tags.
	 * 
	 * @param artist
	 *            The artist name in question.
	 * @param tags
	 *            A comma delimited list of user supplied tags to apply to this
	 *            artist. Accepts a maximum of 10 tags.
	 * @param session
	 *            A Session instance
	 * @return the result of the operation
	 */
	public static Result addTags(String artist, String tags, Session session) {
		return Caller.getInstance().call("artist.addTags", session, "artist",
				artist, "tags", tags);
	}

	/**
	 * Remove a user's tag from an artist.
	 * 
	 * @param artist
	 *            The artist name in question.
	 * @param tag
	 *            A single user tag to remove from this artist.
	 * @param session
	 *            A Session instance
	 * @return the result of the operation
	 */
	public static Result removeTag(String artist, String tag, Session session) {
		return Caller.getInstance().call("artist.removeTag", session, "artist",
				artist, "tag", tag);
	}

	/**
	 * Get the tags applied by an individual user to an artist on Last.fm.
	 * 
	 * @param artist
	 *            The artist name in question
	 * @param session
	 *            A Session instance
	 * @return a list of tags
	 */
	public static Collection<String> getTags(String artist, Session session) {
		Result result = Caller.getInstance().call("artist.getTags", session,
				"artist", artist);
		if (!result.isSuccessful())
			return Collections.emptyList();
		DomElement element = result.getContentElement();
		Collection<String> tags = new ArrayList<String>();
		for (DomElement domElement : element.getChildren("tag")) {
			tags.add(domElement.getChildText("name"));
		}
		return tags;
	}

	/**
	 * Creates artist info out of a DomElement.
	 * 
	 * @param element
	 *            An XML DomElement
	 * @return artist info
	 */
	static Artist artistFromElement(DomElement element) {
		Artist artist = new Artist(null, null);
		MusicEntry.loadStandardInfo(artist, element);
		// match for similar artists response
		if (element.hasChild("match")) {
			artist.similarityMatch = Float.parseFloat(element
					.getChildText("match"));
		}
		// similar artists
		DomElement similar = element.getChild("similar");
		if (similar != null) {
			Collection<DomElement> children = similar.getChildren("artist");
			for (DomElement child : children) {
				artist.similar.add(artistFromElement(child));
			}
		}
		return artist;
	}
	
	/**
	 * Creates artist info out of an XmlPullParser
	 * 
	 * @pre the parser's current tag is "artist"
	 * @param XmlPullParser
	 *            An XmlPullParser 
	 * @return artist info
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	static Artist artistFromSearch(XmlPullParser xpp) throws XmlPullParserException, IOException {
		Artist artist = new Artist(null, null);
		//MusicEntry.loadStandardInfo(artist, element);
		// match for similar artists response
		int event = xpp.nextTag();
		boolean loop = true;
		while(loop)
		{
			String n = xpp.getName();
			switch(event)
			{
			case XmlPullParser.START_TAG:
				if(n.equals("name")) {
					artist.name = xpp.nextText();
				} else if(n.equals("mbid")) {
					artist.mbid = xpp.nextText();
				} else if(n.equals("url")) {
					artist.url = xpp.nextText();
				} else if(n.equals("image")) {
					//TODO
				} else if(n.equals("streamable")) {
					artist.streamable = xpp.nextText().equals("1");
				}
			case XmlPullParser.END_TAG:
				if(n.equals("artist"))
					loop = false;
				break;
			case XmlPullParser.TEXT:
			default:
				break;
			}
			event = xpp.next();
		}
		return artist;
	}

}
