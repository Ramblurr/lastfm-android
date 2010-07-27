/***************************************************************************
 *   Copyright 2005-2009 Last.fm Ltd.                                      *
 *   Portions contributed by Casey Link, Lukasz Wisniewski,                *
 *   Mike Jennings, and Michael Novak Jr.                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
 ***************************************************************************/
package fm.last.android.activity;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.KeyEvent;
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
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.adapter.TagListAdapter;
import fm.last.android.utils.UserTask;
import fm.last.android.widget.TagLayout;
import fm.last.android.widget.TagLayoutListener;
import fm.last.api.LastFmServer;
import fm.last.api.Session;

/**
 * Activity for tagging albums, artists and songs
 * 
 * @author Lukasz Wisniewski
 */
public class Tag extends Activity {
	String mArtist;
	String mTrack;

	LastFmServer mServer = AndroidLastFmServerFactory.getServer();
	Session mSession = LastFMApplication.getInstance().session;

	ArrayList<String> mTrackOldTags;
	ArrayList<String> mTrackNewTags;
	ArrayList<String> mTopTags;
	ArrayList<String> mUserTags;

	TagListAdapter mTopTagListAdapter;
	TagListAdapter mUserTagListAdapter;

	Animation mFadeOutAnimation;
	boolean animate = false;

	// --------------------------------
	// XML LAYOUT start
	// --------------------------------
	EditText mTagEditText;
	Button mTagBackButton;
	Button mTagForwardButton;
	Button mTagButton;
	TagLayout mTagLayout;
	TabHost mTabHost;
	ListView mTagList;

	ProgressDialog mSaveDialog;

	// --------------------------------
	// XML LAYOUT start
	// --------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// mLogin = new DbImpl(this).getLogin();

		mArtist = getIntent().getStringExtra("lastfm.artist");
		mTrack = getIntent().getStringExtra("lastfm.track");

		// loading activity layout
		setContentView(R.layout.tag);

		// binding views to XML-layout
		mTagEditText = (EditText) findViewById(R.id.tag_text_edit);
		mTagButton = (Button) findViewById(R.id.tag_add_button);
		mTagLayout = (TagLayout) findViewById(R.id.TagLayout);
		mTagList = (ListView) findViewById(R.id.TagList);
		mTabHost = (TabHost)findViewById(R.id.TabBar);
		mTabHost.setup();

