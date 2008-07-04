package fm.last.home;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import fm.last.R;

public class MenuAdapter extends BaseAdapter {
	private Activity m_activity;
	static String[] listItems = new String[] { "Events" };
	
	public MenuAdapter( Activity activity )
	{
		m_activity = activity;
	}
	
	public int getCount()
	{
		return listItems.length;
	}

	public Object getItem(int position)
	{
		return listItems[ position ];
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if( convertView == null )
		{
			convertView = new LinearLayout( m_activity );
			TextView tv = new TextView( m_activity );
			tv.setGravity( Gravity.CENTER_VERTICAL );
			tv.setPadding( 4, 0, 4, 0 );
			tv.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.FILL_PARENT, 40 ) );
			tv.setId( R.id.name );
			((LinearLayout)convertView).addView( tv );
		}
		
		TextView tv = (TextView)convertView.findViewById( R.id.name );
		tv.setText( listItems[ position ] );
		return null;
	}

}
