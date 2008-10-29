package fm.last.events;

import java.net.MalformedURLException;
import java.net.URL;

import fm.last.ImageLoader;
import fm.last.Log;
import fm.last.R;
import fm.last.events.Event.EventResult;

import android.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EventsAdapter extends BaseAdapter
{
	private EventsView m_view = null;
	Event.EventResult m_results = null;
	private String m_postcode = null;
	private int m_eventPagesToLoad = 0;
	private int m_eventPagesLoaded = 0;
	
	//Cached ImageLoader
	private ImageLoader m_imageLoader;
	
	public EventsAdapter( EventsView view )
	{
		m_view = view;
		m_imageLoader = new ImageLoader(view, true);
	}
	

	public synchronized void loadEventsByLocation()
	{
		Event.getEventsByLocation( m_postcode, m_eventPagesToLoad++, m_handler );
	}
	
	private EventHandler m_handler = new EventHandler()
	{

		public void onError( final String error )
		{
			m_view.runOnUiThread( new Runnable(){
				public void run()
				{
					m_view.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_OFF);
					m_view.loadingComplete();
					
					AlertDialog.Builder alertBuilder = new AlertDialog.Builder( m_view );
					alertBuilder.setTitle( "Error" )
								.setMessage( error )
								.setIcon( R.drawable.icon )
								.show();

					if( m_results == null )
					{
						//If the first page of events won't even load then just 
						//close the view - no point showing an empty list
						m_view.finish();
					}
			}} );

		}

		public void onSuccess( EventResult result )
		{
			m_eventPagesLoaded++;
			if( m_results == null )
			{
				m_results = result;
			}
			else
			{
				m_results.addAll( result );
			}
			
			m_view.runOnUiThread( new Runnable(){
				public void run()
				{
					notifyDataSetChanged();
					m_view.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_OFF);
					m_view.loadingComplete();
				}
			});
		}
		
	};
	
	public void loadEventsByLocation( String postcode )
	{
		if( postcode != m_postcode )
		{
			m_postcode = postcode;
			m_results = null;
		}
		loadEventsByLocation();
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
		if( m_results != null &&
			m_eventPagesToLoad < m_results.pageCount() &&
		    position > (6*(m_eventPagesToLoad-1)))
		{
			m_view.runOnUiThread(new Runnable(){
				public void run()
				{
					m_view.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
				}
			});
			loadEventsByLocation();
		}
		
		Event event = m_results.get(position); 
		LayoutInflater viewInflater = m_view.getWindow().getLayoutInflater();

		if( convertView == null )
		{
			convertView = viewInflater.inflate( R.layout.event_partial, 
											    parent,
											    false );
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
}
