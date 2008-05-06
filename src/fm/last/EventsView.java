package fm.last;

import android.os.Bundle;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.widget.*;
import android.view.View;
import java.util.ArrayList;

public class EventsView extends ListActivity implements Button.OnClickListener {

	private ArrayList<Event> m_eventList = new ArrayList<Event>();

	//private ListView m_eventsLayout;
	private Button m_prevButton, m_moreButton = null;
	private ProgressDialog m_progressDialog = null;
	private EventsAdapter m_eventsAdapter;

	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.events_view);
		
		m_prevButton = (Button) findViewById(R.id.events_previous);
		m_moreButton = (Button) findViewById(R.id.events_more);
		
		m_eventsAdapter = new EventsAdapter( this );
		setListAdapter( m_eventsAdapter );
		

		m_eventsAdapter.registerDataSetObserver( new DataSetObserver(){
			public void onChanged()
			{
				loadingComplete();
				Log.i( "Retrieved events" );
			}
		} );
		
		Log.i( "Downloading event information.." );

		m_eventsAdapter.getPagesByLocation("E5 0ES");
		showLoading();

	}

	
	public void showLoading() {
		m_progressDialog = 
			ProgressDialog.show(this, 
								getResources().getString(R.string.eventsProgressTitle), 
								getResources().getString(R.string.eventsProgressMessage));
	}
	
	public void loadingComplete() {
		runOnUIThread(new Runnable(){
			public void run()
			{
				m_progressDialog.dismiss();
			}
		});
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
	
	
	public void onClick(View view) {
		Log.i( "Button Click id:" + view.getId() );
		switch( view.getId() )
		{
		case R.id.events_more:
			scrollPageNext();
			break;
		case R.id.events_previous:
			break;
		default:
			startRadio( view.getId() );
			break;
		}
	}
}
