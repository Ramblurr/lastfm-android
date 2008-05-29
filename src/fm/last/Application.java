package fm.last;

public class Application extends android.app.Application
{
	static private Application instance;
	
	public void onCreate()
	{
		instance = this;
	}
	
	static Application instance()
	{
		return instance;
	}
}
