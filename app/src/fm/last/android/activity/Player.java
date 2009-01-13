package fm.last.android.activity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.LastFm;
import fm.last.android.OnListRowSelectedListener;
import fm.last.android.R;
import fm.last.android.RemoteImageHandler;
import fm.last.android.RemoteImageView;
import fm.last.android.Worker;
import fm.last.android.adapter.EventListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.OnEventRowSelectedListener;
import fm.last.android.player.RadioPlayerService;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.Rotate3dAnimation;
import fm.last.android.utils.UserTask;
import fm.last.android.widget.TabBar;
import fm.last.api.Album;
import fm.last.api.Artist;
import fm.last.api.Event;
import fm.last.api.ImageUrl;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Tag;
import fm.last.api.User;
import fm.last.api.WSError;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;

public class Player extends Activity
{

	private ImageButton mStopButton;
	private ImageButton mNextButton;
	private ImageButton mOntourButton;
	private RemoteImageView mAlbum;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private TextView mArtistName;
	private TextView mTrackName;
	private TextView mStationName;
	private ProgressBar mProgress;
	private long mDuration;
	private boolean paused;
    private ProgressDialog mProgressDialog;
    private ViewFlipper mDetailFlipper;

	private static final int REFRESH = 1;

	private final static int TAB_BIO = 0;
	private final static int TAB_SIMILAR = 1;
	private final static int TAB_TAGS = 2;
	private final static int TAB_EVENTS = 3;
	private final static int TAB_LISTENERS = 4;

	LastFmServer mServer = AndroidLastFmServerFactory.getServer();

	private ImageCache mImageCache;
	private String mBio;
	private ListAdapter mSimilarAdapter;
	private ListAdapter mFanAdapter;
	private ListAdapter mTagAdapter;
	private EventListAdapter mEventAdapter;
	private String mLastInfoArtist = "";
	
	TextView mTextView;
	TabBar mTabBar;
	ViewFlipper mViewFlipper;
	WebView mWebView;
	ListView mSimilarList;
	ListView mTagList;
	ListView mFanList;
	ListView mEventList;


	private Worker mAlbumArtWorker;
	private RemoteImageHandler mAlbumArtHandler;
	private IntentFilter mIntentFilter;

