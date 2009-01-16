package fm.last.android.adapter;

import java.util.ArrayList;

import fm.last.android.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter class for tags used within TagActivity, implements
 * specific behavior like fading out clicked tags, smaller font
 * etc.
 * 
 * @author Lukasz Wisniewski
 */
public class TagListAdapter extends ListAdapter {
	
	/**
	 * Internal class representing single row entry
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class Entry{
		/**
		 * Text to be displayed
		 */
		String text;
		/**
		 * Indicates whether the entry has
		 * already been added meaning whether
		 * it should be grayed out
		 */
		boolean added;
		
		public Entry(String text, boolean added) {
			this.text = text;
			this.added = added;
		}
	}
	
	private ArrayList<Entry> mList; 

	// TODO public TagListAdapter(Activity context, ArrayList<String> presentTags)
	
	public TagListAdapter(Activity context) {
		super(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;
		
		ViewHolder holder;

		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(R.layout.list_row, null);
			
			holder = new ViewHolder();
			holder.icon = (ImageView)row.findViewById(R.id.row_icon);
			holder.label = (TextView)row.findViewById(R.id.row_label);
			
			row.setTag(holder);
		} else {
			holder = (ViewHolder)row.getTag();
		}
		
		// TODO remove hardcoded colors
		if(mList.get(position).added){
			holder.label.setTextColor(0x337a7a7a);
		} else {
			holder.label.setTextColor(0xff000000);
		}
		
		holder.label.setText(mList.get(position).text);

		return row;
	}

	public int getCount() {
		if(mList != null)
			return mList.size();
		else
			return 0;
	}

	public Object getItem(int position) {
		return mList.get(position).text;
	}

	public long getItemId(int position) {
		return position;
	}
	
	
	/**
	 * Holder pattern implementation, performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
        TextView label;
        ImageView icon;
    }
	
	public void setSource(ArrayList<String> list) {
		mList = new ArrayList<Entry>();
		for(int i=0; i<list.size(); i++){
			mList.add(new Entry(list.get(i), false));
		}
		notifyDataSetChanged();
	}
	
	/**
	 * Notify adapter that tag at given
	 * position has been added
	 * 
	 * @param position
	 */
	public void tagAdded(int position){
		mList.get(position).added = true;
		notifyDataSetChanged();
	}
	
	/**
	 * Notify adapter that given tag was
	 * unadded
	 * 
	 * @param tag
	 */
	public void tagUnadded(String tag){
		for(int i=0;i<mList.size();i++){
			if(mList.get(i).text.equals(tag)){
				mList.get(i).added = false;
			}
		}
		notifyDataSetChanged();
	}

}
