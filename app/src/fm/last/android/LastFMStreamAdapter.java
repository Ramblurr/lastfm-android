package fm.last.android;

import java.util.Hashtable;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class LastFMStreamAdapter extends BaseAdapter
{

    Hashtable<String, String> mData;

    Activity context;

    LastFMStreamAdapter( Activity context )
    {

        mData = new Hashtable<String, String>();
        this.context = context;
    }

    public int getCount()
    {

        return mData.size();
    }

    public Object getItem( int position )
    {

        Object item = mData.keySet().toArray()[position];
        return item;
    }

    public long getItemId( int position )
    {

        return position;
    }

    public View getView( int position, View convertView, ViewGroup parent )
    {

        View row = convertView;
        ViewWrapper wrapper = null;

        if ( row == null )
        {
            LayoutInflater inflater = context.getLayoutInflater();
            row = inflater.inflate( R.layout.list_row, null );
            wrapper = new ViewWrapper( row );
            row.setTag( wrapper );
        }
        else
        {
            wrapper = ( ViewWrapper ) row.getTag();
        }
        wrapper.getLabel().setText( ( String ) getItem( position ) );

        return ( row );
    }

    public void putStation( String label, String station )
    {

        mData.put( label, station );
    }

    public void resetList()
    {

        mData.clear();
    }

    public void updateModel()
    {

        notifyDataSetChanged();
    }

    public String getLabel( int position )
    {

        return ( String ) getItem( position );
    }

    public String getStation( int position )
    {

        return mData.get( getLabel( position ) );
    }

}
