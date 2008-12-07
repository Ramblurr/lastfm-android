package test;

import java.util.Collection;

import net.roarsoftware.lastfm.Tag;
import android.util.Log;

public class TagTest {
	String apiKey = "xx";
	String secret = "xx";

	public void testSearch() {
		Collection<String> list = Tag.search("dance", apiKey);
		if( list == null ) {
			Log.d("Test", "LIST was null");
			return;
		}
		for(String t : list)
		{
			Log.d("Test", "Found tag: " + t);
		}
	}
}
