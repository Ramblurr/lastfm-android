package fm.last.home;

import fm.last.R;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.view.ViewFactory;

public class ConvertViewFactory implements ViewFactory {

  public View createView(Context context) {
    LinearLayout convertView = new LinearLayout( context );
    
    TextView tv = new TextView( context );
    tv.setGravity( Gravity.CENTER_VERTICAL );
    tv.setPadding( 4, 0, 4, 0 );
    LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, 40 );
    tlp.weight = 10;
    tlp.gravity = Gravity.LEFT;
    tv.setLayoutParams( tlp );
    
    tv.setId( R.id.name );
    convertView.addView( tv );
    
    
    ImageView iv = new ImageView( context );
    iv.setId( android.R.id.icon );
    iv.setImageResource( R.drawable.streaming );
    LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, 40 );
    ilp.gravity = 0;
    ilp.weight = 0;
    tv.setLayoutParams( ilp );
    return convertView;
  }

}
