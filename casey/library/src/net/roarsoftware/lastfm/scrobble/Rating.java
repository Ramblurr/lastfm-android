package net.roarsoftware.lastfm.scrobble;

/**
 * The source of the track. See <a
 * href="http://www.last.fm/api/submissions#subs"
 * >http://www.last.fm/api/submissions#subs</a> for more information.
 * 
 * @author Casey Link <unnamedrambler@gmail.com>
 */
public enum Rating {

	/**
	 * Love (on any mode if the user has manually loved the track). This implies
	 * a listen.
	 */
	LOVE("L"),

	/**
	 * Ban (only if source=L). This implies a skip, and the client should skip
	 * to the next track when a ban happens.
	 */
	BAN("B"),

	/**
	 * Skip (only if source=L)
	 */
	SKIP("S"),

	/**
	 * Source unknown.
	 */
	UNKNOWN("");

	private String rating;

	Rating(String rating) {
		this.rating = rating;
	}

	/**
	 * Returns the corresponding code for this source.
	 * 
	 * @return the code
	 */
	public String getRating() {
		return rating;
	}
}