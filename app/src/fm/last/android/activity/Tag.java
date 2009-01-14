package fm.last.android.activity;

import java.io.IOException;
import java.util.ArrayList;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.TagListAdapter;
import fm.last.android.widget.TabBar;
import fm.last.android.widget.TabBarListener;
import fm.last.android.widget.TagLayout;
import fm.last.android.widget.TagLayoutListener;
import fm.last.api.LastFmServer;
import fm.last.api.Session;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Activity for tagging albums, artists and songs
 * 
 * @author Lukasz Wisniewski
 */
public class Tag extends Activity implements TabBarListener {
	String mArtist;
	String mTrack;
	
	LastFmServer mServer = AndroidLastFmServerFactory.getServer();
	Session mSession = ( Session ) LastFMApplication.getInstance().map.get( "lastfm_session" );

	ArrayList<String> mTrackOldTags;
	ArrayList<String> mTrackNewTags;
	ArrayList<String> mTopTags;
	ArrayList<String> mUserTags;

	TagListAdapter mTopTagListAdapter;
	TagListAdapter mUserTagListAdapter;

	Animation mFadeOutAnimation;
	boolean animate = false;

	private final int TAB_TOPTAGS = 0;
	private final int TAB_MYTAGS = 1;
	
	// --------------------------------
	// XML LAYOUT start
	// --------------------------------
	EditText mTagEditText;
	Button mTagBackButton;
	Button mTagForwardButton;
	Button mTagButton;
	TagLayout mTagLayout;
	TabBar mTabBar;
	ListView mTagList;
	// --------------------------------
	// XML LAYOUT start
	// --------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//mLogin = new DbImpl(this).getLogin();

		mArtist = getIntent().getStringExtra("lastfm.artist");
		mTrack = getIntent().getStringExtra("lastfm.track");	

		// loading activity layout
		setContentView(R.layout.tag);

		// binding views to XML-layout
		mTagEditText = (EditText) findViewById(R.id.tag_text_edit);
		mTagButton = (Button) findViewById(R.id.tag_add_button);
		mTagLayout = (TagLayout)findViewById(R.id.TagLayout);
		mTagList = (ListView)findViewById(R.id.TagList);
		mTabBar = (TabBar)findViewById(R.id.TabBar);

