/**
 * 
 */
package fm.last.android.scrobbler;

import java.io.Serializable;

import fm.last.api.RadioTrack;

/**
 * @author sam
 *
 */
public class ScrobblerQueueEntry implements Serializable {
	private static final long serialVersionUID = 1L;
	public String artist;
	public String title;
	public String album;
	public Long startTime;
	public Integer duration;
	public String trackAuth = "";
	public String rating = "";
	public Boolean postedNowPlaying = false;
	
	public RadioTrack toRadioTrack() {
		return new RadioTrack("", title, "", album, artist, duration.toString(), "", trackAuth);
	}
}