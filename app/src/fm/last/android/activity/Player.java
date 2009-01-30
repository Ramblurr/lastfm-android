package fm.last.android.activity;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Formatter;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.RemoteImageHandler;
import fm.last.android.RemoteImageView;
import fm.last.android.Worker;
import fm.last.android.activity.Event.EventActivityResult;
import fm.last.android.adapter.EventListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.NotificationAdapter;
import fm.last.android.player.RadioPlayerService;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.UserTask;
import fm.last.android.widget.TabBar;
import fm.last.api.Album;
import fm.last.api.Artist;
import fm.last.api.Event;
import fm.last.api.ImageUrl;
import fm.last.api.LastFmServer;
import fm.last.api.Tag;
import fm.last.api.User;
import fm.last.api.WSError;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

public class Player extends Activity
{

	private ImageButton mLoveButton;
	private ImageButton mBanButton;
	private ImageButton mStopButton;
	private ImageButton mNextButton;
	private ImageButton mOntourButton;
	private RemoteImageView mAlbum;
	private TextView mCurrentTime;
	private TextView mTotalTime;
	private TextView mArtistName;
	private TextView mTrackName;
	private ProgressBar mProgress;
	private long mDuration;
	private boolean paused;
	private ProgressDialog mProgressDialog;
	private ViewFlipper mDetailFlipper;

	private static final int REFRESH = 1;

	LastFmServer mServer = AndroidLastFmServerFactory.getServer();

	private ImageCache mImageCache;
	private String mBio;
	private ListAdapter mSimilarAdapter;
	private ListAdapter mFanAdapter;
	private ListAdapter mTagAdapter;
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

	private String mLastArtist = "";
	private String mLastTrack = "";

	private EventActivityResult mOnEventActivityResult;
	
    
	@Override
	public void onCreate( Bundle icicle )
	{

		super.onCreate( icicle );
		requestWindowFeature( Window.FEATURE_NO_TITLE );

		setContentView( R.layout.audio_player );
		setVolumeControlStream( android.media.AudioManager.STREAM_MUSIC );

		mCurrentTime = ( TextView ) findViewById( R.id.currenttime );
		mTotalTime = ( TextView ) findViewById( R.id.totaltime );
		mProgress = ( ProgressBar ) findViewById( android.R.id.progress );
		mProgress.setMax( 1000 );
		mAlbum = ( RemoteImageView ) findViewById( R.id.album );
		mArtistName = ( TextView ) findViewById( R.id.track_artist );
		mTrackName = ( TextView ) findViewById( R.id.track_title );

		mLoveButton = ( ImageButton ) findViewById( R.id.love );
		mLoveButton.setOnClickListener( mLoveListener );
		mBanButton = ( ImageButton ) findViewById( R.id.ban );
		mBanButton.setOnClickListener( mBanListener );
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
		mTagList = (ListView) findViewById(R.id.tags_list_view);
		mFanList = (ListView) findViewById(R.id.listeners_list_view);		
		mEventList = (ListView) findViewById(R.id.events_list_view);

		mTabBar.setViewFlipper(mViewFlipper);
		mTabBar.addTab("Bio", R.drawable.bio);
		mTabBar.addTab("Similar", R.drawable.similar_artists);
		mTabBar.addTab("Tags", R.drawable.tags);
		mTabBar.addTab("Events", R.drawable.events);
		mTabBar.addTab("Fans", R.drawable.top_listeners);

		mAlbumArtWorker = new Worker( "album art worker" );
		mAlbumArtHandler = new RemoteImageHandler( mAlbumArtWorker.getLooper(), mHandler );

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction( RadioPlayerService.META_CHANGED );
		mIntentFilter.addAction( RadioPlayerService.PLAYBACK_FINISHED );
		mIntentFilter.addAction( RadioPlayerService.PLAYBACK_STATE_CHANGED );
		mIntentFilter.addAction( RadioPlayerService.STATION_CHANGED );
		mIntentFilter.addAction( RadioPlayerService.PLAYBACK_ERROR );
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)  {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.player, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)  {
		//Toggle the "View Info" / "Now Playing" menu item
		MenuItem changeView = menu.findItem( R.id.info_menu_item );
		if( mDetailFlipper.getDisplayedChild() == 1 ) {
			changeView.setTitle( "Now Playing" );
			changeView.setIcon( R.drawable.view_artwork );		
		}
		else
		{
			changeView.setTitle( "View Info" );
			changeView.setIcon( R.drawable.info );
		}
		return super.onPrepareOptionsMenu(menu);
	}

