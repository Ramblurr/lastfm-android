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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.util.Log;

import fm.last.api.Album;
import fm.last.api.Artist;
import fm.last.api.Event;
import fm.last.api.Friends;
import fm.last.api.Geo;
import fm.last.api.LastFmServer;
import fm.last.api.MD5;
import fm.last.api.Metro;
import fm.last.api.RadioPlayList;
import fm.last.api.Session;
import fm.last.api.SessionInfo;
import fm.last.api.Station;
import fm.last.api.Tag;
import fm.last.api.Tasteometer;
import fm.last.api.Track;
import fm.last.api.User;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;
import fm.last.xml.XMLBuilder;

/**
 * An implementation of LastFmServer
 * 
 * @author Mike Jennings
 * @author Casey Link
 */
final class LastFmServerImpl implements LastFmServer {
	private String api_key;
	private String shared_secret;
	private String baseUrl;

	private class Parser<T> {
		@SuppressWarnings("unchecked")
		public T getItem(String baseUrl, Map<String, String> params, String nodeName, XMLBuilder<?> builder) throws IOException, WSError {
			String response = UrlUtil.doGet(baseUrl, params);
			Document responseXML = null;
			try {
				responseXML = XMLUtil.stringToDocument(response);
			} catch (SAXException e) {
				throw new IOException(e.getMessage());
			}

			Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
			String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
			if (!status.contains("ok")) {
				Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
				if (errorNode != null) {
					WSErrorBuilder eb = new WSErrorBuilder();
					throw eb.build(params.get("method"), errorNode);
				}
				return null;
			} else {
				Node itemNode = XMLUtil.findNamedElementNode(lfmNode, nodeName);

				return (T)builder.build(itemNode);
			}
		}

		@SuppressWarnings("unchecked")
		public List<T> getList(String baseUrl, Map<String, String> params, String nodeName, String elementName, XMLBuilder<?> builder) throws IOException, WSError {
			String response = UrlUtil.doGet(baseUrl, params);

			Document responseXML = null;
			try {
				responseXML = XMLUtil.stringToDocument(response);
			} catch (SAXException e) {
				throw new IOException(e.getMessage());
			}

			Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
			String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
			if (!status.contains("ok")) {
				Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
				if (errorNode != null) {
					WSErrorBuilder eb = new WSErrorBuilder();
					throw eb.build(params.get("method"), errorNode);
				}
				return null;
			} else {
				Node baseNode = XMLUtil.findNamedElementNode(lfmNode, nodeName);

				List<Node> elementNodes = XMLUtil.findNamedElementNodes(baseNode, elementName);
				ArrayList<T> items = new ArrayList<T>();
				for (Node itemNode : elementNodes) {
					items.add((T)builder.build(itemNode));
				}

				return items;
			}
		}
	}

	public void post(String baseUrl, Map<String, String> params) throws IOException, WSError {
		String response = UrlUtil.doPost(baseUrl, params);

		Document responseXML = null;
		try {
			responseXML = XMLUtil.stringToDocument(response);
		} catch (SAXException e) {
			Log.e("Last.fm", "Bad XML: " + response);
			throw new IOException(e.getMessage());
		}

		Node lfmNode = XMLUtil.findNamedElementNode(responseXML, "lfm");
		String status = lfmNode.getAttributes().getNamedItem("status").getNodeValue();
		if (!status.contains("ok")) {
			Node errorNode = XMLUtil.findNamedElementNode(lfmNode, "error");
			if (errorNode != null) {
				WSErrorBuilder eb = new WSErrorBuilder();
				throw eb.build(params.get("method"), errorNode);
			}
		}
	}

	LastFmServerImpl(String baseUrl, String api_key, String shared_secret) {
		this.baseUrl = baseUrl;
		this.api_key = api_key;
		this.shared_secret = shared_secret;
	}

