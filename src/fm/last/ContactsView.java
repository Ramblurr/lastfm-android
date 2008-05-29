package fm.last;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class ContactsView extends ListActivity
{
	private ListAdapter adapter;
	
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		
		Cursor c = getContentResolver().query( People.CONTENT_URI, null, null, null, null );
		startManagingCursor( c );
		
		String[] columns = new String[] { People.NAME };
		int[] names = new int[] { android.R.id.text1 };
		
		adapter = new SimpleCursorAdapter( this, android.R.layout.simple_list_item_1, c, columns, names );
		setListAdapter( adapter );
	}
}