	@Override
	public void onCreate( Bundle icicle )
	{

		super.onCreate( icicle );
		requestWindowFeature( Window.FEATURE_NO_TITLE );

		setContentView( R.layout.audio_player );

		mCurrentTime = ( TextView ) findViewById( R.id.currenttime );
		mTotalTime = ( TextView ) findViewById( R.id.totaltime );
		mProgress = ( ProgressBar ) findViewById( android.R.id.progress );
		mProgress.setMax( 1000 );
		mAlbum = ( RemoteImageView ) findViewById( R.id.album );
		mArtistName = ( TextView ) findViewById( R.id.track_artist );
		mTrackName = ( TextView ) findViewById( R.id.track_title );
		
		mStopButton = ( ImageButton ) findViewById( R.id.stop );
		mStopButton.requestFocus();
		mStopButton.setOnClickListener( mStopListener );
		mNextButton = ( ImageButton ) findViewById( R.id.skip );
		mNextButton.setOnClickListener( mNextListener );
		mOntourButton = ( ImageButton ) findViewById( R.id.ontour );
		mOntourButton.setOnClickListener( mOntourListener );
		mDetailFlipper = ( ViewFlipper ) findViewById( R.id.playback_detail_flipper );
		mDetailFlipper.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
		mTabBar = (TabBar) findViewById(R.id.TabBar);
		mViewFlipper = (ViewFlipper) findViewById(R.id.ViewFlipper);
		mWebView = (WebView) findViewById(R.id.webview);
		mSimilarList = (ListView) findViewById(R.id.similar_list_view);
		mSimilarList.setOnItemSelectedListener(new OnListRowSelectedListener(mSimilarList));
		mTagList = (ListView) findViewById(R.id.tags_list_view);
		mTagList.setOnItemSelectedListener(new OnListRowSelectedListener(mTagList));
		mFanList = (ListView) findViewById(R.id.listeners_list_view);
		mFanList.setOnItemSelectedListener(new OnListRowSelectedListener(mFanList));
		mEventList = (ListView) findViewById(R.id.events_list_view);
		mEventList.setOnItemSelectedListener(new OnEventRowSelectedListener(mEventList));
        ((OnEventRowSelectedListener)mEventList.getOnItemSelectedListener()).setResources(R.drawable.list_item_rest_fullwidth, R.drawable.list_item_focus_fullwidth);

		mTabBar.setViewFlipper(mViewFlipper);
		mTabBar.addTab("Bio", R.drawable.bio, R.drawable.bio, TAB_BIO);
		mTabBar.addTab("Similar", R.drawable.similar_artists, R.drawable.similar_artists, TAB_SIMILAR);
		mTabBar.addTab("Tags", R.drawable.tags, R.drawable.tags, TAB_TAGS);
		mTabBar.addTab("Events", R.drawable.events, R.drawable.events, TAB_EVENTS);
		mTabBar.addTab("Listeners", R.drawable.top_listeners, R.drawable.top_listeners, TAB_LISTENERS);

		mAlbumArtWorker = new Worker( "album art worker" );
		mAlbumArtHandler = new RemoteImageHandler( mAlbumArtWorker.getLooper(), mHandler );

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction( RadioPlayerService.META_CHANGED );
		mIntentFilter.addAction( RadioPlayerService.PLAYBACK_FINISHED );
		mIntentFilter.addAction( RadioPlayerService.PLAYBACK_STATE_CHANGED );
		mIntentFilter.addAction( RadioPlayerService.STATION_CHANGED );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.player, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public void showMetadata() {
    	if(!mLastInfoArtist.contentEquals(mArtistName.getText())) {
    		mLastInfoArtist = mArtistName.getText().toString();
			new LoadBioTask().execute((Void)null);
			new LoadSimilarTask().execute((Void)null);
			new LoadListenersTask().execute((Void)null);
			new LoadTagsTask().execute((Void)null);
    	}
		mTabBar.setActive(TAB_BIO);
		mDetailFlipper.showNext();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.info_menu_item:
			showMetadata();
			break;
		case R.id.buy_menu_item:
            Intent intent = new Intent( Intent.ACTION_SEARCH );
            intent.setComponent(new ComponentName("com.amazon.mp3","com.amazon.mp3.android.client.SearchActivity"));
            intent.putExtra(SearchManager.QUERY, "test");
            startActivity( intent );
            break;
		case R.id.tag_menu_item:
			fireTagActivity();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void fireTagActivity(){
		String artist = null;
		String track = null;

		try {
			artist = LastFMApplication.getInstance().player.getArtistName();
			track = LastFMApplication.getInstance().player.getTrackName();
			Intent myIntent = new Intent(this, fm.last.android.activity.Tag.class);
			myIntent.putExtra("lastfm.artist", artist);
			myIntent.putExtra("lastfm.track", track);
			startActivity(myIntent);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		paused = false;
		try {
			if(mStationName != null)
				mStationName.setText( LastFMApplication.getInstance().player.getStationName() );
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateTrackInfo();
		long next = refreshNow();
		queueNextRefresh( next );
	}

	@Override
	public void onStop()
	{

		paused = true;
		mHandler.removeMessages( REFRESH );

		
		super.onStop();
	}

	@Override
	public void onSaveInstanceState( Bundle outState )
	{

		outState.putBoolean( "configchange", getChangingConfigurations() != 0 );
		super.onSaveInstanceState( outState );
	}

	@Override
	protected void onPause() {
		unregisterReceiver( mStatusListener );
		super.onPause();
	}

	@Override
	public void onResume()
	{
		registerReceiver( mStatusListener, mIntentFilter );
		super.onResume();
		updateTrackInfo();
	}

	@Override
	public void onDestroy()
	{
		mAlbumArtWorker.quit();
		super.onDestroy();
	}

	/*private View.OnClickListener mLoveListener = new View.OnClickListener()
    {

        public void onClick( View v )
        {

            if ( LastFMApplication.getInstance().player == null )
                return;
            try
            {
                LastFMApplication.getInstance().player.love();
            }
            catch ( RemoteException ex )
            {
                System.out.println( ex.getMessage() );
            }
        }
    };*/

	/*private View.OnClickListener mBanListener = new View.OnClickListener()
    {

        public void onClick( View v )
        {

            if ( LastFMApplication.getInstance().player == null )
                return;
            try
            {
                LastFMApplication.getInstance().player.ban();
            }
            catch ( RemoteException ex )
            {
                System.out.println( ex.getMessage() );
            }
        }
    };*/

	private View.OnClickListener mNextListener = new View.OnClickListener()
	{

		public void onClick( View v )
		{

			if ( LastFMApplication.getInstance().player == null )
				return;
			Thread t = new Thread() {
				public void run() {
					try
					{  
						LastFMApplication.getInstance().player.skip();
					}
					catch ( RemoteException ex )
					{
						System.out.println( ex.getMessage() );
					}
				}
			};
			t.start();
		}
	};
	
	private View.OnClickListener mOntourListener = new View.OnClickListener(){

		public void onClick(View v) {
			showMetadata();
			mTabBar.setActive(TAB_EVENTS);
		}
		
	};
	
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if( keyCode == KeyEvent.KEYCODE_BACK )
        {
            if( mDetailFlipper.getDisplayedChild() == 1 )
            {
            	mDetailFlipper.showPrevious();
                return true;
            } else {
            	finish();
            	return true;
            }
        }
        return false;
    }
	
	private View.OnClickListener mStopListener = new View.OnClickListener()
	{

		public void onClick( View v )
		{

			if ( LastFMApplication.getInstance().player == null )
				return;
			try
			{
				LastFMApplication.getInstance().player.stop();
			}
			catch ( RemoteException ex )
			{
				System.out.println( ex.getMessage() );
			}
			finish();
		}
	};

	private BroadcastReceiver mStatusListener = new BroadcastReceiver()
	{

		@Override
		public void onReceive( Context context, Intent intent )
		{

			String action = intent.getAction();
			if ( action.equals( RadioPlayerService.META_CHANGED ) )
			{
				// redraw the artist/title info and
				// set new max for progress bar
				updateTrackInfo();
			}
			else if ( action.equals( RadioPlayerService.PLAYBACK_FINISHED ) )
			{
				finish();
			}
			else if ( action.equals( RadioPlayerService.STATION_CHANGED ) )
			{
				try
				{
					if(mStationName != null)
						mStationName.setText( LastFMApplication.getInstance().player.getStationName() );
				}
				catch ( RemoteException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if ( action.equals( RadioPlayerService.PLAYBACK_ERROR ) )
			{
				// TODO add a skip counter and try to skip 3 times before display an error message
				try
				{
					WSError error = LastFMApplication.getInstance().player.getError();
					if(error != null) {
						LastFMApplication.getInstance().presentError(context, error);
					} else {
    					LastFMApplication.getInstance().presentError(context, getResources().getString(R.string.ERROR_PLAYBACK_FAILED_TITLE),
    							getResources().getString(R.string.ERROR_PLAYBACK_FAILED));
					}
				}
				catch ( RemoteException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	private void updateTrackInfo()
	{

		System.out.println( "Updating track info" );
		if ( LastFMApplication.getInstance().player == null )
		{
			return;
		}
		try
		{
			String artistName = LastFMApplication.getInstance().player.getArtistName();
			mArtistName.setText( artistName );
			mTrackName.setText( LastFMApplication.getInstance().player.getTrackName() );
			new LoadAlbumArtTask().execute((Void)null);
			
			// fetching artist events (On Tour indicator & Events tab)
			if(!mLoadEventsTaskArtist.equals(artistName)){
				mLoadEventsTaskArtist = artistName;
				new LoadEventsTask().execute((Void)null);
			}
		}
		catch ( RemoteException ex )
		{
			finish();
		}
	}

	private void queueNextRefresh( long delay )
	{

		if ( !paused )
		{
			Message msg = mHandler.obtainMessage( REFRESH );
			mHandler.removeMessages( REFRESH );
			mHandler.sendMessageDelayed( msg, delay );
		}
	}

	private long refreshNow()
	{

		if ( LastFMApplication.getInstance().player == null )
			return 500;
		try
		{
			mDuration = LastFMApplication.getInstance().player.getDuration();
			long pos = LastFMApplication.getInstance().player.getPosition();
			long remaining = 1000 - ( pos % 1000 );
			if ( ( pos >= 0 ) && ( mDuration > 0 )  && ( pos <= mDuration ))
			{
				mCurrentTime.setText( makeTimeString( this, pos / 1000 ) );
				mTotalTime.setText( makeTimeString( this, mDuration / 1000 ) );
				mProgress.setProgress( ( int ) ( 1000 * pos / mDuration ) );
				if(mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
			}
			else
			{
				mCurrentTime.setText( "--:--" );
				mTotalTime.setText( "--:--" );
				mProgress.setProgress( 0 );
				if(mProgressDialog == null) {
					mProgressDialog = ProgressDialog.show(this, "", "Buffering", true, false);
				}
			}
			// return the number of milliseconds until the next full second, so
			// the counter can be updated at just the right time
			return remaining;
		}
		catch ( RemoteException ex )
		{
		}
		return 500;
	}

	private final Handler mHandler = new Handler()
	{

		public void handleMessage( Message msg )
		{

			switch ( msg.what )
			{
			case RemoteImageHandler.REMOTE_IMAGE_DECODED:
				mAlbum.setArtwork( ( Bitmap ) msg.obj );
				mAlbum.invalidate();
				break;

			case REFRESH:
				long next = refreshNow();
				queueNextRefresh( next );
				break;

				/*
				 * case QUIT: // This can be moved back to onCreate once the bug
				 * that prevents // Dialogs from being started from
				 * onCreate/onResume is fixed. new
				 * AlertDialog.Builder(MediaPlaybackActivity.this)
				 * .setTitle(R.string.service_start_error_title)
				 * .setMessage(R.string.service_start_error_msg)
				 * .setPositiveButton(R.string.service_start_error_button, new
				 * DialogInterface.OnClickListener() { public void
				 * onClick(DialogInterface dialog, int whichButton) { finish(); } })
				 * .setCancelable(false) .show(); break;
				 */

			default:
				break;
			}
		}
	};

	/*
	 * Try to use String.format() as little as possible, because it creates a
	 * new Formatter every time you call it, which is very inefficient. Reusing
	 * an existing Formatter more than tripled the speed of makeTimeString().
	 * This Formatter/StringBuilder are also used by makeAlbumSongsLabel()
	 */
	private static StringBuilder sFormatBuilder = new StringBuilder();
	private static Formatter sFormatter = new Formatter( sFormatBuilder, Locale
			.getDefault() );
	private static final Object[] sTimeArgs = new Object[5];

	public static String makeTimeString( Context context, long secs )
	{

		String durationformat = context.getString( R.string.durationformat );

		/*
		 * Provide multiple arguments so the format can be changed easily by
		 * modifying the xml.
		 */
		sFormatBuilder.setLength( 0 );

		final Object[] timeArgs = sTimeArgs;
		timeArgs[0] = secs / 3600;
		timeArgs[1] = secs / 60;
		timeArgs[2] = ( secs / 60 ) % 60;
		timeArgs[3] = secs;
		timeArgs[4] = secs % 60;

		return sFormatter.format( durationformat, timeArgs ).toString();
	}

    private class LoadAlbumArtTask extends UserTask<Void, Void, Boolean> {
    	String artUrl;
    	
        @Override
    	public void onPreExecute() {
        }
    	
	    @Override
	    public Boolean doInBackground(Void...params) {
	    	Album album;
	        boolean success = false;
	        
			try {
				artUrl = LastFMApplication.getInstance().player.getArtUrl();
				String artistName = LastFMApplication.getInstance().player.getArtistName();
				String albumName = LastFMApplication.getInstance().player.getAlbumName();
				if(albumName != null && albumName.length() > 0) {
					album = mServer.getAlbumInfo(artistName, albumName);
					if(album != null) {
						for(ImageUrl image : album.getImages()) {
							if(image.getSize().contentEquals("extralarge")) {
								artUrl = image.getUrl();
								break;
							}
						}
					}
				}
				success = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
	        return success;
	    }
	    
	    @Override
	    public void onPostExecute(Boolean result) {
			if ( artUrl != RadioPlayerService.UNKNOWN )
			{
				mAlbumArtHandler.removeMessages( RemoteImageHandler.GET_REMOTE_IMAGE );
				mAlbumArtHandler.obtainMessage( RemoteImageHandler.GET_REMOTE_IMAGE, artUrl )
				.sendToTarget();
			}
	    }
    }
	
    private class LoadBioTask extends UserTask<Void, Void, Boolean> {
        @Override
    	public void onPreExecute() {
  			mWebView.loadData("Loading...", "text/html", "utf-8");
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
        	Artist artist;
            boolean success = false;
            
    		try {
    			artist = mServer.getArtistInfo(mArtistName.getText().toString(), null, null);
    			String imageURL = "";
    			for(ImageUrl image : artist.getImages()) {
    				if(image.getSize().contentEquals("large")) {
    					imageURL = image.getUrl();
    					break;
    				}
    			}
    			mBio = "<html><body style='margin:0; padding:0; color:black; background: white; font-family: Helvetica; font-size: 11pt;'>" +
					"<div style='padding:17px; margin:0; top:0px; left:0px; width:286; position:absolute;'>" +
					"<img src='" + imageURL + "' style='margin-top: 4px; float: left; margin-right: 0px; margin-bottom: 14px; width:64px; height:64px; border:1px solid gray; padding: 1px;'/>" +
					"<div style='float:right; width: 180px; padding:0px; margin:0px; margin-top:1px; margin-left:3px;'>" +
					"<span style='font-size: 15pt; font-weight:bold; padding:0px; margin:0px;'>" + mArtistName.getText() + "</span><br/>" +
					"<span style='color:gray; font-weight: normal; font-size: 10pt;'>" + artist.getListeners() + " listeners<br/>" +
					artist.getPlaycount() + " plays</span></div>" +
					"<br style='clear:both;'/>" + artist.getBio().getContent() + "</div></body></html>";
    			success = true;
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	if(result) {
    			mWebView.loadData(mBio, "text/html", "utf-8");
       		 } else {
    			mWebView.loadData("Unable to fetch bio", "text/html", "utf-8");
       		 }
        }
    }
	
    private class LoadSimilarTask extends UserTask<Void, Void, Boolean> {
    	
        @Override
    	public void onPreExecute() {
	        String[] strings = new String[]{"Loading..."};
	        mSimilarList.setOnItemClickListener(null);
	        mSimilarList.setAdapter(new ArrayAdapter<String>(Player.this, 
	                R.layout.list_row, R.id.row_label, strings)); 
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
            
			mSimilarAdapter = new ListAdapter(Player.this, getImageCache());
			mSimilarList.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> l, View v,
						int position, long id) {
					Artist artist = (Artist)mSimilarAdapter.getItem(position);
					mSimilarAdapter.enableLoadBar(position);
			    	LastFMApplication.getInstance().playRadioStation(Player.this, "lastfm://artist/"+Uri.encode(artist.getName())+"/similarartists", false);
				}
				
			});

    		try {
    			Artist[] similar = mServer.getSimilarArtists(mArtistName.getText().toString(), null);
    			ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
    			for(int i=0; i< ((similar.length < 10) ? similar.length : 10); i++){
    				ListEntry entry = new ListEntry(similar[i], 
    						R.drawable.artist_icon, 
    						similar[i].getName(), 
    						similar[i].getImages()[0].getUrl(),
    						R.drawable.radio_icon);
    				iconifiedEntries.add(entry);
    			}
    			mSimilarAdapter.setSourceIconified(iconifiedEntries);
    			success = true;
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	if(result) {
        		mSimilarList.setAdapter(mSimilarAdapter);
        		mSimilarList.setOnScrollListener(mSimilarAdapter.getOnScrollListener());
        	} else {
    	        String[] strings = new String[]{"No Similar Artists"};
    	        mSimilarList.setAdapter(new ArrayAdapter<String>(Player.this, 
    	                R.layout.list_row, R.id.row_label, strings)); 
        	}
        }
    }

    private class LoadListenersTask extends UserTask<Void, Void, Boolean> {
    	
        @Override
    	public void onPreExecute() {
   	        String[] strings = new String[]{"Loading..."};
   	        mFanList.setAdapter(new ArrayAdapter<String>(Player.this, 
   	                R.layout.list_row, R.id.row_label, strings)); 
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
            
			mFanAdapter = new ListAdapter(Player.this, getImageCache());

    		try {
    			User[] fans = mServer.getTrackTopFans(mTrackName.getText().toString(), mArtistName.getText().toString(), null);
    			ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
    			for(int i=0; i< ((fans.length < 10) ? fans.length : 10); i++){
    				ListEntry entry = new ListEntry(fans[i], 
    						R.drawable.profile_unknown, 
    						fans[i].getName(), 
    						fans[i].getImages()[0].getUrl(),
    						R.drawable.arrow);
    				iconifiedEntries.add(entry);
    			}
    			mFanAdapter.setSourceIconified(iconifiedEntries);
    			success = true;
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	if(result) {
        		mFanList.setAdapter(mFanAdapter);
        		mFanList.setOnScrollListener(mFanAdapter.getOnScrollListener());
        	} else {
    	        String[] strings = new String[]{"No Top Listeners"};
    	        mFanList.setAdapter(new ArrayAdapter<String>(Player.this, 
    	                R.layout.list_row, R.id.row_label, strings)); 
        	}
        }
    }
	
    private class LoadTagsTask extends UserTask<Void, Void, Boolean> {
    	
        @Override
    	public void onPreExecute() {
   	        String[] strings = new String[]{"Loading..."};
   	        mTagList.setAdapter(new ArrayAdapter<String>(Player.this, 
   	                R.layout.list_row, R.id.row_label, strings));
   	        mTagList.setOnItemClickListener(null);
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;

			mTagAdapter = new ListAdapter(Player.this, getImageCache());
			mTagList.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> l, View v,
						int position, long id) {
					Tag tag = (Tag)mTagAdapter.getItem(position);
					mTagAdapter.enableLoadBar(position);
			    	LastFMApplication.getInstance().playRadioStation(Player.this, "lastfm://globaltags/"+Uri.encode(tag.getName()), false);
				}
				
			});

    		try {
    			Tag[] tags = mServer.getTrackTopTags(mArtistName.getText().toString(), mTrackName.getText().toString(), null);
    			ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
    			for(int i=0; i< ((tags.length < 10) ? tags.length : 10); i++){
    				ListEntry entry = new ListEntry(tags[i], 
    						R.drawable.tag_dark,
    						tags[i].getName(), 
    						R.drawable.radio_icon);
    				iconifiedEntries.add(entry);
    			}
    			mTagAdapter.setSourceIconified(iconifiedEntries);
    			success = true;
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	if(result) {
        		mTagList.setAdapter(mTagAdapter);
        	} else {
    	        String[] strings = new String[]{"No Tags"};
    	        mTagList.setAdapter(new ArrayAdapter<String>(Player.this, 
    	                R.layout.list_row, R.id.row_label, strings)); 
        	}
        }
    }
	
	private OnItemClickListener mEventOnItemClickListener = new OnItemClickListener(){

		public void onItemClick(final AdapterView<?> parent, final View v,
				final int position, long id) {
            Intent intent = new Intent( Player.this, fm.last.android.activity.Event.class );
            Event event = (Event)mEventAdapter.getItem(position);
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            startActivity( intent );
		}

	};
	
    private class LoadEventsTask extends UserTask<Void, Void, Boolean> {
    	
        @Override
    	public void onPreExecute() {
        	mOntourButton.setAnimation(null);
        	mOntourButton.setVisibility(View.GONE);
        	
   	        String[] strings = new String[]{"Loading..."};
   	        mEventList.setAdapter(new ArrayAdapter<String>(Player.this, 
   	                R.layout.list_row, R.id.row_label, strings)); 
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;

            mEventAdapter = new EventListAdapter(Player.this);

    		try {
    			Event[] events = mServer.getArtistEvents(mArtistName.getText().toString());
    			mEventAdapter.setEventsSource(events);
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
        		mEventList.setAdapter(mEventAdapter);
        		mEventList.setOnItemClickListener(mEventOnItemClickListener);
        		mOntourButton.setVisibility(View.INVISIBLE);
        		Animation a = AnimationUtils.loadAnimation(Player.this, R.anim.tag_fadein);
        		mOntourButton.startAnimation(a);
        	} else {
        		mEventList.setOnItemClickListener(null);
    	        String[] strings = new String[]{"No Upcoming Events"};
    	        mEventList.setAdapter(new ArrayAdapter<String>(Player.this, 
    	                R.layout.list_row, R.id.row_label, strings));
    	        mOntourButton.setVisibility(View.GONE);
        	}
        }
    }
    
    private String mLoadEventsTaskArtist = "";
	
	private ImageCache getImageCache(){
		if(mImageCache == null){
			mImageCache = new ImageCache();
		}
		return mImageCache;
	}
	
	
}
