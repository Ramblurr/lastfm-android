/* IsoDate utility class for kXML-RPC
 *
 * This class replaces org.kobjects.isodate.IsoDate that as previously used by 
 * kxmlrpc for encoding and decoding Date to/from ISO8601 format. Previously it 
 * incorrectly coded to full ISO8601 format, when the XML-RPC specification uses 
 * a ISO8601-style format (which is NOT ISO8601 compliant).
 *
 * Copyright (C) 2007 David Johnson ( djohnsonhk@users.sourceforge.net )
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA			   
 */

package org.kxmlrpc.util;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;


public class IsoDate {
    
     /*
      * Convert a Date object to an XML-RPC compliant string format
      */
     public static String dateToString (Date date) {        
        Calendar c = Calendar.getInstance ();
	c.setTime (date);
        String str = "";
        int year = c.get (Calendar.YEAR);
        str = str + year;
        int month = c.get (Calendar.MONTH) + 1;
        if (month<10) str = str + '0';
        str = str + month;
        int day = c.get (Calendar.DAY_OF_MONTH);
        if (day<10) str = str + '0';
        str = str + day + 'T';
        int hour = c.get (Calendar.HOUR_OF_DAY);
        if (hour<10) str = str + '0';
        str = str + hour + ':';
        int min = c.get (Calendar.MINUTE);
        if (min<10) str = str + '0';
        str = str + min + ':';
        int sec = c.get (Calendar.SECOND);     
        if (sec<10) str = str + '0';
        str = str + sec;
        return str;
     }
     
     /*
      * Convert a XML-RPC compliant string format to Date object
      * Needs to throw some sort of exception if not correctly formed?
      */
     public static Date stringToDate(String text) {
         Calendar c = Calendar.getInstance();
         c.set(Calendar.YEAR, Integer.parseInt(text.substring(0, 4)));
         c.set(Calendar.MONTH, Integer.parseInt(text.substring(4, 6)) - 1);
         c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(text.substring(6, 8)));
         c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(text.substring(9, 11)));
         c.set(Calendar.MINUTE, Integer.parseInt(text.substring(12, 14)));
         c.set(Calendar.SECOND, Integer.parseInt(text.substring(15)));  
         c.set(Calendar.MILLISECOND, 0);
         return c.getTime();
     }
    
}
