package net.roarsoftware.lastfm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import net.roarsoftware.xml.DomElement;

/**
 * Abstract superclass for all beans containing image data.
 * 
 * @author Janni Kovacs
 */
public abstract class ImageHolder {

	protected Map<ImageSize, String> imageUrls = new HashMap<ImageSize, String>();

	/**
	 * Returns the URL of the image in the specified size, or <code>null</code>
	 * if not available.
	 * 
	 * @param size
	 *            The preferred size
	 * @return an image URL
	 */
	public String getImageURL(ImageSize size) {
		return imageUrls.get(size);
	}

	protected static void loadImages(ImageHolder holder, DomElement element) {
		Collection<DomElement> images = element.getChildren("image");
		for (DomElement image : images) {
			String attribute = image.getAttribute("size");
			ImageSize size;
			if (attribute == null)
				size = ImageSize.MEDIUM; // workaround for image responses
											// without size attr.
			else
				size = ImageSize.valueOf(attribute.toUpperCase(Locale.ENGLISH));
			holder.imageUrls.put(size, image.getText());
		}
	}

	protected static void loadImages(ImageHolder holder, XmlPullParser xpp) {
		try {
			String name = xpp.getName();
			if (!xpp.getName().equals("image"))
				return;
			String attr = null;
			try {
				attr = xpp.getAttributeValue(0);
			} catch (Exception e) {} // in case there is no size attribue
			String url = xpp.nextText();
			ImageSize size;
			if (attr == null)
				size = ImageSize.MEDIUM;
			else
				size = ImageSize.valueOf(attr.toUpperCase(Locale.ENGLISH));
			holder.imageUrls.put(size, url);
		} catch (Exception e) {}
	}
}
