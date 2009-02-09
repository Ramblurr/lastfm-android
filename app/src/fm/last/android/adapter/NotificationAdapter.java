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

import fm.last.android.R;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ViewSwitcher;

/**
 * This adapter is not representing any particular data
 * however may be used to place some notifications within
 * a ListView widget
 * 
 * @author Lukasz Wisniewski
 */
public class NotificationAdapter extends BaseAdapter {

	/**
	 * Adapter mode in which only a note is shown
	 */
	public static final int INFO_MODE = 0;

	/**
	 * Adapter mode in which it's showing circular progress bar and a note
	 */
	public static final int LOAD_MODE = 1;

	protected Activity mContext;
	
	private int mMode = 0;
	private String mText;

	/**
	 * Default constructor
	 * 
	 * @param context
	 */
	public NotificationAdapter(Activity context) {
		this.mContext = context;
	}
	
	/**
	 * Constructor allowing to set adapter's mode and informative text
	 * 
	 * @param context
	 * @param mode Mode in which this adapter should operate, <code>LOAD_MODE</code> or <code>INFO_MODE</code>
	 * @param text Informative text e.g.: "Loading", "No events found" etc.
	 */
	public NotificationAdapter(Activity context, int mode, String text) {
		this.mContext = context;
		this.mText = text;
		this.mMode = mode;
	}

	public int getCount() {
		return 1;
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}
	
	public boolean isEnabled(int position) {
        return false;
    }

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		
		ViewHolder holder;
		
		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(R.layout.list_row, null);
			
			holder = new ViewHolder();
			holder.label = (TextView)row.findViewById(R.id.row_label);
			holder.vs = (ViewSwitcher)row.findViewById(R.id.row_view_switcher);
			
			row.setTag(holder);
		} else {
			holder = (ViewHolder)row.getTag();
		}
		
		holder.label.setText(mText);
		
		// INFO_MODE
		if(mMode == INFO_MODE){
			holder.vs.setVisibility(View.GONE);
		}else{ //LOAD_MODE
			holder.vs.setDisplayedChild(1);
			holder.vs.setVisibility(View.VISIBLE);
		}
		return row;
	}
	
	/**
	 * "Holder pattern implementation, performance boost"... well not really
	 * here but I put it for my convenience 
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
        TextView label;
        ViewSwitcher vs;
    }

	/**
	 * Sets mode of the <code>NotificationAdapter</code>
	 * 
	 * @param mode Mode in which this adapter should operate, <code>LOAD_MODE</code> or <code>INFO_MODE</code>
	 * @param text Informative text e.g.: "Loading", "No events found" etc.
	 */
	public void setMode(int mode, String text){
		this.mMode = mode;
		this.mText = text;
		
		notifyDataSetChanged();
	}
	
}