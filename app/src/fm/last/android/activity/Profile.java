package fm.last.android.activity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Stack;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.LastFm;
import fm.last.android.OnListRowSelectedListener;
import fm.last.android.R;
import fm.last.android.RemoteImageHandler;
import fm.last.android.RemoteImageView;
import fm.last.android.Worker;
import fm.last.android.adapter.EventListAdapter;
import fm.last.android.adapter.LastFMStreamAdapter;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.adapter.SeparatedListAdapter;
import fm.last.android.player.RadioPlayerService;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.UserTask;
import fm.last.android.widget.TabBar;
import fm.last.android.widget.TabBarListener;
import fm.last.api.Album;
import fm.last.api.Artist;
import fm.last.api.Event;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Track;
import fm.last.api.User;
import fm.last.api.ImageUrl;

public class Profile extends ListActivity implements TabBarListener
{

	private static int TAB_RADIO = 0;
	private static int TAB_PROFILE = 1;
	
	
	//Goddamn Java doesn't let you treat enums as ints easily, so we have to have this mess 
	private static final int PROFILE_TOPARTISTS = 0;
	private static final int PROFILE_TOPALBUMS = 1;
	private static final int PROFILE_TOPTRACKS = 2;
	private static final int PROFILE_RECENTLYPLAYED = 3;
	private static final int PROFILE_EVENTS = 4;
	private static final int PROFILE_FRIENDS = 5;
	
	private static final int DIALOG_ALBUM = 0 ;
	private static final int DIALOG_TRACK = 1;

    private SeparatedListAdapter mMainAdapter;
    private ListAdapter mProfileAdapter;
    private LastFMStreamAdapter mMyStationsAdapter;
    private LastFMStreamAdapter mMyRecentAdapter;
    private User mUser;
	LastFmServer mServer = AndroidLastFmServerFactory.getServer();

	TabBar mTabBar;
	ViewFlipper mViewFlipper;
	ViewFlipper mNestedViewFlipper;
	ListView mProfileList;
    private Stack<Integer> mViewHistory;
	
	View previousSelectedView = null;
	
	ListView mDialogList;
	private ListAdapter mDialogAdapter;;
	
	//Profile lists
	private ImageCache mImageCache;
	ListView mTopArtistsList;
	private ListAdapter mTopArtistsAdapter;
	ListView mTopAlbumsList;
    private ListAdapter mTopAlbumsAdapter;
    ListView mTopTracksList;
    private ListAdapter mTopTracksAdapter;
    ListView mRecentTracksList;
    private ListAdapter mRecentTracksAdapter;
    ListView mEventsList;
    private EventListAdapter mEventsAdapter;
    ListView mFriendsList;
    private ListAdapter mFriendsAdapter;
	
    @Override
    public void onCreate( Bundle icicle )
    {

        super.onCreate( icicle );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.home );
        Session session = ( Session ) LastFMApplication.getInstance().map
                .get( "lastfm_session" );
        
        String username = getIntent().getStringExtra("lastfm.profile.username");
        if( username == null )
            username = session.getName();
        
        Button b = new Button(this);
        b.setOnClickListener( mNewStationListener );
        b.setBackgroundResource( R.drawable.list_station_starter_rest );
        b.setText(R.string.home_newstation);
        b.setTextColor(0xffffffff);
		b.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
        getListView().addHeaderView(b);
        
        mViewHistory = new Stack<Integer>();
		mTabBar = (TabBar) findViewById(R.id.TabBar);
		mViewFlipper = (ViewFlipper) findViewById(R.id.ViewFlipper);
		mNestedViewFlipper = (ViewFlipper) findViewById(R.id.NestedViewFlipper);
		mTabBar.setViewFlipper(mViewFlipper);
		mTabBar.setListener(this);
		mTabBar.addTab("Radio", R.drawable.radio, R.drawable.radio, TAB_RADIO);
		mTabBar.addTab("Profile", R.drawable.profile, R.drawable.profile, TAB_PROFILE);
		mTabBar.setActive(TAB_RADIO);
       
        mMyRecentAdapter = new LastFMStreamAdapter( this );

        new LoadUserTask().execute((Void)null);
        SetupMyStations( session );
        SetupRecentStations();
        RebuildMainMenu();
        
