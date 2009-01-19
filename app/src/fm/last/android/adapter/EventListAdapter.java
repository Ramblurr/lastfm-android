package fm.last.android.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import fm.last.android.R;
import fm.last.api.Event;
import android.app.Activity;
import android.widget.ArrayAdapter;

/**
 * ListView adapter for Events
 * 
 * @author Lukasz Wisniewski
 */
public class EventListAdapter extends SeparatedListAdapter{
	
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
	public void setEventsSource(Event[] events){

		// treemap usage to deal with sorting
		Map <Date, ArrayList<Event> > eventMap = new TreeMap<Date, ArrayList<Event> >();
		
		// sorting events by date
		for(Event event : events){
			
			Date date = event.getStartDate();
			
			ArrayList<Event> subEvents;
			
			if(eventMap.containsKey(date)){
				subEvents = eventMap.get(date);
			}
			else {
				subEvents = new ArrayList<Event>();
			}
			subEvents.add(event);
			eventMap.put(date, subEvents);
		}
		
		// adding events to appropriate sections
		for(Map.Entry<Date, ArrayList<Event> > entry : eventMap.entrySet()){
			EventListSectionAdapter eventItemAdapter = new EventListSectionAdapter(mContext);
			eventItemAdapter.setEventsSource(entry.getValue());
			String dateString = mDateFormat.format(entry.getKey());
			this.addSection(dateString, eventItemAdapter);
		}
	}



}
