package fm.last.artist;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import fm.last.R;
import fm.last.radio.RadioView;

public class ArtistRadio extends RadioView {
	Button m_tuneButton = null;
	EditText m_artistInput = null;
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		setHeaderRes( R.layout.similar_artist_radio_partial );
		m_artistInput = (EditText)findViewById( R.id.artist_input );
		m_tuneButton = (Button)findViewById( R.id.tune );
		m_tuneButton.setOnClickListener( new OnClickListener()
		{

			public void onClick(View v) {
				tuneInSimilarArtists(m_artistInput.getText().toString() );
			}
			
		});
	}
}
