package fm.last.android.widget;

import android.view.View;

/**
 * Listener to NavBar widget
 * 
 * @author Lukasz Wisniewski
 */
public interface NavBarListener {
	/**
	 * Notifies of back View clicked
	 * 
	 * @param child View instance
	 */
	void backClicked(View child);
	
	/**
	 * Notifies of forward View clicked
	 * 
	 * @param child View instance
	 */
	void forwardClicked(View child);
	
	/**
	 * Notifies of one of middle View clicked.
	 * Implementation should provide middle view
	 * oriented indexation instead of layout one
	 * 
	 * @param child View instance
	 * @param index (0..n-1) where n is count of middle views
	 */
	void middleClicked(View child, int index);
}
