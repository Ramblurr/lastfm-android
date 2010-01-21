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
package fm.last.api.impl;

import org.w3c.dom.Node;

import fm.last.api.GeoPoint;
import fm.last.xml.XMLBuilder;

/**
 * @author Lukasz Wisniewski
 */
public class GeoPointBuilder extends XMLBuilder<GeoPoint> {

	@Override
	public GeoPoint build(Node geoPointNode) {
		node = geoPointNode;
		try {
			double latitude = 0, longitude = 0;
			if (getText("geo:lat") != null)
				latitude = Double.parseDouble(getText("geo:lat"));

			if (getText("geo:long") != null)
				longitude = Double.parseDouble(getText("geo:long"));
			return new GeoPoint(latitude, longitude);
		} catch (NumberFormatException e) {
			return new GeoPoint(0, 0);
		}
	}

}
