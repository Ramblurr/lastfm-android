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
	 * @param index ViewFlipper child's index
	 */
	public void tabChanged(int index, int previousIndex);
}
