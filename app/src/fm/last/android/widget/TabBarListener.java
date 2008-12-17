package fm.last.android.widget;

/**
 * Listener to TabBar events
 * 
 * @author Lukasz Wisniewski
 */
public interface TabBarListener {
	
	/**
	 * Notifies of tab clicked
	 * 
	 * @param text String tab text
	 * @param index ViewFlipper child's index
	 */
	public void tabChanged(String text, int index);
}
