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
import fm.last.android.player.RadioPlayerService;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.UserTask;
import fm.last.android.widget.TabBar;
import fm.last.android.widget.TabBarListener;
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
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
	private String mBio;
	private IconifiedListAdapter mSimilarAdapter;
	private IconifiedListAdapter mFanAdapter;
	private ArrayAdapter<String> mTagAdapter;
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
		mTabBar.addTab("Similar Artists", R.drawable.similar_artists, R.drawable.similar_artists, TAB_SIMILAR);
		mTabBar.addTab("Tags", R.drawable.tags, R.drawable.tags, TAB_TAGS);
		mTabBar.addTab("Events", R.drawable.events, R.drawable.events, TAB_EVENTS);
		mTabBar.addTab("Top Listeners", R.drawable.top_listeners, R.drawable.top_listeners, TAB_LISTENERS);
		mTabBar.setActive("Bio");
		
		new LoadBioTask().execute((Void)null);
		new LoadSimilarTask().execute((Void)null);
		new LoadListenersTask().execute((Void)null);
		new LoadTagsTask().execute((Void)null);
		new LoadEventsTask().execute((Void)null);
	}

	@Override
	public void tabChanged(String text, int index) {
	}

    private class LoadBioTask extends UserTask<Void, Void, Boolean> {
        @Override
    	public void onPreExecute() {
        	if(mBio == null)
    			mWebView.loadData("Loading...", "text/html", "utf-8");
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
        	Artist artist;
            boolean success = false;
            if(mBio != null)
            	return true;
            
    		try {
    			artist = mServer.getArtistInfo(mArtist, null, null);
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
					"<span style='font-size: 15pt; font-weight:bold; padding:0px; margin:0px;'>" + mArtist + "</span><br/>" +
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
    		if(mSimilarAdapter == null) {
    	        String[] strings = new String[]{"Loading..."};
    	        mSimilarList.setAdapter(new ArrayAdapter<String>(Metadata.this, 
    	                R.layout.iconified_list_row, R.id.radio_row_name, strings)); 
    		}
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
            if(mSimilarAdapter != null)
            	return true;
            
			mSimilarAdapter = new IconifiedListAdapter(Metadata.this, getImageCache());

    		try {
    			Artist[] similar = mServer.getSimilarArtists(mArtist, null);
    			ArrayList<IconifiedEntry> iconifiedEntries = new ArrayList<IconifiedEntry>();
    			for(int i=0; i< similar.length; i++){
    				IconifiedEntry entry = new IconifiedEntry(similar[i], 
    						R.drawable.albumart_mp_unknown, 
    						similar[i].getName(), 
    						similar[i].getImages()[0].getUrl());
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
    	        mSimilarList.setAdapter(new ArrayAdapter<String>(Metadata.this, 
    	                R.layout.iconified_list_row, R.id.radio_row_name, strings)); 
        	}
        }
    }

    private class LoadListenersTask extends UserTask<Void, Void, Boolean> {
    	
        @Override
    	public void onPreExecute() {
    		if(mFanAdapter == null) {
    	        String[] strings = new String[]{"Loading..."};
    	        mFanList.setAdapter(new ArrayAdapter<String>(Metadata.this, 
    	                R.layout.iconified_list_row, R.id.radio_row_name, strings)); 
    		}
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
            if(mFanAdapter != null)
            	return true;
            
			mFanAdapter = new IconifiedListAdapter(Metadata.this, getImageCache());

    		try {
    			User[] fans = mServer.getTrackTopFans(mTrack, mArtist, null);
    			ArrayList<IconifiedEntry> iconifiedEntries = new ArrayList<IconifiedEntry>();
    			for(int i=0; i< fans.length; i++){
    				IconifiedEntry entry = new IconifiedEntry(fans[i], 
    						R.drawable.albumart_mp_unknown, 
    						fans[i].getName(), 
    						fans[i].getImages()[0].getUrl());
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
    	        mFanList.setAdapter(new ArrayAdapter<String>(Metadata.this, 
    	                R.layout.iconified_list_row, R.id.radio_row_name, strings)); 
        	}
        }
    }
	
    private class LoadTagsTask extends UserTask<Void, Void, Boolean> {
    	
        @Override
    	public void onPreExecute() {
    		if(mTagAdapter == null) {
    	        String[] strings = new String[]{"Loading..."};
    	        mTagList.setAdapter(new ArrayAdapter<String>(Metadata.this, 
    	                R.layout.iconified_list_row, R.id.radio_row_name, strings)); 
    		}
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
            if(mTagAdapter != null)
            	return true;
            

    		try {
    			Tag[] tags = mServer.getTrackTopTags(mArtist, mTrack, null);
    			ArrayList<String> entries = new ArrayList<String>();
    			for(int i=0; i< tags.length; i++){
    				String entry = tags[i].getName();
    				entries.add(entry);
    			}
    			mTagAdapter = new ArrayAdapter<String>(Metadata.this, R.layout.iconified_list_row, R.id.radio_row_name, entries);
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
    	        mTagList.setAdapter(new ArrayAdapter<String>(Metadata.this, 
    	                R.layout.iconified_list_row, R.id.radio_row_name, strings)); 
        	}
        }
    }
	
	private OnItemClickListener mEventOnItemClickListener = new OnItemClickListener(){

		public void onItemClick(final AdapterView<?> parent, final View v,
				final int position, long id) {
			mEventAdapter.toggleDescription(position);
		}

	};
	
    private class LoadEventsTask extends UserTask<Void, Void, Boolean> {
    	
        @Override
    	public void onPreExecute() {
    		if(mEventAdapter == null) {
    	        String[] strings = new String[]{"Loading..."};
    	        mEventList.setAdapter(new ArrayAdapter<String>(Metadata.this, 
    	                R.layout.iconified_list_row, R.id.radio_row_name, strings)); 
    		}
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
            if(mEventAdapter != null)
            	return true;

            mEventAdapter = new EventListAdapter(Metadata.this, getImageCache());

    		try {
    			Event[] events = mServer.getArtistEvents(mArtist);
    			mEventAdapter.setEventsSource(events, events.toString());
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
        	} else {
    	        String[] strings = new String[]{"No Upcoming Events"};
    	        mEventList.setAdapter(new ArrayAdapter<String>(Metadata.this, 
    	                R.layout.iconified_list_row, R.id.radio_row_name, strings)); 
        	}
        }
    }
	
	private ImageCache getImageCache(){
		if(mImageCache == null){
			mImageCache = new ImageCache();
		}
		return mImageCache;
	}

}
