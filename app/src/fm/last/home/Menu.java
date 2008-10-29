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
import androidx.list.ListModel;

public class Menu extends ListActivity
{
	private ListModel<MenuData> menuListModel;
	
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		menuListModel = new MenuListModel();
        setListAdapter( new MenuAdapter( this, menuListModel, new ConvertViewFactory()) );
        getListView().setOnItemClickListener( m_listClickListener );
	}
	
	
	private OnItemClickListener m_listClickListener = new OnItemClickListener()
	{
		public void onItemClick(AdapterView parent, View v, int position, long id)
		{
		  MenuData menuData = menuListModel.getItem((int)id);
		  Intent i = new Intent( menuData.getIntentName() );
		  startActivity( i );
		}
	};
}