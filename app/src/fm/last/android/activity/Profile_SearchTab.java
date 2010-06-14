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

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import fm.last.android.LastFMApplication;
import fm.last.android.R;

public class Profile_SearchTab extends Activity implements OnClickListener, OnKeyListener {
	EditText mSearchText;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search);
		
		Intent intent = getIntent();
		if (intent != null && intent.getData() != null) {
			if(intent.getData().getScheme() != null && intent.getData().getScheme().equals("lastfm")) {
				LastFMApplication.getInstance().playRadioStation(this, intent.getData().toString(), false);
			} else {  //The search provider sent us an http:// URL, forward it to the metadata screen
				Intent i = null;
				if(intent.getData().getPath().contains("/user/")) {
					i = new Intent(this, Profile.class);
				} else {
					i = new Intent(this, Metadata.class);
				}
				i.setData(intent.getData());
				startActivity(i);
				finish();
			}
		}

		mSearchText = (EditText)findViewById(R.id.station_editbox);
		mSearchText.setOnClickListener(this);
		mSearchText.setOnKeyListener(this);
		
		Button b = (Button)findViewById(R.id.search);
		b.setOnClickListener(this);
	}

	public void onClick(View arg0) {
		onSearchRequested();
	}

	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (!event.isSystem() && 
                (keyCode != KeyEvent.KEYCODE_DPAD_UP) &&
                (keyCode != KeyEvent.KEYCODE_DPAD_DOWN) &&
                (keyCode != KeyEvent.KEYCODE_DPAD_LEFT) &&
                (keyCode != KeyEvent.KEYCODE_DPAD_RIGHT) &&
                (keyCode != KeyEvent.KEYCODE_DPAD_CENTER)) {
			mSearchText.onKeyDown(keyCode, event);
			onSearchRequested();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onSearchRequested() {
	     startSearch(mSearchText.getText().toString(), false, null, false);
	     mSearchText.setText("");
	     return true;
	 }
}
