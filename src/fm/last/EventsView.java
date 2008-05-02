package fm.last;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.widget.*;
import android.view.Gravity;
import android.view.animation.TranslateAnimation;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import java.util.ArrayList;

public class EventsView extends Activity {

	private ArrayList<Event> m_eventList = new ArrayList<Event>();
	private int m_curPageIndex = 0;
	private LinearLayout m_eventsLayout;
	private Button m_prevButton, m_moreButton = null;
	private EventsController m_controller;
	private ProgressDialog m_progressDialog = null;
	private Handler m_handler = null;

	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.events_view);
		
		m_prevButton = (Button) findViewById(R.id.events_previous);
		m_moreButton = (Button) findViewById(R.id.events_more);
		m_eventsLayout = (LinearLayout) findViewById(R.id.events_layout);
		m_handler = new Handler();

		showLoading();
		m_controller = new EventsController( this );
		
		m_prevButton.setOnClickListener(m_controller);
		m_moreButton.setOnClickListener(m_controller);
	}
	
	public void addEvents( Event[] events )
	{
		for( Event event : events )
		{
			m_eventList.add( event );
		}
		
		m_handler.post( new Runnable() {
			public void run()
			{
				if( m_eventList.size() > (m_curPageIndex + 5) )
					setMoreEvents(true);
				else
					setMoreEvents(false);
				
				if( m_curPageIndex > 0 )
					setPrevEvents(true);
				else
					setPrevEvents(false);
				
			}
		});
	}
	
	public void showLoading() {
		m_progressDialog = 
			ProgressDialog.show(this, 
								getResources().getString(R.string.eventsProgressTitle), 
								getResources().getString(R.string.eventsProgressMessage));
	}
	
	public void loadingComplete() {
		m_handler.post(new Runnable(){
			public void run()
			{
				displayEvents();
				m_progressDialog.dismiss();				
			}
		});
	}

	public void displayEvents() {

		int count = 0;
		for( Event event : m_eventList )
		{
			// Create a new table row based on the event_partial layout resource
			TableRow row = (TableRow) getViewInflate().inflate(
					R.layout.event_partial, null, null);
	
			// Populate the table row data
			TextView text = (TextView) row.getChildAt(1);
			text.setText(event.title());
	
			Button button = (Button) row.getChildAt(0);
			button.setId( count );
			button.setOnClickListener(m_controller);
	
			// For some unknown reason you can't cast a TableView to a
			// ViewGroup - you get a ClassCastException thrown.
			// Therefore it is necessary to add the row to the table manually.
			row.setLayoutParams(new TableLayout.LayoutParams());
			m_eventsLayout.addView(row);
			
			if( ++count % 5 == 0 ||
				count == m_eventList.size())
				break;
		}
	}

	public void setMoreEvents(boolean more) {
		if (more)
			m_moreButton.setVisibility(View.VISIBLE);
		else
			m_moreButton.setVisibility(View.GONE);
	}

	public void setPrevEvents(boolean prev) {
		if (prev)
			m_prevButton.setVisibility(View.VISIBLE);
		else
			m_prevButton.setVisibility(View.GONE);
	}
	
	public void startRadio( int eventId )
	{
		Intent intent = new Intent("RADIOCLIENT");
		intent.putExtra( "eventXml", m_eventList.get( eventId ).xml() );
		startActivity( intent );
	}

	public void scrollPageNext() {
		Log.i( "Scrolling to next page." );
//		TableLayout curPage = m_eventsTables.get( m_curPageIndex );
//		TableLayout nextPage = m_eventsTables.get( m_curPageIndex + 1 );
//		curPage.setVisibility(android.view.View.GONE);
//		nextPage.setVisibility(android.view.View.VISIBLE);
		//m_eventsLayout.addView(nextPage, 0);
//		TranslateAnimation curAnimation = 
//				new TranslateAnimation(0.0f,
//									   -curPage.getWidth(), 
//									   0.0f, 
//									   0.0f);
//		curAnimation.setDuration(1000);
		
//		TranslateAnimation nextAnimation = 
//				new TranslateAnimation(nextPage.getWidth(),
//						   0.0f, 
//						   0.0f, 
//						   0.0f);
//		nextAnimation.setDuration(1000);
//		
//		curPage.startAnimation(curAnimation);
//		nextPage.startAnimation(nextAnimation);
		
	}
}
