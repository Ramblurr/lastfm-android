package fm.last.android.widget;

/**
 * Listener to TagLayout events
 * 
 * @author Lukasz Wisniewski
 */
public interface TagLayoutListener {
	/**
	 * Notifies of click on TagButton inside
	 * TagLayout which equals removing the tag
	 * 
	 * @param tag
	 */
	public void tagRemoved(String tag);
}
