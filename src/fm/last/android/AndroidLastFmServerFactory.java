package fm.last.android;

import fm.last.api.LastFmServer;
import fm.last.api.LastFmServerFactory;

public class AndroidLastFmServerFactory {
  private static final String API_KEY = "c8c7b163b11f92ef2d33ba6cd3c2c3c3";
  private static final String API_SECRET = "73582dfc9e556d307aead069af110ab8";
  private static final String XMLRPC_ROOT_URL = "http://ws.audioscrobbler.com/2.0/";
  private static LastFmServer server;
  
  private AndroidLastFmServerFactory() {
  }
  
  public static LastFmServer getServer() {
    if (server == null) {
      server = LastFmServerFactory.getServer(XMLRPC_ROOT_URL, API_KEY);
    }
    return server;
  }
  
}