	private Map<String, String> createParams(String method) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("method", method);
		params.put("api_key", api_key);
		return params;
	}

	/**
	 * "sign" parameters in the way that last.fm expects.
	 * 
	 * See: http://www.last.fm/api/authspec#8
	 * 
	 * @param params
	 */
	private void signParams(Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		SortedSet<String> keySet = new TreeSet<String>(params.keySet());
		for (String key : keySet) {
			sb.append(key).append(params.get(key));
		}
		sb.append(shared_secret);
		String signature = sb.toString();
		String api_sig = MD5.getInstance().hash(signature);
		// now we pad to 32 chars if we need to:
		while (32 - api_sig.length() > 0)
			api_sig = "0" + api_sig;

		params.put("api_sig", api_sig);
	}

	/**
	 * See: http://www.last.fm/api/show?service=119
	 * 
	 * @param artist
	 * @return
	 * @throws IOException
	 */
	public Artist[] getSimilarArtists(String artist, String limit) throws IOException, WSError {
		Map<String, String> params = createParams("artist.getsimilar");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (limit != null) {
			params.put("limit", limit);
		}
		List<Artist> artists = new Parser<Artist>().getList(baseUrl, params, "similarartists", "artist", new ArtistBuilder());
		return artists.toArray(new Artist[artists.size()]);
	}

	public Artist[] searchForArtist(String artist) throws IOException, WSError {
		Map<String, String> params = createParams("artist.search");
		if (artist != null) {
			params.put("artist", artist);
		}
		return SearchFunctions.searchForArtist(baseUrl, params);
	}

	public Tag[] searchForTag(String tag) throws IOException, WSError {
		Map<String, String> params = createParams("tag.search");
		if (tag != null) {
			params.put("tag", tag);
		}
		return SearchFunctions.searchForTag(baseUrl, params);
	}

	public Track[] searchForTrack(String track) throws IOException, WSError {
		Map<String, String> params = createParams("track.search");
		if (track != null) {
			params.put("track", track);
		}
		return SearchFunctions.searchForTrack(baseUrl, params);
	}
	
	public Event[] searchForEvent(String event) throws IOException {
		Map<String, String> params = createParams("event.search");
		if (event != null) {
			params.put("event", event);
		}
		return SearchFunctions.searchForEvent(baseUrl, params);
	}

	public Event[] searchForFestival(String event) throws IOException {
		Map<String, String> params = createParams("event.search");
		if (event != null) {
			params.put("event", event);
		}
		params.put("festivalsonly", "1");
		return SearchFunctions.searchForEvent(baseUrl, params);
	}

	public Serializable[] multiSearch(String query) throws IOException, WSError {
		Map<String, String> params = createParams("search.multi");
		if (query != null) {
			params.put("term", query);
		}
		return SearchFunctions.multiSearch(baseUrl, params);
	}

	public Friends getFriends(String user, String recenttracks, String limit) throws IOException, WSError {
		Map<String, String> params = createParams("user.getFriends");
		if (user != null) {
			params.put("user", user);
		}
		if (recenttracks != null) {
			params.put("recenttracks", recenttracks);
		}
		if (limit != null) {
			params.put("limit", limit);
		}
		return FriendFunctions.getFriends(baseUrl, params);
	}

	public Track getTrackInfo(String artist, String track, String mbid) throws IOException, WSError {
		Map<String, String> params = createParams("track.getInfo");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (track != null) {
			params.put("track", track);
		}
		if (mbid != null) {
			params.put("mbid", mbid);
		}
		return new Parser<Track>().getItem(baseUrl, params, "track", new TrackBuilder());
	}

	public Session getMobileSession(String username, String authToken) throws IOException, WSError {
		Map<String, String> params = new HashMap<String, String>();
		if (username != null) {
			params.put("username", username);
		}
		if (authToken != null) {
			params.put("authToken", authToken);
		}
		params.put("method", "auth.getMobileSession");
		params.put("api_key", api_key);
		signParams(params);
		return new Parser<Session>().getItem(baseUrl, params, "session", new SessionBuilder());
	}
	
	public SessionInfo getSessionInfo(String sk) throws IOException, WSError {
		Map<String, String> params = createParams("auth.getSessionInfo");
		if (sk != null) {
			params.put("sk", sk);
			signParams(params);
		}
		return new Parser<SessionInfo>().getItem(baseUrl, params, "application", new SessionInfoBuilder());
	}

	public Station tuneToStation(String station, String sk, String lang) throws IOException, WSError {
		Map<String, String> params = createParams("radio.tune");
		if (station != null) {
			params.put("station", station);
		}
		if (sk != null) {
			params.put("sk", sk);
		}
		if (lang != null) {
			params.put("lang", lang);
		}
		params.put("additional_info", "1");
		signParams(params);
		return RadioFunctions.tuneToStation(baseUrl, params);
	}

	public RadioPlayList getRadioPlayList(String bitrate, String rtp, String discovery, String multiplier, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("radio.getPlaylist");
		if (sk != null) {
			params.put("sk", sk);
		}
		if (bitrate == null)
			bitrate = "128";
		params.put("bitrate", bitrate);
		params.put("speed_multiplier", multiplier);
		params.put("rtp", rtp);
		params.put("discovery", discovery);
		params.put("additional_info", "1");
		signParams(params);
		return RadioFunctions.getRadioPlaylist(baseUrl, params);
	}

	public User getUserInfo(String user, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("user.getInfo");
		if (user != null) {
			params.put("user", user);
		}
		if (sk != null) {
			params.put("sk", sk);
			signParams(params);
		}
		return new Parser<User>().getItem(baseUrl, params, "user", new UserBuilder());
	}

	public Event getEventInfo(String event, String sk) throws IOException {
		Map<String, String> params = createParams("event.getInfo");
		if (event != null) {
			params.put("event", event);
		}
		if (sk != null) {
			params.put("sk", sk);
			signParams(params);
		}
		return new Parser<Event>().getItem(baseUrl, params, "event", new EventBuilder());
	}
	
	public Tag[] getTrackTopTags(String artist, String track, String mbid) throws IOException, WSError {
		Map<String, String> params = createParams("track.getTopTags");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (track != null) {
			params.put("track", track);
		}
		if (mbid != null) {
			params.put("mbid", mbid);
		}
		List<Tag> tags = new Parser<Tag>().getList(baseUrl, params, "toptags", "tag", new TagBuilder());
		return tags.toArray(new Tag[tags.size()]);
	}

	public Tag[] getArtistTopTags(String artist, String mbid) throws IOException, WSError {
		Map<String, String> params = createParams("artist.getTopTags");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (mbid != null) {
			params.put("mbid", mbid);
		}
		List<Tag> tags = new Parser<Tag>().getList(baseUrl, params, "toptags", "tag", new TagBuilder());
		return tags.toArray(new Tag[tags.size()]);
	}

	public Tag[] getUserTopTags(String user, Integer limit) throws IOException, WSError {
		Map<String, String> params = createParams("user.getTopTags");
		if (user != null) {
			params.put("user", user);
		}
		if (limit != null) {
			params.put("limit", limit.toString());
		}
		List<Tag> tags = new Parser<Tag>().getList(baseUrl, params, "toptags", "tag", new TagBuilder());
		return tags.toArray(new Tag[tags.size()]);
	}

	public Tag[] getTrackTags(String artist, String track, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("track.getTags");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (track != null) {
			params.put("track", track);
		}
		if (sk != null) {
			params.put("sk", sk);
		}
		signParams(params);
		List<Tag> tags = new Parser<Tag>().getList(baseUrl, params, "tags", "tag", new TagBuilder());
		return tags.toArray(new Tag[tags.size()]);
	}

	public Tag[] getArtistTags(String artist, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("artist.getTags");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (sk != null) {
			params.put("sk", sk);
		}
		signParams(params);
		List<Tag> tags = new Parser<Tag>().getList(baseUrl, params, "tags", "tag", new TagBuilder());
		return tags.toArray(new Tag[tags.size()]);
	}

	public void addTrackTags(String artist, String track, String[] tag, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("track.addTags");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (track != null) {
			params.put("track", track);
		}
		if (tag != null) {
			params.put("tags", TagFunctions.buildTags(tag));
		}
		if (sk != null) {
			params.put("sk", sk);
		}
		signParams(params);
		post(baseUrl, params);
	}

	public void removeTrackTag(String artist, String track, String tag, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("track.removeTag");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (track != null) {
			params.put("track", track);
		}
		if (tag != null) {
			params.put("tag", tag);
		}
		if (sk != null) {
			params.put("sk", sk);
		}
		signParams(params);
		post(baseUrl, params);
	}

	public void addArtistTags(String artist, String[] tag, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("artist.addTags");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (tag != null) {
			params.put("tags", TagFunctions.buildTags(tag));
		}
		if (sk != null) {
			params.put("sk", sk);
		}
		signParams(params);
		post(baseUrl, params);
	}

	public void removeArtistTag(String artist, String tag, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("artist.removeTag");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (tag != null) {
			params.put("tag", tag);
		}
		if (sk != null) {
			params.put("sk", sk);
		}
		signParams(params);
		post(baseUrl, params);
	}

	public Artist getArtistInfo(String artist, String mbid, String lang, String username) throws IOException, WSError {
		Map<String, String> params = createParams("artist.getInfo");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (mbid != null) {
			params.put("mbid", mbid);
		}
		if (lang != null) {
			params.put("lang", lang);
		}
		if (username != null) {
			params.put("username", username);
		}
		return new Parser<Artist>().getItem(baseUrl, params, "artist", new ArtistBuilder());
	}

	public User[] getTrackTopFans(String track, String artist, String mbid) throws IOException, WSError {
		Map<String, String> params = createParams("track.getTopFans");
		if (track != null) {
			params.put("track", track);
		}
		if (artist != null) {
			params.put("artist", artist);
		}
		if (mbid != null) {
			params.put("mbid", mbid);
		}
		List<User> users = new Parser<User>().getList(baseUrl, params, "topfans", "user", new UserBuilder());
		return users.toArray(new User[users.size()]);
	}

	public User[] getArtistTopFans(String artist, String mbid) throws IOException, WSError {
		Map<String, String> params = createParams("artist.getTopFans");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (mbid != null) {
			params.put("mbid", mbid);
		}
		List<User> users = new Parser<User>().getList(baseUrl, params, "topfans", "user", new UserBuilder());
		return users.toArray(new User[users.size()]);
	}

	public Event[] getArtistEvents(String artist) throws IOException, WSError {
		Map<String, String> params = createParams("artist.getEvents");
		if (artist != null) {
			params.put("artist", artist);
		}
		List<Event> events = new Parser<Event>().getList(baseUrl, params, "events", "event", new EventBuilder());
		return events.toArray(new Event[events.size()]);
	}

	public Event[] getUserEvents(String user) throws IOException, WSError {
		Map<String, String> params = createParams("user.getEvents");
		if (user != null) {
			params.put("user", user);
		}
		List<Event> events = new Parser<Event>().getList(baseUrl, params, "events", "event", new EventBuilder());
		return events.toArray(new Event[events.size()]);
	}

	public Event[] getPastUserEvents(String user) throws IOException, WSError {
		Map<String, String> params = createParams("user.getPastEvents");
		if (user != null) {
			params.put("user", user);
		}
		List<Event> events = new Parser<Event>().getList(baseUrl, params, "events", "event", new EventBuilder());
		return events.toArray(new Event[events.size()]);
	}

	public Event[] getUserFriendsEvents(String user) throws IOException, WSError {
		Map<String, String> params = createParams("user.getFriendsEvents");
		if (user != null) {
			params.put("user", user);
		}
		List<Event> events = new Parser<Event>().getList(baseUrl, params, "events", "event", new EventBuilder());
		return events.toArray(new Event[events.size()]);
	}

	public Event[] getUserRecommendedEvents(String user, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("user.getRecommendedEvents");
		params.put("user", user);
		params.put("sk", sk);
		signParams(params);
		List<Event> events = new Parser<Event>().getList(baseUrl, params, "events", "event", new EventBuilder());
		return events.toArray(new Event[events.size()]);
	}

	public Event[] getNearbyEvents(String latitude, String longitude) throws IOException, WSError {
		Map<String, String> params = createParams("geo.getEvents");
		params.put("lat", latitude);
		params.put("long", longitude);
		params.put("distance", "50");
		List<Event> events = new Parser<Event>().getList(baseUrl, params, "events", "event", new EventBuilder());
		return events.toArray(new Event[events.size()]);
	}
	
	public Event[] getFestivalsForMetro(String metro, int page, String sk) throws IOException {
		Map<String, String> params = createParams("geo.getEvents");
		params.put("location", metro);
		params.put("limit", "50");
		params.put("festivalsonly", "1");
		params.put("page", String.valueOf(page));
		if(sk != null) {
			params.put("sk", sk);
			signParams(params);
		}
		List<Event> events = new Parser<Event>().getList(baseUrl, params, "events", "event", new EventBuilder());
		return events.toArray(new Event[events.size()]);
	}

	public Event[] getUserFestivals(String user) throws IOException {
		Map<String, String> params = createParams("user.getEvents");
		if (user != null) {
			params.put("user", user);
		}
		params.put("festivalsonly", "1");
		List<Event> events = new Parser<Event>().getList(baseUrl, params, "events", "event", new EventBuilder());
		return events.toArray(new Event[events.size()]);
	}
	
	public Event[] getUserFriendsFestivals(String user) throws IOException {
		Map<String, String> params = createParams("user.getFriendsEvents");
		if (user != null) {
			params.put("user", user);
		}
		params.put("festivalsonly", "1");
		List<Event> events = new Parser<Event>().getList(baseUrl, params, "events", "event", new EventBuilder());
		return events.toArray(new Event[events.size()]);
	}

	public Artist[] getRecommendedLineupForEvent(String event, String sk) throws IOException {
		Map<String, String> params = createParams("event.getRecommendedLineup");
		if (event != null) {
			params.put("event", event);
		}
		if (sk != null) {
			params.put("sk", sk);
			signParams(params);
		}
		List<Artist> artists = new Parser<Artist>().getList(baseUrl, params, "artists", "artist", new ArtistBuilder());
		return artists.toArray(new Artist[artists.size()]);
	}
	
	public void attendEvent(String event, String status, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("event.attend");
		params.put("event", event);
		params.put("status", status);
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}

	public Artist[] getUserTopArtists(String user, String period) throws IOException {
		Map<String, String> params = createParams("user.getTopArtists");
		if (user != null) {
			params.put("user", user);
		}
		if (period != null) {
			params.put("period", period);
		}
		List<Artist> artists = new Parser<Artist>().getList(baseUrl, params, "topartists", "artist", new ArtistBuilder());
		return artists.toArray(new Artist[artists.size()]);
	}

	public Artist[] getUserRecommendedArtists(String user, String sk) throws IOException {
		Map<String, String> params = createParams("user.getRecommendedArtists");
		if (user != null) {
			params.put("user", user);
		}
		params.put("sk", sk);
		signParams(params);
		List<Artist> artists = new Parser<Artist>().getList(baseUrl, params, "recommendations", "artist", new ArtistBuilder());
		return artists.toArray(new Artist[artists.size()]);
	}

	public Album[] getUserTopAlbums(String user, String period) throws IOException {
		Map<String, String> params = createParams("user.getTopAlbums");
		if (user != null) {
			params.put("user", user);
		}
		if (period != null) {
			params.put("period", period);
		}
		List<Album> albums = new Parser<Album>().getList(baseUrl, params, "topalbums", "album", new AlbumBuilder());
		return albums.toArray(new Album[albums.size()]);
	}

	public Track[] getUserTopTracks(String user, String period) throws IOException {
		Map<String, String> params = createParams("user.getTopTracks");
		if (user != null) {
			params.put("user", user);
		}
		if (period != null) {
			params.put("period", period);
		}
		List<Track> tracks = new Parser<Track>().getList(baseUrl, params, "toptracks", "track", new TrackBuilder());
		return tracks.toArray(new Track[tracks.size()]);
	}

	public Track[] getUserRecentTracks(String user, String nowPlaying, int limit) throws IOException {
		Map<String, String> params = createParams("user.getRecentTracks");
		if (user != null) {
			params.put("user", user);
		}
		if (nowPlaying != null && nowPlaying.length() > 0)
			params.put("nowPlaying", nowPlaying);
		if (limit > 0) {
			params.put("limit", String.valueOf(limit));
		}
		List<Track> tracks = new Parser<Track>().getList(baseUrl, params, "recenttracks", "track", new TrackBuilder());
		return tracks.toArray(new Track[tracks.size()]);
	}

	public void libraryAddAlbum(String album, String sk) throws IOException {
		Map<String, String> params = createParams("library.addAlbum");
		params.put("album", album);
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}

	public void libraryAddArtist(String artist, String sk) throws IOException {
		Map<String, String> params = createParams("library.addArtist");
		params.put("artist", artist);
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}

	public void libraryAddTrack(String track, String sk) throws IOException {
		Map<String, String> params = createParams("library.addTrack");
		params.put("track", track);
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}

	public Tasteometer tasteometerCompare(String user1, String user2, int limit) throws IOException {
		Map<String, String> params = createParams("tasteometer.compare");
		params.put("type1", "user");
		params.put("type2", "user");
		params.put("value1", user1);
		params.put("value2", user2);
		if (limit > 0)
			params.put("limit", String.valueOf(limit));
		return TasteometerFunctions.compare(baseUrl, params);
	}

	public RadioPlayList[] getUserPlaylists(String username) throws IOException {
		Map<String, String> params = createParams("user.getPlaylists");
		params.put("user", username);
		List<RadioPlayList> playlists = new Parser<RadioPlayList>().getList(baseUrl, params, "playlists", "playlist", new RadioPlayListBuilder());
		return playlists.toArray(new RadioPlayList[playlists.size()]);
	}

	public Album getAlbumInfo(String artist, String album) throws IOException {
		Map<String, String> params = createParams("album.getInfo");
		if (artist != null)
			params.put("artist", artist);
		if (album != null)
			params.put("album", album);
		return new Parser<Album>().getItem(baseUrl, params, "album", new AlbumBuilder());
	}

	public void loveTrack(String artist, String track, String sk) throws IOException {
		Map<String, String> params = createParams("track.love");
		params.put("artist", artist);
		params.put("track", track);
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}

	public void banTrack(String artist, String track, String sk) throws IOException {
		Map<String, String> params = createParams("track.ban");
		params.put("artist", artist);
		params.put("track", track);
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}
	
	public void scrobbleTrack(String artist, String track, String album, long timestamp, int duration, String context, String streamid, String sk) throws IOException {
		Map<String, String> params = createParams("track.scrobble");
		params.put("artist", artist);
		params.put("track", track);
		if (album != null)
			params.put("album", album);
		if (streamid != null && streamid.length() > 0)
			params.put("streamId", streamid);
		params.put("timestamp", String.valueOf(timestamp));
		if (duration > 0)
			params.put("duration", String.valueOf(duration));
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}
	
	public void updateNowPlaying(String artist, String track, String album, int duration, String context, String sk) throws IOException {
		Map<String, String> params = createParams("track.updateNowPlaying");
		params.put("artist", artist);
		params.put("track", track);
		if (album != null)
			params.put("album", album);
		if (duration > 0)
			params.put("duration", String.valueOf(duration));
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}
	
	public void removeNowPlaying(String artist, String track, String album, int duration, String context, String sk) throws IOException {
		Map<String, String> params = createParams("track.removeNowPlaying");
		params.put("artist", artist);
		params.put("track", track);
		if (album != null)
			params.put("album", album);
		if (duration > 0)
			params.put("duration", String.valueOf(duration));
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}
	
	public void shareTrack(String artist, String track, String recipient, String sk) throws IOException {
		Map<String, String> params = createParams("track.share");
		params.put("artist", artist);
		params.put("track", track);
		params.put("recipient", recipient);
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}

	public void shareArtist(String artist, String recipient, String sk) throws IOException {
		Map<String, String> params = createParams("artist.share");
		params.put("artist", artist);
		params.put("recipient", recipient);
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}
	
	public void shareEvent(String event, String recipient, String sk) throws IOException {
		Map<String, String> params = createParams("event.share");
		params.put("event", event);
		params.put("recipient", recipient);
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}

	public void addTrackToPlaylist(String artist, String track, String playlistId, String sk) throws IOException {
		Map<String, String> params = createParams("playlist.addTrack");
		params.put("artist", artist);
		params.put("track", track);
		params.put("playlistID", playlistId);
		params.put("sk", sk);
		signParams(params);
		post(baseUrl, params);
	}

	public RadioPlayList[] createPlaylist(String title, String description, String sk) throws IOException {
		Map<String, String> params = createParams("playlist.create");
		params.put("title", title);
		params.put("description", description);
		params.put("sk", sk);
		signParams(params);
		List<RadioPlayList> playlists = new Parser<RadioPlayList>().getList(baseUrl, params, "playlists", "playlist", new RadioPlayListBuilder());
		return playlists.toArray(new RadioPlayList[playlists.size()]);
	}

	public Station[] getUserRecentStations(String user, String sk) throws IOException {
		Map<String, String> params = createParams("user.getRecentStations");
		params.put("user", user);
		params.put("sk", sk);
		signParams(params);
		List<Station> stations = new Parser<Station>().getList(baseUrl, params, "recentstations", "station", new StationBuilder());
		return stations.toArray(new Station[stations.size()]);
	}

	public Station searchForStation(String station) throws IOException {
		Map<String, String> params = createParams("radio.search");
		params.put("name", station);
		return RadioFunctions.searchForStation(baseUrl, params);
	}
	
	public Artist[] topArtistsForTag(String tag) throws IOException {
		Map<String, String> params = createParams("tag.getTopArtists");
		params.put("tag", tag);
		List<Artist> artists = new Parser<Artist>().getList(baseUrl, params, "topartists", "artist", new ArtistBuilder());
		return artists.toArray(new Artist[artists.size()]);
	}

	public void signUp(String username, String password, String email) throws IOException {
		Map<String, String> params = createParams("user.signUp");
		params.put("username", username);
		params.put("password", password);
		params.put("email", email);
		signParams(params);
		post(baseUrl, params);
	}

	public Geo getGeo() throws IOException {
		Map<String, String> params = createParams("bespoke.getGeo");
		return new Parser<Geo>().getItem(baseUrl, params, "geo", new GeoBuilder());
	}

	public List<Metro> getMetros() throws IOException {
		Map<String, String> params = createParams("geo.getMetros");
		return new Parser<Metro>().getList(baseUrl, params, "metros", "metro", new MetroBuilder());
	}
}