	public void showMetadata() {
		if(!mLastInfoArtist.contentEquals(mArtistName.getText())) {
			mLastInfoArtist = mArtistName.getText().toString();
			new LoadBioTask().execute((Void)null);
			new LoadSimilarTask().execute((Void)null);
			new LoadListenersTask().execute((Void)null);
			new LoadTagsTask().execute((Void)null);
			if(mLoadEventsTask != null){
				mLoadEventsTask.updateMetadata();
			}
		}
		mTabBar.setActive( R.drawable.bio );
		mDetailFlipper.showNext();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.info_menu_item:
			showMetadata();
			break;
		case R.id.buy_menu_item:
			try {
				if(LastFMApplication.getInstance().player == null)
					return false;
				Intent intent = new Intent( Intent.ACTION_SEARCH );
				intent.setComponent(new ComponentName("com.amazon.mp3","com.amazon.mp3.android.client.SearchActivity"));
				intent.putExtra("actionSearchString", LastFMApplication.getInstance().player.getArtistName() + " "
						+ LastFMApplication.getInstance().player.getTrackName());
				intent.putExtra("actionSearchType", 0);
				startActivity( intent );
			} catch (Exception e) {
				LastFMApplication.getInstance().presentError(Player.this, "Amazon Unavailable", "The Amazon MP3 store is not currently available on this device.");
			}
			break;
		case R.id.share_menu_item:
			try {
				if(LastFMApplication.getInstance().player == null)
					return false;
				Intent intent = new Intent( this, Share.class );
				intent.putExtra(Share.INTENT_EXTRA_ARTIST, LastFMApplication.getInstance().player.getArtistName());
				intent.putExtra(Share.INTENT_EXTRA_TRACK, LastFMApplication.getInstance().player.getTrackName());
				startActivity( intent );
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.playlist_menu_item:
			try {
				if(LastFMApplication.getInstance().player == null)
					return false;
				Intent intent = new Intent( this, AddToPlaylist.class );
				intent.putExtra(Share.INTENT_EXTRA_ARTIST, LastFMApplication.getInstance().player.getArtistName());
				intent.putExtra(Share.INTENT_EXTRA_TRACK, LastFMApplication.getInstance().player.getTrackName());
				startActivity( intent );
			} catch (Exception e) {
				e.printStackTrace();
			}
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
			if(LastFMApplication.getInstance().player == null)
				return;
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
		if(LastFMApplication.getInstance().player == null)
			LastFMApplication.getInstance().bindPlayerService();
		updateTrackInfo();
		super.onResume();
	}

	@Override
	public void onDestroy()
	{
		mAlbumArtWorker.quit();
		super.onDestroy();
	}

	private View.OnClickListener mLoveListener = new View.OnClickListener()
	{

		public void onClick( View v )
		{

			if ( LastFMApplication.getInstance().player == null )
				return;
			try
			{
				LastFMApplication.getInstance().player.love();
				Toast.makeText(Player.this, "Track has been marked as loved", Toast.LENGTH_SHORT).show();
			}
			catch ( RemoteException ex )
			{
				System.out.println( ex.getMessage() );
			}
		}
	};

	private View.OnClickListener mBanListener = new View.OnClickListener()
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
	};

