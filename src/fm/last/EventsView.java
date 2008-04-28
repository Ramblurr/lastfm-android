package fm.last;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;
import android.widget.TableRow;
import android.widget.TableLayout;
import android.view.ViewGroup.LayoutParams;

public class EventsView extends Activity {

	private TableLayout eventsTable = null;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.events_view);

		eventsTable = (TableLayout) findViewById(R.id.events_table);

		Event[] events = Event.getByLocation("E5 0ES");
		for (Event event : events) {
			addEvent(event);
		}
	}

	public void addEvent(Event event) {
		TextView text = new TextView(this);
		text.setLayoutParams(new TableRow.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		text.setText(event.title(), TextView.BufferType.NORMAL);
		text.setHorizontallyScrolling(false);
		text.setSingleLine(false);
		text.setMaxLines(4);

		TableRow row = new TableRow(this);
		row.setLayoutParams(new TableLayout.LayoutParams());
		row.addView(text);

		eventsTable.addView(row);
	}
}
