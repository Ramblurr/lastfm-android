package fm.last.android.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import fm.last.android.R;
import fm.last.android.utils.ImageCache;
import fm.last.api.Event;

import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.TextView;

/**
 * ListView adapter for Events
 * 
 * @author Lukasz Wisniewski
 */
public class EventListAdapter extends ListAdapter{

	private static String TAG = "EventListAdapter";

	public static final int CONTEXTMENU_ATTENDING = 0xff01;
	public static final int CONTEXTMENU_MAYBE_ATTENDING = 0xff02;
	public static final int CONTEXTMENU_NOT_ATTENDING = 0xff03;
	public static final int CONTEXTMENU_WWW = 0xff04;
	public static final int CONTEXTMENU_MAP = 0xff05;
	public static final int CONTEXTMENU_CALENDAR = 0xff06;

	private ArrayList<Entry> mEntries;

	SimpleDateFormat mDateFormat = new SimpleDateFormat("dd/MM/yy");
	SimpleDateFormat mHourFormat = new SimpleDateFormat("HH:mm");

//	private OnCreateContextMenuListener mOnCreateContextMenuListener;

	private EventListAdapterListener mAdapterListener;
	private int mProvidedPages;
	private int mTotalPages;

	/**
	 * Wrapper around event class, holder for pre-
	 * calculated data like description which is stripped out
	 * from HTML code etc. - performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class Entry{
		Event event;
		//String artists;
		//String distance;
	}

	/**
	 * Default constructor
	 * 
	 * @param context
	 * @param imageCache
	 */
	public EventListAdapter(
			Activity context, ImageCache imageCache) {
		super(context, imageCache);
		init();
	}

	/**
	 * Standard class fields initialization shared between all constructors
	 */
	private void init(){
//		mOnCreateContextMenuListener = new OnCreateContextMenuListener(){
//
//			public void onCreateContextMenu(ContextMenu menu, View v,
//					ContextMenuInfo menuInfo) {
//
//				menu.add(Menu.NONE, CONTEXTMENU_ATTENDING, Menu.NONE, "Attending");
//				menu.add(Menu.NONE, CONTEXTMENU_MAYBE_ATTENDING, Menu.NONE, "Maybe attending");
//				menu.add(Menu.NONE, CONTEXTMENU_NOT_ATTENDING, Menu.NONE, "Not attending");
//				menu.add(Menu.NONE, CONTEXTMENU_MAP, Menu.NONE, "Show on map");
//				menu.add(Menu.NONE, CONTEXTMENU_WWW, Menu.NONE, "Website");
//
//			}
//
//		};
	}
	
	/**
	 * Adds data source to the adapter, tag param is used by ImageDownloader
	 * class to avoid executing same threads twice.
	 * 
	 * @param tag
	 */
	public void addEventsSource(Event[] events){

		for (int i = 0 ; i < events.length; i++) {
			Event event = events[i];

			Entry entry = new Entry();
			entry.event = event;
			mEntries.add(entry);
		}

		notifyDataSetChanged();
	}

	/**
	 * Sets data source for the adapter, tag param is used by ImageDownloader
	 * class to avoid executing same threads twice.
	 * 
	 * @param tag
	 */
	public void setEventsSource(Event[] events){

		mTotalPages = 1;
		mProvidedPages = 1;

		mEntries = new ArrayList<Entry>();
		
		addEventsSource(events);
	}

	public void setListener(EventListAdapterListener l){
		mAdapterListener = l;
	}

