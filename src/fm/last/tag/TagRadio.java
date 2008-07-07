package fm.last.tag;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import fm.last.R;
import fm.last.radio.RadioView;

public class TagRadio extends RadioView 
{
	private String m_tag = "";
	
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		setHeaderRes( R.layout.tag_radio_partial );
		
        Button similarTags = (Button) findViewById( R.id.similartags );
        similarTags.setOnClickListener( new OnClickListener()
        {
        	public void onClick( View v )
        	{
        		Intent i = new Intent( "TAGBROWSER" );
        		i.putExtra( "tag", m_tag );
        		startActivity( i );
        	}
        });
        
		final Bundle extras = getIntent().getExtras();		
		if( extras.containsKey( "tag" ) )
		{
			readTag( extras.getString( "tag" ) );
		}
	}
	
	private void readTag( String tag )
	{
		m_tag = tag;
		
		tuneInTag( tag );
	}

}
