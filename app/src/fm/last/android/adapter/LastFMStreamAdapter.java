package fm.last.android.adapter;

import java.util.ArrayList;
import java.util.Hashtable;

import fm.last.android.LastFMApplication;
import fm.last.android.R;

import android.R.string;
import android.app.Activity;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseAdapter;

/** WHAT IS THIS?!?!?!? */
public class LastFMStreamAdapter extends BaseAdapter
{
	public class Stream
	{
		public Stream(String label, String url)
		{
			mLabel = label;
			mStationUrl = url;
		}

	    public int radioIcon( boolean hasFocus )
	    {
	        try {
	        	if(LastFMApplication.getInstance().player.isPlaying()) {
		        	String current = LastFMApplication.getInstance().player.getStationUrl();
					if (current != null && mStationUrl.compareTo(current) == 0) {
						// now playing is always the same (focus or not) 
						return R.drawable.now_playing;
					}
	        	}
			} catch (RemoteException e) {
			}
			if (hasFocus)
				return R.drawable.list_radio_icon_focus;
			return R.drawable.list_radio_icon_rest;
	    }
		
		public String mLabel;
		public String mStationUrl;
	};
	
	ArrayList<Stream> mItems;
    Activity context;

    public LastFMStreamAdapter( Activity context )
    {
    	mItems = new ArrayList<Stream>();
        this.context = context;
    }
    
    public int getCount()
    {
        return mItems.size();
    }

    public Object getItem( int position )
    {
        return mItems.get( position );
    }

    public long getItemId( int position )
    {
        return position;
    }

    public View getView( int position, View convertView, ViewGroup parent )
    {
        View row = convertView;

        if ( row == null )
        {
            LayoutInflater inflater = context.getLayoutInflater();
            row = inflater.inflate( R.layout.list_row, null );
        }

        row.setBackgroundResource(R.drawable.list_item_rest);
        
        TextView name = (TextView)row.findViewById(R.id.row_label);
        name.setText( mItems.get(position).mLabel );
        
        row.findViewById(R.id.row_view_switcher).setVisibility(View.VISIBLE);
        ImageView icon = (ImageView)row.findViewById(R.id.row_disclosure_icon);
		icon.setImageResource( radioIcon( position, false ) );
        
        if(position == mItems.size() - 1) {
        	row.setBackgroundResource(R.drawable.list_item_rest_rounded_bottom);
        	row.setTag("bottom");
        }
        else
        	row.setBackgroundResource(R.drawable.list_item_rest);
        return ( row );
    }
    
    public int radioIcon( int position, boolean hasFocus )
    {
    	return mItems.get(position).radioIcon(hasFocus);
    }

    public void putStation( String label, String station )
    {
    	mItems.add( new Stream( label, station ) );
    }
    
    public void putStationAtFront( String label, String station )
    {
    	mItems.add(0, new Stream( label, station ) );
    }

    public void resetList()
    {
    	mItems.clear();
    }

    public void updateModel()
    {
        notifyDataSetChanged();
    }

    public String getLabel( int position )
    {
        return mItems.get(position).mLabel;
    }

    public String getStation( int position )
    {
        return mItems.get(position).mStationUrl;
    }

}
