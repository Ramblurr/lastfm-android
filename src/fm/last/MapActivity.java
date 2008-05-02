package fm.last;

import javax.xml.parsers.DocumentBuilder;

import com.google.android.maps.MapView;
import com.google.android.maps.Point;

import android.os.Bundle;


public class MapActivity extends com.google.android.maps.MapActivity 
{
	MapView map = null;
	
	protected void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		setContentView( map = new MapView( this ) );
		
		int latitude = (Integer) getIntent().getExtra( "latitude" );
		int longitude =(Integer) getIntent().getExtra( "longitude" );
				
		map.getController().centerMapTo( new Point( latitude, longitude ), true /*ignored*/ );
		map.getController().zoomTo( 18 );
	}
}