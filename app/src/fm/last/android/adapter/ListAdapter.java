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

import java.io.IOException;
import java.io.Serializable;
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
import fm.last.android.R;
import fm.last.android.utils.ImageCache;
import fm.last.android.utils.ImageDownloader;
import fm.last.android.utils.ImageDownloaderListener;

/**
 * Simple adapter for presenting ArrayList of IconifiedEntries as ListView,
 * allows icon customization
 * 
 * @author Lukasz Wisniewski
 * @author Casey Link
 */
public class ListAdapter extends BaseAdapter implements Serializable, ImageDownloaderListener {

	private static final long serialVersionUID = 2679887824070220768L;
	protected transient ImageCache mImageCache;
	protected transient ImageDownloader mImageDownloader;
	protected transient Activity mContext;

	private ArrayList<ListEntry> mList;
	private int mLoadingBar = -1;
	private boolean mScaled = true;
	private boolean mEnabled = true;

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(mList);
	}

	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		try {
			mList = (ArrayList<ListEntry>) in.readObject();
		} catch (ClassCastException e) {
			mList = null;
		}
	}

	public ListAdapter(Activity context) {
		mContext = context;
	}

	/**
	 * Default constructor
	 * 
	 * @param context
	 * @param imageCache
	 */
	public ListAdapter(Activity context, ImageCache imageCache) {
		mContext = context;
		init(imageCache);
	}

	/**
	 * Constructor that takes an array of strings as data
	 * 
	 * @param context
	 * @param data
	 */
	public ListAdapter(Activity context, String[] data) {
		mContext = context;
		mList = new ArrayList<ListEntry>();
		for (int i = 0; i < data.length; i++) {
			ListEntry entry = new ListEntry(data[i], -1, data[i], R.drawable.list_icon_arrow);
			mList.add(entry);
		}
	}

	/**
	 * Sharable code between constructors
	 * 
	 * @param imageCache
	 */
	private void init(ImageCache imageCache) {
		setImageCache(imageCache);
		mList = new ArrayList<ListEntry>();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		ViewHolder holder;

		if (row == null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row = inflater.inflate(R.layout.list_row, null);

			holder = new ViewHolder();
			holder.label = (TextView) row.findViewById(R.id.row_label);
			holder.label_second = (TextView) row.findViewById(R.id.row_label_second);
			holder.image = (ImageView) row.findViewById(R.id.row_icon);
			holder.disclosure = (ImageView) row.findViewById(R.id.row_disclosure_icon);
			holder.vs = (ViewSwitcher) row.findViewById(R.id.row_view_switcher);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		holder.label.setText(mList.get(position).text);
		if (mList.get(position).text_second != null) {
			holder.label_second.setText(mList.get(position).text_second);
			holder.label_second.setVisibility(View.VISIBLE);
		} else {
			holder.label_second.setVisibility(View.GONE);
		}
		if (mList.get(position).icon_id == -1)
			holder.image.setVisibility(View.GONE);
		else
			holder.image.setVisibility(View.VISIBLE);

		// set disclosure image (if set)
		if (mList.get(position).disclosure_id != -1 || mLoadingBar == position) {
			holder.vs.setVisibility(View.VISIBLE);
			holder.disclosure.setImageResource(mList.get(position).disclosure_id);
		} else {
			holder.vs.setVisibility(View.GONE);
		}

		holder.vs.setDisplayedChild(mLoadingBar == position ? 1 : 0);

		// optionally if an URL is specified
		if (mList.get(position).url != null) {
			Bitmap bmp = mImageCache.get(mList.get(position).url);
			if (bmp != null) {
				holder.image.setImageBitmap(bmp);
			} else {
				holder.image.setImageResource(mList.get(position).icon_id);
			}
		} else if (mList.get(position).icon_id >= 0) {

			holder.image.setImageResource(mList.get(position).icon_id);

		} else if (mList.get(position).disclosure_id >= 0) {

			holder.image.setImageResource(mList.get(position).disclosure_id);
		}

		if (!mScaled) {
			((ImageView) row.findViewById(R.id.row_icon)).setScaleType(ImageView.ScaleType.CENTER);
		}

		return row;
	}

	@Override
	public boolean isEnabled(int position) {
		return mEnabled;
	}

	/**
	 * Holder pattern implementation, performance boost
	 * 
	 * @author Lukasz Wisniewski
	 * @author Casey Link
	 */
	static class ViewHolder {
		TextView label;
		TextView label_second;
		ImageView image;
		ImageView disclosure;
		ViewSwitcher vs;
	}

	/**
	 * Sets list of Iconified Entires as a source for the adapter
	 * 
	 * @param list
	 */
	public void setSourceIconified(ArrayList<ListEntry> list) {
		mList = list;
		if (list == null)
			return;
		ArrayList<String> urls = new ArrayList<String>();
		Iterator<ListEntry> it = list.iterator();
		while (it.hasNext()) {
			ListEntry entry = it.next();
			if (entry.url != null) {
				urls.add(entry.url);
			}
		}

		// super.setSource(oldList);

		try {
			if (mImageDownloader.getUserTask() == null) {
				mImageDownloader.getImages(urls);
			}
		} catch (java.util.concurrent.RejectedExecutionException e) {
			e.printStackTrace();
		}
	}

	public void setIconsUnscaled() {
		// some icons shouldn't be scaled :(
		// this is indeed dirty, class needs separating out
		mScaled = false;
	}

	public void asynOperationEnded() {
		this.notifyDataSetChanged();

		// if(mListener != null){
		// mListener.end();
		// }
	}

	public void imageDownloadProgress(int imageDownloaded, int imageCount) {
		this.notifyDataSetChanged();
	}

	public void asynOperationStarted() {
		// TODO mDownloading = true;
		// if(mListener != null){
		// mListener.started();
		// }
	}

	// public void setListener(PreparationListener mListener) {
	// this.mListener = mListener;
	// }

	/**
	 * Enables load bar at given position, at the same time only one can be
	 * launched per adapter
	 * 
	 * @param position
	 */
	public void enableLoadBar(int position) {
		this.mLoadingBar = position;
		notifyDataSetChanged();
	}

	/**
	 * Disables load bar
	 */
	public void disableLoadBar() {
		this.mLoadingBar = -1;
		notifyDataSetChanged();
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}

	public int getCount() {
		if (mList != null)
			return mList.size();
		else
			return 0;
	}

	public Object getItem(int position) {
		return mList.get(position).value;
	}

	public long getItemId(int position) {
		return position;
	}

	public void setImageCache(ImageCache imageCache) {
		mImageDownloader = new ImageDownloader(imageCache);
		mImageDownloader.setListener(this);
		mImageCache = imageCache;
	}

	public void setDisabled() {
		mEnabled = false;
	}

	public void setContext(Activity context) {
		mContext = context;
	}

	public void refreshList() {
		setSourceIconified(mList);
	}

	public void disableDisclosureIcons() {
		for (ListEntry l : mList)
			l.disclosure_id = -1;
	}

}
