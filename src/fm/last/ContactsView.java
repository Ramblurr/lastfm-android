package fm.last;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

//TODO handle zero contacts

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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		Cursor c = (Cursor)adapter.getItem(position);
		Intent i = new Intent();
		final int idIndex = c.getColumnIndexOrThrow( People._ID );
		i.putExtra( "id", c.getString( idIndex));
		setResult(RESULT_OK, i );
		finish();
	}
}