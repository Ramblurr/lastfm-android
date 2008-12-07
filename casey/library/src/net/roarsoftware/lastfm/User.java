package net.roarsoftware.lastfm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import net.roarsoftware.xml.DomElement;

/**
 * Contains user information and provides bindings to the methods in the user.
 * namespace.
 * 
 * @author Janni Kovacs
 */
public class User
{

    private String name;
    private String url;

    private String language;
    private String country;
    private String age;
    private String gender;
    private String imageUrl;
    private boolean subscriber;
    private int numPlaylists;
    private int playcount;

    public User( String name, String url )
    {

        this.name = name;
        this.url = url;
    }

    public User()
    {

        // TODO Auto-generated constructor stub
    }

    public String getName()
    {

        return name;
    }

    public String getUrl()
    {

        return url;
    }

    public String getAge()
    {

        return age;
    }

    public String getCountry()
    {

        return country;
    }

    public String getGender()
    {

        return gender;
    }

    public String getLanguage()
    {

        return language;
    }

    public int getNumPlaylists()
    {

        return numPlaylists;
    }

    public int getPlaycount()
    {

        return playcount;
    }

    public boolean isSubscriber()
    {

        return subscriber;
    }

    public String getFullImageURL()
    {
        return imageUrl;
    }
    
    public String get64sImageURL()
    {
        if( imageUrl != null )
            return imageUrl.replace( "serve/126", "serve/64s" ); // 64 square
        return null;
    }

    public static Collection<User> getFriends( String user, String apiKey )
    {

        return getFriends( user, false, 100, apiKey );
    }

    public static Collection<User> getFriends( String user,
            boolean recenttracks, int limit, String apiKey )
    {

        Result result = Caller.getInstance().call( "user.getFriends", apiKey,
                "user", user, "recenttracks",
                String.valueOf( recenttracks ? 1 : 0 ), "limit",
                String.valueOf( limit ) );
        if ( !result.isSuccessful() )
            return Collections.emptyList();
        DomElement element = result.getContentElement();
        List<User> friends = new ArrayList<User>();
        for ( DomElement domElement : element.getChildren( "user" ) )
        {
           // friends.add( userFromElement( domElement ) );
        }
        return friends;
    }

    public static Collection<User> getNeighbours( String user, String apiKey )
    {

        return getFriends( user, false, 100, apiKey );
    }

    public static Collection<User> getNeighbours( String user, int limit,
            String apiKey )
    {

        Result result = Caller.getInstance().call( "user.getNeighbours",
                apiKey, "user", user, "limit", String.valueOf( limit ) );
        if ( !result.isSuccessful() )
            return Collections.emptyList();
        DomElement element = result.getContentElement();
        List<User> friends = new ArrayList<User>();
        for ( DomElement domElement : element.getChildren( "user" ) )
        {
            //friends.add( userFromElement( domElement ) );
        }
        return friends;
    }

    public static Collection<Track> getRecentTracks( String user, String apiKey )
    {

        return getRecentTracks( user, 10, apiKey );
    }

    public static Collection<Track> getRecentTracks( String user, int limit,
            String apiKey )
    {

        Result result = Caller.getInstance().call( "user.getRecentTracks",
                apiKey, "user", user, "limit", String.valueOf( limit ) );
        if ( !result.isSuccessful() )
            return Collections.emptyList();
        DomElement element = result.getContentElement();
        List<Track> tracks = new ArrayList<Track>();
        for ( DomElement e : element.getChildren( "track" ) )
        {
            tracks.add( Track.trackFromElement( e ) );
        }
        return tracks;
    }

    public static boolean userExists( String user, String apiKey )
    {

        Result result = Caller.getInstance().call( "user.getRecentTracks",
                apiKey, "user", user, "limit", String.valueOf( 1 ) );
        if ( !result.isSuccessful() )
            return false;
        return true;
    }

    public static Collection<Album> getTopAlbums( String user, String apiKey )
    {

        return getTopAlbums( user, Period.OVERALL, apiKey );
    }

