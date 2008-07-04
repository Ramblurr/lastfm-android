package fm.last.tag;

import fm.last.Animation3d;
import fm.last.Log;
import fm.last.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class TagBrowser extends ListActivity
{
	private TagAdapter m_adapter;
	private Button m_backButton;
	private LinearLayout m_topPlaceholder;
	private LinearLayout m_bottomPlaceholder;
	private TextView m_currentTag;
	
	private String m_targetTag = null;

	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		setContentView( R.layout.tag_browser );
		
		m_topPlaceholder = (LinearLayout)findViewById( R.id.top_placeholder );
		m_bottomPlaceholder = (LinearLayout)findViewById( R.id.bottom_placeholder );
		m_currentTag = (TextView)findViewById( R.id.current_tag );
		m_backButton = (Button)findViewById( R.id.back_button );
		
		m_backButton.setOnClickListener( new Button.OnClickListener()
		{

			public void onClick( View view )
			{
				m_topPlaceholder.startAnimation( AnimationUtils.loadAnimation( TagBrowser.this, android.R.anim.slide_out_top ) );
				m_topPlaceholder.setVisibility( View.GONE );
				m_bottomPlaceholder.startAnimation( AnimationUtils.loadAnimation( TagBrowser.this, android.R.anim.slide_out_bottom ) );
				m_bottomPlaceholder.setVisibility( View.GONE );
			}
			
		});
		
		m_adapter = new TagAdapter( this );
		
		//the listView header /must/ be set before the listAdapter is set
		if( getIntent().hasExtra( "tag" ) )
		{
			m_targetTag = getIntent().getStringExtra( "tag" );
			TextView tv = new TextView( this );
			tv.setGravity( Gravity.CENTER_VERTICAL );
			tv.setPadding( 4, 0, 4, 0 );
			tv.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.FILL_PARENT, 40 ) );
			tv.setText( m_targetTag + " Similar Tags" );
			getListView().addHeaderView( tv );
		}
		setListAdapter( m_adapter );

		if( m_targetTag != null )
		{
			m_adapter.getSimilarTags( m_targetTag );
		}
		else
		{
			m_adapter.getTopTags();
		}
		getListView().setAlwaysDrawnWithCacheEnabled( false );

	}
	
	public void onStart()
	{
		super.onStart();
		
		AnimationSet animSet = new AnimationSet( true );
		
		Animation3d anim = new Animation3d( getListView().getWidth(), getListView().getHeight(), false );
		animSet.addAnimation( anim );
	    anim.setDuration( 1000 );
        anim.setFillAfter( true );
		animSet.setInterpolator( new AccelerateInterpolator() );
		AlphaAnimation alphaAnim = new AlphaAnimation( 0.0f, 1.0f );
		animSet.addAnimation( alphaAnim );

		getListView().startAnimation( animSet );
	}
	
	private void showRadioToolbars()
	{
		if( m_topPlaceholder.getVisibility() == View.VISIBLE )
			return;
		
		m_topPlaceholder.startAnimation( AnimationUtils.loadAnimation( this, android.R.anim.slide_in_top ) );
		m_topPlaceholder.setVisibility( View.VISIBLE );
		m_bottomPlaceholder.startAnimation( AnimationUtils.loadAnimation( this, android.R.anim.slide_in_bottom ) );
		m_bottomPlaceholder.setVisibility( View.VISIBLE );
	}
	
	private void hideRadioToolbars()
	{
		if( m_topPlaceholder.getVisibility() == View.GONE )
			return;
		m_topPlaceholder.startAnimation( AnimationUtils.loadAnimation( this, android.R.anim.slide_in_top ) );
		m_topPlaceholder.setVisibility( View.VISIBLE );
		m_bottomPlaceholder.startAnimation( AnimationUtils.loadAnimation( this, android.R.anim.slide_in_bottom ) );
		m_bottomPlaceholder.setVisibility( View.VISIBLE );
	}
	
	protected void onListItemClick( ListView l, View v, int position, long id )
	{
		final String selectedTag = (String)m_adapter.getItem( position );
	
		AnimationSet animSet = new AnimationSet(true);
		
		Animation3d anim = new Animation3d( getListView().getWidth(), getListView().getHeight(), true );
			
	    anim.setDuration(1000);
        anim.setFillAfter(true);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setAnimationListener( new AnimationListener(){

			public void onAnimationEnd()
			{
				Intent intent = new Intent( "TAGRADIO" );
				intent.putExtra( "tag", selectedTag );
				startActivity( intent );
			}

			public void onAnimationRepeat()
			{
				// TODO Auto-generated method stub
				
			}

			public void onAnimationStart()
			{
				// TODO Auto-generated method stub
				
			}
		} );
        animSet.addAnimation( anim );
        AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
        alphaAnim.setDuration( 1000 );
        
        animSet.addAnimation( alphaAnim );
		getListView().startAnimation( animSet );
	}
	
}
