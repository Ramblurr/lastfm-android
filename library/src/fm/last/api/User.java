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

import java.io.Serializable;

/**
 * @author jennings
 *         Date: Oct 20, 2008
 */
public class User implements Serializable {

  private static final long serialVersionUID = 2047407259337226913L;
  public User(String name, String url, ImageUrl[] images, String country, String age, String gender, String playcount, String subscriber) {
    this.name = name;
    this.url = url;
    this.images = images;
    this.country = country;
    this.age = age;
    this.playcount = playcount;
    this.subscriber = subscriber;
  }
  public User(String name, String url, ImageUrl[] images, String country, String age, String gender, String playcount, String realname, String joindate)
  {
      this(name, url, images, country, age, gender, playcount, "");
      this.realname = realname;
      this.joindate = joindate;
  }
  
  public String getName() {
    return name;
  }
  
  public String getRealName() {
      return realname;
    }

  public String getUrl() {
    return url;
  }

  public String getCountry() {
    return country;
  }

  public String getAge() {
    return age;
  }

  public String getGender() {
    return gender;
  }

  public String getPrettyGender() {
	  if (gender == "m") return "male";
	  if (gender == "f") return "female";
	  return null;
  }
  
  public String getPlaycount() {
    return playcount;
  }

  public ImageUrl[] getImages() {
    return images;
  }
  
  public String getJoinDate()
  {
      return joindate;
  }
  
  public String getSubscriber()
  {
	  return subscriber;
  }
  
  private String name;
  private String url;
  private ImageUrl[] images;
  private String country;
  private String age;
  private String gender;
  private String playcount;
  private String realname;
  private String joindate;
  private String subscriber;
}
