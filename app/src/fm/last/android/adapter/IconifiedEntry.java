package fm.last.android.adapter;

/**
 * Elementary class to use with IconifiedListAdapter in order to provide
 * eye-candy ListViews
 * 
 * @author Lukasz Wisniewski
 */
public class IconifiedEntry{
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
	 * Indicates whether an arrow should appear next to the text
	 */
	boolean hasChild = false;
	
	/**
	 * Resource image that will be displayed left to the text
	 */
	int id;
	
	public IconifiedEntry(Object value, int id, String text) {
		this.value = value;
		this.id = id;
		this.text = text;
	}
	
	public IconifiedEntry(Object value, int id, String text, String url) {
		this(value, id, text);
		
		this.url = url;
	}
	
	public IconifiedEntry(Object value, int id, String text, String url, boolean hasChild) {
		this(value, id, text, url);
		
		this.hasChild = hasChild;
	}
	
	public IconifiedEntry(Object value, int id, String text, boolean hasChild) {
		this(value, id, text);
		
		this.hasChild = hasChild;
	}
}
