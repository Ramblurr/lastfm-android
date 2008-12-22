package fm.last.android.activity;

import java.io.IOException;
import java.util.ArrayList;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.adapter.EventListAdapter;
import fm.last.android.adapter.IconifiedEntry;
import fm.last.android.adapter.IconifiedListAdapter;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.utils.ImageCache;
import fm.last.android.widget.TabBar;
import fm.last.android.widget.TabBarListener;
import fm.last.api.Artist;
import fm.last.api.Event;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Tag;
import fm.last.api.User;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;

/**
 * Activity displaying metadata<br>
 * <ul>
 * <li>Artist Bio</li>
 * <li>Similar Artists (start Artist Radio when tapping a row)</li>
 * <li>Top Tags (start global tag radio when tapping a row)</li>
 * <li>Events</li>
 * <li>Top Listeners (show user profile when tapping a row)</li>
 * </ul>
 * 
 * @author Lukasz Wisniewski
 */
public class Metadata extends Activity implements TabBarListener {

	private final static int TAB_BIO = 0;
	private final static int TAB_SIMILAR = 1;
	private final static int TAB_TAGS = 2;
	private final static int TAB_EVENTS = 3;
	private final static int TAB_LISTENERS = 4;

	LastFmServer mServer = AndroidLastFmServerFactory.getServer();
	//Session mSession = ( Session ) LastFMApplication.getInstance().map.get( "lastfm_session" );

	private String mArtist;
	private String mAlbum;
	private String mTrack;
	
	private ImageCache mImageCache;
	private IconifiedListAdapter mSimilarAdapter;
	private IconifiedListAdapter mFanAdapter;
	private ListAdapter mTagAdapter;
	private EventListAdapter mEventAdapter;

	TextView mTextView;
	TabBar mTabBar;
	ViewFlipper mViewFlipper;
	WebView mWebView;
	ListView mSimilarList;
	ListView mTagList;
	ListView mFanList;
	ListView mEventList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mArtist = getIntent().getStringExtra("lastfm.artist");
		mAlbum = getIntent().getStringExtra("lastfm.album");
		mTrack = getIntent().getStringExtra("lastfm.track");	

		// loading activity layout
		setContentView(R.layout.metadata);

		// finding views
		mTextView = (TextView) findViewById(R.id.artist_name);
		mTabBar = (TabBar) findViewById(R.id.TabBar);
		mViewFlipper = (ViewFlipper) findViewById(R.id.ViewFlipper);
		mWebView = (WebView) findViewById(R.id.webview);
		mSimilarList = (ListView) findViewById(R.id.similar_list_view);
		mTagList = (ListView) findViewById(R.id.tags_list_view);
		mFanList = (ListView) findViewById(R.id.listeners_list_view);
		mEventList = (ListView) findViewById(R.id.events_list_view);

		//configuring views
		mTextView.setText(mArtist);

		mTabBar.setViewFlipper(mViewFlipper);
		mTabBar.setListener(this);
		mTabBar.addTab("Bio", R.drawable.bio, R.drawable.bio, TAB_BIO);
		mTabBar.addTab("Similar", R.drawable.similar_artists, R.drawable.similar_artists, TAB_SIMILAR);
		mTabBar.addTab("Tags", R.drawable.tags, R.drawable.tags, TAB_TAGS);
		mTabBar.addTab("Events", R.drawable.events, R.drawable.events, TAB_EVENTS);
		mTabBar.addTab("Listeners", R.drawable.top_listeners, R.drawable.top_listeners, TAB_LISTENERS);
		mTabBar.setActive("Bio");
		
		loadBio();
	}

	@Override
	public void tabChanged(String text, int index) {
		switch (index) {
		case TAB_SIMILAR:
			loadSimilar();
			break;
		case TAB_TAGS:
			loadTags();
			break;
		case TAB_LISTENERS:
			loadListeners();
			break;
		case TAB_EVENTS:
			loadEvents();
			break;
		default:
			break;
		}
		Log.i("Lukasz", "Changed tab to "+text+", index="+index);
	}
	
	private void loadBio(){
		Artist artist = null;
		try {
			artist = mServer.getArtistInfo(mArtist, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(artist != null){
			mWebView.loadData(artist.getBio().getContent(), "text/html", "utf-8");
		}
	}

	private void loadSimilar(){
		if(mSimilarAdapter == null){
			mSimilarAdapter = new IconifiedListAdapter(this, getImageCache());
		}
		else return;
		
		mSimilarList.setAdapter(mSimilarAdapter);
		mSimilarList.setOnScrollListener(mSimilarAdapter.getOnScrollListener());
		
		try {
			Artist[] similar = mServer.getSimilarArtists(mArtist, null);
			//mSimilarAdapter.set
			ArrayList<IconifiedEntry> iconifiedEntries = new ArrayList<IconifiedEntry>();
			for(int i=0; i< similar.length; i++){
				IconifiedEntry entry = new IconifiedEntry(similar[i], 
						R.drawable.albumart_mp_unknown, 
						similar[i].getName(), 
						similar[i].getImages()[0].getUrl());
				iconifiedEntries.add(entry);
			}
			mSimilarAdapter.setSourceIconified(iconifiedEntries);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadListeners(){
		if(mFanAdapter == null){
			mFanAdapter = new IconifiedListAdapter(this, getImageCache());
		}
		else return;
		
		mFanList.setAdapter(mFanAdapter);
		mFanList.setOnScrollListener(mFanAdapter.getOnScrollListener());
		
		try {
			User[] fans = mServer.getTrackTopFans(mTrack, mArtist, null);
			//mSimilarAdapter.set
			ArrayList<IconifiedEntry> iconifiedEntries = new ArrayList<IconifiedEntry>();
			for(int i=0; i< fans.length; i++){
				IconifiedEntry entry = new IconifiedEntry(fans[i], 
						R.drawable.albumart_mp_unknown, 
						fans[i].getName(), 
						fans[i].getImages()[0].getUrl());
				iconifiedEntries.add(entry);
			}
			mFanAdapter.setSourceIconified(iconifiedEntries);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadTags(){
		if(mTagAdapter == null){
			mTagAdapter = new ListAdapter(this);
		}
		else return;
		
		mTagList.setAdapter(mTagAdapter);
		
		try {
			Tag[] tags = mServer.getTrackTopTags(mArtist, mTrack, null);
			//mSimilarAdapter.set
			ArrayList<String> entries = new ArrayList<String>();
			for(int i=0; i< tags.length; i++){
				String entry = tags[i].getName();
				entries.add(entry);
			}
			mTagAdapter.setSource(entries);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private OnItemClickListener mEventOnItemClickListener = new OnItemClickListener(){

		public void onItemClick(final AdapterView<?> parent, final View v,
				final int position, long id) {
			mEventAdapter.toggleDescription(position);
		}

	};
	
	private void loadEvents(){
		if(mEventAdapter == null){
			mEventAdapter = new EventListAdapter(this, getImageCache());
		}
		else return;
		
		mEventList.setAdapter(mEventAdapter);
		mEventList.setOnScrollListener(mEventAdapter.getOnScrollListener());
		mEventList.setOnItemClickListener(mEventOnItemClickListener);
		
		try {
			Event[] events = mServer.getArtistEvents(mArtist);
			mEventAdapter.setEventsSource(events, events.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ImageCache getImageCache(){
		if(mImageCache == null){
			mImageCache = new ImageCache();
		}
		return mImageCache;
	}

}
