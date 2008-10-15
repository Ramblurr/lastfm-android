package fm.last.home;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.list.ListModel;
import androidx.view.ViewFactory;
import fm.last.R;

public class MenuAdapter extends BaseAdapter {
	private Activity m_activity;
	private ListModel<MenuData> listModel;
	private ViewFactory convertViewFactory;
	
	public MenuAdapter( Activity activity, ListModel<MenuData> listModel, ViewFactory convertViewFactory )
	{
		m_activity = activity;
		this.listModel = listModel;
		this.convertViewFactory = convertViewFactory;
	}
	
	public int getCount()
	{
		return listModel.getCount();
	}

	public Object getItem(int position)
	{
	  return listModel.getItem(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if( convertView == null )
		{
			convertView = convertViewFactory.createView(m_activity);
		}
		
		TextView tv = (TextView)convertView.findViewById( R.id.name );
		ImageView iv = (ImageView)convertView.findViewById( android.R.id.icon );
		
		tv.setText( "foo:" + listModel.getItem(position).getMenuText() );
		return convertView;
	}

}
