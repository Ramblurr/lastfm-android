package fm.last.api;

import java.io.Serializable;

/**
 * Represents a biography
 * 
 * @author Lukasz Wisniewski
 */
public class Bio implements Serializable{
	private static final long serialVersionUID = 4621707915046399813L;
	private String published;
	private String summary;
	private String content;
	
	public Bio(String published, String summary, String content) {
		super();
		this.published = published;
		this.summary = summary;
		this.content = content;
	}
	
	public void setPublished(String published) {
		this.published = published;
	}
	public String getPublished() {
		return published;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getSummary() {
		return summary;
	}

	public void setContent(String content) {
		this.content = content;
	}
	public String getContent() {
		return content;
	}
}
