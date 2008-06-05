package fm.last;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fm.last.ws.RequestManager;
import fm.last.ws.RequestParameters;
import fm.last.ws.Response;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FriendsAdapter extends BaseAdapter
{
	private Activity m_view = null;
	ArrayList< User > m_friendsList = new ArrayList< User >();
	
	//Cached ImageLoader
	private ImageLoader m_imageLoader;
	
	public FriendsAdapter( Activity view )
	{
		m_view = view;
		m_imageLoader = new ImageLoader(view, true);
	}
	
	public void loadFriends( String username )
	{
		m_view.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON );
		RequestParameters params = new RequestParameters();
		params.add( "user", username );

		RequestManager.version2().callMethod( "user.getfriends", params, m_requestEvent );
	}
	
	fm.last.ws.EventHandler m_requestEvent = new fm.last.ws.EventHandler()
	{
		public void onError(int id, String error) {
			// TODO Auto-generated method stub
			Log.e( "Error occured in Friend list request: " + error );
		}

		public void onMethodComplete(int id, Response response) {
			Document xmlDoc = response.xmlDocument();
			
			NodeList users = xmlDoc.getElementsByTagName("user");
			for( int i = 0; i < users.getLength(); i++ )
			{
				m_friendsList.add(
						User.newUserFromFriendXML((Element)users.item(i))
				);
			}
			m_view.runOnUIThread( new Runnable()
				{
					public void run() {
						notifyDataSetChanged();
						m_view.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_OFF );					
					}
				}
			);
		}
		
	};
	
	public boolean areAllItemsSelectable() 
	{
		return true;
	}

	public boolean isSelectable(int arg0) 
	{
		return true;
	}

	public int getCount() 
	{
		return m_friendsList.size();
	}

	public Object getItem(int position) { return m_friendsList.get(position); }
	public long getItemId(int position) { return position; }

	public int getNewSelectionForKey(int currentSelection, int keyCode, KeyEvent event) 
	{
		// TODO Auto-generated method stub
		return NO_SELECTION;
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{
		User friend = m_friendsList.get(position); 
		ViewInflate viewInflater = m_view.getWindow().getViewInflate();

		if( convertView == null )
		{
			convertView = viewInflater.inflate( R.layout.friend_partial, 
											    parent,
											    false, null );
			Log.i("Creating new view");
		}
		else
		{
			Log.i("converting old view");
		}
		
		LinearLayout ll = (LinearLayout)convertView;
		
		ImageView avatar = (ImageView)ll.findViewById(R.id.avatar);
		if( friend.imageUrl() != null )
			m_imageLoader.loadImage(avatar, friend.imageUrl());
		else
			avatar.setImageResource(android.R.drawable.empty);
		
		TextView name = (TextView)ll.findViewById(R.id.name);
		name.setText(friend.username());
		
		return ll;
	}
}
