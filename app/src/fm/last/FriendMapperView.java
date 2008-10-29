package fm.last;

import java.io.FileNotFoundException;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.view.Window;

public class FriendMapperView extends ListActivity implements OnItemClickListener
{
	private FriendsAdapter m_eventsAdapter;
	private User m_selectedUser = null;
	
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_OFF);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_INDETERMINATE_ON);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView( R.layout.events_view );
		
		m_eventsAdapter = new FriendsAdapter( this );
		setListAdapter( m_eventsAdapter );
		getListView().setOnItemClickListener(this);

		SharedPreferences prefs = getSharedPreferences( "Last.fm", MODE_PRIVATE );
		String user = prefs.getString( "username", "" );
		m_eventsAdapter.loadFriends( user );
	}

	public void onItemClick(AdapterView parent, View v, int position, long id)
	{
		m_selectedUser = (User)m_eventsAdapter.getItem( position );
		Intent i = new Intent( "CONTACTS_ACTION" );
		startActivityForResult( i, 0 );
	}


	protected void onActivityResult(int requestCode, int resultCode, String data, Bundle extras)
	{
		if( resultCode 	   != RESULT_OK || 
			m_selectedUser == null )
			return;

		try
		{
			m_selectedUser.setAndroidId( Integer.parseInt( data ) );
		} catch ( FileNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_selectedUser = null;
	}
	
}
