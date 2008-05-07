package fm.last;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TableRow;
import android.widget.TextView;

public class EventsAdapter extends BaseAdapter implements Runnable
{
	private String m_postCode = "";
	private EventsView m_view = null;
	private int m_totalEvents = 0;
	private int m_curIndex = 0;
	private int m_curPage = 0;
	private final int PERPAGE = 5;
	Event.EventResult m_results = null;
	private String m_postcode = null;
	private ImageLoader m_imageLoader = new ImageLoader();
	
	public EventsAdapter( EventsView view )
	{
		m_view = view;
	}
	
	public void getPagesByLocation( String postcode )
	{
		m_postcode = postcode;
		new Thread( this ).start();
	}
	
	public void setPostCode( String postCode ) { m_postCode = postCode; }

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
		m_imageLoader.loadImage(iv, event);
		
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
	
	private Bitmap downloadEventImage( Event evt ) 
	{
		Bitmap bm = null;
		try 
		{
			URL url = new URL(evt.imageUrl());
			URLConnection conn = url.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();

			Log.i("Begining download of event image: " + evt.imageUrl());
			bm = BitmapFactory.decodeStream(is);
			Log.i("Completed download of event image");

			is.close();
		}
		catch (Exception e) 
		{
			Log.e("Error downloading event art");
		}

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale( 0.5f, 0.5f );
		return Bitmap.createBitmap( bm, 0, 0, bm.width(), bm.height(), scaleMatrix, true );
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
	
	private class ImageLoader implements Runnable
	{
		Thread m_thread = null;
		Object m_mapLock = new Object(); 
		HashMap<ImageView, Event> m_imageMap = new HashMap<ImageView, Event>();
		public void loadImage( ImageView view, Event event )
		{
			synchronized(m_mapLock)
			{
				m_imageMap.put(view, event);
			}
			if(m_thread == null || !m_thread.isAlive())
			{
				start();
			}
		}
		
		public void start()
		{
			m_thread = new Thread(this);
			m_thread.start();				
		}
		
		public void run()
		{
			Log.i("ImageLoader Thread running");
			while( !m_imageMap.isEmpty() )
			{
				Iterator<Map.Entry<ImageView,Event>> it;
				it = m_imageMap.entrySet().iterator();
				
				Map.Entry<ImageView, Event> pair;
				pair = (Map.Entry<ImageView, Event>)it.next();

				final Bitmap bmp = downloadEventImage(pair.getValue());
				final ImageView iv = pair.getKey();
				m_view.runOnUIThread(new Runnable() {
					public void run()
					{
						iv.setImageBitmap(bmp);
					}
				});

				synchronized (m_mapLock) {
					m_imageMap.remove(pair.getKey());						
				}
			}
		}
	}
}
