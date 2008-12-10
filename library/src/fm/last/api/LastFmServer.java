// Copyright 2008 Google Inc. All Rights Reserved.

package fm.last.api;

import java.io.IOException;

public interface LastFmServer {
  /**
   * See http://www.last.fm/api/show?service=119
   * @param artist
   * @param limit
   * @return
   * @throws IOException
   */
  public Artist[] getSimilarArtists(String artist, String limit) throws IOException;

  public Artist[] searchForArtist(String artist) throws IOException;
  
  public Tag[] searchForTag(String Tag) throws IOException;
  
  /**
   * See http://www.last.fm/api/show?service=263
   * @param user
   * @param recenttracks
   * @param limit
   * @return
   * @throws IOException
   */
  public Friends getFriends(String user, String recenttracks, String limit) throws IOException;

  /**
   * See http://www.last.fm/api/show?service=356
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
   * @param authToken md5(username + md5(password))
   * @return
   * @throws IOException
   */
  public Session getMobileSession(String username, String authToken) throws IOException;


  public Station tuneToStation(String station, String sk) throws IOException;

  public RadioPlayList getRadioPlayList(String sk) throws IOException;

  public User getUserInfo(String sk) throws IOException;
}
