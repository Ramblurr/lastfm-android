/***************************************************************************
 *    Copyright (c) 2008  Jeffrey Sharkey <Jeffrey.Sharkey@gmail.com>        
 *                                                                         
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ****************************************************************************/
package fm.last.android.adapter;

import java.util.LinkedHashMap;
import java.util.Map;

import fm.last.android.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;


public class SeparatedListAdapter extends BaseAdapter
{
    public final Map<String,Adapter> sections = new LinkedHashMap<String,Adapter>();
    public ArrayAdapter<String> headers;
    public final static int TYPE_SECTION_HEADER = 0;

    public SeparatedListAdapter(Context context) {
        headers = new ArrayAdapter<String>(context, R.layout.list_header);
    }

    public void addSection(String section, Adapter adapter) {
        this.headers.add(section);
        this.sections.put(section, adapter);
    }

    public Object getItem(int position) {
        for(Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if(position == 0) return section;
            if(position < size) return adapter.getItem(position - 1);

            // otherwise jump into next section
            position -= size;
        }
        return null;
    }

    public void enableLoadBar(int position) {
        for(Object section : this.sections.keySet()) {
            Adapter adapter = (LastFMStreamAdapter)sections.get(section);
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if(position < size) {
                if( adapter instanceof LastFMStreamAdapter)
                {
                    LastFMStreamAdapter ladapter = (LastFMStreamAdapter) adapter;
            		ladapter.enableLoadBar(position - 1);
            		notifyDataSetChanged();
                }
            	return;
            }

            // otherwise jump into next section
            position -= size;
        }
    }

    public void disableLoadBar() {
        for(Object section : this.sections.keySet()) {
            LastFMStreamAdapter adapter = (LastFMStreamAdapter)sections.get(section);
            if(adapter != null)
            	adapter.disableLoadBar();
        }
    }

    public String getStation(int position) {
        for(Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if(position == 0) return null;
            if(position < size){
                if( adapter instanceof LastFMStreamAdapter)
                {
                    LastFMStreamAdapter ladapter = (LastFMStreamAdapter) adapter;
                    return ladapter.getStation( position - 1 );
                }
            }

            // otherwise jump into next section
            position -= size;
        }
        return null;
    }

    public Adapter getAdapterForPosition(int position) {
        for(Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if(position == 0) return null;
            if(position < size){
            	return adapter;
            }

            // otherwise jump into next section
            position -= size;
        }
        return null;
    }

    public int getCount() {
        // total together all sections, plus one for each section header
        int total = 0;
        for(Adapter adapter : this.sections.values())
            total += adapter.getCount() + 1;
        return total;
    }

    public int getViewTypeCount() {
        // i've commented out sharkey's original implementation because 
        // it was totally crashy (when flinging the list, and moving through 
        // with cursors) and i have no idea why!.  please explain.  [doug]  
    	
    	return 2;
    	
    	// assume that headers count as one, then total all sections
//        int total = 1;
//        for(Adapter adapter : this.sections.values())
//            total += adapter.getViewTypeCount();
//        return total;
    }

    public int getItemViewType(int position) {
        int type = 1;
        for(Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if(position == 0) return TYPE_SECTION_HEADER;
            
            // i've replaced sharkey's original line below because 
            // it was totally crashy (when flinging the list, and moving through 
            // with cursors) and i have no idea why!.  please explain.  [doug]
            
//            if(position < size) return type + adapter.getItemViewType(position - 1);
            if(position < size) return 1;

            // otherwise jump into next section
            position -= size;
            type += adapter.getViewTypeCount();
        }
        return -1;
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return (getItemViewType(position) != TYPE_SECTION_HEADER);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int sectionnum = 0;
        for(Object section : this.sections.keySet()) {
            Adapter adapter = sections.get(section);
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if(position == 0) return headers.getView(sectionnum, convertView, parent);
            if(position < size) return adapter.getView(position - 1, convertView, parent);

            // otherwise jump into next section
            position -= size;
            sectionnum++;
        }
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

}
