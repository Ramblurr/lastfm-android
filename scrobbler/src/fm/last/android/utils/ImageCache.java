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

import java.util.Hashtable;
import java.util.Map;

import android.graphics.Bitmap;


/**
 * Class responsible for caching downloaded images
 * and holding references to them.
 * 
 * @author Lukasz Wisniewski
 */
public class ImageCache extends Hashtable<String, Bitmap> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public ImageCache() {
		super();
	}

	public ImageCache(int capacity, float loadFactor) {
		super(capacity, loadFactor);
	}

	public ImageCache(int capacity) {
		super(capacity);
	}

	public ImageCache(Map<? extends String, ? extends Bitmap> map) {
		super(map);
	}

}
