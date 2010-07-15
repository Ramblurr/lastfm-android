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
package fm.last.api;

import java.io.IOException;

public interface LastFmServer {
	/**
	 * See http://www.last.fm/api/show?service=119
	 * 
	 * @param artist
	 * @param limit
	 * @return
	 * @throws IOException
	 */
	public Artist[] getSimilarArtists(String artist, String limit) throws IOException;

	public Artist[] searchForArtist(String artist) throws IOException;

	public Artist[] topArtistsForTag(String tag) throws IOException;

	public Tag[] searchForTag(String Tag) throws IOException;

	public Track[] searchForTrack(String track) throws IOException;

	/**
	 * See http://www.last.fm/api/show?service=263
	 * 
	 * @param user
	 * @param recenttracks
	 * @param limit
	 * @return
	 * @throws IOException
	 */
	public Friends getFriends(String user, String recenttracks, String limit) throws IOException;

	/**
	 * See http://www.last.fm/api/show?service=356
	 * 
	 * @param artist
	 * @param track
	 * @param mbid
	 * @return
	 * @throws IOException
	 */
	public Track getTrackInfo(String artist, String track, String mbid) throws IOException;

	/**
	 * See http://www.last.fm/api/show?service=266
	 * 
	 * @param username
	 * @param authToken
	 *            md5(username + md5(password))
	 * @return
	 * @throws IOException
	 */
	public Session getMobileSession(String username, String authToken) throws IOException;

	public void signUp(String username, String password, String email) throws IOException;

	public Station tuneToStation(String station, String sk, String lang) throws IOException;

	public RadioPlayList getRadioPlayList(String bitrate, String rtp, String discovery, String sk) throws IOException;

	public User getUserInfo(String user, String sk) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=289
	 * 
	 * @param artist
	 *            The track name in question
	 * @param track
	 *            The artist name in question
	 * @param mbid
	 *            The musicbrainz id for the track
	 * @return array of tags
	 * @throws IOException
	 */
	public Tag[] getTrackTopTags(String artist, String track, String mbid) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=123
	 * 
	 * @param user
	 * @param limit
	 * @return An array of tags
	 * @throws IOException
	 */
	public Tag[] getUserTopTags(String user, Integer limit) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=300
	 * 
	 * @param user
	 * @param period
	 *            overall | 3month | 6month | 12month
	 * @return An array of artists
	 * @throws IOException
	 */
	public Artist[] getUserTopArtists(String user, String period) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=299
	 * 
	 * @param user
	 * @param period
	 *            overall | 3month | 6month | 12month
	 * @return An array of albums
	 * @throws IOException
	 */
	public Album[] getUserTopAlbums(String user, String period) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=301
	 * 
	 * @param user
	 * @param period
	 *            overall | 3month | 6month | 12month
	 * @return An array of tracks
	 * @throws IOException
	 */
	public Track[] getUserTopTracks(String user, String period) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=278
	 * 
	 * @param user
	 * @param nowPlaying
	 * @param limit
	 * @return An array of tracks
	 * @throws IOException
	 */
	public Track[] getUserRecentTracks(String user, String nowPlaying, int limit) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=320
	 * 
	 * @param artist
	 *            The artist name in question
	 * @param track
	 *            The track name in question
	 * @param sk
	 *            A session key generated by authenticating a user via the
	 *            authentication protocol.
	 * @return An array of tags
	 * @throws IOException
	 */
	public Tag[] getTrackTags(String artist, String track, String sk) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=304
	 * 
	 * @param artist
	 *            The artist name in question
	 * @param track
	 *            The track name in question
	 * @param tag
	 *            An array of tags (up to 10 at once)
	 * @param sk
	 *            A session key generated by authenticating a user via the
	 *            authentication protocol.
	 * @throws IOException
	 */
	public void addTrackTags(String artist, String track, String[] tag, String sk) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=316
	 * 
	 * @param artist
	 *            The artist name in question
	 * @param track
	 *            The track name in question
	 * @param tag
	 *            A single user tag to remove from this track.
	 * @param sk
	 *            A session key generated by authenticating a user via the
	 *            authentication protocol.
	 * @throws IOException
	 */
	public void removeTrackTag(String artist, String track, String tag, String sk) throws IOException;

