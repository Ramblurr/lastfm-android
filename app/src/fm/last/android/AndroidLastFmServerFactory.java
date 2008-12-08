package fm.last.android;

import fm.last.api.LastFmServer;
import fm.last.api.LastFmServerFactory;

public class AndroidLastFmServerFactory {
  private static final String API_KEY = "";
  private static final String API_SECRET = "";
  private static final String XMLRPC_ROOT_URL = "http://ws.audioscrobbler.com/2.0/";
  private static LastFmServer server;
  
  private AndroidLastFmServerFactory() {
  }
  
  public static LastFmServer getServer() {
    if (server == null) {
      server = LastFmServerFactory.getServer(XMLRPC_ROOT_URL, API_KEY, API_SECRET);
    }
    return server;
  }
  
}
