package fm.last.home;

public class MenuData {
  private String menuText;
  private String intentName;
  
  public MenuData(String menuText, String intentName) {
    this.menuText = menuText;
    this.intentName = intentName;
  }
  
  public String getMenuText() {
    return menuText;
  }
  
  public String getIntentName() {
    return intentName;
  }

}