    private static Collection<Album> getTopAlbums( String user, Period period,
            String apiKey )
    {

        Result result = Caller.getInstance().call( "user.getTopAlbums", apiKey,
                "user", user, "period", period.getString() );
        if ( !result.isSuccessful() )
            return Collections.emptyList();
        DomElement element = result.getContentElement();
        List<Album> albums = new ArrayList<Album>();
        for ( DomElement domElement : element.getChildren( "album" ) )
        {
            albums.add( Album.albumFromElement( domElement ) );
        }
        return albums;
    }

    public static Collection<Artist> getTopArtists( String user, String apiKey )
    {

        return getTopArtists( user, Period.OVERALL, apiKey );
    }

    private static Collection<Artist> getTopArtists( String user,
            Period period, String apiKey )
    {

        Result result = Caller.getInstance().call( "user.getTopArtists",
                apiKey, "user", user, "period", period.getString() );
        if ( !result.isSuccessful() )
            return Collections.emptyList();
        DomElement element = result.getContentElement();
        List<Artist> artists = new ArrayList<Artist>();
        for ( DomElement domElement : element.getChildren( "artist" ) )
        {
            artists.add( Artist.artistFromElement( domElement ) );
        }
        return artists;
    }

    public static Collection<Track> getTopTracks( String user, String apiKey )
    {

        return getTopTracks( user, Period.OVERALL, apiKey );
    }

    private static Collection<Track> getTopTracks( String user, Period period,
            String apiKey )
    {

        Result result = Caller.getInstance().call( "user.getTopTracks", apiKey,
                "user", user, "period", period.getString() );
        if ( !result.isSuccessful() )
            return Collections.emptyList();
        DomElement element = result.getContentElement();
        List<Track> tracks = new ArrayList<Track>();
        for ( DomElement domElement : element.getChildren( "track" ) )
        {
            tracks.add( Track.trackFromElement( domElement ) );
        }
        return tracks;
    }

    public static Collection<String> getTopTags( String user, String apiKey )
    {

        return getTopTags( user, -1, apiKey );
    }

    private static Collection<String> getTopTags( String user, int limit,
            String apiKey )
    {

        Map<String, String> params = new HashMap<String, String>();
        params.put( "user", user );
        if ( limit != -1 )
        {
            params.put( "limit", String.valueOf( limit ) );
        }
        Result result = Caller.getInstance().call( "user.getTopTags", apiKey,
                params );
        if ( !result.isSuccessful() )
            return Collections.emptyList();
        DomElement element = result.getContentElement();
        List<String> tags = new ArrayList<String>();
        for ( DomElement domElement : element.getChildren( "tag" ) )
        {
            tags.add( domElement.getChildText( "name" ) );
        }
        return tags;
    }

    public static Chart<Album> getWeeklyAlbumChart( String user, String apiKey )
    {

        return getWeeklyAlbumChart( user, null, null, -1, apiKey );
    }

    public static Chart<Album> getWeeklyAlbumChart( String user, int limit,
            String apiKey )
    {

        return getWeeklyAlbumChart( user, null, null, limit, apiKey );
    }

    public static Chart<Album> getWeeklyAlbumChart( String user, String from,
            String to, int limit, String apiKey )
    {

        return Chart.getChart( "user.getWeeklyAlbumChart", "user", user,
                "album", from, to, limit, apiKey );
    }

    public static Chart<Artist> getWeeklyArtistChart( String user, String apiKey )
    {

        return getWeeklyArtistChart( user, null, null, -1, apiKey );
    }

    public static Chart<Artist> getWeeklyArtistChart( String user, int limit,
            String apiKey )
    {

        return getWeeklyArtistChart( user, null, null, limit, apiKey );
    }

    public static Chart<Artist> getWeeklyArtistChart( String user, String from,
            String to, int limit, String apiKey )
    {

        return Chart.getChart( "user.getWeeklyArtistChart", "user", user,
                "artist", from, to, limit, apiKey );
    }

    public static Chart<Track> getWeeklyTrackChart( String user, String apiKey )
    {

        return getWeeklyTrackChart( user, null, null, -1, apiKey );
    }

