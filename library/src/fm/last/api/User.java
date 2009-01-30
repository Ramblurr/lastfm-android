// Copyright 2008 Google Inc. All Rights Reserved.
//
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

import java.io.Serializable;

/**
 * @author jennings
 *         Date: Oct 20, 2008
 */
public class User implements Serializable {

  private static final long serialVersionUID = 2047407259337226913L;
  public User(String name, String url, ImageUrl[] images, String country, String age, String gender, String playcount) {
    this.name = name;
    this.url = url;
    this.images = images;
    this.country = country;
    this.age = age;
    this.playcount = playcount;
  }
  public User(String name, String url, ImageUrl[] images, String country, String age, String gender, String playcount, String realname, String joindate)
  {
      this(name, url, images, country, age, gender, playcount);
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

  private String name;
  private String url;
  private ImageUrl[] images;
  private String country;
  private String age;
  private String gender;
  private String playcount;
  private String realname;
  private String joindate;

}
