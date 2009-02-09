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

/**
 * Math equations
 *  
 * @author Lukasz Wisniewski
 */
public class MathUtils {
	/**
	 * Calculates great-circle distance between two points on earth
	 * using the Haversine formula
	 * 
	 * @param lat1 (in degrees)
	 * @param lon1 (in degrees)
	 * @param lat2 (in degrees)
	 * @param lon2 (in degrees)
	 * @return distance in kilometers
	 */
	public static double distance(double lat1, double lon1, double lat2, double lon2){
		
		double dlat = Math.toRadians(lat2 - lat1);
		double dlon = Math.toRadians(lon2 - lon1);
		
		double a = Math.pow(Math.sin(dlat/2),2) 
			+ Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.pow(Math.sin(dlon/2),2);
		
		double c = 2 * Math.asin( 1 < Math.sqrt(a) ? 1 : Math.sqrt(a));
		
		double d = 6367 * c;
		return d;
	}
}