    public static Chart<Track> getWeeklyTrackChart( String user, int limit,
            String apiKey )
    {

        return getWeeklyTrackChart( user, null, null, limit, apiKey );
    }

    public static Chart<Track> getWeeklyTrackChart( String user, String from,
            String to, int limit, String apiKey )
    {

        return Chart.getChart( "user.getWeeklyTrackChart", "user", user,
                "track", from, to, limit, apiKey );
    }

    public static LinkedHashMap<String, String> getWeeklyChartList(
            String user, String apiKey )
    {

        return Chart.getWeeklyChartList( "user", user, apiKey );
    }

    public static Collection<Chart> getWeeklyChartListAsCharts( String user,
            String apiKey )
    {

        return Chart.getWeeklyChartListAsCharts( "user", user, apiKey );
    }

    /**
     * GetS a list of upcoming events that this user is attending.
     * 
     * @param user
     *            The user to fetch the events for.
     * @param apiKey
     *            A Last.fm API key.
     * @return a list of upcoming events
     */
    public static Collection<Event> getEvents( String user, String apiKey )
    {

        Result result = Caller.getInstance().call( "user.getEvents", apiKey,
                "user", user );
        if ( !result.isSuccessful() )
            return Collections.emptyList();
        DomElement element = result.getContentElement();
        List<Event> events = new ArrayList<Event>();
        for ( DomElement domElement : element.getChildren( "event" ) )
        {
            events.add( Event.eventFromElement( domElement ) );
        }
        return events;
    }

    /**
     * Get the first page of a paginated result of all events a user has
     * attended in the past.
     * 
     * @param user
     *            The username to fetch the events for.
     * @param apiKey
     *            A Last.fm API key.
     * @return a list of past {@link Event}s
     */
    public static PaginatedResult<Event> getPastEvents( String user,
            String apiKey )
    {

        return getPastEvents( user, 1, 0, apiKey );
    }

    /**
     * Gets a paginated list of all events a user has attended in the past.
     * 
     * @param user
     *            The username to fetch the events for.
     * @param page
     *            The page number to scan to.
     * @param limit
     *            The number of events to return per page.
     * @param apiKey
     *            A Last.fm API key.
     * @return a list of past {@link Event}s
     */
    public static PaginatedResult<Event> getPastEvents( String user, int page,
            int limit, String apiKey )
    {

        Result result = Caller.getInstance().call( "user.getPastEvents",
                apiKey, "user", user, "page", String.valueOf( page ), "limit",
                String.valueOf( limit ) );
        if ( !result.isSuccessful() )
            return new PaginatedResult<Event>( 0, 0, Collections
                    .<Event> emptyList() );
        DomElement element = result.getContentElement();
        List<Event> events = new ArrayList<Event>();
        for ( DomElement domElement : element.getChildren( "event" ) )
        {
            events.add( Event.eventFromElement( domElement ) );
        }
        int currentPage = Integer.valueOf( element.getAttribute( "page" ) );
        int totalPages = Integer.valueOf( element.getAttribute( "totalPages" ) );
        return new PaginatedResult<Event>( currentPage, totalPages, events );
    }

    /**
     * Gets a list of a user's playlists on Last.fm. Note that this method only
     * fetches metadata regarding the user's playlists. If you want to retrieve
     * the list of tracks in a playlist use
     * {@link Playlist#fetch(String, String) Playlist.fetch()}.
     * 
     * @param user
     *            The last.fm username to fetch the playlists of.
     * @param apiKey
     *            A Last.fm API key.
     * @return a list of Playlists
     */
    public static Collection<Playlist> getPlaylists( String user, String apiKey )
    {

        Result result = Caller.getInstance().call( "user.getPlaylists", apiKey,
                "user", user );
        if ( !result.isSuccessful() )
            return Collections.emptyList();
        Collection<Playlist> playlists = new ArrayList<Playlist>();
        for ( DomElement element : result.getContentElement().getChildren(
                "playlist" ) )
        {
            playlists.add( Playlist.playlistFromElement( element ) );
        }
        return playlists;
    }

