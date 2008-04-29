package fm.last;

import android.os.Bundle;
import android.app.Activity;
import android.widget.*;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.util.Log;

public class EventsView extends Activity {

	private TableLayout eventsTable = null;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.events_view);

		eventsTable = (TableLayout) findViewById(R.id.events_table);
		
		Log.i( "Last.fm","Downloading event information.." );
		Event.EventList events = Event.getByLocation("E5 0ES");
		Log.i( "Last.fm", "Retrieved events" );
		int count = 0;
		for (Event event : events ) {
			addEvent(event);
			if( ++count == 5 )
				break;
		}

	}

	public void addEvent(Event event) {
		Button button = new Button( this );
		button.setText("Play");
		button.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, 30));
		button.setGravity( Gravity.CENTER_VERTICAL );
		TextView text = new TextView(this);
		text.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, 30));
		text.setText(event.title(), TextView.BufferType.NORMAL);
		text.setHorizontallyScrolling(false);
		text.setSingleLine(false);
		text.setMaxLines(4);
		text.setMaxHeight(70);
		text.setMinHeight(70);
		text.setGravity( Gravity.CENTER_VERTICAL );
		
		TableRow row = new TableRow(this);
		row.setLayoutParams(new TableLayout.LayoutParams());
		row.addView(button);
		row.addView(text);
		
		eventsTable.addView(row);
	}
}
