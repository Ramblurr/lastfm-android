// Copyright 2008 Google Inc. All Rights Reserved.

package fm.last.api.impl;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import fm.last.api.*;

/**
 * An implementation of LastFmServer
 *
 * @author Mike Jennings
 */
final class LastFmServerImpl implements LastFmServer {
	private String api_key;
	private String api_sig;
	private String baseUrl;

	LastFmServerImpl(String baseUrl, String api_key, String api_sig) {
		this.baseUrl = baseUrl;
		this.api_key = api_key;
		this.api_sig = api_sig;
	}

	private Map<String, String> createParams(String method) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("method", method);
		params.put("api_key", api_key);
		return params;    
	}

	private Map<String, String> createParamsWithSig(String method) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("method", method);
		params.put("api_key", api_key);
		params.put("api_sig", api_sig);
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
		sb.append(api_sig);
		String signature = sb.toString();
		String api_sig = MD5.getInstance().hash(signature);
		// now we pad to 32 chars if we need to:
		while( 32 - api_sig.length() > 0 )
			api_sig = "0" + api_sig;

		params.put("api_sig", api_sig);
	}


	/**
	 * See: http://www.last.fm/api/show?service=119
	 * @param artist
	 * @return
	 * @throws IOException
	 */
	public Artist[] getSimilarArtists(String artist, String limit) throws IOException {
		Map<String, String> params = createParams("artist.getsimilar");
		if (artist != null) {
			params.put("artist", artist);
		}
		if (limit != null) {
			params.put("limit", limit);
		}
		return ArtistFunctions.getSimilarArtists(baseUrl, params);
	}

	public Artist[] searchForArtist(String artist) throws IOException {
		Map<String, String> params = createParams("artist.search");
		if (artist != null) {
			params.put("artist", artist);
		}
		return ArtistFunctions.searchForArtist(baseUrl, params);
	}

	public Tag[] searchForTag(String tag) throws IOException {
		Map<String, String> params = createParams("tag.search");
		if (tag != null) {
			params.put("tag", tag);
		}
		return TagFunctions.searchForTag(baseUrl, params);
	}

	public Friends getFriends(String user, String recenttracks, String limit) throws IOException {
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

	public Track getTrackInfo(String artist, String track, String mbid) throws IOException {
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

	public Session getMobileSession(String username, String authToken) throws IOException {
		Map<String, String> params = createParamsWithSig("auth.getMobileSession");
		if (username != null) {
			params.put("username", username);
		}
		if (authToken != null) {
			params.put("authToken", authToken);
		}
		return AuthFunctions.getMobileSession(baseUrl, params);
	}

	public Station tuneToStation(String station, String sk) throws IOException {
		Map<String, String> params = createParams("radio.tune");
		if (station != null) {
			params.put("station", station);
		}
		if (sk != null) {
			params.put("sk", sk);
		}
		signParams(params);
		return RadioFunctions.tuneToStation(baseUrl, params);
	}

	public RadioPlayList getRadioPlayList(String sk) throws IOException {
		Map<String, String> params = createParams("radio.getPlaylist");
		if (sk != null) {
			params.put("sk", sk);
		}
		signParams(params);
		return RadioFunctions.getRadioPlaylist(baseUrl, params);
	}

	public User getUserInfo(String sk) throws IOException {
		Map<String, String> params = createParams("user.getInfo");
		if (sk != null) {
			params.put("sk", sk);
		}
		signParams(params);
		return UserFunctions.getUserInfo(baseUrl, params);
	}

	public Tag[] getTrackTopTags(String artist, String track, String mbid)
	throws IOException {
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

	public Tag[] getUserTopTags(String user, Integer limit) throws IOException {
		Map<String, String> params = createParams("user.getTopTags");
		if (user != null) {
			params.put("user", user);
		}
		if (limit != null) {
			params.put("limit", limit.toString());
		}
		return UserFunctions.getUserTopTags(baseUrl, params);
	}

	public Tag[] getTrackTags(String artist, String track, String sk)
	throws IOException {
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

	public void addTrackTags(String artist, String track, String[] tag,
			String sk) throws IOException {
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

	public void removeTrackTag(String artist, String track, String tag,
			String sk) throws IOException {
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

	@Override
	public Artist getArtistInfo(String artist, String mbid, String lang)
	throws IOException {
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
		return ArtistFunctions.getArtistInfo(baseUrl, params);
	}

	@Override
	public User[] getTrackTopFans(String track, String artist, String mbid)
	throws IOException {
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

	@Override
	public Event[] getArtistEvents(String artist) throws IOException {
		Map<String, String> params = createParams("artist.getEvents");
		if (artist != null) {
			params.put("artist", artist);
		}
		return ArtistFunctions.getArtistEvents(baseUrl, params);
	}
}