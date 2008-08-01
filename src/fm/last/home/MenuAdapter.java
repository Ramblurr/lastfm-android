package fm.last.home;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fm.last.R;

public class MenuAdapter extends BaseAdapter {
	private Activity m_activity;
	static String[] listItems = new String[] { "Events", "Tags", "Friend Mapper", "Similar Artists" };
	static String[] listIntents = new String[] { "EVENTSVIEW", "TAGBROWSER", "FRIENDSVIEW", "SIMILARARTIST" };
	static int[] listIcons = new int[] { };
	
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
			LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, 40 );
			tlp.weight = 10;
			tlp.gravity = Gravity.LEFT;
			tv.setLayoutParams( tlp );
			
			tv.setId( R.id.name );
			((LinearLayout)convertView).addView( tv );
			
			
			ImageView iv = new ImageView( m_activity );
			iv.setId( android.R.id.icon );
			iv.setImageResource( R.drawable.streaming );
			LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, 40 );
			ilp.gravity = 0;
			ilp.weight = 0;
			tv.setLayoutParams( ilp );
		}
		
		TextView tv = (TextView)convertView.findViewById( R.id.name );
		ImageView iv = (ImageView)convertView.findViewById( android.R.id.icon );
		
		tv.setText( listItems[ position ] );
		return convertView;
	}

}