        mProfileList = (ListView)findViewById(R.id.profile_list_view);
        String[] mStrings = new String[]{"Top Artists", "Top Albums", "Top Tracks", "Recently Played", "Events", "Friends"}; // this order must match the ProfileActions enum
        mProfileAdapter = new ListAdapter(Profile.this, mStrings);
        mProfileList.setAdapter(mProfileAdapter); 
        mProfileList.setOnItemClickListener(mProfileClickListener);
        
		getListView().setOnItemSelectedListener(new OnListRowSelectedListener(getListView()));
		((OnListRowSelectedListener)getListView().getOnItemSelectedListener()).setResources(R.drawable.list_item_rest, R.drawable.list_item_focus);
		
		mTopArtistsList = (ListView) findViewById(R.id.topartists_list_view);
		mTopArtistsList.setOnItemSelectedListener(new OnListRowSelectedListener(mTopArtistsList));
		mTopAlbumsList = (ListView) findViewById(R.id.topalbums_list_view);
        mTopAlbumsList.setOnItemSelectedListener(new OnListRowSelectedListener(mTopAlbumsList));
        mTopTracksList = (ListView) findViewById(R.id.toptracks_list_view);
        mTopTracksList.setOnItemSelectedListener(new OnListRowSelectedListener(mTopTracksList));
        mRecentTracksList = (ListView) findViewById(R.id.recenttracks_list_view);
        mRecentTracksList.setOnItemSelectedListener(new OnListRowSelectedListener(mRecentTracksList));
        mEventsList = (ListView) findViewById(R.id.profileevents_list_view);
        mEventsList.setOnItemSelectedListener(new OnListRowSelectedListener(mEventsList));
        mFriendsList = (ListView) findViewById(R.id.profilefriends_list_view);
        mFriendsList.setOnItemSelectedListener(new OnListRowSelectedListener(mFriendsList));
    }
    
    private class LoadUserTask extends UserTask<Void, Void, Boolean> {
        @Override
    	public void onPreExecute() {
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
            
    		try {
    	        Session session = ( Session ) LastFMApplication.getInstance().map
                .get( "lastfm_session" );
				mUser = mServer.getUserInfo( session.getKey() );
    			success = true;
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	/* Set the profile image, maybe? */
        }
    }

    public void tabChanged(int index) {
		//Log.i("Lukasz", "Changed tab to "+text+", index="+index);
	}
	
    private void RebuildMainMenu() 
    {
        mMainAdapter = new SeparatedListAdapter(this);
        mMainAdapter.addSection( getString(R.string.home_mystations), mMyStationsAdapter );
        mMainAdapter.addSection( getString(R.string.home_recentstations), mMyRecentAdapter );
        setListAdapter( mMainAdapter );
        mMainAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void onResume() {
    	SetupRecentStations();
    	RebuildMainMenu();
		super.onResume();
    }
    
    private void SetupRecentStations()
    {
        mMyRecentAdapter.resetList();
        SQLiteDatabase db = null;
        try
        {
            db = this.openOrCreateDatabase( LastFm.DB_NAME, MODE_PRIVATE, null );
            Cursor c = db.rawQuery( "SELECT * FROM "
                    + LastFm.DB_TABLE_RECENTSTATIONS + " ORDER BY Timestamp DESC LIMIT 4", null );
            int urlColumn = c.getColumnIndex( "Url" );
            int nameColumn = c.getColumnIndex( "Name" );
            if ( c.getCount() > 0 )
            {
                c.moveToFirst();
                int i = 0;
                // Loop through all Results
                do
                {
                    i++;
                    String name = c.getString( nameColumn );
                    String url = c.getString( urlColumn );
                    mMyRecentAdapter.putStation( name, url );
                }
                while ( c.moveToNext() );
            }
            c.close();
            db.close();
            mMyRecentAdapter.updateModel();
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
        }

    }

    private void SetupMyStations( final Session session )
    {
        mMyStationsAdapter = new LastFMStreamAdapter( this );
        mMyStationsAdapter.putStation( getString(R.string.home_mylibrary), 
        		"lastfm://user/" + Uri.encode( session.getName() ) + "/personal" );
        mMyStationsAdapter.putStation( getString(R.string.home_myloved), 
        		"lastfm://user/" + Uri.encode( session.getName() ) + "/loved" );
        mMyStationsAdapter.putStation( getString(R.string.home_myrecs), 
        		"lastfm://user/" + Uri.encode( session.getName() ) + "/recommended" );
        mMyStationsAdapter.putStation( getString(R.string.home_myneighborhood), 
        		"lastfm://user/" + Uri.encode( session.getName() ) + "/neighbours" );
        mMyStationsAdapter.updateModel();
    }

    public void onListItemClick( ListView l, View v, int position, long id )
    {
        
    	l.setEnabled(false);
    	l.getOnItemSelectedListener().onItemSelected(l, v, position, id);
    	ViewSwitcher switcher = (ViewSwitcher)v.findViewById(R.id.row_view_switcher);
    	switcher.showNext();
    	LastFMApplication.getInstance().playRadioStation(this, mMainAdapter.getStation(position-1));
    }

    private OnClickListener mNewStationListener = new OnClickListener()
    {

        public void onClick( View v )
        {

            Intent intent = new Intent( Profile.this, NewStation.class );
            startActivity( intent );
        }
    };

    private OnClickListener mNowPlayingListener = new OnClickListener()
    {

        public void onClick( View v )
        {

            Intent intent = new Intent( Profile.this, Player.class );
            startActivity( intent );
        }
    };
    
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if( keyCode == KeyEvent.KEYCODE_BACK )
        {
            if( mViewFlipper.getDisplayedChild() == TAB_PROFILE && !mViewHistory.isEmpty() )
            {
                mProfileAdapter.disableLoadBar();
                mNestedViewFlipper.setDisplayedChild(mViewHistory.pop());
                return true;
            }
            if(event.getRepeatCount() == 0) {
                finish();
                return true;
            }
        }
        return false;
    }
    
    
    private OnItemClickListener mProfileClickListener = new OnItemClickListener()
    {

        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                long id) 
        {
            mProfileAdapter.enableLoadBar(position);
            switch ( position )
            {
            case PROFILE_TOPARTISTS: //"Top Artists"
                new LoadTopArtistsTask().execute((Void)null);
                break;
            case PROFILE_TOPALBUMS: //"Top Albums"
                new LoadTopAlbumsTask().execute((Void)null);
                break;
            case PROFILE_TOPTRACKS: //"Top Tracks"
                new LoadTopTracksTask().execute((Void)null);
                break;
            case PROFILE_RECENTLYPLAYED: //"Recently Played"
                new LoadRecentTracksTask().execute((Void)null);
                break;
            case PROFILE_EVENTS: //"Events"
                new LoadEventsTask().execute((Void)null);
                break;
            case PROFILE_FRIENDS: //"Friends"
                new LoadFriendsTask().execute((Void)null);
                break;
            default: 
                break;
            
            }
            
        }
        
    };
    
    private class LoadTopArtistsTask extends UserTask<Void, Void, Boolean> {
        
        public Boolean doInBackground(Void...params) {
            boolean success = false;
            
            mTopArtistsAdapter = new ListAdapter(Profile.this, getImageCache());
            mTopArtistsList.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> l, View v,
                        int position, long id) {
                    Artist artist = (Artist)mTopArtistsAdapter.getItem(position);
                    mTopArtistsAdapter.enableLoadBar(position);
                    LastFMApplication.getInstance().playRadioStation(Profile.this, "lastfm://artist/"+Uri.encode(artist.getName())+"/similarartists");
                }
                
            });

            try {
                Artist[] topartists = mServer.getUserTopArtists(mUser.getName(), "overall");
                if(topartists.length == 0 )
                    return false;
                ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
                for(int i=0; i< ((topartists.length < 10) ? topartists.length : 10); i++){
                    ListEntry entry = new ListEntry(topartists[i], 
                            R.drawable.artist_icon, 
                            topartists[i].getName(), 
                            topartists[i].getImages()[0].getUrl(),
                            R.drawable.radio_icon);
                    iconifiedEntries.add(entry);
                }
                mTopArtistsAdapter.setSourceIconified(iconifiedEntries);
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
            if(result) {
                mTopArtistsList.setAdapter(mTopArtistsAdapter);
                mTopArtistsList.setOnScrollListener(mTopArtistsAdapter.getOnScrollListener());
            } else {
                String[] strings = new String[]{"No Top Artists"};
                mTopArtistsList.setAdapter(new ArrayAdapter<String>(Profile.this, 
                        R.layout.list_row, R.id.row_label, strings)); 
            }
            mViewHistory.push(mNestedViewFlipper.getDisplayedChild()); // Save the current view
            mNestedViewFlipper.setDisplayedChild(PROFILE_TOPARTISTS + 1);
        }
    }

    private class LoadTopAlbumsTask extends UserTask<Void, Void, Boolean> {
        
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
            
            mTopAlbumsAdapter = new ListAdapter(Profile.this, getImageCache());
            mTopAlbumsList.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> l, View v,
                        int position, long id) {
//                    Album album = (Album) mTopAlbumsAdapter.getItem(position);
                    showDialog(DIALOG_ALBUM);
                }
                
            });

            try {
                Album[] topalbums = mServer.getUserTopAlbums(mUser.getName(), "overall");
                if(topalbums.length == 0 )
                    return false;
                ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
                for(int i=0; i< ((topalbums.length < 10) ? topalbums.length : 10); i++){
                    ListEntry entry = new ListEntry(topalbums[i], 
                            R.drawable.albumart_mp_unknown, 
                            topalbums[i].getTitle(),  
                            topalbums[i].getImages()[0].getUrl(),
                            topalbums[i].getArtist());
                    iconifiedEntries.add(entry);
                }
                mTopAlbumsAdapter.setSourceIconified(iconifiedEntries);
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
            if(result) {
                mTopAlbumsList.setAdapter(mTopAlbumsAdapter);
                mTopAlbumsList.setOnScrollListener(mTopAlbumsAdapter.getOnScrollListener());
            } else {
                String[] strings = new String[]{"No Top Albums"};
                mTopAlbumsList.setAdapter(new ArrayAdapter<String>(Profile.this, 
                        R.layout.list_row, R.id.row_label, strings)); 
            }
            mViewHistory.push(mNestedViewFlipper.getDisplayedChild()); // Save the current view
            mNestedViewFlipper.setDisplayedChild(PROFILE_TOPALBUMS + 1); 
        }
    }
    
    private class LoadTopTracksTask extends UserTask<Void, Void, Boolean> {

        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;

            mTopTracksAdapter = new ListAdapter(Profile.this, getImageCache());
            mTopTracksList.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> l, View v,
                        int position, long id) {
                    Track track = (Track) mTopTracksAdapter.getItem(position);
                    showDialog(DIALOG_TRACK);
                }

            });

            try {
                Track[] toptracks = mServer.getUserTopTracks(mUser.getName(), "overall");
                if(toptracks.length == 0 )
                    return false;
                ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
                for(int i=0; i< ((toptracks.length < 10) ? toptracks.length : 10); i++){
                    ListEntry entry = new ListEntry(toptracks[i],
                            R.drawable.song_icon,
                            toptracks[i].getName(), 
                            toptracks[i].getImages().length == 0 ? "" : toptracks[i].getImages()[0].getUrl(), // some tracks don't have images
                            toptracks[i].getArtist().getName());
                    iconifiedEntries.add(entry);
                }
                mTopTracksAdapter.setSourceIconified(iconifiedEntries);
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
            if(result) {
                mTopTracksList.setAdapter(mTopTracksAdapter);
                mTopTracksList.setOnScrollListener(mTopTracksAdapter.getOnScrollListener());
            } else {
                String[] strings = new String[]{"No Top Tracks"};
                mTopTracksList.setAdapter(new ArrayAdapter<String>(Profile.this,
                        R.layout.list_row, R.id.row_label, strings));
            }
            mViewHistory.push(mNestedViewFlipper.getDisplayedChild()); // Save the current view
            mNestedViewFlipper.setDisplayedChild(PROFILE_TOPTRACKS + 1);
        }
    }
    
    private class LoadRecentTracksTask extends UserTask<Void, Void, Boolean> {

        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;

            mRecentTracksAdapter = new ListAdapter(Profile.this, getImageCache());
            mRecentTracksList.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> l, View v,
                        int position, long id) {
//                    Track track = (Track) mRecentTracksAdapter.getItem(position);
                    showDialog(DIALOG_TRACK);
                }

            });

            try {
                Track[] recenttracks = mServer.getUserRecentTracks(mUser.getName(), 10);
                if(recenttracks.length == 0 )
                    return false;
                ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
                for(int i=0; i< ((recenttracks.length < 10) ? recenttracks.length : 10); i++){
                    ListEntry entry = new ListEntry(recenttracks[i],
                            R.drawable.song_icon,
                            recenttracks[i].getName(), 
                            recenttracks[i].getImages().length == 0 ? "" : recenttracks[i].getImages()[0].getUrl(), // some tracks don't have images
                            recenttracks[i].getArtist().getName());
                    iconifiedEntries.add(entry);
                }
                mRecentTracksAdapter.setSourceIconified(iconifiedEntries);
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
            if(result) {
                mRecentTracksList.setAdapter(mRecentTracksAdapter);
                mRecentTracksList.setOnScrollListener(mRecentTracksAdapter.getOnScrollListener());
            } else {
                String[] strings = new String[]{"No Recent Tracks"};
                mRecentTracksList.setAdapter(new ArrayAdapter<String>(Profile.this,
                        R.layout.list_row, R.id.row_label, strings));
            }
            mViewHistory.push(mNestedViewFlipper.getDisplayedChild()); // Save the current view
            mNestedViewFlipper.setDisplayedChild(PROFILE_RECENTLYPLAYED + 1);
        }
    }
    
    private class LoadEventsTask extends UserTask<Void, Void, Boolean> {

        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;

            mEventsAdapter = new EventListAdapter(Profile.this);
            try {
                fm.last.api.Event[] events = mServer.getUserEvents(mUser.getName());
                mEventsAdapter.setEventsSource(events);
                if(events.length > 0)
                    success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
            if(result) {
                mEventsList.setAdapter(mEventsAdapter);
                //mEventsList.setOnScrollListener(mEventsAdapter.getOnScrollListener());
                mEventsList.setOnItemClickListener(mEventOnItemClickListener);
            } else {
                String[] strings = new String[]{"No Upcoming Events"};
                mEventsList.setAdapter(new ArrayAdapter<String>(Profile.this,
                        R.layout.list_row, R.id.row_label, strings));
            }
            mViewHistory.push(mNestedViewFlipper.getDisplayedChild()); // Save the current view
            mNestedViewFlipper.setDisplayedChild(PROFILE_EVENTS + 1);
        }
    }
    
    private OnItemClickListener mEventOnItemClickListener = new OnItemClickListener(){

        public void onItemClick(final AdapterView<?> parent, final View v,
                final int position, long id) {
            Intent intent = new Intent( Profile.this, fm.last.android.activity.Event.class );
            Event event = (Event)mEventsAdapter.getItem(position);
            intent.putExtra("lastfm.event.id", Integer.toString(event.getId()));
            intent.putExtra("lastfm.event.title", event.getTitle());
            String artists = "";
            for(String artist : event.getArtists()) {
                if(artists.length() > 0)
                    artists += ", ";
                artists += artist;
            }
            for(ImageUrl image : event.getImages()) {
                if(image.getSize().contentEquals("large"))
                    intent.putExtra("lastfm.event.poster", image.getUrl());
            }
            intent.putExtra("lastfm.event.artists", artists);
            intent.putExtra("lastfm.event.venue", event.getVenue().getName());
            intent.putExtra("lastfm.event.street", event.getVenue().getLocation().getStreet());
            intent.putExtra("lastfm.event.month", new SimpleDateFormat("MMM").format(event.getStartDate()));
            intent.putExtra("lastfm.event.day", new SimpleDateFormat("d").format(event.getStartDate()));
            try {
                Event[] events = mServer.getUserEvents(((Session)LastFMApplication.getInstance().map.get("lastfm_session")).getName());
                for(Event e : events) {
                    System.out.printf("Comparing id %d (%s) to %d (%s)\n",e.getId(),e.getTitle(),event.getId(),event.getTitle());
                    if(e.getId() == event.getId()) {
                        System.out.printf("Matched! Status: %s\n", e.getStatus());
                        intent.putExtra("lastfm.event.status", e.getStatus());
                        break;
                    }
                        
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            startActivity( intent );
        }

    };
    
    private class LoadFriendsTask extends UserTask<Void, Void, Boolean> {

        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;

            mFriendsAdapter = new ListAdapter(Profile.this, getImageCache());
            try {
                User[] friends = mServer.getFriends(mUser.getName(), null, null).getFriends();
                if(friends.length == 0 )
                    return false;
                ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
                for(int i=0; i< ((friends.length < 10) ? friends.length : 10); i++){
                    ListEntry entry = new ListEntry(friends[i],
                            R.drawable.profile_unknown,
                            friends[i].getName(), 
                            friends[i].getImages().length == 0 ? "" : friends[i].getImages()[0].getUrl()); // some tracks don't have images
                    iconifiedEntries.add(entry);
                }
                mFriendsAdapter.setSourceIconified(iconifiedEntries);
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
            mFriendsList.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> l, View v,
                        int position, long id) {
                    User user = (User) mFriendsAdapter.getItem(position);
                    Intent profileIntent = new Intent(Profile.this, fm.last.android.activity.Profile.class);
                    profileIntent.putExtra("lastfm.profile.username", user.getName());
                    startActivity(profileIntent);
                }
            });
            if(result) {
                mFriendsList.setAdapter(mFriendsAdapter);
                mFriendsList.setOnScrollListener(mFriendsAdapter.getOnScrollListener());
            } else {
                String[] strings = new String[]{"No Friends Retrieved"};
                mFriendsList.setAdapter(new ArrayAdapter<String>(Profile.this,
                        R.layout.list_row, R.id.row_label, strings));
            }
            mViewHistory.push(mNestedViewFlipper.getDisplayedChild()); // Save the current view
            mNestedViewFlipper.setDisplayedChild(PROFILE_FRIENDS + 1);
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        // Parameters for menu.add are:
        // group -- Not used here.
        // id -- Used only when you want to handle and identify the click yourself.
        // title
        MenuItem profile = menu.add(Menu.NONE, 1, Menu.NONE, "Profile");
        profile.setIcon(R.drawable.profile_unknown);
        MenuItem logout = menu.add(Menu.NONE, 0, Menu.NONE, "Logout");
        logout.setIcon(R.drawable.logout);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
        case 0:
            logout();
            return true;
        case 1:
            // TODO Show profile
            return true;
        }
        return false;
    }
    
    protected Dialog onCreateDialog(int id) 
    {
        final int dialogId = id;
        if( mDialogList == null )
            mDialogList = new ListView(Profile.this);
        mDialogAdapter = new ListAdapter(Profile.this, getImageCache());
        ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();

        ListEntry entry = new ListEntry("Browse in Amazon Store", R.drawable.song_icon, "Browse in Amazon Store"); // TODO need amazon icon
        iconifiedEntries.add(entry);
        entry = new ListEntry("Listen to Similar Artists", R.drawable.radio_icon, "Listen to Similar Artists");
        iconifiedEntries.add(entry);
        switch (id) {
        case DIALOG_ALBUM: 
            entry = new ListEntry("Tag Album", R.drawable.tag_icon, "Tag Album");
            iconifiedEntries.add(entry);
            break;
        case DIALOG_TRACK:
            entry = new ListEntry("Tag Track", R.drawable.tag_icon, "Tag Track");
            iconifiedEntries.add(entry);
            break;
            default:
                break;
        }
        mDialogAdapter.setSourceIconified(iconifiedEntries);
        mDialogList.setAdapter(mDialogAdapter);
        mDialogList.setOnScrollListener(mDialogAdapter.getOnScrollListener());
        mDialogList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> l, View v, int position, long id) 
            {
               switch (position)
               {
                   case 0: // Amazon
                       break;
                   case 1: // Play
                       
                       break;
                   case 2: // Tag
                       break;
               }
               dismissDialog(dialogId);
            }
            });
        return new AlertDialog.Builder(Profile.this).setTitle("Select Action").setView(mDialogList).create();
    }
    
    private void logout()
    {
        SharedPreferences settings = getSharedPreferences( LastFm.PREFS, 0 );
        SharedPreferences.Editor editor = settings.edit();
        editor.remove( "lastfm_user" );
        editor.remove( "lastfm_pass" );
        editor.commit();
        SQLiteDatabase db = null;
        try
        {
            db = this.openOrCreateDatabase( LastFm.DB_NAME, MODE_PRIVATE, null );
            db.execSQL( "DROP TABLE IF EXISTS "
                            + LastFm.DB_TABLE_RECENTSTATIONS );
            db.close();

            if(LastFMApplication.getInstance().player != null)
        		LastFMApplication.getInstance().player.stop();
        }
        catch ( Exception e )
        {
            System.out.println( e.getMessage() );
        }
        Intent intent = new Intent( Profile.this, LastFm.class );
        startActivity( intent );
        finish();
    }
    
    private ImageCache getImageCache(){
        if(mImageCache == null){
            mImageCache = new ImageCache();
        }
        return mImageCache;
    }


}
