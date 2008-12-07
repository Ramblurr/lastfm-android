package net.roarsoftware.lastfm;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

import org.xmlpull.v1.XmlPullParser;

import android.os.Parcel;
import android.os.Parcelable;

import net.roarsoftware.xml.DomElement;

/**
 * Bean for music playlists. Contains the {@link #fetch(String, String) fetch}
 * method and various <code>fetchXXX</code> methods to retrieve playlists from
 * the server. Playlists are identified by lastfm:// playlist urls. Valid urls
 * include:
 * <ul>
 * <li><b>Album Playlists:</b> lastfm://playlist/album/{@literal <album_id>}</li>
 * <li><b>User Playlists:</b> lastfm://playlist/{@literal <playlist_id>}</li>
 * <li><b>Tag Playlists:</b> lastfm://playlist/tag/{@literal <tag_name>}
 * /freetracks</li>
 * </ul>
 * See <a
 * href="http://www.last.fm/api/playlists">http://www.last.fm/api/playlists</a>
 * for more information about playlists.
 * 
 * @author Janni Kovacs
 * @author Casey Link <unnamedrambler@gmail.com>
 */
public class Playlist implements Parcelable {

	private int id;
	private String title;
	private String annotation;
	private int size;
	private String creator;

	private Collection<Track> tracks = new ArrayList<Track>();

	private Playlist() {
	}

	public String getCreator() {
		return creator;
	}

	public int getId() {
		return id;
	}

	public int getSize() {
		return size;
	}

	public String getTitle() {
		return title;
	}

	public String getAnnotation() {
		return annotation;
	}

	public Collection<Track> getTracks() {
		return tracks;
	}
	
	public ArrayBlockingQueue<Track> getTracksQueue() {
		return new ArrayBlockingQueue<Track>(5, true, tracks);
	}

	/**
	 * Fetches an album playlist, which contains the tracks of the specified
	 * album.
	 * 
	 * @param albumId
	 *            The album id as returned in
	 *            {@link Album#getInfo(String, String, String) Album.getInfo}.
	 * @param apiKey
	 *            A Last.fm API key.
	 * @return a playlist
	 */
	public static Playlist fetchAlbumPlaylist(String albumId, String apiKey) {
		return fetch("lastfm://playlist/album/" + albumId, apiKey);
	}

	/**
	 * Fetches a user-created playlist.
	 * 
	 * @param playlistId
	 *            A playlist id.
	 * @param apiKey
	 *            A Last.fm API key.
	 * @return a playlist
	 */
	public static Playlist fetchUserPlaylist(int playlistId, String apiKey) {
		return fetch("lastfm://playlist/" + playlistId, apiKey);
	}

	/**
	 * Fetches a playlist of freetracks for a given tag name.
	 * 
	 * @param tag
	 *            A tag name.
	 * @param apiKey
	 *            A Last.fm API key.
	 * @return a playlist
	 */
	public static Playlist fetchTagPlaylist(String tag, String apiKey) {
		return fetch("lastfm://playlist/tag/" + tag + "/freetracks", apiKey);
	}

	/**
	 * Fetches a playlist using a lastfm playlist url. See the class description
	 * for a list of valid playlist urls.
	 * 
	 * @param playlistUrl
	 *            A valid playlist url.
	 * @param apiKey
	 *            A Last.fm API key.
	 * @return a playlist
	 */
	public static Playlist fetch(String playlistUrl, String apiKey) {
		Result result = Caller.getInstance().call("playlist.fetch", apiKey,
				"playlistURL", playlistUrl);
		return playlistFromElement(result.getContentElement());
	}

	/**
	 * Add a track to a Last.fm user's playlist.
	 * 
	 * @param playlistId
	 *            The ID of the playlist - this is available in
	 *            user.getPlaylists
	 * @param artist
	 *            The artist name that corresponds to the track to be added.
	 * @param track
	 *            The track name to add to the playlist.
	 * @param session
	 *            A Session instance.
	 * @return the result of the operation
	 */
	public static Result addTrack(int playlistId, String artist, String track,
			Session session) {
		return Caller.getInstance().call("playlist.addTrack", session,
				"playlistID", String.valueOf(playlistId), "artist", artist,
				"track", track);
	}

	/**
	 * Creates a playlist from a <playlist> result
	 * 
	 * @pre the parser's current tag is "playlist"
	 * @param xpp
	 *            the xmlpullparser 
	 * @param forRadio
	 *            is the playlist a Radio playlist (part of undocumented API)
	 * @return playlist info, empty if something went wrong
	 */
	static Playlist playlistFromXPP(XmlPullParser xpp, boolean forRadio) {
		Playlist p = new Playlist();
		try {
			int event = xpp.nextTag();
			boolean loop = true;
			while (loop) {
				String n = xpp.getName();
				switch (event) {
				case XmlPullParser.START_TAG:
					if (n.equals("title"))
						p.title = xpp.nextText();
					else if (n.equals("creator"))
						p.creator = xpp.nextText();
					else if (n.equals("annotation"))
						p.annotation = xpp.nextText();
					else if (n.equals("track"))
						p.tracks.add(forRadio ? Track
								.trackFromRadioXPP(xpp) : Track
								.trackFromXPP(xpp));
					break;
				case XmlPullParser.END_TAG:
					if (n.equals("trackList"))
						loop = false;
					break;
				default:
					break;
				}
				event = xpp.next();
			}
		} catch (Exception e) {
		}
		return p;
	}

	static Playlist playlistFromElement(DomElement e) {
		if (e == null)
			return null;
		Playlist p = new Playlist();
		if (e.hasChild("id"))
			p.id = Integer.parseInt(e.getChildText("id"));
		p.title = e.getChildText("title");
		if (e.hasChild("size"))
			p.size = Integer.parseInt(e.getChildText("size"));
		p.creator = e.getChildText("creator");
		p.annotation = e.getChildText("annotation");
		DomElement tl = e.getChild("trackList");
		if (tl != null) {
			for (DomElement te : tl.getChildren("track")) {
				Track t = new Track(te.getChildText("title"), te
						.getChildText("identifier"), te.getChildText("creator"));
				t.album = te.getChildText("album");
				t.duration = Integer.parseInt(te.getChildText("duration")) / 1000;
				t.imageUrls.put(ImageSize.LARGE, te.getChildText("image"));
				t.location = te.getChildText("location");
				p.tracks.add(t);
			}
			if (p.size == 0)
				p.size = p.tracks.size();
		}
		return p;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(size);
		dest.writeInt(id);
		dest.writeString(creator);
		dest.writeString(annotation);
		dest.writeString(title);
		dest.writeParcelableArray((Track[]) tracks.toArray(), 0);
		
	}
	
	public static final Parcelable.Creator<Playlist> CREATOR = new Parcelable.Creator<Playlist>() {
		public Playlist createFromParcel(Parcel in) {
			return new Playlist(in);
		}

		public Playlist[] newArray(int size) {
			return new Playlist[size];
		}	
	};
	
	private Playlist(Parcel in) {
		size = in.readInt();
		id = in.readInt();
		creator = in.readString();
		annotation = in.readString();
		title = in.readString();
		tracks = Arrays.asList((Track[]) in.readParcelableArray(Track.class.getClassLoader()));
	}
}
