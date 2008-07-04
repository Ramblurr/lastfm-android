package fm.last.events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import fm.last.Log;
import fm.last.R;
import fm.last.radio.RadioView;

public class EventRadio extends RadioView {
	private Event m_event;
	
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		setHeaderRes( R.layout.event_radio_partial );
        
        Button info = (Button) findViewById( R.id.info );
        info.setOnClickListener( new OnClickListener()
        {
        	public void onClick( View v )
        	{
        		Intent i = new Intent( "MAP_ACTION" );
        		
        		Event e = EventRadio.this.m_event;
        		i.putExtra( "latitude", e.latitude() );
        		i.putExtra( "longitude", e.longitude() );
        		i.putExtra( "venue", e.venue() );
        		
        		startActivity( i );
        	}
        });
        
		final Bundle extras = getIntent().getExtras();
		if( extras.containsKey( "eventXml" ))
		{
			readEvent( extras.getString( "eventXml" ) );
		}
	}
	
	private void readEvent( String eventXml )
	{
		m_event = Event.EventFromXmlString( eventXml );
		if( m_event == null )
		{
			Log.e("cannot get event from XML:\n" + eventXml );
			return;
		}
		
		if( m_event.headliner() != null )
			((TextView) findViewById( R.id.headliner )).setText( m_event.headliner() );
		
		if( m_event.artists() != null )
		{
			((TextView) findViewById( R.id.venue )).setText( m_event.venue() );
			tuneInSimilarArtists( m_event.headliner() );
		}
	}

}
