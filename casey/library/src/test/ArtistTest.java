package test;

import java.util.Collection;

import net.roarsoftware.lastfm.Artist;
import net.roarsoftware.lastfm.Session;
import android.util.Log;

public class ArtistTest {
	String apiKey = "xx";
	String secret = "xx";

	public void testSearch() {
		Collection<Artist> list = Artist.search("Cher", apiKey);
		if( list == null ) {
			Log.d("Test", "LIST was null");
			return;
		}
		for(Artist a : list)
		{
			Log.d("Test", "Found artist: " + a.getName());
		}
	}
}
