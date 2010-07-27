/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fm.last.android.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import fm.last.android.LastFMApplication;
import fm.last.android.R;

/**
 * This activity is displayed when the system attempts to start an Intent for
 * which there is more than one matching activity, allowing the user to decide
 * which to go to. It is not normally used directly by application developers.
 */
public class ShareResolverActivity extends ListActivity {
	private ResolveListAdapter mAdapter;
	private PackageManager mPm;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Intent i = getIntent();
		i.setType("text/plain");
		i.setAction(Intent.ACTION_SEND);
		String artist = i.getStringExtra(Share.INTENT_EXTRA_ARTIST);
		String track = i.getStringExtra(Share.INTENT_EXTRA_TRACK);
		String URL = "http://www.last.fm/music/" + Uri.encode(artist);
		if(track != null)
			URL += "/_/" + Uri.encode(track);
		i.putExtra(Intent.EXTRA_TEXT, URL);

		onCreate(icicle, i, getString(R.string.share_selectapplication));
	}

	protected void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title) {
		super.onCreate(savedInstanceState);
		mPm = getPackageManager();
		intent.setComponent(null);
		setTitle(title);

		mAdapter = new ResolveListAdapter(this, intent);
		if (mAdapter.getCount() > 1) {
			this.setListAdapter(mAdapter);
		} else if (mAdapter.getCount() == 1) {
			startActivity(mAdapter.intentForPosition(0));
			finish();
			return;
		} else {
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = mAdapter.intentForPosition(position);

		if (intent != null) {
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Clicks", // Category
						"share", // Action
						intent.getComponent().getPackageName(), // Label
						0); // Value
			} catch (SQLiteException e) {
				//Google Analytics doesn't appear to be thread safe
			}

			startActivity(intent);
		}
		finish();
	}

	private final class DisplayResolveInfo {
		ResolveInfo ri;
		CharSequence displayLabel;
		CharSequence extendedInfo;

		DisplayResolveInfo(ResolveInfo pri, CharSequence pLabel, CharSequence pInfo) {
			ri = pri;
			displayLabel = pLabel;
			extendedInfo = pInfo;
		}
	}

	private final class ResolveListAdapter extends BaseAdapter {
		private final Intent mIntent;
		private final LayoutInflater mInflater;

		private List<DisplayResolveInfo> mList;

		public ResolveListAdapter(Context context, Intent intent) {
			mIntent = new Intent(intent);
			mIntent.setComponent(null);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			List<ResolveInfo> rList = mPm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			int N;
			if ((rList != null) && ((N = rList.size()) > 0)) {
				// Only display the first matches that are either of equal
				// priority or have asked to be default options.
				ResolveInfo r0 = rList.get(0);
				for (int i = 1; i < N; i++) {
					ResolveInfo ri = rList.get(i);
					if (Config.LOGV)
						Log.v("ResolveListActivity", r0.activityInfo.name + "=" + r0.priority + "/" + r0.isDefault + " vs " + ri.activityInfo.name + "="
								+ ri.priority + "/" + ri.isDefault);
					if (r0.priority != ri.priority || r0.isDefault != ri.isDefault) {
						while (i < N) {
							rList.remove(i);
							N--;
						}
					}
				}
				if (N > 1) {
					ResolveInfo.DisplayNameComparator rComparator = new ResolveInfo.DisplayNameComparator(mPm);
					Collections.sort(rList, rComparator);
				}
				// Check for applications with same name and use application
				// name or
				// package name if necessary
				mList = new ArrayList<DisplayResolveInfo>();

				List<ResolveInfo> lfmInfoQuery = mPm.queryIntentActivities(new Intent(ShareResolverActivity.this, Share.class),
						PackageManager.MATCH_DEFAULT_ONLY);
				ResolveInfo lfmInfo = lfmInfoQuery.get(0);
				mList.add(new DisplayResolveInfo(lfmInfo, getString(R.string.app_name), getString(R.string.share_friendslist)));

				r0 = rList.get(0);
				int start = 0;
				CharSequence r0Label = r0.loadLabel(mPm);
				for (int i = 1; i < N; i++) {
					if (r0Label == null) {
						r0Label = r0.activityInfo.packageName;
					}
					ResolveInfo ri = rList.get(i);
					CharSequence riLabel = ri.loadLabel(mPm);
					if (riLabel == null) {
						riLabel = ri.activityInfo.packageName;
					}
					if (riLabel.equals(r0Label)) {
						continue;
					}
					processGroup(rList, start, (i - 1), r0, r0Label);
					r0 = ri;
					r0Label = riLabel;
					start = i;
				}
				// Process last group
				processGroup(rList, start, (N - 1), r0, r0Label);
			}
		}

		private void processGroup(List<ResolveInfo> rList, int start, int end, ResolveInfo ro, CharSequence roLabel) {
			// Process labels from start to i
			int num = end - start + 1;
			if (num == 1) {
				// No duplicate labels. Use label for entry at start
				mList.add(new DisplayResolveInfo(ro, roLabel, null));
			} else {
				boolean usePkg = false;
				CharSequence startApp = ro.activityInfo.applicationInfo.loadLabel(mPm);
				if (startApp == null) {
					usePkg = true;
				}
				if (!usePkg) {
					// Use HashSet to track duplicates
					HashSet<CharSequence> duplicates = new HashSet<CharSequence>();
					duplicates.add(startApp);
					for (int j = start + 1; j <= end; j++) {
						ResolveInfo jRi = rList.get(j);
						CharSequence jApp = jRi.activityInfo.applicationInfo.loadLabel(mPm);
						if ((jApp == null) || (duplicates.contains(jApp))) {
							usePkg = true;
							break;
						} else {
							duplicates.add(jApp);
						}
					}
					// Clear HashSet for later use
					duplicates.clear();
				}
				for (int k = start; k <= end; k++) {
					ResolveInfo add = rList.get(k);
					if (usePkg) {
						// Use application name for all entries from start to
						// end-1
						mList.add(new DisplayResolveInfo(add, roLabel, add.activityInfo.packageName));
					} else {
						// Use package name for all entries from start to end-1
						mList.add(new DisplayResolveInfo(add, roLabel, add.activityInfo.applicationInfo.loadLabel(mPm)));
					}
				}
			}
		}

		public Intent intentForPosition(int position) {
			if (mList == null) {
				return null;
			}

			Intent intent = new Intent(mIntent);
			intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			ActivityInfo ai = mList.get(position).ri.activityInfo;
			intent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
			return intent;
		}

		public int getCount() {
			return mList != null ? mList.size() : 0;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = mInflater.inflate(R.layout.list_row, parent, false);
			} else {
				view = convertView;
			}
			bindView(view, mList.get(position));
			return view;
		}

		private final void bindView(View view, DisplayResolveInfo info) {
			TextView text = (TextView) view.findViewById(R.id.row_label);
			TextView text2 = (TextView) view.findViewById(R.id.row_label_second);
			ImageView icon = (ImageView) view.findViewById(R.id.row_icon);
			text.setText(info.displayLabel);
			if (info.extendedInfo != null) {
				text2.setVisibility(View.VISIBLE);
				text2.setText(info.extendedInfo);
			} else {
				text2.setVisibility(View.GONE);
			}
			icon.setImageDrawable(info.ri.loadIcon(mPm));
			icon.setVisibility(View.VISIBLE);
		}
	}
}
