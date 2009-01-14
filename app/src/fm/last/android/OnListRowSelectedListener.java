package fm.last.android;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class OnListRowSelectedListener implements AdapterView.OnItemSelectedListener {
    protected View previousSelectedView;
    protected ListView mListView;
    private Drawable mDisclosureDrawable;
    protected int mHighlightResource = R.drawable.list_item_focus_fullwidth;
    protected int mRestResource = R.drawable.list_item_rest_fullwidth;
    
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
    
	public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
		if(previousSelectedView != null) {
			if(previousSelectedView.getTag() == "bottom")
				previousSelectedView.setBackgroundResource(R.drawable.list_item_rest_rounded_bottom);
			else
				previousSelectedView.setBackgroundResource(mRestResource);
			((ImageView)previousSelectedView.findViewById(R.id.row_disclosure_icon)).setImageDrawable(mDisclosureDrawable);
			((TextView)previousSelectedView.findViewById(R.id.row_label)).setTextColor(0xFF000000);
		}
		if(position >= 0 && mListView.getAdapter().isEnabled(position) && view != null && view.findViewById(R.id.row_disclosure_icon) != null) {
			if(view.getTag() == "bottom")
				view.setBackgroundResource(R.drawable.list_item_focus_rounded_bottom);
			else
				view.setBackgroundResource(mHighlightResource);
			mDisclosureDrawable = ((ImageView)view.findViewById(R.id.row_disclosure_icon)).getDrawable();
			((ImageView)view.findViewById(R.id.row_disclosure_icon)).setImageResource(R.drawable.list_radio_icon_focus);
			((TextView)view.findViewById(R.id.row_label)).setTextColor(0xFFFFFFFF);
			previousSelectedView = view;
		}
		else
		{
			//Reached top of list - flick focus to header
			//This is a bit hacky as it's a) presuming there is a header
			//						 and  b) presuming the header is at child index 0
			View focusView = mListView.getChildAt( 0 );
			if( focusView != null )
			{
				focusView.requestFocus();
			}
		}
		
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		if(previousSelectedView != null) {
			if(previousSelectedView.getTag() == "bottom")
				previousSelectedView.setBackgroundResource(R.drawable.list_item_rest_rounded_bottom);
			else
				previousSelectedView.setBackgroundResource(R.drawable.list_item_rest);
			((ImageView)previousSelectedView.findViewById(R.id.row_disclosure_icon)).setImageDrawable(mDisclosureDrawable);
			((TextView)previousSelectedView.findViewById(R.id.row_label)).setTextColor(0xFF000000);
		}
		
		previousSelectedView = null;
	}
}
