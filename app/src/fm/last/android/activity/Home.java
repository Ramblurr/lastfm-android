package fm.last.android.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

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
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.LastFm;
import fm.last.android.OnListRowSelectedListener;
import fm.last.android.R;
import fm.last.android.RemoteImageHandler;
import fm.last.android.RemoteImageView;
import fm.last.android.Worker;
import fm.last.android.adapter.LastFMStreamAdapter;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.adapter.SeparatedListAdapter;
import fm.last.android.player.RadioPlayerService;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.UserTask;
import fm.last.android.widget.TabBar;
import fm.last.android.widget.TabBarListener;
import fm.last.android.widget.NavBar;
import fm.last.android.widget.NavBarListener;
import fm.last.api.Album;
import fm.last.api.Artist;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.api.Track;
import fm.last.api.User;
import fm.last.api.ImageUrl;

public class Home extends ListActivity implements TabBarListener,NavBarListener
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

    private SeparatedListAdapter mMainAdapter;
    private SeparatedListAdapter mProfileAdapter;
    private LastFMStreamAdapter mMyStationsAdapter;
    private LastFMStreamAdapter mMyRecentAdapter;
    private Worker mProfileImageWorker;
    private RemoteImageHandler mProfileImageHandler;
    private RemoteImageView mProfileImage;
    private User mUser;
	LastFmServer mServer = AndroidLastFmServerFactory.getServer();

	TabBar mTabBar;
	ViewFlipper mViewFlipper;
	ViewFlipper mNestedViewFlipper;
	ListView mProfileList;
    private Stack<Integer> mViewHistory;
	
	View previousSelectedView = null;
	
	private ImageCache mImageCache;
	ListView mTopArtistsList;
	private ListAdapter mTopArtistsAdapter;
	ListView mTopAlbumsList;
    private ListAdapter mTopAlbumsAdapter;
    ListView mTopTracksList;
    private ListAdapter mTopTracksAdapter;
	
	public int test = 5;
	
    @Override
    public void onCreate( Bundle icicle )
    {

        super.onCreate( icicle );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.home );
        Session session = ( Session ) LastFMApplication.getInstance().map
                .get( "lastfm_session" );
        TextView tv = ( TextView ) findViewById( R.id.home_usersname );
        if(tv != null)
        	tv.setText( session.getName() );
        
        NavBar n = ( NavBar ) findViewById( R.id.NavBar );
        if(n != null)
        	n.setListener(this);

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
		mTabBar.addTab("Radio", TAB_RADIO);
		mTabBar.addTab("Profile", TAB_PROFILE);
		mTabBar.setActive(TAB_RADIO);
       
        mProfileImage = ( RemoteImageView ) findViewById( R.id.home_profileimage );

        mProfileImageWorker = new Worker( "profile image worker" );
        mProfileImageHandler = new RemoteImageHandler( mProfileImageWorker
                .getLooper(), mHandler );

        SetupProfile( session );
        mMyRecentAdapter = new LastFMStreamAdapter( this );

        SetupMyStations( session );
        SetupRecentStations();
        RebuildMainMenu();
        
        mProfileList = (ListView)findViewById(R.id.profile_list_view);
        String[] mStrings = new String[]{"Top Artists", "Top Albums", "Top Tracks", "Recently Played", "Events", "Friends"}; // this order must match the ProfileActions enum
        mProfileList.setAdapter(new ArrayAdapter<String>(this, 
                R.layout.list_row, R.id.row_label, mStrings)); 
        mProfileList.setOnItemClickListener(mProfileClickListener);
        
		getListView().setOnItemSelectedListener(new OnListRowSelectedListener(getListView()));
		((OnListRowSelectedListener)getListView().getOnItemSelectedListener()).setResources(R.drawable.list_item_rest, R.drawable.list_item_focus);
		
		mTopArtistsList = (ListView) findViewById(R.id.topartists_list_view);
		mTopArtistsList.setOnItemSelectedListener(new OnListRowSelectedListener(mTopArtistsList));
		mTopAlbumsList = (ListView) findViewById(R.id.topalbums_list_view);
        mTopAlbumsList.setOnItemSelectedListener(new OnListRowSelectedListener(mTopAlbumsList));
        mTopTracksList = (ListView) findViewById(R.id.toptracks_list_view);
        mTopTracksList.setOnItemSelectedListener(new OnListRowSelectedListener(mTopTracksList));
    }
    
	public void tabChanged(int index) {
		//Log.i("Lukasz", "Changed tab to "+text+", index="+index);
	}
	
	public void backClicked(View child) {
		logout();
	}

	public void middleClicked(View child, int index) {
		if(index == 1) { //Now Playing button (portrait)
			mNowPlayingListener.onClick(child);
		}
	}

	public void forwardClicked(View child) {
	}

	
    @Override
    public void onStart()
    {
        IntentFilter f = new IntentFilter();
        f.addAction( RadioPlayerService.STATION_CHANGED );
        registerReceiver( mStatusListener, f );
        super.onStart();
    }

    @Override
    public void onDestroy() {
    	unregisterReceiver( mStatusListener );
    	super.onDestroy();
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
    	try {
			if(LastFMApplication.getInstance().player.isPlaying()) {
				if(mProfileImage != null)
					mProfileImage.setVisibility(View.GONE);
				findViewById( R.id.now_playing ).setVisibility(View.VISIBLE);
		        Button b = (Button)findViewById(R.id.now_playing);
		        b.setOnClickListener( mNowPlayingListener );
			} else {
				if(mProfileImage != null)
					mProfileImage.setVisibility(View.VISIBLE);
				findViewById( R.id.now_playing ).setVisibility(View.GONE);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onResume();
    }
    
    private BroadcastReceiver mStatusListener = new BroadcastReceiver()
    {

        @Override
        public void onReceive( Context context, Intent intent )
        {

            /*String action = intent.getAction();
            if ( action.equals( RadioPlayerService.STATION_CHANGED ) )
            {
            }
            if ( action.equals( RadioPlayerService.PLAYBACK_FINISHED ) )
            {
            	if(mProfileImage != null)
            		mProfileImage.setVisibility(View.VISIBLE);
            	findViewById( R.id.now_playing ).setVisibility(View.GONE);
            }*/
        }
    };
    
    
    private void SetupProfile( final Session session )
    {

        final Handler uiThreadCallback = new Handler();
        final Runnable runInUIThread = new Runnable()
        {

            public void run()
            {

                FinishSetupProfile();
            }
        };

        new Thread()
        {

            @Override
            public void run()
            {
                try {
					mUser = mServer.getUserInfo( session.getKey() );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                uiThreadCallback.post( runInUIThread );
            }
        }.start();
    }

    private void FinishSetupProfile()
    {
        if( mUser == null)
            return; //TODO HANDLE
    	ImageUrl[] images = mUser.getImages();
        if ( images.length > 0 )
        {
            mProfileImageHandler
                    .removeMessages( RemoteImageHandler.GET_REMOTE_IMAGE );
            mProfileImageHandler
                    .obtainMessage( RemoteImageHandler.GET_REMOTE_IMAGE,
                            images[0].getUrl() ).sendToTarget();
        }
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

            Intent intent = new Intent( Home.this, NewStation.class );
            startActivity( intent );
        }
    };

    private OnClickListener mNowPlayingListener = new OnClickListener()
    {

        public void onClick( View v )
        {

            Intent intent = new Intent( Home.this, Player.class );
            startActivity( intent );
        }
    };
    
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if( keyCode == KeyEvent.KEYCODE_BACK )
        {
            if( mViewFlipper.getDisplayedChild() == TAB_PROFILE && !mViewHistory.isEmpty() )
            {
                mNestedViewFlipper.setDisplayedChild(mViewHistory.pop());
                return true;
            }
        }
        return false;
    }
    
    
    private OnItemClickListener mProfileClickListener = new OnItemClickListener()
    {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                long id) 
        {
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
                break;
            case PROFILE_EVENTS: //"Events"
                break;
            case PROFILE_FRIENDS: //"Friends"
                break;
            default: 
                break;
            
            }
            
        }
        
    };
    
    private class LoadTopArtistsTask extends UserTask<Void, Void, Boolean> {


        @Override
        public void onPreExecute() {
            String[] strings = new String[]{"Loading..."};
            mTopArtistsList.setOnItemClickListener(null);
            mTopArtistsList.setAdapter(new ArrayAdapter<String>(Home.this, 
                    R.layout.list_row, R.id.row_label, strings)); 
            mViewHistory.push(mNestedViewFlipper.getDisplayedChild()); // Save the current view
            mNestedViewFlipper.setDisplayedChild(PROFILE_TOPARTISTS + 1); 
        }
        
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
            
            mTopArtistsAdapter = new ListAdapter(Home.this, getImageCache());
            mTopArtistsList.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> l, View v,
                        int position, long id) {
                    Artist artist = (Artist)mTopArtistsAdapter.getItem(position);
                    mTopArtistsAdapter.enableLoadBar(position);
                    LastFMApplication.getInstance().playRadioStation(Home.this, "lastfm://artist/"+Uri.encode(artist.getName())+"/similarartists");
                }
                
            });

            try {
                Artist[] topartists = mServer.getUserTopArtists(mUser.getName(), "overall");
                ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
                for(int i=0; i< ((topartists.length < 10) ? topartists.length : 10); i++){
                    ListEntry entry = new ListEntry(topartists[i], 
                            R.drawable.albumart_mp_unknown, 
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
                mTopArtistsList.setAdapter(new ArrayAdapter<String>(Home.this, 
                        R.layout.list_row, R.id.row_label, strings)); 
            }
        }
    }

    private class LoadTopAlbumsTask extends UserTask<Void, Void, Boolean> {


        @Override
        public void onPreExecute() {
            String[] strings = new String[]{"Loading..."};
            mTopAlbumsList.setOnItemClickListener(null);
            mTopAlbumsList.setAdapter(new ArrayAdapter<String>(Home.this, 
                    R.layout.list_row, R.id.row_label, strings)); 
            mViewHistory.push(mNestedViewFlipper.getDisplayedChild()); // Save the current view
            mNestedViewFlipper.setDisplayedChild(PROFILE_TOPALBUMS + 1); 
        }
        
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
            
            mTopAlbumsAdapter = new ListAdapter(Home.this, getImageCache());
            mTopAlbumsList.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> l, View v,
                        int position, long id) {
                    Album album = (Album) mTopAlbumsAdapter.getItem(position);
                    /*mTopAlbumsAdapter.enableLoadBar(position);
                    LastFMApplication.getInstance().playRadioStation(Home.this, "lastfm://artist/"+Uri.encode(album.getArtist())+"/similarartists", false);*/
                    Toast.makeText( Home.this, "Clicked " + album.getTitle(), Toast.LENGTH_LONG ).show();
                }
                
            });

            try {
                Album[] topalbums = mServer.getUserTopAlbums(mUser.getName(), "overall");
                ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
                for(int i=0; i< ((topalbums.length < 10) ? topalbums.length : 10); i++){
                    ListEntry entry = new ListEntry(topalbums[i], 
                            R.drawable.albumart_mp_unknown, 
                            topalbums[i].getTitle(), //TODO this should be prettified somehow  
                            topalbums[i].getImages()[0].getUrl(),
                            R.drawable.radio_icon); //TODO different icon
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
                mTopAlbumsList.setAdapter(new ArrayAdapter<String>(Home.this, 
                        R.layout.list_row, R.id.row_label, strings)); 
            }
        }
    }
    
    private class LoadTopTracksTask extends UserTask<Void, Void, Boolean> {


        @Override
        public void onPreExecute() {
            String[] strings = new String[]{"Loading..."};
            mTopTracksList.setOnItemClickListener(null);
            mTopTracksList.setAdapter(new ArrayAdapter<String>(Home.this,
                    R.layout.list_row, R.id.row_label, strings));
            mViewHistory.push(mNestedViewFlipper.getDisplayedChild()); // Save the current view
            mNestedViewFlipper.setDisplayedChild(PROFILE_TOPTRACKS + 1);
        }

        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;

            mTopTracksAdapter = new ListAdapter(Home.this, getImageCache());
            mTopTracksList.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> l, View v,
                        int position, long id) {
                    Track track = (Track) mTopTracksAdapter.getItem(position);
                    /*mTopTracksAdapter.enableLoadBar(position);
                    LastFMApplication.getInstance().playRadioStation(Home.this, "lastfm://artist/"+Uri.encode(album.getArtist())+"/similarartists", false);*/
                    Toast.makeText( Home.this, "Clicked " + track.getName(), Toast.LENGTH_LONG ).show();
                }

            });

            try {
                Track[] toptracks = mServer.getUserTopTracks(mUser.getName(), "overall");
                ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
                for(int i=0; i< ((toptracks.length < 10) ? toptracks.length : 10); i++){
                    ListEntry entry = new ListEntry(toptracks[i],
                            R.drawable.albumart_mp_unknown,
                            toptracks[i].getName(), //TODO this should be prettified somehow
                            toptracks[i].getImages().length == 0 ? "" : toptracks[i].getImages()[0].getUrl(),
                            R.drawable.radio_icon); //TODO different icon
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
                mTopTracksList.setAdapter(new ArrayAdapter<String>(Home.this,
                        R.layout.list_row, R.id.row_label, strings));
            }
        }
    }
    
    private final Handler mHandler = new Handler()
    {

        public void handleMessage( Message msg )
        {

            switch ( msg.what )
            {
            case RemoteImageHandler.REMOTE_IMAGE_DECODED:
            	if(mProfileImage != null) {
                    mProfileImage.setArtwork( ( Bitmap ) msg.obj );
                    mProfileImage.invalidate();
            	}
                break;

            default:
                break;
            }
        }
    };
    
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
        Intent intent = new Intent( Home.this, LastFm.class );
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
