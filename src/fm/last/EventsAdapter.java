package fm.last;

import java.net.MalformedURLException;
import java.net.URL;

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EventsAdapter extends BaseAdapter implements Runnable
{
	private EventsView m_view = null;
	Event.EventResult m_results = null;
	private String m_postcode = null;
	private int m_eventPagesToLoad = 0;
	private int m_eventPagesLoaded = 0;
	private Thread m_thread = null;
	
	//Cached ImageLoader
	private ImageLoader m_imageLoader;
	
	public EventsAdapter( EventsView view )
	{
		m_view = view;
		m_imageLoader = new ImageLoader(view, true);
	}
	

	public synchronized void getPagesByLocation()
	{
		m_eventPagesToLoad++;
		
		if(m_thread == null || !m_thread.isAlive())
		{
			//TODO: Check if it's necessary to create a new Thread instance
			//		every time we want to restart the Thread. This seems 
			//		unnecessary but I'm getting an error if I don't do it!
			m_thread = new Thread( this );
			m_thread.start();
		}
	}
	
	public void getPagesByLocation( String postcode )
	{
		if( postcode != m_postcode )
		{
			m_postcode = postcode;
			m_results = null;
		}
		getPagesByLocation();
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
		if( m_results != null )
			return m_results.size();
		else
			return 0;
	}

	public Object getItem(int position) { return m_results.get(position); }
	public long getItemId(int position) { return position; }

	public int getNewSelectionForKey(int currentSelection, int keyCode, KeyEvent event) 
	{
		// TODO Auto-generated method stub
		return NO_SELECTION;
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{
		//Trigger the download of the next events list page
		if( position > (6*(m_eventPagesToLoad-1)))
		{
			Log.i("Triggering download of more events: "+position+" > " + (10*(m_eventPagesToLoad-1)));
			m_view.runOnUIThread(new Runnable(){
				public void run()
				{
					m_view.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
				}
			});
			getPagesByLocation();
		}
		
		Event event = m_results.get(position); 
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
		do
		{
			if( m_results == null )
			{
				m_results = Event.getPagesByLocation(m_postcode, m_eventPagesLoaded);
			}
			else
			{
				m_results.addAll( Event.getPagesByLocation(m_postcode, m_eventPagesLoaded));
			}
			m_view.runOnUIThread( new Runnable()
			{
				public void run()
				{
					notifyDataSetChanged();
				}
			});
		}while( ++m_eventPagesLoaded < m_eventPagesToLoad );
		m_view.runOnUIThread( new Runnable(){
			public void run()
			{
				m_view.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_OFF);				
			}
		});
	}
}
