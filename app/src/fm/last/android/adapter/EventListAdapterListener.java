package fm.last.android.adapter;

/**
 * Observer that must be implemented by any class
 * supplying data to the <code>EventListAdapter</code>
 * 
 * @author Lukasz Wisniewski
 */
public interface EventListAdapterListener {
	
	/**
	 * Requests asynchronously for a paginated event results,
	 * implementation must provide data with <code>EventListAdapter.providePage</code> method
	 * 
	 * @param pageNumber
	 */
	void getPaginatedPage(int pageNumber);
	
}
