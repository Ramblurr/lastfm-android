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
import java.util.Stack;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.activity.Event.EventActivityResult;
import fm.last.android.adapter.EventListAdapter;
import fm.last.android.adapter.ListAdapter;
import fm.last.api.Event;
import fm.last.api.LastFmServer;

public class Profile_EventsTab extends ListActivity implements LocationListener {
	// Java doesn't let you treat enums as ints easily, so we have to have this
	// mess
	private static final int EVENTS_MYEVENTS = 0;
	private static final int EVENTS_RECOMMENDED = 1;
	private static final int EVENTS_NEARME = 2;

	private ListAdapter mEventsAdapter;
	private String mUsername; // store this separate so we have access to it
								// before User obj is retrieved
	LastFmServer mServer = AndroidLastFmServerFactory.getServer();

	ViewFlipper mNestedViewFlipper;
	private Stack<Integer> mViewHistory;

	View previousSelectedView = null;

	// Animations
	Animation mPushRightIn;
	Animation mPushRightOut;
	Animation mPushLeftIn;
	Animation mPushLeftOut;

	ListView[] mEventsLists = new ListView[3];

	private EventActivityResult mOnEventActivityResult;

	Location mLocation = null;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.events);

		mUsername = getIntent().getStringExtra("user");

		mViewHistory = new Stack<Integer>();
		mNestedViewFlipper = (ViewFlipper) findViewById(R.id.NestedViewFlipper);
		mNestedViewFlipper.setAnimateFirstView(false);
		mNestedViewFlipper.setAnimationCacheEnabled(false);

		getListView().requestFocus();
		
		String[] mStrings = new String[] { "My Events", "Recommended by Last.fm", "Events Near Me" }; // this
																									// order
																									// must
																									// match
																									// the
																									// ProfileActions
																									// enum
		mEventsAdapter = new ListAdapter(Profile_EventsTab.this, mStrings);
		getListView().setAdapter(mEventsAdapter);

		// TODO should be functions and not member variables, caching is evil
		mEventsLists[EVENTS_MYEVENTS] = (ListView) findViewById(R.id.myevents_list_view);
		mEventsLists[EVENTS_MYEVENTS].setOnItemClickListener(mEventItemClickListener);

		mEventsLists[EVENTS_RECOMMENDED] = (ListView) findViewById(R.id.recommended_list_view);
		mEventsLists[EVENTS_RECOMMENDED].setOnItemClickListener(mEventItemClickListener);

		mEventsLists[EVENTS_NEARME] = (ListView) findViewById(R.id.nearme_list_view);
		mEventsLists[EVENTS_NEARME].setOnItemClickListener(mEventItemClickListener);

		// Loading animations
		mPushLeftIn = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
		mPushLeftOut = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
		mPushRightIn = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
		mPushRightOut = AnimationUtils.loadAnimation(this, R.anim.push_right_out);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == RESULT_OK) {
			int status = data.getExtras().getInt("status", -1);
			if (mOnEventActivityResult != null && status != -1) {
				mOnEventActivityResult.onEventStatus(status);
			}
		}
	}
	
	@Override
	protected void onPause() {
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(this);
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!mViewHistory.isEmpty()) {
				setPreviousAnimation();
				mEventsAdapter.disableLoadBar();
				mNestedViewFlipper.setDisplayedChild(mViewHistory.pop());
				LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
				lm.removeUpdates(this);
				return true;
			}
			if (event.getRepeatCount() == 0) {
				finish();
				return true;
			}
		}
		return false;
	}

	private void setNextAnimation() {
		mNestedViewFlipper.setInAnimation(mPushLeftIn);
		mNestedViewFlipper.setOutAnimation(mPushLeftOut);
	}

	private void setPreviousAnimation() {
		mNestedViewFlipper.setInAnimation(mPushRightIn);
		mNestedViewFlipper.setOutAnimation(mPushRightOut);
	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		setNextAnimation();
		mEventsAdapter.enableLoadBar(position);

		switch (position) {
		case EVENTS_MYEVENTS:
			LastFMApplication.getInstance().tracker.trackPageView("/Profile/Events");
			new LoadMyEventsTask().execute((Void) null);
			break;
		case EVENTS_RECOMMENDED:
			LastFMApplication.getInstance().tracker.trackPageView("/Profile/Events/Recommended");
			new LoadRecommendedEventsTask().execute((Void) null);
			break;
		case EVENTS_NEARME:
			LastFMApplication.getInstance().tracker.trackPageView("/Profile/Events/Nearby");
			LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			mLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			new LoadNearbyEventsTask().execute((Void) null);
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000L, 500.0f, this);
			break;
		default:
			break;

		}
	}

	private OnItemClickListener mEventItemClickListener = new OnItemClickListener() {

		public void onItemClick(final AdapterView<?> parent, final View v, final int position, long id) {
			try {
				final Event event = (Event) parent.getAdapter().getItem(position);
				mOnEventActivityResult = new EventActivityResult() {
					public void onEventStatus(int status) {
						event.setStatus(String.valueOf(status));
						mOnEventActivityResult = null;
					}
				};
				startActivityForResult(fm.last.android.activity.Event.intentFromEvent(Profile_EventsTab.this, event), 0);
			} catch (ClassCastException e) {
				// when the list item is not an event
			}
		}

	};

	private class LoadMyEventsTask extends AsyncTask<Void, Void, EventListAdapter> {

		@Override
		public EventListAdapter doInBackground(Void... params) {

			try {
				fm.last.api.Event[] events = mServer.getUserEvents(mUsername);
				if (events.length > 0) {
					EventListAdapter result = new EventListAdapter(Profile_EventsTab.this);
					result.setEventsSource(events);
					return result;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onPostExecute(EventListAdapter result) {
			if (result != null) {
				mEventsLists[EVENTS_MYEVENTS].setAdapter(result);
			} else {
				String[] strings = new String[] { getString(R.string.profile_noevents) };
				ListAdapter adapter = new ListAdapter(Profile_EventsTab.this, strings);
				adapter.disableDisclosureIcons();
				adapter.setDisabled();
				mEventsLists[EVENTS_MYEVENTS].setAdapter(adapter);
			}
			mViewHistory.push(mNestedViewFlipper.getDisplayedChild()); // Save
																		// the
																		// current
																		// view
			mNestedViewFlipper.setDisplayedChild(EVENTS_MYEVENTS + 1);
		}
	}
	
	private class LoadRecommendedEventsTask extends AsyncTask<Void, Void, EventListAdapter> {

		@Override
		public EventListAdapter doInBackground(Void... params) {

			try {
				fm.last.api.Event[] events = mServer.getUserRecommendedEvents(mUsername, LastFMApplication.getInstance().session.getKey());
				if (events.length > 0) {
					EventListAdapter result = new EventListAdapter(Profile_EventsTab.this);
					result.setEventsSource(events);
					return result;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onPostExecute(EventListAdapter result) {
			if (result != null) {
				mEventsLists[EVENTS_RECOMMENDED].setAdapter(result);
			} else {
				String[] strings = new String[] { getString(R.string.profile_noevents) };
				ListAdapter adapter = new ListAdapter(Profile_EventsTab.this, strings);
				adapter.disableDisclosureIcons();
				adapter.setDisabled();
				mEventsLists[EVENTS_RECOMMENDED].setAdapter(adapter);
			}
			mViewHistory.push(mNestedViewFlipper.getDisplayedChild()); // Save
																		// the
																		// current
																		// view
			mNestedViewFlipper.setDisplayedChild(EVENTS_RECOMMENDED + 1);
		}
	}
	
	private class LoadNearbyEventsTask extends AsyncTask<Void, Void, EventListAdapter> {

		@Override
		public EventListAdapter doInBackground(Void... params) {

			try {
				if(mLocation != null) {
					String latitude = String.valueOf(mLocation.getLatitude());
					String longitude = String.valueOf(mLocation.getLongitude());
					fm.last.api.Event[] events = mServer.getNearbyEvents(latitude, longitude);
					if (events.length > 0) {
						EventListAdapter result = new EventListAdapter(Profile_EventsTab.this);
						result.setEventsSource(events);
						return result;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onPostExecute(EventListAdapter result) {
			if (result != null) {
				mEventsLists[EVENTS_NEARME].setAdapter(result);
			} else {
				String[] strings = new String[] { getString(R.string.profile_noevents) };
				ListAdapter adapter = new ListAdapter(Profile_EventsTab.this, strings);
				adapter.disableDisclosureIcons();
				adapter.setDisabled();
				mEventsLists[EVENTS_NEARME].setAdapter(adapter);
			}
			if(mViewHistory.empty()) {
				mViewHistory.push(mNestedViewFlipper.getDisplayedChild()); // Save
																			// the
																			// current
																			// view
				mNestedViewFlipper.setDisplayedChild(EVENTS_NEARME + 1);
			}
		}
	}

	public void onLocationChanged(Location location) {
		if(location != null) {
			mLocation = location;
			new LoadNearbyEventsTask().execute((Void) null);
		}
	}

	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
}
