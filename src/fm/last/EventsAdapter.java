package fm.last;

import java.net.MalformedURLException;
import java.net.URL;

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EventsAdapter extends BaseAdapter implements Runnable
{
	private EventsView m_view = null;
	Event.EventResult m_results = null;
	private String m_postcode = null;

	//Cached ImageLoader
	private ImageLoader m_imageLoader;
	
	public EventsAdapter( EventsView view )
	{
		m_view = view;
		m_imageLoader = new ImageLoader(view, true);
	}
	
	public void getPagesByLocation( String postcode )
	{
		m_postcode = postcode;
		new Thread( this ).start();
	}
	
	public void setPostCode( String postCode ) { }

	public boolean areAllItemsSelectable() 
	{
		return true;
	}

	public boolean isSelectable(int arg0) 
	{
		return true;
	}

	public int getCount() 
	{
		if( m_results != null &&
			m_results.totalCount() > 0 )
			return m_results.events().length;
		else
			return 0;
	}

	public Object getItem(int position) { return m_results.events()[position]; }
	public long getItemId(int position) { return position; }

	public int getNewSelectionForKey(int currentSelection, int keyCode, KeyEvent event) 
	{
		// TODO Auto-generated method stub
		return NO_SELECTION;
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{
		Event event = m_results.events()[position]; 
		ViewInflate viewInflater = m_view.getWindow().getViewInflate();
		boolean newView = false;
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
		
		ImageView iv = (ImageView)ll.getChildAt(0);
		try {
			m_imageLoader.loadImage(iv, new URL(event.imageUrl()));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LinearLayout detailLL = (LinearLayout)ll.getChildAt(1);
		
		TextView eventTitle = (TextView)detailLL.getChildAt(0);
		eventTitle.setText(event.venue());
		
		TextView eventArtists = (TextView)detailLL.getChildAt(1);
		
		String artistsString = event.artists().isEmpty() ? "" : event.artists().get(0);
		for( int i = 1; i < event.artists().size(); i++)
			artistsString += ", " + event.artists().get(i);
		
		eventArtists.setText(artistsString);
		
		return ll;
	}
	
	public void run()
	{
		m_results = Event.getPagesByLocation(m_postcode, 5);
		m_view.runOnUIThread( new Runnable()
		{
			public void run()
			{
				notifyDataSetChanged();
			}
		});
	}
}
