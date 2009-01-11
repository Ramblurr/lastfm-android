package fm.last.android.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import fm.last.android.R;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.MathUtils;
import fm.last.api.Event;
import fm.last.api.GeoPoint;
import fm.last.api.Venue;

import android.app.Activity;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

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
	private Location mLocation;
	private static int DEFAULT_RES_ID = R.drawable.events;

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
		boolean unrolled = false;
		String artists;
		String description;
		String distance;
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
	 * @param events
	 * @param tag
	 */
	public void addEventsSource(Event[] events){

		for (int i = 0 ; i < events.length; i++) {
			Event event = events[i];

			Entry entry = new Entry();
			entry.event = event;
			mEntries.add(entry);

			String url = event.getImages()[0].getUrl();
			//mUrls.add(url);
		}

		notifyDataSetChanged();
	}

	/**
	 * Sets data source for the adapter, tag param is used by ImageDownloader
	 * class to avoid executing same threads twice.
	 * 
	 * @param events
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
			holder.artists = (TextView)row.findViewById(R.id.ExtendedRowSmallerText0);
			holder.venueName = (TextView)row.findViewById(R.id.ExtendedRowSmallerText1);
			holder.time = (TextView)row.findViewById(R.id.ExtendedRowSmallerText2);
			holder.distance = (TextView)row.findViewById(R.id.ExtendedRowSmallerText2Right);
			holder.vs = (ViewSwitcher)row.findViewById(R.id.ExtendedRowViewSwitcher);

			row.setTag(holder);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}

		holder.eventName.setText(mEntries.get(position).event.getTitle());
		holder.venueName.setText(mEntries.get(position).event.getVenue().getName());

		String date = "";
		if(mEntries.get(position).event.getStartDate() != null){
			date = mDateFormat.format(mEntries.get(position).event.getStartDate());
		}

		String hour = "";
		if(mEntries.get(position).event.getStartTime() != null){
			hour = mHourFormat.format(mEntries.get(position).event.getStartTime());
		}

		holder.time.setText( date + " " + hour );

		// formating artists text
		if(mEntries.get(position).artists == null){
			String[] artists = mEntries.get(position).event.getArtists();
			String output = "";
			for (int i = 0; i< artists.length; i++) {
				String artist = artists[i];
				output += artist;
				if(i + 1 < artists.length){
					output += ", ";
				}
			}
			mEntries.get(position).artists = output;
		}
		holder.artists.setText(mEntries.get(position).artists);
		//holder.artists.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));

		// formating description text
		if(mEntries.get(position).unrolled && mEntries.get(position).description == null){
			String text = mEntries.get(position).event.getDescription();
			if(text == null){
				text = "";
			}

			// removing HTML tags
			text = text.replaceAll("\\<.*?\\>", "");

			// adding quotes
			text = text.replaceAll("&quot;", "\"");
			mEntries.get(position).description = text;
		}

		holder.vs.setVisibility(View.GONE);

		// calculate distance if location is provided
		if(mLocation != null){
			Venue venue = mEntries.get(position).event.getVenue();
			if(venue == null){
				holder.distance.setText("unknown");
			}
			else {
				if(mEntries.get(position).distance == null){
					fm.last.api.Location venueLocation = venue.getLocation();
					GeoPoint geoPoint = venueLocation.getGeoPoint();
					double d = MathUtils.distance(mLocation.getLatitude(), mLocation.getLongitude(),
							geoPoint.getLatitude(), geoPoint.getLongitude());
					mEntries.get(position).distance = String.format("%.2f km away", d);
				}
				holder.distance.setText(mEntries.get(position).distance);
			}
			holder.distance.setVisibility(View.VISIBLE);
		} else {
			holder.distance.setVisibility(View.GONE);
		}

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
		TextView artists;
		TextView venueName;
		TextView time;
		TextView distance;
		ViewSwitcher vs;
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
		if(mEntries != null){
			Iterator<Entry> it = mEntries.iterator();
			while(it.hasNext()){
				Entry e = it.next();
				e.distance = null;
			}
		}
		this.mLocation = l;
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
