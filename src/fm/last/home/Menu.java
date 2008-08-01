package fm.last.home;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class Menu extends ListActivity
{
	private ArrayList<HashMap<String, String>> m_list = null; 
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		m_list = new ArrayList<HashMap<String, String>>();
		populateList();
		
        setListAdapter( new MenuAdapter( this ) );
        getListView().setOnItemClickListener( m_listClickListener );
	}
	
	private void populateList()
	{
		addMenuItem( "Events", "EVENTSVIEW" );
		addMenuItem( "Tags", "TAGBROWSER" );
		addMenuItem( "Friend Mapper", "FRIENDSVIEW" );
		addMenuItem( "Similar Artist", "SIMILARARTIST" );
	}
	
	private void addMenuItem( String title, String action )
	{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put( "title", title );
		map.put( "action", action );
		m_list.add( map );
	}
	
	private OnItemClickListener m_listClickListener = new OnItemClickListener()
	{
		public void onItemClick(AdapterView parent, View v, int position, long id)
		{
			String intentName = m_list.get( (int)id ).get( "action" );
			Intent i = new Intent( intentName );
			startActivity( i );
		}
	};
}