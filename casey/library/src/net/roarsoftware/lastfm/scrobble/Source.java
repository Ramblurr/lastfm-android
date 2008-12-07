package net.roarsoftware.lastfm.scrobble;

/**
 * The source of the track. See <a
 * href="http://www.last.fm/api/submissions#subs"
 * >http://www.last.fm/api/submissions#subs</a> for more information.
 * 
 * @author Janni Kovacs
 */
public enum Source {

	/**
	 * Chosen by the user (the most common value, unless you have a reason for
	 * choosing otherwise, use this).
	 */
	USER("P"),

	/**
	 * Non-personalised broadcast (e.g. Shoutcast, BBC Radio 1).
	 */
	NON_PERSONALIZED_BROADCAST("R"),

	/**
	 * Personalised recommendation except Last.fm (e.g. Pandora, Launchcast).
	 */
	PERSONALIZED_BROADCAST("E"),

	/**
	 * Source unknown.
	 */
	UNKNOWN("U"),

	/**
	 * Last.fm (any mode). In this case, the 5-digit Last.fm recommendation key
	 * must be appended to this source ID to prove the validity of the
	 * submission. The recommendation key is Track.trackauth
	 */
	LASTFM("L");

	private String code;
	private String authkey;

	Source(String code) {
		this.code = code;		
	}

	/**
	 * Sets the track recommendation key for use with the LASTFM type.
	 * @param key the recommendation key (Track.trackauth)
	 */
	public void setAuthKey(String key)
	{
		authkey = key;
	}
	
	/**
	 * Gets the track recommendation key for use with the LASTFM type.
	 * @return the recommendation key (Track.trackauth)
	 */
	public String getAuthKey()
	{
		return authkey;
	}

	/**
	 * Returns the corresponding code for this source.
	 * 
	 * @return the code
	 */
	public String getCode() {
		if( code == "L")
			return code + authkey;
		return code;
	}
}
