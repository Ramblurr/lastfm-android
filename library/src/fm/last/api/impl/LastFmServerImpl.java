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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import fm.last.api.Album;
import fm.last.api.Artist;
import fm.last.api.AudioscrobblerService;
import fm.last.api.Event;
import fm.last.api.Friends;
import fm.last.api.LastFmServer;
import fm.last.api.MD5;
import fm.last.api.RadioPlayList;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.api.Tag;
import fm.last.api.Tasteometer;
import fm.last.api.Track;
import fm.last.api.User;
import fm.last.api.WSError;

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
		return ArtistFunctions.getSimilarArtists(baseUrl, params);
	}

	public Artist[] searchForArtist(String artist) throws IOException, WSError {
		Map<String, String> params = createParams("artist.search");
		if (artist != null) {
			params.put("artist", artist);
		}
		return ArtistFunctions.searchForArtist(baseUrl, params);
	}

	public Tag[] searchForTag(String tag) throws IOException, WSError {
		Map<String, String> params = createParams("tag.search");
		if (tag != null) {
			params.put("tag", tag);
		}
		return TagFunctions.searchForTag(baseUrl, params);
	}

	public Track[] searchForTrack(String track) throws IOException, WSError {
		Map<String, String> params = createParams("track.search");
		if (track != null) {
			params.put("track", track);
		}
		return TrackFunctions.searchForTrack(baseUrl, params);
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
		return TrackFunctions.getTrackInfo(baseUrl, params);
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
		signParams(params); // apparently unrequired
		return AuthFunctions.getMobileSession(baseUrl, params);
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
		signParams(params);
		return RadioFunctions.tuneToStation(baseUrl, params);
	}

	public RadioPlayList getRadioPlayList(String bitrate, String rtp, String discovery, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("radio.getPlaylist");
		if (sk != null) {
			params.put("sk", sk);
		}
		if (bitrate == null)
			bitrate = "128";
		params.put("bitrate", bitrate);
		params.put("speed_multiplier", "2");
		params.put("rtp", rtp);
		params.put("discovery", discovery);
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
		return UserFunctions.getUserInfo(baseUrl, params);
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
		return TrackFunctions.getTrackTopTags(baseUrl, params);
	}

	public Tag[] getArtistTopTags(String artist, String mbid) throws IOException, WSError {
		Map<String, String> params = createParams("artist.getTopTags");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (mbid != null) {
			params.put("mbid", mbid);
		}
		return TrackFunctions.getTrackTopTags(baseUrl, params);
	}

	public Tag[] getUserTopTags(String user, Integer limit) throws IOException, WSError {
		Map<String, String> params = createParams("user.getTopTags");
		if (user != null) {
			params.put("user", user);
		}
		if (limit != null) {
			params.put("limit", limit.toString());
		}
		return UserFunctions.getUserTopTags(baseUrl, params);
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
		return TrackFunctions.getTrackTags(baseUrl, params);
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
		TrackFunctions.addTrackTags(baseUrl, params);
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
		TrackFunctions.removeTrackTag(baseUrl, params);
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
		return ArtistFunctions.getArtistInfo(baseUrl, params);
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
		return TrackFunctions.getTrackTopFans(baseUrl, params);
	}

	public User[] getArtistTopFans(String artist, String mbid) throws IOException, WSError {
		Map<String, String> params = createParams("artist.getTopFans");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (mbid != null) {
			params.put("mbid", mbid);
		}
		return TrackFunctions.getTrackTopFans(baseUrl, params);
	}

	public Event[] getArtistEvents(String artist) throws IOException, WSError {
		Map<String, String> params = createParams("artist.getEvents");
		if (artist != null) {
			params.put("artist", artist);
		}
		return ArtistFunctions.getArtistEvents(baseUrl, params);
	}

	public Event[] getUserEvents(String user) throws IOException, WSError {
		Map<String, String> params = createParams("user.getEvents");
		if (user != null) {
			params.put("user", user);
		}
		return UserFunctions.getUserEvents(baseUrl, params);
	}

	public Event[] getUserRecommendedEvents(String user, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("user.getRecommendedEvents");
		params.put("user", user);
		params.put("sk", sk);
		signParams(params);
		return UserFunctions.getUserEvents(baseUrl, params);
	}

	public Event[] getNearbyEvents(String latitude, String longitude) throws IOException, WSError {
		Map<String, String> params = createParams("geo.getEvents");
		params.put("lat", latitude);
		params.put("long", longitude);
		params.put("distance", "50");
		return UserFunctions.getUserEvents(baseUrl, params);
	}

	public void attendEvent(String event, String status, String sk) throws IOException, WSError {
		Map<String, String> params = createParams("event.attend");
		params.put("event", event);
		params.put("status", status);
		params.put("sk", sk);
		signParams(params);
		UserFunctions.attendEvent(baseUrl, params);
	}

	public Artist[] getUserTopArtists(String user, String period) throws IOException {
		Map<String, String> params = createParams("user.getTopArtists");
		if (user != null) {
			params.put("user", user);
		}
		if (period != null) {
			params.put("period", period);
		}
		return UserFunctions.getUserTopArtists(baseUrl, params);
	}

	public Artist[] getUserRecommendedArtists(String user, String sk) throws IOException {
		Map<String, String> params = createParams("user.getRecommendedArtists");
		if (user != null) {
			params.put("user", user);
		}
		params.put("sk", sk);
		signParams(params);
		return UserFunctions.getUserRecommendedArtists(baseUrl, params);
	}

	public Album[] getUserTopAlbums(String user, String period) throws IOException {
		Map<String, String> params = createParams("user.getTopAlbums");
		if (user != null) {
			params.put("user", user);
		}
		if (period != null) {
			params.put("period", period);
		}
		return UserFunctions.getUserTopAlbums(baseUrl, params);
	}

	public Track[] getUserTopTracks(String user, String period) throws IOException {
		Map<String, String> params = createParams("user.getTopTracks");
		if (user != null) {
			params.put("user", user);
		}
		if (period != null) {
			params.put("period", period);
		}
		return UserFunctions.getUserTopTracks(baseUrl, params);
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
		return UserFunctions.getUserRecentTracks(baseUrl, params);
	}

	public void libraryAddAlbum(String album, String sk) throws IOException {
		Map<String, String> params = createParams("library.addAlbum");
		params.put("album", album);
		params.put("sk", sk);
		signParams(params);
		LibraryFunctions.addAlbum(baseUrl, params);
	}

	public void libraryAddArtist(String artist, String sk) throws IOException {
		Map<String, String> params = createParams("library.addArtist");
		params.put("artist", artist);
		params.put("sk", sk);
		signParams(params);
		LibraryFunctions.addArtist(baseUrl, params);

	}

	public void libraryAddTrack(String track, String sk) throws IOException {
		Map<String, String> params = createParams("library.addTrack");
		params.put("track", track);
		params.put("sk", sk);
		signParams(params);
		LibraryFunctions.addTrack(baseUrl, params);
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
		return UserFunctions.getUserPlaylists(baseUrl, params);
	}

	public Album getAlbumInfo(String artist, String album) throws IOException {
		Map<String, String> params = createParams("album.getInfo");
		if (artist != null)
			params.put("artist", artist);
		if (album != null)
			params.put("album", album);
		return AlbumFunctions.getAlbumInfo(baseUrl, params);
	}

	public AudioscrobblerService createAudioscrobbler(Session session, String clientVersion) {
		return new AudioscrobblerService(session, api_key, shared_secret, clientVersion);
	}

	public void loveTrack(String artist, String track, String sk) throws IOException {
		Map<String, String> params = createParams("track.love");
		params.put("artist", artist);
		params.put("track", track);
		params.put("sk", sk);
		signParams(params);
		TrackFunctions.loveTrack(baseUrl, params);
	}

	public void banTrack(String artist, String track, String sk) throws IOException {
		Map<String, String> params = createParams("track.ban");
		params.put("artist", artist);
		params.put("track", track);
		params.put("sk", sk);
		signParams(params);
		TrackFunctions.banTrack(baseUrl, params);
	}

	public void shareTrack(String artist, String track, String recipient, String sk) throws IOException {
		Map<String, String> params = createParams("track.share");
		params.put("artist", artist);
		params.put("track", track);
		params.put("recipient", recipient);
		params.put("sk", sk);
		signParams(params);
		TrackFunctions.shareTrack(baseUrl, params);
	}

	public void shareArtist(String artist, String recipient, String sk) throws IOException {
		Map<String, String> params = createParams("artist.share");
		params.put("artist", artist);
		params.put("recipient", recipient);
		params.put("sk", sk);
		signParams(params);
		TrackFunctions.shareTrack(baseUrl, params);
	}

	public void addTrackToPlaylist(String artist, String track, String playlistId, String sk) throws IOException {
		Map<String, String> params = createParams("playlist.addTrack");
		params.put("artist", artist);
		params.put("track", track);
		params.put("playlistID", playlistId);
		params.put("sk", sk);
		signParams(params);
		TrackFunctions.shareTrack(baseUrl, params);
	}

	public RadioPlayList[] createPlaylist(String title, String description, String sk) throws IOException {
		Map<String, String> params = createParams("playlist.create");
		params.put("title", title);
		params.put("description", description);
		params.put("sk", sk);
		signParams(params);
		// This returns the same XML response as user.getPlaylists
		return UserFunctions.getUserPlaylists(baseUrl, params);
	}

	public Station[] getUserRecentStations(String user, String sk) throws IOException {
		Map<String, String> params = createParams("user.getRecentStations");
		params.put("user", user);
		params.put("sk", sk);
		signParams(params);
		// This returns the same XML response as user.getPlaylists
		return UserFunctions.getUserRecentStations(baseUrl, params);
	}

	public Station searchForStation(String station) throws IOException {
		Map<String, String> params = createParams("radio.search");
		params.put("name", station);
		return RadioFunctions.searchForStation(baseUrl, params);
	}
	
	public Artist[] topArtistsForTag(String tag) throws IOException {
		Map<String, String> params = createParams("tag.getTopArtists");
		params.put("tag", tag);

		return TagFunctions.topArtistsForTag(baseUrl, params);
	}

	public void signUp(String username, String password, String email) throws IOException {
		Map<String, String> params = createParams("user.signUp");
		params.put("username", username);
		params.put("password", password);
		params.put("email", email);
		signParams(params);
		UserFunctions.signUp(baseUrl, params);
	}

}