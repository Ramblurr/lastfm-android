// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
