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
package fm.last.android.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Class responsible for downloading images asynchronously
 * in a separate threads
 * 
 * @author Lukasz Wisniewski
 */
public class ImageDownloader {
	
	private static final String TASK_TAG_DEFAULT = "default";
	
	protected static final String TAG = "ImageDownloader";
	ImageDownloaderListener mListener;
	ImageCache mImageCache;
	
	/**
	 * Handle to ongoing tasks
	 */
	private Hashtable<String, UserTask<ArrayList<String>, Integer, Object> > mTasks;

	/**
	 * Default constructor
	 * 
	 * @param imageCache
	 */
	public ImageDownloader(ImageCache imageCache){
		if(imageCache == null){
			imageCache = new ImageCache();
		} 
		this.mImageCache = imageCache;
		
		mTasks = new Hashtable<String, UserTask<ArrayList<String>, Integer, Object> >();
	}

	public void setListener(ImageDownloaderListener l){
		this.mListener = l;
	}
	
	/**
	 * Requests images download, for multiple running on one instance use
	 * getImages(ArrayList&lt;String&gt; urls, String tag)
	 * 
	 * @param urls
	 */
	public void getImages(ArrayList<String> urls){
		getImages(urls, TASK_TAG_DEFAULT);
	}

	/**
	 * Requests images download additionally tagging running task with a
	 * given string vale 
	 * 
	 * @param urls
	 * @param tag
	 */
	@SuppressWarnings("unchecked")
	public void getImages(ArrayList<String> urls, String tag){
		
		UserTask< ArrayList<String>, Integer, Object> userTask = 
			new UserTask< ArrayList<String>, Integer, Object>(){

			@Override
			public void onPostExecute(Object result) {
				if(mListener != null){
					mListener.asynOperationEnded();
				}
			}

			@Override
			public void onPreExecute() {
				if(mListener != null){
					mListener.asynOperationStarted();
				}
			}

			@Override
			public void onProgressUpdate(Integer... values) {
				if(mListener != null){
					mListener.imageDownloadProgress(values[0].intValue(), values[1].intValue());
				}
			}

			@Override
			public Object doInBackground(ArrayList<String>... params) {
				ArrayList<String> urls = params[0];
				
				// loop through all images and download url and download them
				for(int i=0; i<urls.size(); i++){

					// check if we have already downloaded an url
					if(urls.get(i)!=null && !mImageCache.containsKey(urls.get(i))){

						InputStream stream = null;
						URL imageUrl;

						try {
							imageUrl = new URL(urls.get(i));
							try {
								stream = imageUrl.openStream();
								final Bitmap bmp = BitmapFactory.decodeStream(stream);
								try {
									mImageCache.put(urls.get(i), bmp);
								} catch (NullPointerException e) {
									Log.e(TAG, "Failed to cache "+urls.get(i));
								}
							} catch (IOException e) {
								Log.w(TAG, "Couldn't load bitmap from url: " + urls.get(i), e);
							} finally {
								try {
									if(stream != null){
										stream.close();
									}
								} catch (IOException e) {}
							}
						} catch (MalformedURLException e) {
							Log.w(TAG, "Wrong url: " + urls.get(i), e);
						}
					} // END: check if we have already downloaded an url

					// passing already downloaded images and total count
					publishProgress((Integer)(i+1), (Integer)urls.size());

				} // for loop end
				return null;
			}

		};
		mTasks.put(tag, userTask);
		userTask.execute(urls);

	}

	/**
	 * Returns UserTask instance initialized with getImages(ArrayList&lt;String&gt; urls)
	 * request
	 * 
	 * @return
	 */
	public final UserTask<ArrayList<String>, Integer, Object> getUserTask() {
		if(mTasks.containsKey(TASK_TAG_DEFAULT)){
			return mTasks.get(TASK_TAG_DEFAULT);
		}
		
		return null;
	}
	
	/**
	 * Returns UserTask instance initialized with getImages(ArrayList&lt;String&gt; urls, String tag)
	 * request
	 * 
	 * @return
	 */
	public final UserTask<ArrayList<String>, Integer, Object> getUserTask(String tag) {
		if(mTasks.containsKey(tag)){
			return mTasks.get(tag);
		}
		
		return null;
	}
}
