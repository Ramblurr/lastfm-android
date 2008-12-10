package fm.last.api;

import java.io.Serializable;

/**
 * Represents a Tag
 *
 * @author Casey Link
 */
public class Tag implements Serializable {
	private String name;
	private int tagcount;
	private String url;
	
	public Tag( String name, int tagcount, String url )
	{
		this.name = name;
		this.tagcount = tagcount;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public int getTagcount() {
		return tagcount;
	}

	public String getUrl() {
		return url;
	}

}
