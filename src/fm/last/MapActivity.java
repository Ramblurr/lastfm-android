package fm.last;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayController;
import com.google.android.maps.Point;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;


public class MapActivity extends com.google.android.maps.MapActivity 
{
	MapView map;
	Point point;
	String venue;
	
	/** currently no support for traditional overlay controllers */
	class Overlay extends com.google.android.maps.Overlay
	{
		@Override
		public void draw( Canvas canvas, PixelCalculator calculator, boolean shadow )
		{
			int[] c = new int[2]; //crap API
			calculator.getPointXY( point, c ); //crap API
			int x = c[0], y = c[1]; //crap API
			
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
		setContentView( map = new MapView( this ) );
		
		OverlayController c = map.createOverlayController();
		c.add( new Overlay(), true );
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		int latitude = getIntent().getIntExtra( "latitude", 0 );
		int longitude = getIntent().getIntExtra( "longitude", 0 );
				
		venue = getIntent().getStringExtra( "venue" );
		point = new Point( latitude, longitude );
		
		map.getController().centerMapTo( point, true /*ignored*/ );
		map.getController().zoomTo( 18 );
	}
}