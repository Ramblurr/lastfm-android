// Copyright 2008 Google Inc. All Rights Reserved.

package fm.last.api.impl;

import fm.last.api.LastFmServer;

/**
 * @author Mike Jennings
 */
public class XmlRpcLastfmServerFactory {

  private XmlRpcLastfmServerFactory() {
  }

  public static LastFmServer getServer(String baseurl, String api_key, String api_secret) {
    return new LastFmServerImpl(baseurl, api_key, api_secret);
  }

}
