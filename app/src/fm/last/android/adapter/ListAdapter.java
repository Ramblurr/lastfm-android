package fm.last.android.adapter;

import java.util.ArrayList;

import fm.last.android.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Simple adapter for presenting ArrayList data as ListView
 * 
 * @author Lukasz Wisniewski
 */
public class ListAdapter extends BaseAdapter{

	private ArrayList<String> mList;
	protected Activity mContext;
	protected int mResId = 0;
	
	/**
	 * Default constructor
	 * 
	 * @param context
	 */
	public ListAdapter(Activity context){
		this.mContext = context;
		this.mList = new ArrayList<String>();
	}

	/**
	 * Sets adapter data source
	 * 
	 * @param list
	 */
	public void setSource(ArrayList<String> list){
		mList = list;
		notifyDataSetChanged();
	}
	
	/**
	 * Sets adapter data source, simple image
	 * 
	 * @param list
	 * @param resId image to be displayed next to each entry
	 */
	public void setSource(ArrayList<String> list, int resId){
		setSource(list);
		mResId = resId;
	}

	public int getCount() {
		return mList.size();
	}

	public Object getItem(int position) {
		return mList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;
		
		ViewHolder holder;

		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(R.layout.list_row, null);
			
			holder = new ViewHolder();
			holder.iv = (ImageView)row.findViewById(R.id.radio_row_img);
			holder.radioName = (TextView)row.findViewById(R.id.radio_row_name);
			
			row.setTag(holder);
		} else {
			holder = (ViewHolder)row.getTag();
		}

		if(mResId != 0){
			holder.iv.setImageResource(mResId);
		}
		else {
			holder.iv.setVisibility(View.GONE);
		}
		holder.radioName.setText(mList.get(position));

		return row;
	}
	
	/**
	 * This is a static class allowing holder pattern implementation as shown
	 * in Google's API Demos. This saves time that normally would be wasted on
	 * inflating views from XML - using this method inflates views within a row
	 * only once, not each time we scroll the list
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
        TextView radioName;
        ImageView iv;
    }
	
	/**
	 * Removes item at given position
	 * 
	 * @param position
	 */
	public void removeItem(int position){
		mList.remove(position);
		notifyDataSetChanged();
	}
	
	/**
	 * Forces OnScrollListener implementation by inherited classes, makes 
	 * programmer aware of problems that may arise when loading dynamic content
	 * that most likely will be addressed here  
	 * 
	 * @return
	 */
	public OnScrollListener getOnScrollListener(){
		return null;
	}

}