	@Override
	public int getCount() {
		if(mEntries != null){
			return mEntries.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return mEntries.get(position).event;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;

		ViewHolder holder;

		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(R.layout.event_row, null);

			holder = new ViewHolder();
			holder.eventName = (TextView)row.findViewById(R.id.ExtendedRowBiggerText);
			holder.venueName = (TextView)row.findViewById(R.id.ExtendedRowSmallerText0);
			holder.countryName = (TextView)row.findViewById(R.id.ExtendedRowSmallerText1);

			row.setTag(holder);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}

		holder.eventName.setText(mEntries.get(position).event.getTitle());
		holder.venueName.setText(mEntries.get(position).event.getVenue().getName()+", "
				+mEntries.get(position).event.getVenue().getLocation().getCity());
		holder.countryName.setText(mEntries.get(position).event.getVenue().getLocation().getCountry());

//		String date = "";
//		if(mEntries.get(position).event.getStartDate() != null){
//			date = mDateFormat.format(mEntries.get(position).event.getStartDate());
//		}
//
//		String hour = "";
//		if(mEntries.get(position).event.getStartTime() != null){
//			hour = mHourFormat.format(mEntries.get(position).event.getStartTime());
//		}

		return row;
	}

	/**
	 * Class implementing holder pattern,
	 * performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
		TextView eventName;
		TextView venueName;
		TextView countryName;
	}

	/**
	 * Returns OnCreateContextMenuListener instance associated with 
	 * this instance of EventListAdapter
	 * 
	 * @return
	 */
	public OnCreateContextMenuListener getOnCreateContextMenuListener() {
		//return mOnCreateContextMenuListener;
		return null;
	}

//	/**
//	 * Function to be called from parent's onContextItemSelected
//	 * to handle appropriate MenuItem's Id
//	 * 
//	 * @param item
//	 * @param session
//	 * @return
//	 */
//	public boolean onContextItemSelected(MenuItem item, Session session) {
//		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
//
//		Event event = (Event) getItem(info.position);
//
//		switch(item.getItemId()){
//		case CONTEXTMENU_ATTENDING:
//			Event.attend(""+event.getId(), AttendanceStatus.ATTENDING, session);
//			break;
//		case CONTEXTMENU_MAYBE_ATTENDING:
//			Event.attend(""+event.getId(), AttendanceStatus.MAYBE_ATTENDING, session);
//			break;
//		case CONTEXTMENU_NOT_ATTENDING:
//			Event.attend(""+event.getId(), AttendanceStatus.NOT_ATTENDING, session);
//			break;
//		case CONTEXTMENU_MAP:
//			IntentLauncher.fireGeoIntent(event, mContext);
//			break;
//		case CONTEXTMENU_WWW:
//			IntentLauncher.fireWebIntent(event.getUrl(), mContext);
//			break;
//		default:
//			break;
//		}
//
//		return true;
//	}

	/**
	 * Allows associating current location with the adapter.
	 * Basing on current location and events data, distance between
	 * them (1-*) will be calculated and displayed
	 * in the ListView instance by this adapter.
	 * Setting new location forces recalculation. 
	 * 
	 * @param l
	 */
	public void setCurrentLocation(Location l){
		// clean old distances
//		if(mEntries != null){
//			Iterator<Entry> it = mEntries.iterator();
//			while(it.hasNext()){
//				Entry e = it.next();
//				e.distance = null;
//			}
//		}
//		this.mLocation = l;
	}

	private boolean fetchingMore = false;

	private void fetchMore(){
		mAdapterListener.getPaginatedPage(mProvidedPages+1);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

		if(mProvidedPages < mTotalPages){
			if(firstVisibleItem + visibleItemCount == totalItemCount){
				// TODO load paginated results
				if (!fetchingMore){
					fetchingMore = true;
					Log.i(TAG, String.format("total = %d, provided = %d", mTotalPages, mProvidedPages ) );
					Log.i(TAG, "fetching more results..." );
					fetchMore();
				}
			}
		}
	}

//	/**
//	 * Method for providing event data to adapter on-the-fly
//	 * 
//	 * @param events
//	 */
//	public void providePage(PaginatedResult<Event> events){
//		Log.i(TAG, "providePage "+events.getPage());
//		if(events.getPage() == 1){
//			setEventsSource(events.getPageResults(), events.getPageResults().toString());
//			mTotalPages = events.getTotalPages();
//			mProvidedPages = 1;
//		}
//		else {
//			mProvidedPages++;
//			addEventsSource(events.getPageResults(), events.getPageResults().toString());
//		}
//		fetchingMore = false;
//	}

}