		// loading & setting animations
		mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.tag_row_fadeout);
		mTagLayout.setAnimationsEnabled(true);
		
		// configure the tabs
		mTabBar.setListener(this);
		mTabBar.addTab("Suggested Tags", R.drawable.list_add_to_playlist, R.drawable.list_add_to_playlist, R.drawable.list_add_to_playlist, TAB_TOPTAGS);
		mTabBar.addTab("Your Tags", R.drawable.profile, R.drawable.profile, R.drawable.profile, TAB_MYTAGS);
		mTabBar.setActive(TAB_TOPTAGS);
		
		// restoring or creatingData
		restoreMe();
		
		mTagButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				addTag(mTagEditText.getText().toString());
			}
			
		});
		
		mTagLayout.setTagLayoutListener(new TagLayoutListener(){

			public void tagRemoved(String tag) {
				removeTag(tag);
			}
			
		});
		mTagLayout.setAreaHint(R.string.tagarea_hint);

		mTagList.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(final AdapterView<?> parent, final View view, final int position,
					long time) {
				if(!animate){
					String tag = (String)parent.getItemAtPosition(position);
					if(addTag(tag)){
						mFadeOutAnimation.setAnimationListener(new AnimationListener(){

							public void onAnimationEnd(Animation animation) {
								((TagListAdapter)parent.getAdapter()).tagAdded(position);
								animate = false;
							}

							public void onAnimationRepeat(Animation animation) {
							}

							public void onAnimationStart(Animation animation) {
								animate = true;
							}

						});
						view.startAnimation(mFadeOutAnimation);
					}
				}

			}

		});
	}
	
	/**
	 * Restores already added tags when orientation is changed
	 */
	@SuppressWarnings("unchecked")
	private void restoreMe(){
		mTopTagListAdapter = new TagListAdapter(this);
		mUserTagListAdapter = new TagListAdapter(this);
		
		if (getLastNonConfigurationInstance()!=null){
			Object savedState[] = (Object[])getLastNonConfigurationInstance();
			mTopTags = (ArrayList<String>) savedState[0];
			mUserTags = (ArrayList<String>) savedState[1];
			mTrackOldTags = (ArrayList<String>) savedState[2];
			mTrackNewTags = (ArrayList<String>) savedState[3];
			fillData();
		}  
		else {
			fm.last.api.Tag topTags[] = null;
			fm.last.api.Tag userTags[] = null;
			fm.last.api.Tag oldTags[] = null;
			try {
				topTags = mServer.getTrackTopTags(mArtist, mTrack, null);
				userTags = mServer.getUserTopTags(mSession.getName(), 50);
				oldTags = mServer.getTrackTags(mArtist, mTrack, mSession.getKey());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			mTopTags = new ArrayList<String>();
			if(topTags != null){
				for(int i=0; i < topTags.length; i++){
					mTopTags.add(topTags[i].getName());
				}
			}
			
			mUserTags = new ArrayList<String>();
			if(userTags != null){
				for(int i=0; i < userTags.length; i++){
					mUserTags.add(userTags[i].getName());
				}
			}
			
			mTrackOldTags = new ArrayList<String>();
			if(oldTags != null){
				for(int i=0; i < oldTags.length; i++){
					mTrackOldTags.add(oldTags[i].getName());
				}
			}
			
			mTrackNewTags = new ArrayList<String>(mTrackOldTags);
			
			fillData();
			
//			new UserTask<Object, Integer, Object>(){
//
//				@Override
//				public Object doInBackground(Object... params) {
//					mTopBar.showCircularBar();
//					mTopTags = new ArrayList<String>(Track.getTopTags(mArtist, mTrack, Login.key));
//					mUserTags = new ArrayList<String>(User.getTopTags(mLogin.getUsername(), Login.key));
//					mTrackOldTags = new ArrayList<String>(Track.getTags(mArtist, mTrack, mLogin.getSession()));
//					mTrackNewTags = new ArrayList<String>(mTrackOldTags);
//					return null;
//				}
//
//				@Override
//				public void onPostExecute(Object result) {
//					fillData();
//					mTopBar.showActionButton();
//				}
//				
//			}.execute((Object)null);
		}
	}
	
	/**
	 * Fills mTopTagListAdapter, mUserTagListListAdapter and mTagLayout with
	 * data (mTopTags, mUserTags & mTrackNewTags)
	 */
	private void fillData(){
		mTopTagListAdapter.setSource(mTopTags);
		mUserTagListAdapter.setSource(mUserTags);
		for(int i=0; i<mTrackNewTags.size(); i++){
			mTagLayout.addTag(mTrackNewTags.get(i));
		}
		mTagList.setAdapter(mTopTagListAdapter);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Object savedState[] = new Object[4];
		savedState[0] = mTopTags;
		savedState[1] = mUserTags;
		savedState[2] = mTrackOldTags;
		savedState[3] = mTrackNewTags;
		
		return savedState;
	}

	/**
	 * Commit tag changes to last.fm server 
	 */
	private void commit(){

		ArrayList<String> addTags = new ArrayList<String>();
		ArrayList<String> removeTags = new ArrayList<String>();

		// TODO maybe nicer diff algorithm here

		for(int i=0; i<mTrackOldTags.size(); i++){
			String oldTag = mTrackOldTags.get(i);
			boolean presentInNew = false;
			for(int j=0; j<mTrackNewTags.size(); j++){
				if(oldTag.equals(mTrackNewTags.get(j))){
					presentInNew = true;
					break;
				}
			}
			if(!presentInNew){
				removeTags.add(oldTag);
			}
		}

		for(int i=0; i<mTrackNewTags.size(); i++){
			String newTag = mTrackNewTags.get(i);
			boolean presentInOld = false;
			for(int j=0; j<mTrackOldTags.size(); j++){
				if(newTag.equals(mTrackOldTags.get(j))){
					presentInOld = true;
					break;
				}
			}
			if(!presentInOld){
				addTags.add(newTag);
			}
		}

		// TODO write arraylist to string for up to 10 tags
		String[] tag = new String[addTags.size()];
		addTags.toArray(tag);
		try {
			mServer.addTrackTags(mArtist, mTrack, tag, mSession.getKey());
		} catch (IOException e) {
			e.printStackTrace();
		}
//		for(int i=0; i<addTags.size(); i++){
//			Track.addTags(mArtist, mTrack, addTags.get(i), mLogin.getSession());
//		}
		for(int i=0; i<removeTags.size(); i++){
			try {
				Log.i("Lukasz", "removing tag "+removeTags.get(i));
				mServer.removeTrackTag(mArtist, mTrack, removeTags.get(i), mSession.getKey());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        // Parameters for menu.add are:
        // group -- Not used here.
        // id -- Used only when you want to handle and identify the click yourself.
        // title
        MenuItem save = menu.add(Menu.NONE, 1, Menu.NONE, "Save");
        save.setIcon(android.R.drawable.ic_menu_save);
        MenuItem cancel = menu.add(Menu.NONE, 0, Menu.NONE, "Cancel");
        cancel.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			finish();
			break;
		case 1:
			commit();
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

    public void tabChanged(int index) {
    	switch(index) {
	    	case TAB_MYTAGS:
				mTagList.setAdapter(mUserTagListAdapter);
	    		break;
	    	case TAB_TOPTAGS:
				mTagList.setAdapter(mTopTagListAdapter);
	    		break;
    	}
	}

	/**
	 * Adds new tag to mTagLayout and mTrackNewTags
	 * 
	 * @param tag
	 * @return true if successful
	 */
	private boolean addTag(String tag){
		for(int i=0; i<mTrackNewTags.size(); i++){
			if(mTrackNewTags.get(i).equals(tag)){
				// tag already exists, abort
				return false;
			}
		}
		mTrackNewTags.add(tag);
		mTagLayout.addTag(tag);
		return true;
	}

	/**
	 * Removes given tag from mTagLayout and mTrackNewTags
	 * 
	 * @param tag
	 */
	private void removeTag(String tag){
		for(int i=mTrackNewTags.size()-1; mTrackNewTags.size()>0 && i >= 0; i--){
			if(mTrackNewTags.get(i).equals(tag)){
				mTrackNewTags.remove(i);
			}
		}
		mTopTagListAdapter.tagUnadded(tag);
		mUserTagListAdapter.tagUnadded(tag);
	}
}
