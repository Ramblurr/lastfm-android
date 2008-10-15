package fm.last;

import com.google.android.maps.*;
import android.graphics.*;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;


public class MapActivity extends com.google.android.maps.MapActivity 
{
	MapView map;
	GeoPoint point;
	String venue;
	
	/** currently no support for traditional overlay controllers */
	class Overlay extends com.google.android.maps.Overlay
	{
		@Override
		public void draw( Canvas canvas, MapView map, boolean shadow )
		{
			Point p = new Point();
			Projection projection = map.getProjection();
			projection.toPixels( point, p );
			
			int x = p.x, y = p.y;
			
			Paint paint = new Paint();
			paint.setAntiAlias( true );
			
			Typeface typeface = Typeface.create( paint.getTypeface(), Typeface.BOLD );
			paint.setTypeface( typeface );
			paint.setTextAlign( Paint.Align.CENTER );
			canvas.drawText( venue, x, y - 15, paint );

			RectF oval = new RectF( x - 10, y - 10, x + 10, y + 10 );
			paint.setARGB( 200, 255, 0, 0 );
			canvas.drawOval( oval, paint );
		}
	}
	
	@Override
	protected void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		map = new MapView( this, "myapikey" );
		setContentView(map);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		int latitude = getIntent().getIntExtra( "latitude", 0 );
		int longitude = getIntent().getIntExtra( "longitude", 0 );
				
		venue = getIntent().getStringExtra( "venue" );
		point = new GeoPoint( latitude, longitude );
		
		map.getController().setCenter( point );
		//TODO: zoom in
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}