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
package fm.last.android.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import fm.last.android.R;
import fm.last.api.Event;

/**
 * ListView adapter for Events (section)
 * 
 * @author Lukasz Wisniewski
 */
public class EventListSectionAdapter extends ListAdapter {
	private static final long serialVersionUID = 2070559787839689784L;
	private ArrayList<Event> mEvents;

	// private EventListAdapterListener mAdapterListener;
	// private int mProvidedPages;
	// private int mTotalPages;

	/**
	 * Default constructor
	 * 
	 * @param context
	 */
	public EventListSectionAdapter(Activity context) {
		super(context);
		init();
	}

	/**
	 * Standard class fields initialization shared between all constructors
	 */
	private void init() {
	}

	/**
	 * Sets data source for the adapter.
	 * 
	 * @param events
	 */
	public void setEventsSource(ArrayList<Event> events) {

		mEvents = events;
		// notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (mEvents != null) {
			return mEvents.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return mEvents.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		ViewHolder holder;

		if (row == null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row = inflater.inflate(R.layout.event_row, null);

			holder = new ViewHolder();
			holder.eventName = (TextView) row.findViewById(R.id.ExtendedRowBiggerText);
			holder.venueName = (TextView) row.findViewById(R.id.ExtendedRowSmallerText0);
			holder.countryName = (TextView) row.findViewById(R.id.ExtendedRowSmallerText1);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		holder.eventName.setText(mEvents.get(position).getTitle());
		holder.venueName.setText(mEvents.get(position).getVenue().getName() + ", " + mEvents.get(position).getVenue().getLocation().getCity());
		holder.countryName.setText(mEvents.get(position).getVenue().getLocation().getCountry());

		// String date = "";
		// if(mEntries.get(position).event.getStartDate() != null){
		// date =
		// mDateFormat.format(mEntries.get(position).event.getStartDate());
		// }
		//
		// String hour = "";
		// if(mEntries.get(position).event.getStartTime() != null){
		// hour =
		// mHourFormat.format(mEntries.get(position).event.getStartTime());
		// }

		return row;
	}

	/**
	 * Class implementing holder pattern, performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
		TextView eventName;
		TextView venueName;
		TextView countryName;
	}

	// private boolean fetchingMore = false;
	//
	// private void fetchMore(){
	// mAdapterListener.getPaginatedPage(mProvidedPages+1);
	// }
	//
	// @Override
	// public void onScroll(AbsListView view, int firstVisibleItem,
	// int visibleItemCount, int totalItemCount) {
	// super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
	//
	// if(mProvidedPages < mTotalPages){
	// if(firstVisibleItem + visibleItemCount == totalItemCount){
	// // TODO load paginated results
	// if (!fetchingMore){
	// fetchingMore = true;
	// Log.i(TAG, String.format("total = %d, provided = %d", mTotalPages,
	// mProvidedPages ) );
	// Log.i(TAG, "fetching more results..." );
	// fetchMore();
	// }
	// }
	// }
	// }

	// /**
	// * Method for providing event data to adapter on-the-fly
	// *
	// * @param events
	// */
	// public void providePage(PaginatedResult<Event> events){
	// Log.i(TAG, "providePage "+events.getPage());
	// if(events.getPage() == 1){
	// setEventsSource(events.getPageResults(),
	// events.getPageResults().toString());
	// mTotalPages = events.getTotalPages();
	// mProvidedPages = 1;
	// }
	// else {
	// mProvidedPages++;
	// addEventsSource(events.getPageResults(),
	// events.getPageResults().toString());
	// }
	// fetchingMore = false;
	// }

}