	private View.OnClickListener mNextListener = new View.OnClickListener()
	{

		public void onClick( View v )
		{

			if ( LastFMApplication.getInstance().player == null )
				return;
			try {
				//If the player is in a stopped state, call startRadio instead of skip
				if(LastFMApplication.getInstance().player.isPlaying())
					LastFMApplication.getInstance().player.skip();
				else
					LastFMApplication.getInstance().player.startRadio();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private View.OnClickListener mOntourListener = new View.OnClickListener(){

		public void onClick(View v) {
			showMetadata();
			mTabBar.setActive( R.drawable.events );
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
				if(mDetailFlipper.getDisplayedChild() == 1)
					mDetailFlipper.showPrevious();
			}
			else if ( action.equals( RadioPlayerService.PLAYBACK_ERROR ) )
			{
				// TODO add a skip counter and try to skip 3 times before display an error message
				try
				{
					if(LastFMApplication.getInstance().player == null)
						return;
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
		try
		{
			if ( LastFMApplication.getInstance().player == null ||
					(mLastArtist.equals(LastFMApplication.getInstance().player.getArtistName()) &&
							mLastTrack.equals(LastFMApplication.getInstance().player.getTrackName()))
			)
				return;
			mLastArtist = LastFMApplication.getInstance().player.getArtistName();
			mLastTrack = LastFMApplication.getInstance().player.getTrackName();
			String artistName = LastFMApplication.getInstance().player.getArtistName();
			mArtistName.setText( artistName );
			mTrackName.setText( LastFMApplication.getInstance().player.getTrackName() );

			Bitmap art = LastFMApplication.getInstance().player.getAlbumArt();
			mAlbum.setArtwork(art);
			mAlbum.invalidate();
			if(art == null) new LoadAlbumArtTask().execute((Void)null);
			
			// fetching artist events (On Tour indicator & Events tab)
			if(!mLoadEventsTaskArtist.equals(artistName)){
				mLoadEventsTaskArtist = artistName;
				mLoadEventsTask = new LoadEventsTask();
				mLoadEventsTask.execute((Void)null);
			}
		}
		catch (java.util.concurrent.RejectedExecutionException e )
		{
			e.printStackTrace();
		}
		catch ( RemoteException ex )
		{
			//FIXME why do we finish() ?????
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
				if(mProgressDialog == null && LastFMApplication.getInstance().player.isPlaying()) {
					mProgressDialog = ProgressDialog.show(this, "", "Buffering", true, false);
					mProgressDialog.setVolumeControlStream( android.media.AudioManager.STREAM_MUSIC );
					mProgressDialog.setCancelable(true);
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
				try {
					if(LastFMApplication.getInstance().player != null)
						LastFMApplication.getInstance().player.setAlbumArt((Bitmap)msg.obj);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	 * 
	 * Hi I changed this due to a bug I managed to make at time zero.
	 * But honestly, this kind of optimisation is a bit much. --mxcl
	 */

	public static String makeTimeString( Context context, long secs )
	{
		return new Formatter().format( "%02d:%02d", secs / 60, secs % 60 ).toString();
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
				if(LastFMApplication.getInstance().player != null) {
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
				}
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

				String listeners = "";
				String plays = "";
				try {
					NumberFormat nf = NumberFormat.getInstance();
					listeners = nf.format(Integer.parseInt(artist
							.getListeners()));
					plays = nf.format(Integer.parseInt(artist.getPlaycount()));
				} catch (NumberFormatException e) {
				}

				mBio = "<html><body style='margin:0; padding:0; color:black; background: white; font-family: Helvetica; font-size: 11pt;'>"
					+ "<div style='padding:17px; margin:0; top:0px; left:0px; position:absolute;'>"
					+ "<img src='"
					+ imageURL
					+ "' style='margin-top: 4px; float: left; margin-right: 0px; margin-bottom: 14px; width:64px; border:1px solid gray; padding: 1px;'/>"
					+ "<div style='margin-left:84px; margin-top:3px'>"
					+ "<span style='font-size: 15pt; font-weight:bold; padding:0px; margin:0px;'>"
					+ mArtistName.getText()
					+ "</span><br/>"
					+ "<span style='color:gray; font-weight: normal; font-size: 10pt;'>"
					+ listeners
					+ " listeners<br/>"
					+ plays
					+ " plays</span></div>"
					+ "<br style='clear:both;'/>"
					+ formatBio(artist.getBio().getContent())
					+ "</div></body></html>";
				
				success = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return success;
		}

		private String formatBio(String wikiText) {
			// last.fm api returns the wiki text without para formatting, correct that:
			return wikiText.replaceAll("\\n+", "<br>"); 
		}

		@Override
		public void onPostExecute(Boolean result) {
			if(result) {
				try {
	    			mWebView.loadData(
	    					new String(mBio.getBytes(), "utf-8"),		// need to do this, but is there a better way? 
	    					"text/html", 
	    					"utf-8");
	    			// request focus to make the web view immediately scrollable
	    			mWebView.requestFocus();	
	    			return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mWebView.loadData("Unable to fetch bio", "text/html", "utf-8");
		}
	}

	private class LoadSimilarTask extends UserTask<Void, Void, ArrayList<ListEntry>> {

		@Override
		public void onPreExecute() {
			mSimilarList.setOnItemClickListener(null);
			mSimilarList.setAdapter(new NotificationAdapter(Player.this, NotificationAdapter.LOAD_MODE, "Loading...")); 
		}

		@Override
		public ArrayList<ListEntry> doInBackground(Void...params) {

			try {
				Artist[] similar = mServer.getSimilarArtists(mArtistName.getText().toString(), null);
				ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
				for(int i=0; i< ((similar.length < 10) ? similar.length : 10); i++){
					ListEntry entry = new ListEntry(similar[i], 
							R.drawable.artist_icon, 
							similar[i].getName(), 
							similar[i].getImages()[0].getUrl(),
							R.drawable.list_icon_station);
					iconifiedEntries.add(entry);
				}
				return iconifiedEntries;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onPostExecute(ArrayList<ListEntry> iconifiedEntries) {
			if(iconifiedEntries != null) {
				mSimilarAdapter = new ListAdapter(Player.this, getImageCache());
				mSimilarAdapter.setSourceIconified(iconifiedEntries);
				mSimilarList.setAdapter(mSimilarAdapter);
				mSimilarList.setOnItemClickListener(new OnItemClickListener() {

					public void onItemClick(AdapterView<?> l, View v,
							int position, long id) {
						Artist artist = (Artist)mSimilarAdapter.getItem(position);
						mSimilarAdapter.enableLoadBar(position);
						LastFMApplication.getInstance().playRadioStation(Player.this, "lastfm://artist/"+Uri.encode(artist.getName())+"/similarartists", false);
					}

				});
			} else {
				mSimilarList.setAdapter(new NotificationAdapter(Player.this, NotificationAdapter.INFO_MODE, "No Similar Artists")); 
			}
		}
	}

	private class LoadListenersTask extends UserTask<Void, Void, ArrayList<ListEntry>> {

		@Override
		public void onPreExecute() {
			mFanList.setAdapter(new NotificationAdapter(Player.this, NotificationAdapter.LOAD_MODE, "Loading..."));
			mFanList.setOnItemClickListener(null);
		}

		@Override
		public ArrayList<ListEntry> doInBackground(Void...params) {
			try {
				User[] fans = mServer.getTrackTopFans(mTrackName.getText().toString(), mArtistName.getText().toString(), null);
				ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
				for(int i=0; i< ((fans.length < 10) ? fans.length : 10); i++){
					ListEntry entry = new ListEntry(fans[i], 
							R.drawable.profile_unknown, 
							fans[i].getName(), 
							fans[i].getImages()[0].getUrl(),
							R.drawable.list_icon_arrow);
					iconifiedEntries.add(entry);
				}
				return iconifiedEntries;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onPostExecute(ArrayList<ListEntry> iconifiedEntries) {
			if(iconifiedEntries != null) {
				mFanAdapter = new ListAdapter(Player.this, getImageCache());
				mFanAdapter.setSourceIconified(iconifiedEntries);
				mFanList.setAdapter(mFanAdapter);
				mFanList.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> l, View v,
							int position, long id) {
						User user = (User)mFanAdapter.getItem(position);
						Intent profileIntent = new Intent(Player.this, fm.last.android.activity.Profile.class);
						profileIntent.putExtra("lastfm.profile.username", user.getName());
						startActivity(profileIntent);
					}
				});
			} else {
				mFanList.setAdapter(new NotificationAdapter(Player.this, NotificationAdapter.INFO_MODE, "No Top Listeners")); 
			}
		}
	}

	private class LoadTagsTask extends UserTask<Void, Void, ArrayList<ListEntry>> {

		@Override
		public void onPreExecute() {
			mTagList.setAdapter(new NotificationAdapter(Player.this, NotificationAdapter.LOAD_MODE, "Loading..."));
			mTagList.setOnItemClickListener(null);
		}

		@Override
		public ArrayList<ListEntry> doInBackground(Void...params) {
			try {
				Tag[] tags = mServer.getTrackTopTags(mArtistName.getText().toString(), mTrackName.getText().toString(), null);
				ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
				for(int i=0; i< ((tags.length < 10) ? tags.length : 10); i++){
					ListEntry entry = new ListEntry(tags[i], 
							-1,
							tags[i].getName(), 
							R.drawable.list_icon_station);
					iconifiedEntries.add(entry);
				}
				return iconifiedEntries;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onPostExecute(ArrayList<ListEntry> iconifiedEntries) {
			if(iconifiedEntries != null) {
				mTagAdapter = new ListAdapter(Player.this, getImageCache());
				mTagAdapter.setSourceIconified(iconifiedEntries);
				mTagList.setAdapter(mTagAdapter);
				mTagList.setOnItemClickListener(new OnItemClickListener() {

					public void onItemClick(AdapterView<?> l, View v, int position, long id) {
						Tag tag = (Tag) mTagAdapter.getItem(position);
						mTagAdapter.enableLoadBar(position);
						LastFMApplication.getInstance().playRadioStation(Player.this, "lastfm://globaltags/"+Uri.encode(tag.getName()), false);
					}

				});
			} else {
				mTagList.setAdapter(new NotificationAdapter(Player.this, NotificationAdapter.INFO_MODE, "No Tags"));
			}
		}
	}

	private OnItemClickListener mEventOnItemClickListener = new OnItemClickListener(){

		public void onItemClick(final AdapterView<?> parent, final View v,
				final int position, long id) {
			
            final Event event = (Event) parent.getAdapter().getItem(position);

            Intent intent = fm.last.android.activity.Event.intentFromEvent(Player.this, event);
			try {
				Event[] events = mServer.getUserEvents((LastFMApplication.getInstance().map.get("lastfm_session")).getName());
				for(Event e : events) {
//					System.out.printf("Comparing id %d (%s) to %d (%s)\n",e.getId(),e.getTitle(),event.getId(),event.getTitle());
					if(e.getId() == event.getId()) {
//						System.out.printf("Matched! Status: %s\n", e.getStatus());
						intent.putExtra("lastfm.event.status", e.getStatus());
						break;
					}

				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
    	    mOnEventActivityResult = new EventActivityResult() {
    	    	public void onEventStatus(int status) 
    	    	{
    	    		event.setStatus(String.valueOf(status));
    	    		mOnEventActivityResult = null;
    	    	}
    	    };
    	    
            startActivityForResult( intent, 0 );
		}

	};

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if( requestCode == 0 && resultCode == RESULT_OK ) {
	    	int status = data.getExtras().getInt("status", -1);
	    	if (mOnEventActivityResult != null && status != -1) {
	    		mOnEventActivityResult.onEventStatus(status);
	    	}
    	}
    }
	
	private LoadEventsTask mLoadEventsTask;
	
	private BaseAdapter mEventAdapter;

	/**
	 * This load task is slightly bigger as it has to handle OnTour indicator
	 * and Metadata's event list. The main problem here is new events must be
	 * downloaded on track change even if the user is viewing old events in the
	 * metadata view.
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class LoadEventsTask extends UserTask<Void, Void, Boolean> {

		/**
		 * Checks whether this task should modify Metadata's event list
		 */
		public boolean mChangeMetadata = false;
		
		/**
		 * States whether any events were fetched or not
		 */
		private boolean mSuccess = false;
		
		/**
		 * New adapter representing events data
		 */
		private BaseAdapter mNewEventAdapter;
		
		private void updateMetadata(){
			mChangeMetadata = true;
			
			if(getStatus() == Status.PENDING || getStatus() == Status.RUNNING){
				mEventList.setOnItemClickListener(null);
				mEventList.setAdapter(new NotificationAdapter(Player.this, NotificationAdapter.LOAD_MODE, "Loading..."));
			}
			
			if(getStatus() == Status.FINISHED){
				reallyUpdateMetadata();
			}
		}
		
		private void reallyUpdateMetadata(){
			mEventAdapter = mNewEventAdapter;
			mEventList.setAdapter(mEventAdapter);
			if(mSuccess){
				mEventList.setOnItemClickListener(mEventOnItemClickListener);
			} else {
				mEventList.setOnItemClickListener(null);
			}
		}

		@Override
		public void onPreExecute() {
			mOntourButton.setAnimation(null);
			mOntourButton.setVisibility(View.GONE);
		}

		@Override
		public Boolean doInBackground(Void...params) {
			mSuccess = false;

			mNewEventAdapter = new EventListAdapter(Player.this);

			try {
				Event[] events = mServer.getArtistEvents(mArtistName.getText().toString());
				((EventListAdapter) mNewEventAdapter).setEventsSource(events);
				if(events.length > 0)
					mSuccess = true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(!mSuccess){
				mNewEventAdapter = new NotificationAdapter(Player.this, NotificationAdapter.INFO_MODE, "No Upcoming Events");
				mEventList.setOnItemClickListener(null);
			}
			
			return mSuccess;
		}

		@Override
		public void onPostExecute(Boolean result) {
			if(result) {
				if(mChangeMetadata){
					reallyUpdateMetadata();
				}
				mOntourButton.setVisibility(View.INVISIBLE);
				Animation a = AnimationUtils.loadAnimation(Player.this, R.anim.tag_fadein);
				a.setAnimationListener(new AnimationListener(){

					public void onAnimationEnd(Animation animation) {
						mOntourButton.setVisibility(View.VISIBLE);
					}

					public void onAnimationRepeat(Animation animation) {
					}

					public void onAnimationStart(Animation animation) {
					}

				});
				mOntourButton.startAnimation(a);
			} else {
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
