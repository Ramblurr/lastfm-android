package fm.last.android.activity;

import java.io.IOException;
import java.util.ArrayList;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.OnListRowSelectedListener;
import fm.last.android.R;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.adapter.NotificationAdapter;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.UserTask;
import fm.last.api.LastFmServer;
import fm.last.api.RadioPlayList;
import fm.last.api.Session;
import fm.last.api.User;
import fm.last.api.WSError;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Activity adding a track to a user's Last.fm playlist
 * 
 * The track metadata is passed via intent extras INTENT_EXTRA_TRACK and 
 * INTENT_EXTRA_ARTIST
 * 
 * @author Sam Steele
 */
public class AddToPlaylist extends Activity {
	private ListView mPlaylistsList;
	private EditText mNewPlaylist;
	private Button mCreateBtn;
	private ImageCache mImageCache;
	LastFmServer mServer = AndroidLastFmServerFactory.getServer();
	
	public static final String INTENT_EXTRA_TRACK = "lastfm.track";
	public static final String INTENT_EXTRA_ARTIST = "lastfm.artist";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.add_to_playlist);
		
		mNewPlaylist = (EditText)findViewById(R.id.new_playlist);
		mNewPlaylist.setOnKeyListener( new View.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch( event.getKeyCode() )
				{
					case KeyEvent.KEYCODE_ENTER:
						mCreateBtn.performClick();
						return true;
					default:
						return false;
				}
			}
			
		});
		
		mCreateBtn = (Button)findViewById(R.id.create_button);
		mCreateBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if(mNewPlaylist.getText().length() > 0) {
					new CreatePlaylistTask(mNewPlaylist.getText().toString()).execute((Void)null);
				}
			}
			
		});

		mPlaylistsList = (ListView)findViewById(R.id.playlists);
		mPlaylistsList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> l, View v, int position,
					long id) {
				ListAdapter adapter = (ListAdapter) mPlaylistsList.getAdapter(); 
				adapter.enableLoadBar(position);
				RadioPlayList playlist = (RadioPlayList) adapter.getItem(position);
				String artist = getIntent().getStringExtra(INTENT_EXTRA_ARTIST);
				String track = getIntent().getStringExtra(INTENT_EXTRA_TRACK);
				new AddToPlaylistTask(artist, track, playlist.getId())
						.execute((Void) null);
			}
		});

		new LoadPlaylistsTask().execute((Void)null);
	}
	
	private ImageCache getImageCache(){
        if(mImageCache == null){
            mImageCache = new ImageCache();
        }
        return mImageCache;
    }

    private class AddToPlaylistTask extends UserTask<Void, Void, Boolean> {
    	String mArtist;
    	String mTrack;
    	String mPlaylistId;
    	
    	public AddToPlaylistTask(String artist, String track, String playlistId) {
    		mArtist = artist;
    		mTrack = track;
    		mPlaylistId = playlistId;
    	}
    	
        @Override
        public Boolean doInBackground(Void...params) {
            Session session = ( Session ) LastFMApplication.getInstance().map
            .get( "lastfm_session" );

            try {
    			mServer.addTrackToPlaylist(mArtist, mTrack, mPlaylistId, session.getKey());
                return true;
            } catch (WSError e) {
            	// 'invalidate parameters' error in this case means
				// "track already exists in playlist", which we will
				// usefully treat as a non-error.
				// of course we're assuming that we always get our
				// parameters right.  but in the face of not enough
            	// error codes from the api, what are you gonna do?
            	if (e.getCode() == WSError.ERROR_InvalidParameters) 
            		return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	((ListAdapter) mPlaylistsList.getAdapter()).disableLoadBar();
        	if(result) {
            	AddToPlaylist.this.finish();
        	} else {
        		Toast.makeText(AddToPlaylist.this, "An error occured while adding track to playlist. Please try again.", Toast.LENGTH_SHORT).show();
        	}
        }
    }

    private class LoadPlaylistsTask extends UserTask<Void, Void, ArrayList<ListEntry>> {
    	@Override
    	public void onPreExecute() {
        	mPlaylistsList.setAdapter(new NotificationAdapter(AddToPlaylist.this, NotificationAdapter.LOAD_MODE, "Loading..."));
    	}
    	
        @Override
        public ArrayList<ListEntry> doInBackground(Void...params) {
            Session session = ( Session ) LastFMApplication.getInstance().map
            .get( "lastfm_session" );

            try {
                RadioPlayList[] playlists = mServer.getUserPlaylists(session.getName());
                if(playlists == null || playlists.length == 0 )
                	return null;
                
                ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
                for(int i=0; i < playlists.length; i++){
                    ListEntry entry = new ListEntry(playlists[i],
                            -1,
                            playlists[i].getTitle());
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
                ListAdapter adaptor = new ListAdapter(AddToPlaylist.this, getImageCache()); 
                adaptor.setSourceIconified(iconifiedEntries);
                mPlaylistsList.setAdapter(adaptor);
            } else {
            	mPlaylistsList.setAdapter(new NotificationAdapter(AddToPlaylist.this, NotificationAdapter.INFO_MODE, "No Playlists")); 
            }
        }
    }
    
    private class CreatePlaylistTask extends UserTask<Void, Void, ArrayList<ListEntry>> {
    	private String mTitle;
    	
    	public CreatePlaylistTask(String title) {
    		mTitle = title;
    	}
    	
    	@Override
    	public void onPreExecute() {
        	mNewPlaylist.setEnabled(false);
    		Toast.makeText(AddToPlaylist.this, "Creating playlist", Toast.LENGTH_LONG).show();
    	}
    	
        @Override
        public ArrayList<ListEntry>  doInBackground(Void...params) {
            Session session = ( Session ) LastFMApplication.getInstance().map
            .get( "lastfm_session" );

            try {
                RadioPlayList[] playlists = mServer.createPlaylist(mTitle, "", session.getKey());
                if(playlists == null || playlists.length == 0 )
                    return null;
                
                playlists = mServer.getUserPlaylists(session.getName());
                if(playlists == null || playlists.length == 0 )
                    return null;
                
                ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
                for(int i=0; i < playlists.length; i++){
                    ListEntry entry = new ListEntry(playlists[i],
                            -1,
                            playlists[i].getTitle());
                    iconifiedEntries.add(entry);
                }
                
                return iconifiedEntries;
            } catch (WSError e) {
            	// TODO: something useful...
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(ArrayList<ListEntry> iconifiedEntries) {
            if(iconifiedEntries != null) {
                mNewPlaylist.setText("");	// ?
                ListAdapter adapter = new ListAdapter(AddToPlaylist.this, getImageCache());
                adapter.setSourceIconified(iconifiedEntries);
                mPlaylistsList.setAdapter(adapter);
            } else {
        		Toast.makeText(AddToPlaylist.this, "An error occurred while creating the playlist. Please try again.", Toast.LENGTH_SHORT).show();
            }
        	mNewPlaylist.setEnabled(true);
        }
    }
}
