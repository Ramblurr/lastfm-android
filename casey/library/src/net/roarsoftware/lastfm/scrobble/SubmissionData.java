package net.roarsoftware.lastfm.scrobble;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Bean that contains track information.
 *
 * @author Janni Kovacs
 */
public class SubmissionData {

	private String artist;
	private String track;
	private String album;
	private long startTime;
	private Source source;
	private Rating rating;
	private int length;
	private int tracknumber;

	public SubmissionData(String artist, String track, String album, int length, int tracknumber, Source source, Rating rating,
						  long startTime) {
		this.artist = artist;
		this.track = track;
		this.album = album;
		this.length = length;
		this.tracknumber = tracknumber;
		this.source = source;
		this.rating = rating;
		this.startTime = startTime;
	}

	String toString(String sessionId, int index) {
		String b = album != null ? album : "";
		String artist = this.artist;
		String track = this.track;
		try {
			artist = URLEncoder.encode(artist, "UTF-8");
			track = URLEncoder.encode(track, "UTF-8");
			b = URLEncoder.encode(b, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// UTF-8 always available
		}
		String l = length == -1 ? "" : String.valueOf(length);
		String n = tracknumber == -1 ? "" : String.valueOf(tracknumber);
		return String
				.format("s=%s&a[%10$d]=%s&t[%10$d]=%s&i[%10$d]=%s&o[%10$d]=%s&r[%10$d]=%s&l[%10$d]=%s&b[%10$d]=%s&n[%10$d]=%s&m[%10$d]=",
						sessionId, artist, track, startTime, source.getCode(), rating.getRating(), l, b, n, index);
	}

}
