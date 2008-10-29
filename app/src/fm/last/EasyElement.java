package fm.last;

import org.w3c.dom.Element;

/** @author max@last.fm
 * Makes better code
 * Auto error handling
 * Forces you to parse the xml document as you expect it rather than using recursive searches
 */
public class EasyElement
{
	private Element e; 

	public EasyElement( Element e )
	{
		this.e = e;
	}

	public EasyElement e( String name )
	{
		try 
		{
			return new EasyElement( (Element) e.getElementsByTagName( name ).item( 0 ) );
		}
		catch (Exception e)
		{
			return this;
		}
	}

	public int microDegrees()
	{
		String degrees = value();
		double tmp = Double.valueOf( degrees ) * 1E6;
		return new Double( tmp ).intValue();
	}
	
	public Element e()
	{
		return e;
	}
	
	public String value()
	{
		try
		{
			return e.getFirstChild().getNodeValue();
		}
		catch ( Exception e )
		{
			return "";
		}
	}
}	
