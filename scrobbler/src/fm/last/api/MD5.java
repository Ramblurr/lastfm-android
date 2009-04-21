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
package fm.last.api;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

/**
 * Knows how to compute md5 hash
 *
 * @author Mike Jennings
 */
public class MD5 {
  private static MD5 singleton;

  public static MD5 getInstance() {
    if (singleton == null) {
      singleton = new MD5();
    }
    return singleton;
  }

  private MessageDigest md5;

  private MD5() {
    try {
      md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      // MD5 is built-in, so this should never happen
      throw new RuntimeException(e);
    }
  }

  /**
   * Given a string value, compute the md5 hash of this string (as a hex value)
   * @param value
   * @return
   */
  public String hash(String value) {
    md5.reset();
    byte[] hashBytes;
    try {
      hashBytes = md5.digest(value.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      // should not happen, UTF-8 is built-in
      throw new RuntimeException(e);
    }
    String hash = toHex(hashBytes);
    
	while( 32 - hash.length() > 0 )
		hash = "0" + hash;
	return hash;
  }

  private static String toHex(byte[] array) {
    BigInteger bi = new BigInteger(1, array);
    return bi.toString(16).toLowerCase();
  }
}
