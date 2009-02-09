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

import java.io.Serializable;

/**
 * Elementary class to use with IconifiedListAdapter in order to provide
 * eye-candy ListViews
 * 
 * @author Lukasz Wisniewski
 */
public class ListEntry implements Serializable{
	private static final long serialVersionUID = -4113826543207097385L;

	/**
	 * Text that will appear in ListView's row
	 */
	String text;
	
	/**
     * The 2nd row of text that will appear in ListView's row
     */
    String text_second;
	
	/**
	 * Value that will be returned by Adapter.getItem
	 */
	Object value;
	
	/**
	 * Url to the external image that will be displayed left
	 * to the text (optional instead of id)
	 */
	String url;
	
	/**
	 * Resource image that will be displayed left to the text
	 */
	int icon_id = -1;
	
	/**
	 * Resource image that will be displayed right to the text
	 */
	int disclosure_id = -1;
	
	public ListEntry(Object value, int icon_id, String text) {
		this.value = value;
		this.icon_id = icon_id;
		this.text = text;
	}
	
	public ListEntry(Object value, int id, String text, String url) {
		this(value, id, text);
		
		this.url = url;
	}

	public ListEntry(Object value, int id, String text, int disclosure_id) {
		this(value, id, text);
		
		this.disclosure_id = disclosure_id;
	}
	
	public ListEntry(Object value, int id, String text, String url, int disclosure_id) {
		this(value, id, text, url);
		
		this.disclosure_id = disclosure_id;
	}
	public ListEntry(Object value, int id, String text, String url, String text_second) {
        this(value, id, text, url);
        this.text_second = text_second;
    }
	public ListEntry(Object value, int id, String text, String url, int disclosure_id, String text_second) {
	    this(value, id, text, url, disclosure_id);
        
        this.text_second = text_second;
    }
}
