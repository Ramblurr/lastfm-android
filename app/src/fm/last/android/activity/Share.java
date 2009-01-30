package fm.last.android.activity;

import java.util.ArrayList;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.adapter.NotificationAdapter;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.UserTask;
import fm.last.android.widget.TabBar;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.User;
import fm.last.api.WSError;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Activity for sharing tracks with Last.fm users and address book entries
 * 
 * The track metadata is passed via intent extras INTENT_EXTRA_TRACK and 
 * INTENT_EXTRA_ARTIST
 * 
 * @author Sam Steele
 */
public class Share extends Activity {
	private TabBar mTabBar;
	private ListView mFriendsList;
	private ListAdapter mFriendsAdapter;
	private ViewFlipper mViewFlipper;
	private ImageCache mImageCache;
	ListView mDialogList;
	private Dialog mDialog;
	LastFmServer mServer = AndroidLastFmServerFactory.getServer();
	
	public static final String INTENT_EXTRA_TRACK = "lastfm.track";
	public static final String INTENT_EXTRA_ARTIST = "lastfm.artist";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.share);
		
		mViewFlipper = (ViewFlipper)findViewById(R.id.ViewFlipper);
		mTabBar = (TabBar)findViewById(R.id.TabBar);
		mTabBar.setViewFlipper(mViewFlipper);
		mTabBar.addTab("Email" );
		mTabBar.addTab("Friends" );

		
		findViewById(R.id.email_button).setOnClickListener( new OnClickListener()
		{
			public void onClick( View v )
			{
				EditText edit = (EditText)findViewById(R.id.email);
				String text = edit.getText().toString();
				
				if (text == null || text.length() == 0)
					return;
				
				findViewById(R.id.email_button).setEnabled( false );
				
                String artist = getIntent().getStringExtra(INTENT_EXTRA_ARTIST);
                String track = getIntent().getStringExtra(INTENT_EXTRA_TRACK);
                new ShareTrackTask(artist, track, text).execute((Void)null);			
			}
		} );

		mFriendsList = (ListView)findViewById(R.id.friends_list_view);
		new LoadFriendsTask().execute((Void)null);
	}
	
	private ImageCache getImageCache(){
        if(mImageCache == null){
            mImageCache = new ImageCache();
        }
        return mImageCache;
    }

    private class ShareTrackTask extends UserTask<Void, Void, Boolean> {
    	String mArtist;
    	String mTrack;
    	String mRecipient;
    	
    	public ShareTrackTask(String artist, String track, String recipient) {
    		mArtist = artist;
    		mTrack = track;
    		mRecipient = recipient;
    	}
    	
        @Override
        public Boolean doInBackground(Void...params) {
            try {
                Session session = LastFMApplication.getInstance().map.get( "lastfm_session" );
    			mServer.shareTrack(mArtist, mTrack, mRecipient, session.getKey());
                return true;
            } catch (WSError e) {
            	// can't presentError here. it's not a UI thread. the app crashes.
            	// leave it to the toasting in onPostExecute
            	//LastFMApplication.getInstance().presentError(Share.this, e);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	if (mFriendsAdapter != null)
        		mFriendsAdapter.disableLoadBar();
        	if(mDialog != null) {
        		mDialog.dismiss();
        		mDialog = null;
        	}
        	if(result) {
            	Share.this.finish();
            	Toast.makeText(Share.this, "The track was shared.", Toast.LENGTH_SHORT).show();
        	} else {
        		Toast.makeText(Share.this, "An error occurred when sharing. Please try again.", Toast.LENGTH_SHORT).show();
        		findViewById(R.id.email_button).setEnabled( true );
        	}
        }
    }
	
    private class LoadFriendsTask extends UserTask<Void, Void, ArrayList<ListEntry>> {
    	@Override
    	public void onPreExecute() {
        	mFriendsList.setAdapter(new NotificationAdapter(Share.this, NotificationAdapter.LOAD_MODE, "Loading..."));
        	mFriendsList.setOnItemClickListener(null);
    	}
    	
        @Override
        public ArrayList<ListEntry> doInBackground(Void...params) {
            try {
                Session session = LastFMApplication.getInstance().map.get( "lastfm_session" );
                User[] friends = mServer.getFriends(session.getName(), null, null).getFriends();
                if(friends.length == 0 )
                    return null;
                
                ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
                for(int i=0; i < friends.length; i++){
                    ListEntry entry = new ListEntry(friends[i],
                            R.drawable.profile_unknown,
                            friends[i].getName(), 
                            friends[i].getImages().length == 0 ? "" : friends[i].getImages()[0].getUrl()); // some tracks don't have images
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
                mFriendsAdapter = new ListAdapter(Share.this, getImageCache());
                mFriendsAdapter.setSourceIconified(iconifiedEntries);
                mFriendsList.setAdapter(mFriendsAdapter);
                mFriendsList.setOnItemClickListener(new OnItemClickListener() {

                    public void onItemClick(AdapterView<?> l, View v,
                            int position, long id) {
                        mFriendsAdapter.enableLoadBar(position);
                        User user = (User) mFriendsAdapter.getItem(position);
                        String artist = getIntent().getStringExtra(INTENT_EXTRA_ARTIST);
                        String track = getIntent().getStringExtra(INTENT_EXTRA_TRACK);
                        new ShareTrackTask(artist,track,user.getName()).execute((Void)null);
                    }
                });
            } else {
            	mFriendsList.setAdapter(new NotificationAdapter(Share.this, NotificationAdapter.INFO_MODE, "No Friends :(")); 
            }
        }
    }
}
