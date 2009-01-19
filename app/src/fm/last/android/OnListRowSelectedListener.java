package fm.last.android;

import fm.last.android.adapter.LastFMStreamAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class OnListRowSelectedListener implements AdapterView.OnItemSelectedListener {
    protected View mPreviousSelectedView;
    protected ListView mListView;
    protected int mPreviousPosition;
    protected int mHighlightResource = R.drawable.list_item_focus_fullwidth;
    protected int mRestResource = R.drawable.list_item_rest_fullwidth;
    protected int mHighlightIconResource = R.drawable.list_radio_icon_focus;
    protected int mRestIconResource = R.drawable.list_radio_icon_rest;
    
    public OnListRowSelectedListener(ListView listView) {
    	mListView = listView;

    	mListView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
	    	public void onFocusChange(View v, boolean hasFocus) {
	    		if(v == mListView) {
	    			if(hasFocus)
	    			{
	    				mListView.getOnItemSelectedListener().onItemSelected(mListView, mListView.getSelectedView(), mListView.getSelectedItemPosition(), mListView.getSelectedItemId());
	    			}
	    			else
	    			{
	    				mListView.getOnItemSelectedListener().onNothingSelected(null);
	    			}
	    		}
	    	}
    	});
    }
    
    public void setResources(int rest, int highlight) {
    	mRestResource = rest;
    	mHighlightResource = highlight;
    }

    private void highlight(View view, int position)
    {
		int iconResource = mHighlightIconResource;
		Object o = mListView.getAdapter().getItem(position);
		if (o instanceof LastFMStreamAdapter.Stream) {
			iconResource = ((LastFMStreamAdapter.Stream) o).radioIcon( true );
		}
		
		if(view.getTag() == "bottom")
			view.setBackgroundResource(R.drawable.list_item_focus_rounded_bottom);
		else
			view.setBackgroundResource(mHighlightResource);
		
		((ImageView)view.findViewById(R.id.row_disclosure_icon)).setImageResource( iconResource );
		((TextView)view.findViewById(R.id.row_label)).setTextColor(0xFFFFFFFF);
		
    }

    private void unhighlight(View view, int position)
    {
		int iconResource = mRestIconResource;
		Object o = mListView.getAdapter().getItem(position);
		if (o instanceof LastFMStreamAdapter.Stream) {
			iconResource = ((LastFMStreamAdapter.Stream) o).radioIcon( false );
		}
		
		if (view.getTag() == "bottom")
			view.setBackgroundResource(R.drawable.list_item_rest_rounded_bottom);
		else if (view.getTag() != "header")
			view.setBackgroundResource(mRestResource);
		
		if (view.findViewById(R.id.row_disclosure_icon) != null)
			((ImageView)view.findViewById(R.id.row_disclosure_icon)).setImageResource( iconResource );
		
		if (view.findViewById(R.id.row_label) != null)
			((TextView)view.findViewById(R.id.row_label)).setTextColor(0xFF000000);
    }
    
    
    
    public void setIconResources( int rest, int highlight ) {
    	mRestIconResource = rest;
    	mHighlightIconResource = highlight;
    }
            
	public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) 
	{
		if(mPreviousSelectedView != null) {
			if (mPreviousSelectedView.getTag() == "header")
				mPreviousSelectedView.getOnFocusChangeListener().onFocusChange( mPreviousSelectedView , false);
			else
				unhighlight(mPreviousSelectedView, mPreviousPosition);
		}

		if (view != null) {
			if (view.getTag() == "header") {
				view.requestFocus(); 
			}
			else if (mListView.getAdapter().isEnabled(position) && 
				view != null && 
				view.findViewById(R.id.row_disclosure_icon) != null)
			{
				highlight(view, position);
			}
		}
		
		mPreviousSelectedView = view;
		mPreviousPosition = position;
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		if(mPreviousSelectedView != null) {
			unhighlight(mPreviousSelectedView, mPreviousPosition);			
		}
		mPreviousSelectedView = null;
	}
}
