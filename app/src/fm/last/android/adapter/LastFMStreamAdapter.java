/***************************************************************************
 *   Copyright 2005-2009 Last.fm Ltd.                                      *
 *   Portions contributed by Casey Link, Lukasz Wisniewski,                *
 *   Mike Jennings, and Michael Novak Jr.                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
 ***************************************************************************/
package fm.last.android.adapter;

import java.util.ArrayList;

import fm.last.android.LastFMApplication;
import fm.last.android.R;

import android.app.Activity;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.widget.ViewSwitcher;

/** The adapter for radio streams, uses non-full-width list entry graphics */
public class LastFMStreamAdapter extends BaseAdapter
{
	public class Stream
	{
		public Stream(String label, String url)
		{
			mLabel = label;
			mStationUrl = url;
		}

	    public int icon()
	    {
	        try {
	        	if(LastFMApplication.getInstance().player != null && LastFMApplication.getInstance().player.isPlaying()) {
		        	String current = LastFMApplication.getInstance().player.getStationUrl();
					if (current != null && mStationUrl.compareTo(current) == 0) {
						// now playing is always the same (focus or not) 
						return R.drawable.now_playing;
					}
	        	}
			}
	        catch (RemoteException e) 
			{}
			return R.drawable.list_icon_station;
	    }
		
		public String mLabel;
		public String mStationUrl;
	};
	
	ArrayList<Stream> mItems;
    Activity context;
    private int mLoadingBar = -1;

    /**
	 * Enables load bar at given position,
	 * at the same time only one can
	 * be launched per adapter
	 * 
	 * @param position
	 */
	public void enableLoadBar(int position){
		this.mLoadingBar = position;
		notifyDataSetChanged();
	}
	
	/**
	 * Disables load bar
	 */
	public void disableLoadBar(){
		this.mLoadingBar = -1;
		notifyDataSetChanged();
	}

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

        if(row == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            row = inflater.inflate( R.layout.list_row, null );
        }
        else
        	row.setTag( "" ); // when reused, don't pretend to be something else

        
        TextView name = (TextView)row.findViewById(R.id.row_label);
        name.setText( mItems.get(position).mLabel );
        
        ViewSwitcher switcher = (ViewSwitcher)row.findViewById(R.id.row_view_switcher);
        row.findViewById(R.id.row_view_switcher).setVisibility(View.VISIBLE);
        
        ((ImageView)row.findViewById( R.id.row_disclosure_icon )).setImageResource( mItems.get(position).icon() );
        
		switcher.setDisplayedChild( mLoadingBar == position ? 1 : 0 );
        
        if(position == mItems.size() - 1) {
        	row.setBackgroundResource(R.drawable.list_entry_rounded_bottom);
        	row.setTag("bottom");
        } else
        	row.setBackgroundResource(R.drawable.list_entry);

        return row;
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
