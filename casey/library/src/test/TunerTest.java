package test;

import net.roarsoftware.lastfm.Authenticator;
import net.roarsoftware.lastfm.Credentials;
import net.roarsoftware.lastfm.Playlist;
import net.roarsoftware.lastfm.Radio;
import net.roarsoftware.lastfm.Session;
import net.roarsoftware.lastfm.Track;
import net.roarsoftware.lastfm.Tuner;


public class TunerTest {
	String apiKey = Credentials.API_KEY;
	String secret = Credentials.SECRET;
	String user = Credentials.USER;
	String pass = Credentials.PASS;

	public void testTuner() {
		Session session = Authenticator.getMobileSession(user, pass, apiKey,
				secret);
		Radio.RadioStation r = Radio.RadioStation.similarArtists("Beck");
		Tuner t = new Tuner(r, session);
		if(t == null )
			System.out.println("Tuner null");
		System.out.println("Got station: " + t.getStationName());
		
		t.fetchFiveMoreTracks();
		Playlist p = t.fetchPlaylist();
		
		for(Track track : p.getTracks() )
		{
			System.out.println("Got track: " + track.getName() + " @ " + track.getLocation());
		}
	}

}
