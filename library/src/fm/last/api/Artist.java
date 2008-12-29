// Copyright 2008 Google Inc. All Rights Reserved.

package fm.last.api;

import java.io.Serializable;

/**
 * Represents an Artist
 *
 * @author Mike Jennings
 */
public class Artist implements Serializable {
	private String name;
	private String mbid;
	private String match;
	private String url;
	private ImageUrl[] images;
	private Artist[] similar;
	private String streamable;
	private Bio bio;
	private String playcount;
	private String listeners;

	public Artist(String name, String mbid, String match, String url, ImageUrl[] images, String streamable, String playcount, String listeners) {
		this.name = name;
		this.mbid = mbid;
		this.match = match;
		this.url = url;
		this.images = images;
		this.streamable = streamable;
		this.playcount = playcount;
		this.listeners = listeners;
	}

	public String getName() {
		return name;
	}

	public String getMbid() {
		return mbid;
	}

	public String getMatch() {
		return match;
	}

	public String getUrl() {
		return url;
	}

	public ImageUrl[] getImages() {
		return images;
	}

	public String getStreamable() {
		return streamable;
	}

	public void setBio(Bio bio) {
		this.bio = bio;
	}

	public Bio getBio() {
		return bio;
	}

	public void setSimilar(Artist[] similar) {
		this.similar = similar;
	}

	public Artist[] getSimilar() {
		return similar;
	}
	
	public String getPlaycount() {
		return playcount;
	}
	
	public String getListeners() {
		return listeners;
	}
}
