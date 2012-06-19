package org.fatlyz.notelib;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class NLWelcomeAct extends Activity{

	private ViewPager vpWelcome;
	private ArrayList<View> listViews;
	
	@Override
	public void onBackPressed() {
		Toast.makeText(NLWelcomeAct.this, R.string.welcome_newstaff, Toast.LENGTH_LONG).show();
		startActivity(new Intent(NLWelcomeAct.this, NLMainAct.class));
		overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
		try {NLWelcomeAct.this.finish();} catch (Throwable e) {e.printStackTrace();}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.act_welcome);

		listViews = new ArrayList<View>();
		vpWelcome = (ViewPager) findViewById(R.id.vpWelcome);
		
		//Add images
		ImageView _tmpImgView;
		LinearLayout _tmpLinearLayout;
		
		//--
		//Inflate & Find layout
		_tmpLinearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.act_welcome_view, null);
		//Set image
		_tmpImgView = (ImageView) _tmpLinearLayout.findViewById(R.id.welcome_view_img0);
		_tmpImgView.setImageResource(R.drawable.welcome_0);
		//add
		listViews.add(_tmpLinearLayout);
		_tmpLinearLayout.startAnimation(AnimationUtils.loadAnimation(NLWelcomeAct.this, R.anim.welcome_img_fade_in));
		
		
		//--
		//Inflate & Find layout
		_tmpLinearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.act_welcome_view, null);
		//Set image
		_tmpImgView = (ImageView) _tmpLinearLayout.findViewById(R.id.welcome_view_img0);
		_tmpImgView.setImageResource(R.drawable.welcome_1);
		//add
		listViews.add(_tmpLinearLayout);
		
		//--
		//Inflate & Find layout
		_tmpLinearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.act_welcome_view, null);
		//Set image
		_tmpImgView = (ImageView) _tmpLinearLayout.findViewById(R.id.welcome_view_img0);
		_tmpImgView.setImageResource(R.drawable.welcome_2);
		//add
		listViews.add(_tmpLinearLayout);
		
		//--
		//Inflate & Find layout
		_tmpLinearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.act_welcome_view, null);
		//Set image
		_tmpImgView = (ImageView) _tmpLinearLayout.findViewById(R.id.welcome_view_img0);
		_tmpImgView.setImageResource(R.drawable.welcome_3);
		//add
		listViews.add(_tmpLinearLayout);
		
		//--
		//Inflate & Find layout
		_tmpLinearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.act_welcome_view, null);
		//Set image
		_tmpImgView = (ImageView) _tmpLinearLayout.findViewById(R.id.welcome_view_img0);
		_tmpImgView.setImageResource(R.drawable.welcome_4);
		//add
		listViews.add(_tmpLinearLayout);

		//Click on last Image
		_tmpImgView.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		//Adapter
		PagerAdapter aPagerAdapter = new PagerAdapter() {
			@Override
			public int getCount() {
				return listViews.size();
			}
			
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				((ViewPager) container).removeView(listViews.get(position)); 
			}

			@Override 
	        public Object instantiateItem(View arg0, int arg1) { 
              ((ViewPager) arg0).addView(listViews.get(arg1)); 
              return listViews.get(arg1); 
           }
			
		};
		
		vpWelcome.setAdapter(aPagerAdapter);
		
		
	}//onCreate

	
	
	
	
}
