package fm.last.android.activity;

import java.util.ArrayList;
import java.util.Hashtable;

import fm.last.android.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.BaseAdapter;

public class LastFMStreamAdapter extends BaseAdapter
{
	ArrayList<String> mLabels;
	ArrayList<String> mStations;
    Activity context;

    LastFMStreamAdapter( Activity context )
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
        	if(mStations.get(position).startsWith("lastfm://"))
                row = inflater.inflate( R.layout.station_row, null );
        	else
                row = inflater.inflate( R.layout.disclosure_row, null );
        }

        TextView name = (TextView)row.findViewById(R.id.label);
        name.setText( mLabels.get(position) );

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
