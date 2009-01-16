package fm.last.android.activity;

import java.io.IOException;
import java.util.ArrayList;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.OnListRowSelectedListener;
import fm.last.android.R;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.UserTask;
import fm.last.android.widget.TabBar;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.User;
import fm.last.api.WSError;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.Contacts.ContactMethods;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;
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
	private ListView mAddressBookList;
	private SimpleCursorAdapter mAddressBookAdapter;
	private ListView mFriendsList;
	private ListAdapter mFriendsAdapter;
	private ViewFlipper mViewFlipper;
	private ImageCache mImageCache;
	ListView mDialogList;
	private SimpleCursorAdapter mDialogAdapter;
	private Dialog mDialog;
	LastFmServer mServer = AndroidLastFmServerFactory.getServer();
	
	private final int TAB_ADDRESSBOOK = 0;
	private final int TAB_FRIENDS = 1;
	
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
		mTabBar.addTab("Address Book", TAB_ADDRESSBOOK);
		mTabBar.addTab("Friends", TAB_FRIENDS);
		mTabBar.setActive(TAB_ADDRESSBOOK);
		mAddressBookList = (ListView)findViewById(R.id.addressbook_list_view);
		Cursor C = managedQuery(People.CONTENT_URI, null, People.PRIMARY_EMAIL_ID + " not null", null, null);

		String[] columns = new String[] {People.NAME};
		int[] names = new int[] {R.id.row_label};

		mAddressBookAdapter = new SimpleCursorAdapter(this, R.layout.list_row, C, columns, names);
		mAddressBookList.setAdapter(mAddressBookAdapter);
		mAddressBookList.setOnItemSelectedListener(new OnListRowSelectedListener(mAddressBookList));
		mAddressBookList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> l, View v,
                    int position, long id) {
            	Cursor C = (Cursor) mAddressBookAdapter.getItem(position);
            	long personid = C.getLong(C.getColumnIndex(People._ID));
            	C = managedQuery(ContactMethods.CONTENT_EMAIL_URI, null, ContactMethods.PERSON_ID + " = " + personid, null, null);
            	if(C.getCount() > 0) {
            		String[] columns = new String[] {ContactMethods.DATA};
            		int[] names = new int[] {R.id.row_label};
                    mDialogList = new ListView(Share.this);
                    mDialogAdapter = new SimpleCursorAdapter(Share.this, R.layout.list_row, C, columns, names);

                    mDialogList.setAdapter(mDialogAdapter);
                    mDialogList.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> l, View v, int position, long id) 
                        {
                        	Cursor C = (Cursor) mDialogAdapter.getItem(position);
                    		String artist = getIntent().getStringExtra(INTENT_EXTRA_ARTIST);
                            String track = getIntent().getStringExtra(INTENT_EXTRA_TRACK);
                        	String email = C.getString(C.getColumnIndex(ContactMethods.DATA));
                            ViewSwitcher switcher = (ViewSwitcher)v.findViewById(R.id.row_view_switcher);
                            switcher.setVisibility(View.VISIBLE);
                            switcher.setDisplayedChild(1);
                            new ShareTrackTask(artist,track,email).execute((Void)null);
                        }
                        });
                    mDialog = new AlertDialog.Builder(Share.this).setTitle("Select Address").setView(mDialogList).show();
            	}
            }
        });
		
		mFriendsList = (ListView)findViewById(R.id.friends_list_view);
		mFriendsList.setOnItemSelectedListener(new OnListRowSelectedListener(mFriendsList));
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
            Session session = ( Session ) LastFMApplication.getInstance().map
            .get( "lastfm_session" );
            boolean success = false;

            try {
    			mServer.shareTrack(mArtist, mTrack, mRecipient, session.getKey());
                success = true;
            } catch (WSError e) {
            	LastFMApplication.getInstance().presentError(Share.this, e);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	mFriendsAdapter.disableLoadBar();
        	if(mDialog != null) {
        		mDialog.dismiss();
        		mDialog = null;
        	}
        	if(result) {
            	Share.this.finish();
        	} else {
        		Toast.makeText(Share.this, "An error occured while sharing. Please try again.", Toast.LENGTH_SHORT).show();
        	}
        }
    }
	
    private class LoadFriendsTask extends UserTask<Void, Void, Boolean> {
    	@Override
    	public void onPreExecute() {
            String[] strings = new String[]{"Loading"};
            mFriendsList.setAdapter(new ArrayAdapter<String>(Share.this,
                    R.layout.list_row, R.id.row_label, strings));
    	}
    	
        @Override
        public Boolean doInBackground(Void...params) {
            Session session = ( Session ) LastFMApplication.getInstance().map
            .get( "lastfm_session" );
            boolean success = false;

            mFriendsAdapter = new ListAdapter(Share.this, getImageCache());
            try {
                User[] friends = mServer.getFriends(session.getName(), null, null).getFriends();
                if(friends.length == 0 )
                    return false;
                ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
                for(int i=0; i < friends.length; i++){
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
                    mFriendsAdapter.enableLoadBar(position);
                    User user = (User) mFriendsAdapter.getItem(position);
                    String artist = getIntent().getStringExtra(INTENT_EXTRA_ARTIST);
                    String track = getIntent().getStringExtra(INTENT_EXTRA_TRACK);
                    new ShareTrackTask(artist,track,user.getName()).execute((Void)null);
                }
            });
            if(result) {
                mFriendsList.setAdapter(mFriendsAdapter);
                mFriendsList.setOnScrollListener(mFriendsAdapter.getOnScrollListener());
            } else {
                String[] strings = new String[]{"No Friends Retrieved"};
                mFriendsList.setAdapter(new ArrayAdapter<String>(Share.this,
                        R.layout.list_row, R.id.row_label, strings));
            }
        }
    }
}
