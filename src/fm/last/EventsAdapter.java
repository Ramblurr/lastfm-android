package fm.last;

import java.util.ArrayList;

import android.database.DataSetObserver;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TableRow;
import android.widget.TextView;

public class EventsAdapter extends BaseAdapter implements Runnable{
	private String m_postCode = "";
	private EventsView m_view = null;
	private int m_totalEvents = 0;
	private int m_curIndex = 0;
	private int m_curPage = 0;
	private final int PERPAGE = 5;
	Event.EventResult m_results = null;
	private String m_postcode = null;

	
	public EventsAdapter( EventsView view )
	{
		m_view = view;
	}
	
	public void getPagesByLocation( String postcode )
	{
		m_postcode = postcode;
		new Thread(this).start();
	}
	
	public void setPostCode( String postCode ) {
		m_postCode = postCode;
	}


	public boolean areAllItemsSelectable() {
		return true;
	}

	public boolean isSelectable(int arg0) {
		return true;
	}

	public int getCount() {
		if( m_results != null &&
			m_results.totalCount() > 0 )
			return m_results.events().length;
		else
			return 0;
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return m_results.events()[position];
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public int getNewSelectionForKey(int currentSelection, int keyCode,
			KeyEvent event) {
		// TODO Auto-generated method stub
		return NO_SELECTION;
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ViewInflate viewInflater = m_view.getWindow().getViewInflate();
		
		if( convertView == null )
		{
			convertView = viewInflater.inflate( R.layout.event_partial, 
											    parent,
											    false, null );
			Log.i("Creating new view");
		}
		else
		{
			Log.i("converting old view");
		}
		
		LinearLayout ll = (LinearLayout)convertView;
		TextView tv = (TextView)ll.getChildAt(1);
		tv.setPadding(20, 20, 20, 20);
		tv.setText(m_results.events()[position].toString());
		return ll;
	}

	public void run() {
		m_results = Event.getPagesByLocation(m_postcode, 5);
		m_view.runOnUIThread( new Runnable(){
			public void run()
			{
				notifyDataSetChanged();
			}
		});
	}
}
