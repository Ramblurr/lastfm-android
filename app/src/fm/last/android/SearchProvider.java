package fm.last.android;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;

import java.io.IOException;
import java.io.Serializable;

import fm.last.api.Album;
import fm.last.api.Artist;
import fm.last.api.LastFmServer;
import fm.last.api.Tag;
import fm.last.api.Track;
import fm.last.api.User;
import fm.last.api.WSError;

public class SearchProvider extends ContentProvider {

    public static String AUTHORITY = "lastfm";
    public static Uri SUGGESTIONS_URI = Uri.parse("content://lastfm/search_suggest_query/");
    
    private static final int SEARCH_SUGGEST = 0;
    private static final int SHORTCUT_REFRESH = 1;
    private static final UriMatcher sURIMatcher = buildUriMatcher();

    /**
     * The columns we'll include in our search suggestions.  There are others that could be used
     * to further customize the suggestions, see the docs in {@link SearchManager} for the details
     * on additional columns that are supported.
     */
    private static final String[] COLUMNS = {
            "_id",  // must include this column
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            "_imageURL"
            };


    /**
     * Sets up a uri matcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, SHORTCUT_REFRESH);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SHORTCUT_REFRESH);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (!TextUtils.isEmpty(selection)) {
            throw new IllegalArgumentException("selection not allowed for " + uri);
        }
        if (selectionArgs != null && selectionArgs.length != 0) {
            throw new IllegalArgumentException("selectionArgs not allowed for " + uri);
        }
        if (!TextUtils.isEmpty(sortOrder)) {
            throw new IllegalArgumentException("sortOrder not allowed for " + uri);
        }
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                String query = null;
                if (uri.getPathSegments().size() > 1) {
                    query = uri.getLastPathSegment().toLowerCase();
                }
                return getSuggestions(query, projection);
            case SHORTCUT_REFRESH:
                String shortcutId = null;
                if (uri.getPathSegments().size() > 1) {
                    shortcutId = uri.getLastPathSegment();
                }
                return refreshShortcut(shortcutId, projection);
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    private Cursor getSuggestions(String query, String[] projection) {
        String processedQuery = query == null ? "" : query.toLowerCase();
        if(processedQuery.length() < 1)
        	return null;
        
		LastFmServer server = AndroidLastFmServerFactory.getServer();
		try {
	        MatrixCursor cursor = new MatrixCursor(COLUMNS);
	        long id = 0;

	        try {
	        	Serializable[] results;
	        	results = server.multiSearch(query);
	        	
		        for (int i = 0; i < results.length; i++) {
		        	if(results[i].getClass().equals(Artist.class)) {
		        		Artist artist = (Artist)results[i];
		        		
			            cursor.addRow(new Object[] {
			                    id++,                  // _id
			                    artist.getName(),           // text1
			                    LastFMApplication.getInstance().getString(R.string.action_viewinfo),     // text2
			                    Uri.parse("http://www.last.fm/music/"+artist.getName()),           // intent_data (included when clicking on item)
			                    artist.getImages().length == 0 ? "" : artist.getURLforImageSize("extralarge")
			            });
		        	} else if(results[i].getClass().equals(Album.class)) {
		        		//Album album = (Album)results[i];
		        		
		        		//TODO: We need an album display
		        	} else if(results[i].getClass().equals(Track.class)) {
		        		Track track = (Track)results[i];

		        		cursor.addRow(new Object[] {
			                    id++,                  // _id
			                    track.getArtist().getName() + " - " + track.getName(),           // text1
			                    LastFMApplication.getInstance().getString(R.string.action_viewinfo),     // text2
			                    Uri.parse("http://www.last.fm/music/"+Uri.encode(track.getArtist().getName())+"/_/"+track.getName()),           // intent_data (included when clicking on item)
			                    track.getImages().length == 0 ? "" : track.getURLforImageSize("extralarge")
			            });
		        	} else if(results[i].getClass().equals(Tag.class)) {
		        		Tag tag = (Tag)results[i];

			            cursor.addRow(new Object[] {
			                    id++,                  // _id
			                    LastFMApplication.getInstance().getString(R.string.newstation_tagradio,tag.getName()),           // text1
			                    LastFMApplication.getInstance().getString(R.string.action_tagradio),     // text2
			                    Uri.parse("lastfm://globaltags/"+tag.getName()),           // intent_data (included when clicking on item)
			                    -1
			            });
		        	}
				}
	        } catch (WSError e) {
	        }

	        try {
		        User u = server.getUserInfo(processedQuery, LastFMApplication.getInstance().session.getKey());
		        if(u != null && u.getName().toLowerCase().equals(processedQuery)) {
		            cursor.addRow(new Object[] {
		                    id++,                  // _id
		                    processedQuery,           // text1
		                    LastFMApplication.getInstance().getString(R.string.action_viewprofile),     // text2
		                    Uri.parse("http://www.last.fm/user/"+processedQuery),           // intent_data (included when clicking on item)
		                    u.getImages().length == 0 ? "" : u.getURLforImageSize("extralarge")
		            });
		        }
	        } catch (WSError e) {
	        }

	        return cursor;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }

    /**
     * Note: this is unused as is, but if we included
     * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our results, we
     * could expect to receive refresh queries on this uri for the id provided, in which case we
     * would return a cursor with a single item representing the refreshed suggestion data.
     */
    private Cursor refreshShortcut(String shortcutId, String[] projection) {
        return null;
    }

    /**
     * All queries for this provider are for the search suggestion and shortcut refresh mime type.
     */
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case SHORTCUT_REFRESH:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
