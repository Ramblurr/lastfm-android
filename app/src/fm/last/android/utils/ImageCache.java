package fm.last.android.utils;

import java.util.Hashtable;
import java.util.Map;

import android.graphics.Bitmap;


/**
 * Class responsible for caching downloaded images
 * and holding references to them.
 * 
 * @author Lukasz Wisniewski
 */
public class ImageCache extends Hashtable<String, Bitmap> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public ImageCache() {
		super();
	}

	public ImageCache(int capacity, float loadFactor) {
		super(capacity, loadFactor);
	}

	public ImageCache(int capacity) {
		super(capacity);
	}

	public ImageCache(Map<? extends String, ? extends Bitmap> map) {
		super(map);
	}

}
