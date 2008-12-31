package fm.last.android.adapter;

/**
 * Elementary class to use with IconifiedListAdapter in order to provide
 * eye-candy ListViews
 * 
 * @author Lukasz Wisniewski
 */
public class ListEntry{
	/**
	 * Text that will appear in ListView's row
	 */
	String text;
	
	/**
	 * Value that will be returned by Adapter.getItem
	 */
	Object value;
	
	/**
	 * Url to the external image that will be displayed left
	 * to the text (optional instead of id)
	 */
	String url;
	
	/**
	 * Resource image that will be displayed left to the text
	 */
	int icon_id;
	
	/**
	 * Resource image that will be displayed right to the text
	 */
	int disclosure_id = -1;
	
	public ListEntry(Object value, int icon_id, String text) {
		this.value = value;
		this.icon_id = icon_id;
		this.text = text;
	}
	
	public ListEntry(Object value, int id, String text, String url) {
		this(value, id, text);
		
		this.url = url;
	}

	public ListEntry(Object value, int id, String text, int disclosure_id) {
		this(value, id, text);
		
		this.disclosure_id = disclosure_id;
	}
	
	public ListEntry(Object value, int id, String text, String url, int disclosure_id) {
		this(value, id, text, url);
		
		this.disclosure_id = disclosure_id;
	}
}
