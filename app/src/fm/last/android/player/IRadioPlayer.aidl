package fm.last.android.player;

import fm.last.api.Session;
import fm.last.api.WSError;

interface IRadioPlayer {
	void setSession(in Session session);
	boolean tune(in String url, in Session session);

	void pause();
	void stop();
	void startRadio();
	void resetScrobbler();	// temporary (while the scrobbler lives in the radio service)

	void love();
	void ban();
	void skip(); 
	
	String getArtistName();
	String getAlbumName();
	String getTrackName();
	String getArtUrl();
	long   getDuration();
	long   getPosition();
	int	   getBufferPercent();
	
	boolean isPlaying();
	String getStationName();
	String getStationUrl();
	Bitmap getAlbumArt();
	void setAlbumArt(in Bitmap art);
	
	WSError getError();
} 