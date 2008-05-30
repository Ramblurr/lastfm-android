package fm.last;

import android.os.Bundle;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation; 

public class FriendMapperView extends ListActivity implements OnItemClickListener
{
	private FriendsAdapter m_eventsAdapter;

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
		Intent i = new Intent( "CONTACTS_ACTION" );
		startSubActivity( i, 0 );
	}


	protected void onActivityResult(int requestCode, int resultCode, String data, Bundle extras)
	{
		if( resultCode != RESULT_OK )
			return;
		
		Toast.makeText(this, data, 10000 ).show();
	}
	
}