    /**
     * Gets the last 50 tracks loved by a user.
     * 
     * @param user
     *            The user name to fetch the loved tracks for.
     * @param apiKey
     *            A Last.fm API key.
     * @return the loved tracks
     */
    public static Collection<Track> getLovedTracks( String user, String apiKey )
    {

        Result result = Caller.getInstance().call( "user.getLovedTracks",
                apiKey, "user", user );
        if ( !result.isSuccessful() )
            return Collections.emptyList();
        DomElement element = result.getContentElement();
        Collection<Track> tracks = new ArrayList<Track>();
        for ( DomElement domElement : element.getChildren( "track" ) )
        {
            tracks.add( Track.trackFromElement( domElement ) );
        }
        return tracks;
    }

    /**
     * Retrieves profile information about the current authenticated user.
     * 
     * @param session
     *            A Session instance
     * @return User info
     */
    public static User getInfo( Session session )
    {

        Result result = Caller.getInstance().call( "User.getInfo", session );
        if ( !result.isSuccessful() )
            return null;
        return userFromXPP( result.getParser() );
    }

    /**
     * Retrieves a URL to the current authenticated user's profile image.
     * 
     * @param session
     *            A Session instance
     * @return String a url to the profile picture. returns null if there is no
     *         picture or an error occurred
     */
    public static String getProfileImage( Session session )
    {

        Result result = Caller.getInstance().call( "User.getInfo", session );
        if ( !result.isSuccessful() )
            return null;
        try
        {
            result.getParser().nextTag();
            if ( !result.getParser().getName().equals( "user" ) )
                return null;
            int event = result.getParser().nextTag();
            boolean loop = true;
            while ( loop )
            {
                String n = result.getParser().getName();
                switch ( event )
                {
                case XmlPullParser.START_TAG:
                    if ( n.equals( "image" ) )
                        return result.getParser().nextText();
                    break;
                case XmlPullParser.END_TAG:
                    if ( n.equals( "image" ) )
                        loop = false;
                    break;
                case XmlPullParser.TEXT:
                default:
                    break;
                }
                event = result.getParser().next();
            }
        }
        catch ( Exception e )
        {
            System.out.println( "Error getting user's profile pic: "
                    + e.getMessage() );
            e.printStackTrace();
        }
        return null;
    }

    static User userFromXPP( XmlPullParser xpp )
    {

        User user = new User();
        try
        {
            xpp.nextTag();
            if ( !xpp.getName().equals( "user" ) )
                return null;
            int event = xpp.nextTag();
            boolean loop = true;
            while ( loop )
            {
                String n = xpp.getName();
                switch ( event )
                {
                case XmlPullParser.START_TAG:
                    if ( n.equals( "image" ) )
                        user.imageUrl = xpp.nextText();
                    else if ( n.equals( "name" ) )
                        user.name = xpp.nextText();
                    else if ( n.equals( "url" ) )
                        user.url = xpp.nextText();
                    else if ( n.equals( "lang" ) )
                        user.language = xpp.nextText();
                    else if ( n.equals( "country" ) )
                        user.country = xpp.nextText();
                    else if ( n.equals( "age" ) )
                        user.age = xpp.nextText();
                    else if ( n.equals( "gender" ) )
                        user.gender = xpp.nextText();
                    else if ( n.equals( "subscriber" ) )
                        user.subscriber = xpp.nextText().equals( "1" );
                    else if ( n.equals( "playcount" ) )
                        user.playcount = Integer.parseInt( xpp.nextText() );
                        break;
                case XmlPullParser.END_TAG:
                    if ( n.equals( "user" ) )
                        loop = false;
                    break;
                case XmlPullParser.TEXT:
                default:
                    break;
                }
                event = xpp.next();
            }
        }
        catch ( Exception e )
        {
            System.out.println( "Error getting user's profile pic: "
                    + e.getMessage() );
            e.printStackTrace();
        }
        return user;
    }
}
