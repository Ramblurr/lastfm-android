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
 * Class responsible for downloading images asynchronously in a separate threads
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
	private Hashtable<String, UserTask<String, Integer, Object>> mTasks;

	/**
	 * Default constructor
	 * 
	 * @param imageCache
	 */
	public ImageDownloader(ImageCache imageCache) {
		if (imageCache == null) {
			imageCache = new ImageCache();
		}
		this.mImageCache = imageCache;

		mTasks = new Hashtable<String, UserTask<String, Integer, Object>>();
	}

	public void setListener(ImageDownloaderListener l) {
		this.mListener = l;
	}

	/**
	 * Requests image download
	 * 
	 * @param url
	 */
	public void getImage(String url) {

		UserTask<String, Integer, Object> userTask = new UserTask<String, Integer, Object>() {
			URL imageUrl;

			@Override
			public void onPostExecute(Object result) {
				if (mListener != null && imageUrl != null) {
					mListener.imageDownloaded(imageUrl.toString());
				}
			}

			@Override
			public void onPreExecute() {
			}

			@Override
			public Object doInBackground(String... params) {
				String url = params[0];

				if (url == null || url.trim().length() == 0)
					return null;
				// check if we have already downloaded an url
				if (!mImageCache.containsKey(url)) {

					InputStream stream = null;

					try {
						imageUrl = new URL(url);
						try {
							stream = imageUrl.openStream();
							final Bitmap bmp = BitmapFactory.decodeStream(stream);
							try {
								mImageCache.put(url, bmp);
							} catch (NullPointerException e) {
								Log.e(TAG, "Failed to cache " + url);
							}
						} catch (IOException e) {
							Log.w(TAG, "Couldn't load bitmap from url: " + url, e);
						} finally {
							try {
								if (stream != null) {
									stream.close();
								}
							} catch (IOException e) {
							}
						}
					} catch (MalformedURLException e) {
						Log.w(TAG, "Wrong url: " + url, e);
					}
				} // END: check if we have already downloaded an url

				return null;
			}

		};
		mTasks.put(url, userTask);
		userTask.execute(url);

	}

	/**
	 * Returns UserTask instance initialized with
	 * getImages(ArrayList&lt;String&gt; urls) request
	 * 
	 * @return
	 */
	public final UserTask<String, Integer, Object> getUserTask() {
		if (mTasks.containsKey(TASK_TAG_DEFAULT)) {
			return mTasks.get(TASK_TAG_DEFAULT);
		}

		return null;
	}

	/**
	 * Returns UserTask instance initialized with
	 * getImages(ArrayList&lt;String&gt; urls, String tag) request
	 * 
	 * @return
	 */
	public final UserTask<String, Integer, Object> getUserTask(String tag) {
		if (mTasks.containsKey(tag)) {
			return mTasks.get(tag);
		}

		return null;
	}
}
