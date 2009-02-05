package fm.last.android.player;

interface IRadioPlayer {
	void setSession(in Session session);
	boolean tune(in String url, in Session session);

	void pause();
	void stop();
	void startRadio();

	void skip(); 
	
	String getArtistName();
	String getAlbumName();
	String getTrackName();
	String getArtUrl();
	long   getDuration();
	long   getPosition();
	int	   getBufferPercent();
	
	boolean isPlaying();
	int getState();
	String getStationName();
	String getStationUrl();
	Bitmap getAlbumArt();
	void setAlbumArt(in Bitmap art);
	
	WSError getError();
} 