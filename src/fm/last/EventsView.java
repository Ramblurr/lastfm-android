package fm.last;

import android.os.Bundle;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import java.util.ArrayList;

public class EventsView extends ListActivity implements OnItemClickListener {
	private ProgressDialog m_progressDialog = null;
	private EventsAdapter m_eventsAdapter;

	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.events_view);
		
		m_eventsAdapter = new EventsAdapter( this );
		setListAdapter( m_eventsAdapter );
		getListView().setOnItemClickListener(this);
		

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
	
	public void startRadio( Event evt )
	{
		Intent intent = new Intent("RADIOCLIENT");
		intent.putExtra( "eventXml", evt.xml() );
		startActivity( intent );
	}	

	public void scrollPageNext() {
		Log.i( "Scrolling to next page." );
	}
	
	public void onItemClick(AdapterView parent, View v, int position, long id) {
		startRadio((Event)m_eventsAdapter.getItem(position));
		return;
	}

}
