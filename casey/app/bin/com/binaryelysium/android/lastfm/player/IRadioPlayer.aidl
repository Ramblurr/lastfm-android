package com.binaryelysium.android.lastfm.player;
import net.roarsoftware.lastfm.Radio;
import net.roarsoftware.lastfm.Session;
interface IRadioPlayer {
	void setTuner( in Radio tuner);
	void setSession( in Session session);    

	void pause();
	void stop();
	void skipForward();
	void startRadio();

	void love();
	void skip();
	void ban(); 
	
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
	
} 