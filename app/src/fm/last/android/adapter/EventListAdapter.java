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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.widget.ArrayAdapter;
import fm.last.android.R;
import fm.last.api.Event;

/**
 * ListView adapter for Events
 * 
 * @author Lukasz Wisniewski
 */
public class EventListAdapter extends SeparatedListAdapter {

	SimpleDateFormat mDateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy");

	private Activity mContext;

	public EventListAdapter(Activity context) {
		super(context);
		mContext = context;
		headers = new ArrayAdapter<String>(context, R.layout.event_header);
	}

	/**
	 * Sets data source for the adapter.
	 * 
	 * @param events
	 */
	public void setEventsSource(Event[] events) {

		// treemap usage to deal with sorting
		Map<Date, ArrayList<Event>> eventMap = new TreeMap<Date, ArrayList<Event>>();

		// sorting events by date
		for (Event event : events) {

			Date date = event.getStartDate();

			ArrayList<Event> subEvents;

			if (eventMap.containsKey(date)) {
				subEvents = eventMap.get(date);
			} else {
				subEvents = new ArrayList<Event>();
			}
			subEvents.add(event);
			eventMap.put(date, subEvents);
		}

		// adding events to appropriate sections
		for (Map.Entry<Date, ArrayList<Event>> entry : eventMap.entrySet()) {
			EventListSectionAdapter eventItemAdapter = new EventListSectionAdapter(mContext);
			eventItemAdapter.setEventsSource(entry.getValue());
			String dateString = mDateFormat.format(entry.getKey());
			this.addSection(dateString, eventItemAdapter);
		}
	}

}
