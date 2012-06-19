package org.fatlyz.notelib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

public class NBSimpleAdapter extends SimpleAdapter {

	//
	public List<Integer> mToLoadThumbItemList = new ArrayList<Integer>();
	private List<Integer> mPassedThumbItemList = new ArrayList<Integer>();
	
	private Context mContext = null;
	private int mScrWidth = 0;
	
	
	
	public NBSimpleAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
		mContext = context;
		mScrWidth = ((Activity) mContext).getWindowManager().getDefaultDisplay().getWidth();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//
		if(!mPassedThumbItemList.contains(position)){
			mPassedThumbItemList.add(position);
			mToLoadThumbItemList.add(position);
			//Log.d("pos", childPosition + "");
		}

		
		return super.getView(position, convertView, parent);
	}

	@Override
	public void setViewBinder(ViewBinder viewBinder) {
		ViewBinder viewBinder2 = new ViewBinder(){
	        /**
	         * Binds the specified data to the specified view.
	         *
	         * When binding is handled by this ViewBinder, this method must return true.
	         * If this method returns false, SimpleAdapter will attempts to handle
	         * the binding on its own.
	         *
	         * @param view the view to bind the data to
	         * @param data the data to bind to the view
	         * @param textRepresentation a safe String representation of the supplied data:
	         *        it is either the result of data.toString() or an empty String but it
	         *        is never null
	         *
	         * @return true if the data was bound to the view, false otherwise
	         */
			public boolean setViewValue(View arg0, Object arg1, String arg2) {
				if(arg0 instanceof ImageView) {
					
                    if (arg1 instanceof Integer) {
                        ((ImageView)arg0).setImageResource((Integer) arg1);
                        return true;
                    } else if (arg1 instanceof Bitmap) {
                    	((ImageView)arg0).setImageBitmap((Bitmap) arg1);
                    	
                    	if(mScrWidth < 400){
                    		//resize
                    		LayoutParams _tmpLP = arg0.getLayoutParams();
                    		_tmpLP.height = (int) (mScrWidth / 6);
                    		_tmpLP.width = (int) (mScrWidth / 2.3);
                    		arg0.setLayoutParams(_tmpLP);
                    	}
                		
		                return true;
					} else {
						((ImageView)arg0).setImageResource(R.color.c_transparent);
						return true;
					}
		            
				}else{
					return false;
				}
			}
		};

		super.setViewBinder(viewBinder2);
	}

	public void clearPassedThumbItemList(){
		mPassedThumbItemList.clear();
		mToLoadThumbItemList.clear();
	}
	
}










