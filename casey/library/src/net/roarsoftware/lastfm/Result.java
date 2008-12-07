package net.roarsoftware.lastfm;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import net.roarsoftware.xml.DomElement;

/**
 * The <code>Result</code> class contains the response sent by the server, i.e.
 * the status (either ok or failed), an error code and message if failed and the
 * xml response sent by the server.
 * 
 * @author Janni Kovacs
 */
public class Result {

	public enum Status {
		OK, FAILED
	}

	final public static int FAILURE = -999; // Used when something irrecoverable
											// goes wrong with parsing
	final public static String NAMESPACE_OPENSEARCH = "http://a9.com/-/spec/opensearch/1.1/"; 
	private Status status;
	private String errorMessage = null;
	private int errorCode = -1;
	private int httpErrorCode = -1;

	private Document resultDocument;
	private XmlPullParser xmlParser;
	private DomElement lfm;

	public Result(Document resultDocument) {
		this.status = Status.OK;
		this.resultDocument = resultDocument;
		lfm = new DomElement(resultDocument.getDocumentElement());
	}

	public Result(XmlPullParser xpp) {
		try {
			xpp.nextTag();
			xpp.require(XmlPullParser.START_TAG, null, "lfm");
			String s = xpp.getAttributeValue(0);
			this.status = "ok".equals(s) ? Status.OK : Status.FAILED;
			if (status == Status.FAILED) {
				xpp.next();
				xpp.require(XmlPullParser.START_TAG, null, "error");
				this.errorCode = Integer.parseInt(xpp.getAttributeValue(0));
				int event = xpp.next();
				if (event == XmlPullParser.TEXT)
					this.errorMessage = xpp.getText();
			} else {
				this.status = Status.OK;
				this.xmlParser = xpp;
			}
		} catch (XmlPullParserException e) {
			this.errorCode = Result.FAILURE;
			this.errorMessage = e.getMessage();
		} catch (IOException e) {
			this.errorCode = Result.FAILURE;
			this.errorMessage = e.getMessage();
		}
	}

	public Result(String errorMessage) {
		this.status = Status.FAILED;
		this.errorMessage = errorMessage;
	}

	static Result createOkResult(Document resultDocument) {
		return new Result(resultDocument);
	}

	static Result createOkResult(XmlPullParser xpp) {
		return new Result(xpp);
	}

	static Result createHttpErrorResult(int httpErrorCode, String errorMessage) {
		Result r = new Result(errorMessage);
		r.httpErrorCode = httpErrorCode;
		return r;
	}

	static Result createRestErrorResult(int errorCode, String errorMessage) {
		Result r = new Result(errorMessage);
		r.errorCode = errorCode;
		return r;
	}

	/**
	 * Returns if the operation was successful. Same as
	 * <code>getStatus() == Status.OK</code>.
	 * 
	 * @return <code>true</code> if the operation was successful
	 */
	public boolean isSuccessful() {
		return status == Status.OK;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public int getHttpErrorCode() {
		return httpErrorCode;
	}

	public Status getStatus() {
		return status;
	}

	public Document getResultDocument() {
		return resultDocument;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public DomElement getContentElement() {
		if (!isSuccessful())
			return null;
		return new DomElement((Element) resultDocument.getDocumentElement()
				.getElementsByTagName("*").item(0));
	}

	public XmlPullParser getParser() {
		return xmlParser;
	}
	public DomElement getLfm() {
		return lfm;
	}

}
