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
		
        setListAdapter(new SimpleAdapter(this, m_list,
                android.R.layout.simple_list_item_1, new String[] { "title" },
                new int[] { android.R.id.text1 }));
        getListView().setOnItemClickListener( m_listClickListener );
	}
	
	private void populateList()
	{
		{
			HashMap<String, String> map = new HashMap<String, String>();
			map.put( "title", "Events" );
			map.put( "intent", "EVENTVIEW" );
			m_list.add( map );
		}
		
		{
			HashMap<String, String> map = new HashMap<String, String>();
			map.put( "title", "Tags" );
			map.put( "intent", "TAGBROWSER" );
			m_list.add( map );
		}
		
		{
			HashMap<String, String> map = new HashMap<String, String>();
			map.put( "title", "Friend Mapper" );
			map.put( "intent", "FRIENDSVIEW" );
			m_list.add( map );
		}
		
		{
			HashMap<String, String> map = new HashMap<String, String>();
			map.put( "title", "Similar Artist" );
			map.put( "intent", "SIMILARARTIST" );
			m_list.add( map );
		}
	}
	
	private OnItemClickListener m_listClickListener = new OnItemClickListener()
	{
		public void onItemClick(AdapterView parent, View v, int position, long id)
		{
			String intentName = m_list.get( (int)id ).get( "intent" );
			Intent i = new Intent( intentName );
			startActivity( i );
		}
	};
}