	/**
	 * See http://www.last.fm/api/show?service=267
	 * 
	 * @param artist
	 *            (Optional) : The artist name in question
	 * @param mbid
	 *            (Optional) : The musicbrainz id for the artist
	 * @param lang
	 *            (Optional) : The language to return the biography in,
	 *            expressed as an ISO 639 alpha-2 code.
	 * @return Artist instance
	 * @throws IOException
	 */
	public Artist getArtistInfo(String artist, String mbid, String lang) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=312
	 * 
	 * @param track
	 *            track (Optional) : The track name in question
	 * @param artist
	 *            artist (Optional) : The artist name in question
	 * @param mbid
	 *            mbid (Optional) : The musicbrainz id for the track
	 * @return top fans array
	 * @throws IOException
	 */
	public User[] getTrackTopFans(String track, String artist, String mbid) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=117
	 * 
	 * @param artist
	 *            (Required) : The artist name in question
	 * @return artist events array
	 * @throws IOException
	 */
	public Event[] getArtistEvents(String artist) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=291
	 * 
	 * @param user
	 *            (Required) : The user name in question
	 * @return user events array
	 * @throws IOException
	 */
	public Event[] getUserEvents(String user) throws IOException;

	public Event[] getUserRecommendedEvents(String user, String sk) throws IOException;
	public Event[] getNearbyEvents(String latitude, String longitude) throws IOException;

	
	/**
	 * See http://www.lastfm.pl/api/show?service=307
	 * 
	 * @param event
	 *            (Required) : The event id
	 * @param status
	 *            (Required) : The status
	 * @return
	 * @throws IOException
	 */
	public void attendEvent(String event, String status, String sk) throws IOException, WSError;

	/**
	 * See http://www.lastfm.pl/api/show?service=371
	 * 
	 * @param artist
	 *            (Required) : The artist name you wish to add
	 * @throws IOException
	 */
	public void libraryAddArtist(String artist, String sk) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=370
	 * 
	 * @param album
	 *            (Required) : The album name you wish to add
	 * @throws IOException
	 */
	public void libraryAddAlbum(String album, String sk) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=372
	 * 
	 * @param track
	 *            (Required) : The track name you wish to add
	 * @throws IOException
	 */
	public void libraryAddTrack(String track, String sk) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=258
	 * 
	 * @param user1
	 *            (Required) : The first user you wish to compare
	 * @param user2
	 *            (Required) : The second user you wish to compare
	 * @param limit
	 *            (Optional) : The limit on the # of results
	 * @return A Tasteometer object
	 * @throws IOException
	 */
	public Tasteometer tasteometerCompare(String user1, String user2, int limit) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=313
	 * 
	 * @param username
	 *            (Required) : The user who's playlists you'd like to fetch
	 * @return An array of RadioPlayList objects
	 * @throws IOException
	 */
	public RadioPlayList[] getUserPlaylists(String username) throws IOException;

	/**
	 * See http://www.lastfm.pl/api/show?service=290
	 * 
	 * @param artist
	 *            (Optional) : The artist name in question
	 * @param album
	 *            (Optional) : The album name in question
	 * @return An Album object
	 * @throws IOException
	 */
	public Album getAlbumInfo(String artist, String album) throws IOException;

	/** if this isn't self explanatory you fail */
	public AudioscrobblerService createAudioscrobbler(Session session, String clientVersion);

	public void loveTrack(String artist, String track, String sk) throws IOException;

	public void banTrack(String artist, String track, String sk) throws IOException;

	public void shareTrack(String artist, String track, String recipient, String sk) throws IOException;

	public void addTrackToPlaylist(String artist, String track, String playlistId, String sk) throws IOException;

	public RadioPlayList[] createPlaylist(String title, String description, String sk) throws IOException;

	public Station[] getUserRecentStations(String user, String sk) throws IOException;
	
	public Station searchForStation(String query) throws IOException;
}
