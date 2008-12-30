package fm.last.android.adapter;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.AbsListView.OnScrollListener;

import fm.last.android.R;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.ImageDownloader;
import fm.last.android.utils.ImageDownloaderListener;

/**
 * Simple adapter for presenting ArrayList of IconifiedEntries as ListView, 
 * allows icon customization
 * 
 * @author Lukasz Wisniewski
 */
public class IconifiedListAdapter extends ListAdapter implements ImageDownloaderListener, OnScrollListener {
	
	protected ImageCache mImageCache;
	protected ImageDownloader mImageDownloader;
//	protected PreparationListener mListener;
	
	private ArrayList<IconifiedEntry> mList;
	private boolean mScrolling = false;
	private int mLoadingBar = -1;
	
//	/**
//	 * Default constructor with additional parameter for preparation listener
//	 * 
//	 * @param context
//	 * @param imageCache
//	 * @param pl
//	 */
//	public IconifiedListAdapter(Activity context, ImageCache imageCache, PreparationListener pl) {
//		super(context);
//		setListener(pl);
//		init(imageCache);
//	}


	/**
	 * Default constructor
	 * 
	 * @param context
	 * @param imageCache
	 */
	public IconifiedListAdapter(Activity context, ImageCache imageCache) {
		super(context);
		init(imageCache);
	}

	/**
	 * Sharable code between constructors
	 * 
	 * @param imageCache
	 */
	private void init(ImageCache imageCache){
		mImageDownloader = new ImageDownloader(imageCache);
		mImageDownloader.setListener(this);
		mImageCache = imageCache;
		mList = new ArrayList<IconifiedEntry>();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;

		ViewHolder holder;

		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(R.layout.iconified_list_row, null);

			holder = new ViewHolder();
			holder.radioName = (TextView)row.findViewById(R.id.radio_row_name);
			holder.image = (ImageView)row.findViewById(R.id.radio_row_img);
			holder.vs = (ViewSwitcher)row.findViewById(R.id.row_view_switcher);

			row.setTag(holder);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}

		holder.radioName.setText(mList.get(position).text);
		//holder.image.setImageResource(mList.get(position).id);

		// add arrow if selection has a child
		if(mList.get(position).hasChild){
			holder.vs.setVisibility(View.VISIBLE);
		} else {
			holder.vs.setVisibility(View.GONE);
		}
		
		if(mLoadingBar == position){
			holder.vs.setDisplayedChild(1);
		}
		else{
			holder.vs.setDisplayedChild(0);
		}

		// optionally if an URL is specified
		if(mList.get(position).url != null){
			Bitmap bmp = mImageCache.get(mList.get(position).url);
			if(bmp != null){
				holder.image.setImageBitmap(bmp);
			} else {
				holder.image.setImageResource(mList.get(position).id);
			}
		} else {
			holder.image.setImageResource(mList.get(position).id);
		}

		return row;
	}

	/**
	 * Holder pattern implementation,
	 * performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
		TextView radioName;
		ImageView image;
		ViewSwitcher vs;
	}

	/**
	 * Sets list of Iconified Entires as a source for the adapter
	 * 
	 * @param list
	 */
	public void setSourceIconified(ArrayList<IconifiedEntry> list) {
		mList = list;

		ArrayList<String> urls = new ArrayList<String>();
		Iterator<IconifiedEntry> it = list.iterator ();
		while (it.hasNext ()) {
			IconifiedEntry entry = it.next();
			if(entry.url != null){
				urls.add(entry.url);
			}
		}

//		super.setSource(oldList);

		if(mImageDownloader.getUserTask() == null){
			mImageDownloader.getImages(urls);
		}
	}

	public void asynOperationEnded() {
		this.notifyDataSetChanged();
		
//		if(mListener != null){
//			mListener.end();
//		}
	}

	public void imageDownloadProgress(int imageDownloaded, int imageCount) {
		// TODO if scrolling do notifyDataSetChanged
//		if(mListener != null){
//			mListener.progress(imageDownloaded, imageCount);
//		}
		
		if(!mScrolling){
			this.notifyDataSetInvalidated();
		} else {
			this.notifyDataSetChanged();
		}
	}

	public void asynOperationStarted() {
		// TODO mDownloading = true;
//		if(mListener != null){
//			mListener.started();
//		}
	}

//	public void setListener(PreparationListener mListener) {
//		this.mListener = mListener;
//	}
	
	public OnScrollListener getOnScrollListener(){
		return this;
	}
	
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


	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}


	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrolling = true;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position).value;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
