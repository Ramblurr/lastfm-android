package fm.last;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fm.last.ws.RequestManager;
import fm.last.ws.RequestParameters;
import fm.last.ws.Response;

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
	private FriendsView m_view = null;
	ArrayList< User > m_friendsList = new ArrayList< User >();
	private String m_postcode = null;
	private int m_eventPagesToLoad = 0;
	private int m_eventPagesLoaded = 0;
	private Thread m_thread = null;
	
	//Cached ImageLoader
	private ImageLoader m_imageLoader;
	
	public FriendsAdapter( FriendsView view )
	{
		m_view = view;
		m_imageLoader = new ImageLoader(view, true);
	}
	
	public void loadFriends( String username )
	{
		RequestParameters params = new RequestParameters();
		params.add( "user", username );
		Response response = RequestManager.version2().callMethod( "user.getfriends", params );
		
		Document xmlDoc = response.xmlDocument();
			
		NodeList users = xmlDoc.getElementsByTagName("user");
		for( int i = 0; i < users.getLength(); i++ )
		{
			m_friendsList.add(
					User.newUserFromFriendXML((Element)users.item(i))
			);
		}
		notifyDataSetChanged();
	}
	
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
