package fm.last;

import android.view.View;
import android.widget.Button;

public class EventsController implements  Button.OnClickListener, Runnable {
	private String m_postCode = "";
	private EventsView m_view = null;
	private int m_totalEvents = 0;
	private int m_curIndex = 0;
	private int m_curPage = 0;
	private final int PERPAGE = 5;
	
	public void run()
	{
		fetchEvents();
	}
	
	public EventsController( EventsView view )
	{
		m_view = view;
		
		setPostCode( "E5 0ES" );
		new Thread( this ).start();
	}
	
	public void setPostCode( String postCode ) {
		m_postCode = postCode;
	}

	public void fetchEvents()
	{
		Log.i( "Downloading event information.." );
		Event.EventResult result = Event.getPagesByLocation(m_postCode, PERPAGE);
		m_totalEvents = result.totalCount();
		
		Log.i( "Retrieved events" );
		
		m_view.addEvents( result.events() );
		m_view.loadingComplete();
	}
	
	
	public void onClick(View view) {
		Log.i( "Button Click id:" + view.getId() );
		switch( view.getId() )
		{
		case R.id.events_more:
			m_view.scrollPageNext();
			break;
		case R.id.events_previous:
			break;
		default:
			m_view.startRadio( view.getId() );
			break;
		}
	}
}
