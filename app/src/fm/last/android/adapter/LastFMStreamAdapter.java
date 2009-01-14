package fm.last.android.adapter;

import java.util.ArrayList;
import java.util.Hashtable;

import fm.last.android.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseAdapter;

/** WHAT IS THIS?!?!?!? */
public class LastFMStreamAdapter extends BaseAdapter
{
	ArrayList<String> mLabels;
	ArrayList<String> mStations;
    Activity context;

    public LastFMStreamAdapter( Activity context )
    {
    	mLabels = new ArrayList<String>();
    	mStations = new ArrayList<String>();
        this.context = context;
    }

    public int getCount()
    {

        return mLabels.size();
    }

    public Object getItem( int position )
    {
        return mLabels.get(position);
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
        name.setText( mLabels.get(position) );
        
        row.findViewById(R.id.row_view_switcher).setVisibility(View.VISIBLE);
        ImageView icon = (ImageView)row.findViewById(R.id.row_disclosure_icon);
        icon.setImageResource(R.drawable.radio_icon);
        
        if(position == mLabels.size() - 1) {
        	row.setBackgroundResource(R.drawable.list_item_rest_rounded_bottom);
        	row.setTag("bottom");
        }
        else
        	row.setBackgroundResource(R.drawable.list_item_rest);
        return ( row );
    }

    public void putStation( String label, String station )
    {
    	mLabels.add(label);
    	mStations.add(station);
    }

    public void resetList()
    {
    	mLabels.clear();
    	mStations.clear();
    }

    public void updateModel()
    {

        notifyDataSetChanged();
    }

    public String getLabel( int position )
    {
        return mLabels.get(position);
    }

    public String getStation( int position )
    {
        return mStations.get(position);
    }

}
