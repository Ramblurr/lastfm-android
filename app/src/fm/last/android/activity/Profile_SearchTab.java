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

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import fm.last.android.R;
import fm.last.android.SearchProvider;
import fm.last.android.adapter.ListAdapter;
import fm.last.android.adapter.ListEntry;
import fm.last.android.utils.ImageCache;

public class Profile_SearchTab extends ListActivity implements OnClickListener, OnKeyListener {
	private EditText mSearchText;
	private Button mSearchButton;
	private ImageCache mImageCache;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search);

		mSearchText = (EditText)findViewById(R.id.station_editbox);
		mSearchText.setOnKeyListener(this);
		
		mSearchButton = (Button)findViewById(R.id.search);
		mSearchButton.setOnClickListener(this);
		
		mImageCache = new ImageCache();
	}

	public void onClick(View arg0) {
		mSearchText.setEnabled(false);
		mSearchButton.setEnabled(false);
		new SearchTask().execute((Void)null);
	}

	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_ENTER) {
			mSearchButton.performClick();
			return true;
		}
		return false;
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		ListAdapter a = (ListAdapter)getListAdapter();
		
		if(a != null) {
			String URI = (String)a.getItem(position);
			Intent i = new Intent(this, Profile.class);
			i.setData(Uri.parse(URI));
			startActivity(i);
		}
	}
	
	@Override
	public boolean onSearchRequested() {
	     return true;
	 }
	
	private class SearchTask extends AsyncTask<Void, Void, ArrayList<ListEntry>> {

		@Override
		public ArrayList<ListEntry> doInBackground(Void... params) {
			try {
				Cursor managedCursor = managedQuery(Uri.withAppendedPath(SearchProvider.SUGGESTIONS_URI,mSearchText.getText().toString()),null,null,null,null);
				if (managedCursor.getCount() == 0)
					return null;
				
				ArrayList<ListEntry> iconifiedEntries = new ArrayList<ListEntry>();
				while(managedCursor.moveToNext()) {
					String text1 = managedCursor.getString(managedCursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1));
					String text2 = managedCursor.getString(managedCursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_2));
					String value = managedCursor.getString(managedCursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_INTENT_DATA));
					String imageURL = managedCursor.getString(managedCursor.getColumnIndexOrThrow("_imageURL"));
					int disclosure = managedCursor.getInt(managedCursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_ICON_2));
					
					ListEntry entry = new ListEntry(value, R.drawable.profile_unknown, text1, imageURL, disclosure, text2);
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
			ListAdapter adapter;
			
			if (iconifiedEntries != null) {
				adapter = new ListAdapter(Profile_SearchTab.this, mImageCache);
				adapter.setSourceIconified(iconifiedEntries);
			} else {
				String[] strings = new String[] { getString(R.string.newstation_noresults) };
				adapter = new ListAdapter(Profile_SearchTab.this, strings);
				adapter.disableDisclosureIcons();
				adapter.setDisabled();
			}
			setListAdapter(adapter);
			getListView().setVisibility(View.VISIBLE);
			mSearchText.setEnabled(true);
			mSearchButton.setEnabled(true);
		}
	}

}
