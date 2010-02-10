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
package fm.last.android.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import fm.last.android.R;
import fm.last.api.User;

public class ProfileBubble extends LinearLayout {

	User mUser;
	TextView mFirst;
	TextView mSecond;
	AlbumArt mAvatar;

	public ProfileBubble(Context context) {
		super(context);
		init();
	}

	public ProfileBubble(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		removeAllViews();

		LayoutInflater.from(getContext()).inflate(R.layout.profile_bubble, this);
		// we did do this but it looks wrong due to lack of
		// this.setBackgroundResource(R.drawable.profile_bubble_bg);

		mFirst = (TextView) findViewById(R.id.profile_username);
		mSecond = (TextView) findViewById(R.id.profile_meta);
		mSecond.setText(getContext().getText(R.string.profile_loading));
		mAvatar = (AlbumArt) findViewById(R.id.profile_avatar);
		mAvatar.setDefaultImageResource(R.drawable.profile_unknown);
	}

	public void setUser(User user) {
		mUser = user;

		if (user.getRealName() == null || user.getRealName().trim().length() == 0)
			mFirst.setText(user.getName());
		else
			mFirst.setText(user.getRealName());

		List<String> seconds = new ArrayList<String>();

		if (user.getAge() != null && user.getAge().trim().length() > 0)
			seconds.add(user.getAge());
		if (user.getGender() != null) {
			switch (user.getGender()) {
			case MALE:
				seconds.add(getContext().getString(R.string.profile_gender_male));
				break;
			case FEMALE:
				seconds.add(getContext().getString(R.string.profile_gender_female));
				break;
			}
		}
		if (user.getCountry() != null) {
			Locale current = Locale.getDefault();
			String displayCountry;
			if (current.getLanguage().equalsIgnoreCase("de")) {
				// translate supported languages
				displayCountry = user.getCountry().getDisplayCountry();
			} else {
				// default to English for non-supported languages
				displayCountry = user.getCountry().getDisplayCountry(Locale.ENGLISH);
			}
			if (displayCountry != null && displayCountry.trim().length() > 0) {
				seconds.add(displayCountry);
			}
		}

		String second = "";
		for (String s : seconds)
			second += s + ", ";

		int playcount = Integer.parseInt(mUser.getPlaycount());
		String plays = getContext().getString(R.string.profile_userplays, playcount, mUser.getJoinDate());
		mSecond.setText(second + plays);

		if (mUser.getImages().length > 0 && mAvatar != null) {
			mAvatar.fetch(mUser.getImages()[0].getUrl());
		}
	}
}