		// loading & setting animations
		mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.tag_row_fadeout);
		mTagLayout.setAnimationsEnabled(true);

		// configure the tabs
		mTabHost.addTab(mTabHost.newTabSpec("suggested")
                .setIndicator(getString(R.string.tag_suggestedtags), getResources().getDrawable(R.drawable.ic_tab_tags))
                .setContent(R.id.dummy));
		mTabHost.addTab(mTabHost.newTabSpec("mine")
                .setIndicator(getString(R.string.tag_mytags), getResources().getDrawable(R.drawable.ic_tab_profile))
                .setContent(R.id.dummy));
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

			public void onTabChanged(String tabId) {
				if(tabId.equals("mine")) {
					mTagList.setAdapter(mUserTagListAdapter);
				} else {
					mTagList.setAdapter(mTopTagListAdapter);
				}
			}
		});
		
		// restoring or creatingData
		restoreMe();
		
		// add callback listeners
		mTagEditText.setOnKeyListener(new View.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_ENTER:
					mTagButton.performClick();
					mTagEditText.setText("");
					return true;
				default:
					return false;
				}
			}

		});

		mTagButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if(mTagEditText != null)
					addTag(mTagEditText.getText().toString());
			}

		});

		mTagLayout.setTagLayoutListener(new TagLayoutListener() {

			public void tagRemoved(String tag) {
				removeTag(tag);
			}

		});
		mTagLayout.setAreaHint(R.string.tagarea_hint);

		mTagList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(final AdapterView<?> parent, final View view, final int position, long time) {
				if (!animate) {
					String tag = (String) parent.getItemAtPosition(position);
					if (addTag(tag)) {
						mFadeOutAnimation.setAnimationListener(new AnimationListener() {

							public void onAnimationEnd(Animation animation) {
								((TagListAdapter) parent.getAdapter()).tagAdded(position);
								animate = false;
							}

							public void onAnimationRepeat(Animation animation) {
							}

							public void onAnimationStart(Animation animation) {
								animate = true;
							}

						});
						view.findViewById(R.id.row_label).startAnimation(mFadeOutAnimation);
					}
				}

			}

		});
		
		mTabHost.setCurrentTabByTag("suggested");
		mTagList.requestFocus();
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			LastFMApplication.getInstance().tracker.trackPageView("/Tag");
		} catch (SQLiteException e) {
			//Google Analytics doesn't appear to be thread safe
		}
	}

	/**
	 * Restores already added tags when orientation is changed
	 */
	@SuppressWarnings("unchecked")
	private void restoreMe() {
		mTopTagListAdapter = new TagListAdapter(this);
		mUserTagListAdapter = new TagListAdapter(this);

		if (getLastNonConfigurationInstance() != null) {
			Object savedState[] = (Object[]) getLastNonConfigurationInstance();
			mTopTags = (ArrayList<String>) savedState[0];
			mUserTags = (ArrayList<String>) savedState[1];
			mTrackOldTags = (ArrayList<String>) savedState[2];
			mTrackNewTags = (ArrayList<String>) savedState[3];

			// this looks insane, and well, it is. Basically when changing
			// orientation we are
			// serialised and unserialised - unserialisation happens here. But
			// if we are still
			// loading tags the above 4 members will be null, so we need to do a
			// new usertask
			// because otherwise we'll never get the tags, and we can't
			// serialise the usertask
			try {
				fillData();
				return;
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		new LoadTagTask().execute((Object) null);
	}

	private void fetchDataFromServer() {
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
		if (topTags != null) {
			for (int i = 0; i < topTags.length; i++) {
				mTopTags.add(topTags[i].getName());
			}
		}

		mUserTags = new ArrayList<String>();
		if (userTags != null) {
			for (int i = 0; i < userTags.length; i++) {
				mUserTags.add(userTags[i].getName());
			}
		}

		mTrackOldTags = new ArrayList<String>();
		if (oldTags != null) {
			for (int i = 0; i < oldTags.length; i++) {
				mTrackOldTags.add(oldTags[i].getName());
			}
		}

		mTrackNewTags = new ArrayList<String>(mTrackOldTags);
	}

	/**
	 * Fills mTopTagListAdapter, mUserTagListListAdapter and mTagLayout with
	 * data (mTopTags, mUserTags & mTrackNewTags)
	 */
	private void fillData() {
		mTopTagListAdapter.setSource(mTopTags, mTrackNewTags);
		mUserTagListAdapter.setSource(mUserTags, mTrackNewTags);
		for (int i = 0; i < mTrackNewTags.size(); i++) {
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
	private void commit() {

		ArrayList<String> addTags = new ArrayList<String>();
		ArrayList<String> removeTags = new ArrayList<String>();

		// TODO maybe nicer diff algorithm here

		for (int i = 0; i < mTrackOldTags.size(); i++) {
			String oldTag = mTrackOldTags.get(i);
			boolean presentInNew = false;
			for (int j = 0; j < mTrackNewTags.size(); j++) {
				if (oldTag.equals(mTrackNewTags.get(j))) {
					presentInNew = true;
					break;
				}
			}
			if (!presentInNew) {
				removeTags.add(oldTag);
			}
		}

		for (int i = 0; i < mTrackNewTags.size(); i++) {
			String newTag = mTrackNewTags.get(i);
			boolean presentInOld = false;
			for (int j = 0; j < mTrackOldTags.size(); j++) {
				if (newTag.equals(mTrackOldTags.get(j))) {
					presentInOld = true;
					break;
				}
			}
			if (!presentInOld) {
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

		for (int i = 0; i < removeTags.size(); i++) {
			try {
				mServer.removeTrackTag(mArtist, mTrack, removeTags.get(i), mSession.getKey());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tag, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.cancel_menu_item:
			finish();
			break;
		case R.id.save_menu_item:
			new SaveTagTask().execute((Object) null);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Adds new tag to mTagLayout and mTrackNewTags
	 * 
	 * @param tag
	 * @return true if successful
	 */
	private boolean addTag(String tag) {
		if (!isValidTag(tag))
			return false;

		for (int i = 0; i < mTrackNewTags.size(); i++) {
			if (mTrackNewTags.get(i).equals(tag)) {
				// tag already exists, abort
				return false;
			}
		}
		mTrackNewTags.add(tag);
		mTagLayout.addTag(tag);
		return true;
	}

	/**
	 * Validates a tag
	 * 
	 * @param tag
	 * @return true if tag is valid
	 */
	private boolean isValidTag(String tag) {
		if (tag == null || tag.trim().length() == 0)
			return false;

		return true;
	}

	/**
	 * Removes given tag from mTagLayout and mTrackNewTags
	 * 
	 * @param tag
	 */
	private void removeTag(String tag) {
		for (int i = mTrackNewTags.size() - 1; mTrackNewTags.size() > 0 && i >= 0; i--) {
			if (mTrackNewTags.get(i).equals(tag)) {
				mTrackNewTags.remove(i);
			}
		}
		mTopTagListAdapter.tagUnadded(tag);
		mUserTagListAdapter.tagUnadded(tag);
	}

	/**
	 * Fetches tags from the server
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class LoadTagTask extends UserTask<Object, Integer, Object> {
		ProgressDialog mLoadDialog;

		@Override
		public void onPreExecute() {
			if (mLoadDialog == null) {
				mLoadDialog = ProgressDialog.show(Tag.this, "", getString(R.string.tag_loading), true, false);
				mLoadDialog.setCancelable(true);
			}
		}

		@Override
		public Object doInBackground(Object... params) {
			fetchDataFromServer();
			return null;
		}

		@Override
		public void onPostExecute(Object result) {
			fillData();
			try {
				if (mLoadDialog != null) {
					mLoadDialog.dismiss();
					mLoadDialog = null;
				}
			} catch (IllegalArgumentException e) {
				// for some reason this happens if you change orientation during
				// the tag loading phase
				// we reason it is because a new activity is created, but it's a
				// bit mysterious
				e.printStackTrace();
			}
		}
	}

	/**
	 * Saves tags to the server
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class SaveTagTask extends UserTask<Object, Integer, Object> {

		@Override
		public void onPreExecute() {
			if (mSaveDialog == null) {
				mSaveDialog = ProgressDialog.show(Tag.this, "", getString(R.string.tag_saving), true, false);
				mSaveDialog.setCancelable(true);
			}
		}

		@Override
		public Object doInBackground(Object... params) {
			commit();
			return null;
		}

		@Override
		public void onPostExecute(Object result) {
			if (mSaveDialog != null) {
				mSaveDialog.dismiss();
				mSaveDialog = null;
			}
			finish();
		}

	}
}
