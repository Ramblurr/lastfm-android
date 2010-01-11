/**
 * 
 */
package fm.last.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * @author sam
 *
 */
public class Amazon {
    private static final String ACTION_EXTERNAL_EVENT = "com.amazon.mp3.action.EXTERNAL_EVENT";
	private static final String TYPE_TOP_MUSIC_BROWSE = "com.amazon.mp3.type.TOP_MUSIC_BROWSE";
	private static final String TYPE_GENRE_BROWSE = "com.amazon.mp3.type.GENRE_BROWSE";
	private static final String TYPE_SHOW_ALBUM_DETAIL = "com.amazon.mp3.type.SHOW_ALBUM_DETAIL";
	private static final String TYPE_SEARCH = "com.amazon.mp3.type.SEARCH";
    private static final String EXTRA_GENRE_NAME = "com.amazon.mp3.extra.GENRE_NAME";
	private static final String EXTRA_EXTERNAL_EVENT_TYPE = "com.amazon.mp3.extra.EXTERNAL_EVENT_TYPE";
	private static final String EXTRA_BROWSE_TYPE = "com.amazon.mp3.extra.BROWSE_TYPE";
	private final static String EXTRA_ALBUM_ASIN = "com.amazon.mp3.extra.ALBUM_ASIN";
	private final static String EXTRA_AUTO_PLAY_TRACK_ASIN = "com.amazon.mp3.extra.AUTO_PLAY_TRACK_ASIN";
	private final static String EXTRA_SEARCH_STRING = "com.amazon.mp3.extra.SEARCH_STRING";
	private final static String EXTRA_SEARCH_TYPE = "com.amazon.mp3.extra.SEARCH_TYPE";
	private static final int BROWSE_TYPE_SONGS = 0;
	private static final int BROWSE_TYPE_ALBUMS = 1;
	private static final int SEARCH_TYPE_SONGS = 0;
	private static final int SEARCH_TYPE_ALBUMS = 1;
	
	public static int getAmazonVersion(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		int result = -1;
		try {
			PackageInfo pi = pm.getPackageInfo("com.amazon.mp3", PackageManager.GET_ACTIVITIES);
			result = pi.versionCode;
		} catch (Exception e) {
			result = -1;
		}
		return result;
	}
	
	public static void searchForTrack(Context ctx, String artist, String track) {
        String query = artist + " " + track;
        int searchType = 0;
    	Intent intent;
        try {
			if(getAmazonVersion(ctx) > 60000) {
                intent = new Intent( ACTION_EXTERNAL_EVENT );
                intent.putExtra(EXTRA_EXTERNAL_EVENT_TYPE, TYPE_SEARCH);
                intent.putExtra(EXTRA_SEARCH_STRING, query);
                intent.putExtra(EXTRA_SEARCH_TYPE, SEARCH_TYPE_SONGS);
        	} else {
                intent = new Intent( Intent.ACTION_SEARCH );
                intent.setComponent(new ComponentName("com.amazon.mp3","com.amazon.mp3.android.client.SearchActivity"));
                intent.putExtra("actionSearchString", query);
                intent.putExtra("actionSearchType", searchType);
        	}
            ctx.startActivity( intent );
        } catch (Exception e) {
			LastFMApplication.getInstance().presentError(ctx, ctx.getString(R.string.ERROR_AMAZON_TITLE),
					ctx.getString(R.string.ERROR_AMAZON));
        }
	}
	
	public static void searchForAlbum(Context ctx, String artist, String album) {
        String query = artist + " " + album;
        int searchType = 1;
    	Intent intent;
        try {
			if(getAmazonVersion(ctx) > 60000) {
                intent = new Intent( ACTION_EXTERNAL_EVENT );
                intent.putExtra(EXTRA_EXTERNAL_EVENT_TYPE, TYPE_SEARCH);
                intent.putExtra(EXTRA_SEARCH_STRING, query);
                intent.putExtra(EXTRA_SEARCH_TYPE, SEARCH_TYPE_ALBUMS);
        	} else {
                intent = new Intent( Intent.ACTION_SEARCH );
                intent.setComponent(new ComponentName("com.amazon.mp3","com.amazon.mp3.android.client.SearchActivity"));
                intent.putExtra("actionSearchString", query);
                intent.putExtra("actionSearchType", searchType);
        	}
            ctx.startActivity( intent );
        } catch (Exception e) {
			LastFMApplication.getInstance().presentError(ctx, ctx.getString(R.string.ERROR_AMAZON_TITLE),
					ctx.getString(R.string.ERROR_AMAZON));
        }
	}
}
