package test;

import net.roarsoftware.lastfm.Authenticator;
import net.roarsoftware.lastfm.Playlist;
import net.roarsoftware.lastfm.Radio;
import net.roarsoftware.lastfm.Result;
import net.roarsoftware.lastfm.Session;
import net.roarsoftware.lastfm.Credentials;
import net.roarsoftware.lastfm.Track;

public class RadioTest {

	String apiKey = Credentials.API_KEY;
	String secret = Credentials.SECRET;
	String user = Credentials.USER;
	String pass = Credentials.PASS;

	public void testTuner() {
		Session session = Authenticator.getMobileSession(user, pass, apiKey,
				secret);
		Radio.RadioStation r = Radio.RadioStation.similarArtists("Beck");
		Radio t = Radio.newRadio("JavaTest", "0.0");
		t.handshake(session.getUsername(), session.getPasswordHash());
		t.changeStation(r);
		System.out.println("Fetching playlist");
		Playlist p = t.fetchPlaylist();
		for(Track tr : p.getTracks())
		{
			System.out.println(tr.getName());
		}
		System.out.println("Total tracks: " + p.getTracks().size());

		if(t == null )
			System.out.println("Tuner null");
		
	}

}
