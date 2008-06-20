package fm.last;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class Animation3d extends Animation
{
	private Camera m_camera;
	private int m_width, m_height;
	boolean m_reverse = false;
	
	public Animation3d( int width, int height, boolean reverse )
	{
		this( width, height );
		m_reverse = reverse;
	}
	
	public Animation3d( int width, int height )
	{
		m_width = width;
		m_height = height;
	}
	
	public void initialize( int width, int height, int parentWidth, int parentHeight )
	{
		super.initialize( width, height, parentWidth, parentHeight );
		m_camera = new Camera();
	}
	
    protected void applyTransformation(float interpolatedTime, Transformation t) 
    {
        float zPos;
        int center_x = -(m_width / 4);
        int center_y = m_height / 4;
    	        
        if( !m_reverse )
        	interpolatedTime = (1.0f - interpolatedTime);
                
        zPos = -310.0f * interpolatedTime;
        
        final Camera camera = m_camera;

        final Matrix matrix = t.getMatrix();

        camera.save();
        
        camera.translate( center_x * interpolatedTime, center_y * interpolatedTime, zPos);

        camera.getMatrix(matrix);
        camera.restore();
    }
}