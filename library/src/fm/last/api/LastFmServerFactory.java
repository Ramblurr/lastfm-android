// Copyright 2008 Google Inc. All Rights Reserved.

package fm.last.api;

import fm.last.api.impl.XmlRpcLastfmServerFactory;

/**
 * A factory for LastFmServer objects
 *
 * @author Mike Jennings
 */
public class LastFmServerFactory {

  private LastFmServerFactory() {
  }

  public static LastFmServer getServer(String baseurl, String api_key, String api_secret) {
    return XmlRpcLastfmServerFactory.getServer(baseurl, api_key, api_secret);
  }
}
