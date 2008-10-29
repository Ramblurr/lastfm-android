package fm.last.home;

import java.util.ArrayList;
import java.util.List;

import androidx.list.ListModel;

public class MenuListModel implements ListModel<MenuData> {
  private static List<MenuData> items;
  
  static {
    items = new ArrayList<MenuData>(4);
    items.add(new MenuData("Events", "EVENTSVIEW"));
    items.add(new MenuData("Tags", "TAGBROWSER"));
    items.add(new MenuData("Friend Mapper", "FRIENDSVIEW"));
    items.add(new MenuData("Similar Artists", "SIMILARARTIST"));
  }

  public MenuListModel() {
    
  }
  
  public int getCount() {
    return items.size();
  }

  public MenuData getItem(int position) {
    return items.get(position);
  }
  
}
