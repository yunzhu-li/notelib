package org.fatlyz.notelib;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class NLMainAct extends Activity {
	
	//network
	private NANetworkUtils mNetUtils = null;
	
	//txtFeedback which needed to be modified in another thread.
	private EditText txtFeedback;
	
	//Animation needed
	private ListView lstMain;
	
	//bars
	private LinearLayout llSearchBar;
	private LinearLayout llFeedbackBar;
	
	//
	private boolean mUpdateThdRunning = false;
	
	@Override
	public void onBackPressed() {
		if(llSearchBar.getVisibility() != LinearLayout.GONE){
			llSearchBar.findViewById(R.id.common_searchbar_txtKeyword).setEnabled(false);llSearchBar.setVisibility(LinearLayout.GONE);
			return;
		}
		if(llFeedbackBar.getVisibility() != LinearLayout.GONE){
			llFeedbackBar.findViewById(R.id.common_feedbackbar_txtContact).setEnabled(false);
			llFeedbackBar.findViewById(R.id.common_feedbackbar_txtFeedback).setEnabled(false);
			llFeedbackBar.setVisibility(LinearLayout.GONE);
			return;
		}
		
		super.onBackPressed();
		NLMainAct.this.finish();
		overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
		
		new Thread(){
        	public void run(){
        		Looper.prepare();
        		try {Thread.sleep(500);
				} catch (InterruptedException e) {e.printStackTrace();}
        		Log.i("Notelib-Thread", "Sending KILL Singal");
        		android.os.Process.killProcess(android.os.Process.myPid());	
        	}
		}.start();
		
	}//onBackPressed
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_main);
		
		//Check update
		checkUpdate(false);
		
		//Time prompt
		Time t = new Time(); 
		t.setToNow(); 
		int _hr = t.hour; // 0-23
		if (_hr >= 1 && _hr <= 4){
			Toast.makeText(NLMainAct.this,
					getString(R.string.main_time_prompt_0) + 
					_hr +
					getString(R.string.main_time_prompt_1)
					, Toast.LENGTH_LONG).show();
		}
		
		//Exit button
		Button nBtnExit = (Button)findViewById(R.id.NLMainAct_btnExit);
		nBtnExit.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		//Help button
		ImageView nBtnHelp = (ImageView)findViewById(R.id.NLMainAct_btnHelp);
		nBtnHelp.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				startActivity(new Intent(NLMainAct.this, NLWelcomeAct.class));
				overridePendingTransition(R.anim.act_enter_noscale, R.anim.act_exit);
    			try {NLMainAct.this.finish();} catch (Throwable e) {e.printStackTrace();}
			}
		});
		
		// --------------------------- Search bar--------------------------------------------------------
		//Search bar
		llSearchBar = (LinearLayout) findViewById(R.id.NLMainAct_incSearchbar);
		llSearchBar.setVisibility(LinearLayout.GONE);

		//EditText in search bar
		final EditText txtKeyword = (EditText) findViewById(R.id.common_searchbar_txtKeyword);
		
		//Up button in search bar
		ImageView btnSUp = (ImageView)findViewById(R.id.common_searchbar_btnUp);
		btnSUp.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				txtKeyword.setEnabled(false);llSearchBar.setVisibility(LinearLayout.GONE);
			}//onClick
		});
		
		//New button in search bar
		Button btnSNew = (Button)findViewById(R.id.common_searchbar_btnNew);
		btnSNew.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(llSearchBar.getVisibility() == LinearLayout.VISIBLE)
				{
					txtKeyword.setEnabled(false);llSearchBar.setVisibility(LinearLayout.GONE);
					//Start NLCatalogAct
					startActivity(new Intent(NLMainAct.this, NLNoteListAct.class).putExtra(
							"LoadMode","New"));
					overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
				}
			}//onClick
		});

		//Search button in search bar
		ImageView btnSearch = (ImageView)findViewById(R.id.common_searchbar_btnSearch);
		btnSearch.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(llSearchBar.getVisibility() == LinearLayout.VISIBLE)
				{
					if(txtKeyword.getText().length() != 0){
						txtKeyword.setEnabled(false);llSearchBar.setVisibility(LinearLayout.GONE);
						//Start NLCatalogAct
						startActivity(new Intent(NLMainAct.this, NLNoteListAct.class)
						.putExtra("LoadMode", "Search")
						.putExtra("SearchKeyword", txtKeyword.getText().toString()));			
						overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
					}else{
						Toast.makeText(NLMainAct.this, getString(R.string.common_search_nokeywords), Toast.LENGTH_SHORT).show();
					}
				}
			}//onClick
		});
		// --------------------------- Search bar--------------------------------------------------------
		// --------------------------- Feedback bar--------------------------------------------------------
		//Feedback bar
		llFeedbackBar = (LinearLayout) findViewById(R.id.NLMainAct_incFeedbackBar);
		llFeedbackBar.setVisibility(LinearLayout.GONE);

		//EditText in Feedback bar
		txtFeedback = (EditText) findViewById(R.id.common_feedbackbar_txtFeedback);
		final EditText txtContact = (EditText) findViewById(R.id.common_feedbackbar_txtContact);
		
		//Up button in Feedback bar
		ImageView btnFUp = (ImageView)findViewById(R.id.common_feedbackbar_btnUP);
		btnFUp.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				txtContact.setEnabled(false);txtFeedback.setEnabled(false);llFeedbackBar.setVisibility(LinearLayout.GONE);
			}//onClick
		});

		//Submit button in Feedback bar
		Button btnSubmit = (Button)findViewById(R.id.common_feedbackbar_btnSubmit);
		btnSubmit.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(llFeedbackBar.getVisibility() == LinearLayout.VISIBLE)
				{
					if(txtFeedback.getText().length() != 0){
						txtContact.setEnabled(false);txtFeedback.setEnabled(false);llFeedbackBar.setVisibility(LinearLayout.GONE);
						submitFeedback(txtFeedback.getText().toString(), txtContact.getText().toString());
					}else{
						Toast.makeText(NLMainAct.this, getString(R.string.main_feedback_noinput), Toast.LENGTH_SHORT).show();
					}
				}
			}//onClick
		});
		// --------------------------- Feedback bar--------------------------------------------------------
		
		// Settings button
		
		ImageView btnSettings = (ImageView) findViewById(R.id.NLMainAct_btnSettings);
		btnSettings.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				//Settings
				final SharedPreferences sPref =  getSharedPreferences("NoteLibPref", MODE_PRIVATE);
				final AlertDialog popDialogSettings = new AlertDialog.Builder(NLMainAct.this).show();
				Window popWindow = popDialogSettings.getWindow();
				popWindow.setContentView(R.layout.pop_settings);
				
				CheckBox chkAutoLoadImages = (CheckBox) popWindow.findViewById(R.id.popSettings_chkAutoLoadImages);
				CheckBox chkAutoLoadThumbs = (CheckBox) popWindow.findViewById(R.id.popSettings_chkAutoLoadThumbs);
				//Get Settings
				chkAutoLoadImages.setChecked(sPref.getBoolean("AutoLoadImages", false));
				chkAutoLoadThumbs.setChecked(sPref.getBoolean("AutoLoadThumbs", false));
				//Set listeners
				chkAutoLoadImages.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						sPref.edit().putBoolean("AutoLoadImages", isChecked).commit();
					}
				});
				chkAutoLoadThumbs.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						sPref.edit().putBoolean("AutoLoadThumbs", isChecked).commit();
					}
				});
				
				Button btnOK = (Button)popWindow.findViewById(R.id.popSettings_btnOK);
				btnOK.setOnClickListener(new OnClickListener(){
					public void onClick(View arg0) {
						popDialogSettings.dismiss();
					}//onClick
				});
			}
		});

		
		
		//-------------------------------------------------------------------------------------------------------------
		//List thing
		lstMain = (ListView)findViewById(R.id.lstMain);
		
        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();  
        //Search
        HashMap<String, Object> hMap = new HashMap<String, Object>();  
        hMap.put("ItemIcon", R.drawable.ic_note_search);
        hMap.put("ItemTitle", getString(R.string.main_search));
        hMap.put("ItemSubtitle", getString(R.string.main_searchsub));
        listItem.add(0, hMap);
        //All notes
        hMap = new HashMap<String, Object>(); 
        hMap.put("ItemIcon", R.drawable.ic_catalog);
        hMap.put("ItemTitle", getString(R.string.main_catalog));
        hMap.put("ItemSubtitle", getString(R.string.main_catalogsub));
        listItem.add(1, hMap);
        //bookmarks
        hMap = new HashMap<String, Object>(); 
        hMap.put("ItemIcon", R.drawable.ic_bookmarks);
        hMap.put("ItemTitle", getString(R.string.main_bookmarks));
        hMap.put("ItemSubtitle", getString(R.string.main_bookmarkssub));
        listItem.add(2, hMap);
        //Update
        hMap = new HashMap<String, Object>(); 
        hMap.put("ItemIcon", R.drawable.ic_update);
        hMap.put("ItemTitle", getString(R.string.main_update));
        hMap.put("ItemSubtitle", getString(R.string.main_updatesub) + getString(R.string.app_ver));
        listItem.add(3, hMap);
        //feedback
        hMap = new HashMap<String, Object>(); 
        hMap.put("ItemIcon", R.drawable.ic_feedback);
        hMap.put("ItemTitle", getString(R.string.main_feedback));
        hMap.put("ItemSubtitle", getString(R.string.main_feedbacksub));
        listItem.add(4, hMap);
        //Web site
        hMap = new HashMap<String, Object>(); 
        hMap.put("ItemIcon", R.drawable.ic_ybnotes);
        hMap.put("ItemTitle", getString(R.string.main_ybnotes));
        hMap.put("ItemSubtitle", getString(R.string.main_ybnotessub));
        listItem.add(5, hMap);
        //About
        hMap = new HashMap<String, Object>(); 
        hMap.put("ItemIcon", R.drawable.ic_about);
        hMap.put("ItemTitle", getString(R.string.main_about));
        hMap.put("ItemSubtitle", getString(R.string.main_aboutsub));
        listItem.add(6, hMap);
        
        //Create adapter
        SimpleAdapter listAdapter = new SimpleAdapter(this,listItem,//Data Source
            R.layout.lstitem_main,//ListItem Layout XML
            //SubItems
            new String[] {"ItemIcon", "ItemTitle", "ItemSubtitle"},   
            new int[] {R.id.lstMainItemIcon, R.id.lstMainItemTitle, R.id.lstMainItemSubtitle});
        //Set adapter
        lstMain.setAdapter(listAdapter);
        
        //Click listener ----------------------------------------------------------------------
        lstMain.setOnItemClickListener(new OnItemClickListener()
        {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(arg2 != 0 && arg2 != 3){
					//Hide bars
					txtKeyword.setEnabled(false);llSearchBar.setVisibility(LinearLayout.GONE);
					txtContact.setEnabled(false);txtFeedback.setEnabled(false);llFeedbackBar.setVisibility(LinearLayout.GONE);
				}
				if (arg2 == 0){  //Search
					if(llSearchBar.getVisibility() == LinearLayout.GONE){
						llSearchBar.startAnimation(AnimationUtils.loadAnimation(NLMainAct.this, R.anim.bar_in));
						txtKeyword.setEnabled(true);llSearchBar.setVisibility(LinearLayout.VISIBLE);
						txtContact.setEnabled(false);txtFeedback.setEnabled(false);llFeedbackBar.setVisibility(LinearLayout.GONE);
						txtKeyword.setText("");
						txtKeyword.requestFocus();
						txtKeyword.requestFocusFromTouch();
					}
				}
				if (arg2 == 1){  //Notes Catalog
					startActivity(new Intent(NLMainAct.this, NLCatalogAct.class));
					overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
				}
				if (arg2 == 2){  //Bookmarks
					startActivity(new Intent(NLMainAct.this, NLNoteListAct.class).putExtra("LoadMode", "Bookmarks"));
					overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
				}
				if (arg2 == 3){  //Check update
					checkUpdate(true);
				}
				if (arg2 == 4){  //Feedback
					if(llFeedbackBar.getVisibility() == LinearLayout.GONE){
						txtKeyword.setEnabled(false);llSearchBar.setVisibility(LinearLayout.GONE);
						llFeedbackBar.startAnimation(AnimationUtils.loadAnimation(NLMainAct.this, R.anim.bar_in));
						txtContact.setEnabled(true);txtFeedback.setEnabled(true);llFeedbackBar.setVisibility(LinearLayout.VISIBLE);
						txtFeedback.requestFocus();
						txtFeedback.requestFocusFromTouch();
					}
				}
				if (arg2 == 5){  //Open ybnotes
					Intent _tmpIntent = new Intent();
					_tmpIntent.setAction("android.intent.action.VIEW");
					_tmpIntent.setData(Uri.parse(getString(R.string.main_ybnotesurl)));  
					startActivity(_tmpIntent);
					overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
				}
				if (arg2 == 6){ //About
					final AlertDialog popDialogAbout = new AlertDialog.Builder(NLMainAct.this).show();
					Window popWindow = popDialogAbout.getWindow();
					popWindow.setContentView(R.layout.pop_about);
					((ImageView)popWindow.findViewById(R.id.popAbout_icon)).setImageResource(R.drawable.ic_banknote);
					Button btnDismiss = (Button)popWindow.findViewById(R.id.popAbout_btnDismiss);
					btnDismiss.setOnClickListener(new OnClickListener(){
						public void onClick(View arg0) {
							popDialogAbout.dismiss();
						}//onClick
					});
				}
			}//OnItemClick
        }
        );
        //Click listener ----------------------------------------------------------------------
		
	}//onCreate
	
	
	//Check update---------------------------------------------------------------------------------------------
	private void checkUpdate(boolean bToast){
		
		final boolean bFToast = bToast;
		
		//Set text
		if(bFToast){
			Toast.makeText(NLMainAct.this, R.string.main_update_checking, Toast.LENGTH_SHORT).show();
		}

		if(mUpdateThdRunning == true){return;}
		mUpdateThdRunning = true;
		
		//Create Handler
		final Handler thdHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.obj != null){
					//Get latest version
					String _latestVer = new String((char[])msg.obj);
					//if equal
					if(getString(R.string.app_ver).equals(_latestVer)){
						Log.d("Notelib-Update:", "Up to date");
						
						if(bFToast){
							//Pop msg
							Toast.makeText(NLMainAct.this,
										getString(R.string.main_update_latest) + 
										getString(R.string.main_updatesub) + 
										getString(R.string.app_ver), Toast.LENGTH_SHORT).show();
						}
					}else{
						//Update found
						Log.d("Notelib-Update:", "Update found");
						//Pop Dialog
						final AlertDialog progDlg;
						try{
							progDlg = new ProgressDialog.Builder(NLMainAct.this)
								.show();
						}catch(Throwable e){e.printStackTrace();return;}
						//GetWindow
						final Window popWindow = progDlg.getWindow();
						popWindow.setContentView(R.layout.pop_update);
						//Find buttons
						final Button popDBtn0 = ((Button)popWindow.findViewById(R.id.popDialog_btn0));
						final Button popDBtn1 = ((Button)popWindow.findViewById(R.id.popDialog_btn1));
						
						
						((TextView)popWindow.findViewById(R.id.popDialog_txtMsg)).setText(
								getString(R.string.main_update_needupdate) + _latestVer + getString(R.string.main_update_whetherupdate)
								);
						popDBtn0.setVisibility(0); popDBtn0.setText(R.string.main_update_yes);
						popDBtn1.setVisibility(0); popDBtn1.setText(R.string.main_update_no);
						popDBtn0.setOnClickListener(new OnClickListener(){
							public void onClick(View arg0) {
								Intent _tmpIntent = new Intent();
								_tmpIntent.setAction("android.intent.action.VIEW");
								_tmpIntent.setData(Uri.parse("http://fatlyz.com/notelib/mobile/update.html"));  
								startActivity(_tmpIntent);
								overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
							}
						});
						popDBtn1.setOnClickListener(new OnClickListener(){
							public void onClick(View arg0) {
								progDlg.dismiss();	
							}
						});
					}//if(msg.obj != null) else
				}else{//Failed
					if(bFToast){
						Toast.makeText(NLMainAct.this, R.string.main_update_network_err, Toast.LENGTH_SHORT).show();
					}
				}
				mUpdateThdRunning = false;
				super.handleMessage(msg);
			}//handleMessage
		};
		//Create thread
		new Thread(){
			public void run(){
				Looper.prepare();
				
				Message _msg = new Message();
				
				//Fetch Data
				if (mNetUtils == null){
					mNetUtils = new NANetworkUtils();
				}else{
					mNetUtils.closeConnection();
				}

				//dispatch data
				_msg.obj = dispatchUpdateData(
						mNetUtils.fetchString("http://fatlyz.com/notelib/mobile/curversion.php?key=K06T8aDKpYJl5FEQ", new Handler())
						);
				
				//Close connection
				mNetUtils.finalize();
				
				thdHandler.sendMessage(_msg);
				
			}
		}.start();
	}
	
	private char[] dispatchUpdateData(String dataStr){

		if (dataStr == null){return null;}
		
		char[] buf = dataStr.toCharArray();

		//Find !
		int _exPos = 0;
		for(int _ix = 0; _ix < dataStr.length(); _ix++){
			if(buf[_ix] == '!'){
				if(_exPos == 0){_exPos = _ix + 1;}else{
					return new String(buf, _exPos, _ix - _exPos).toCharArray();
				}
			}
		}
		return null;
	}
	
	
	
	private void submitFeedback(String feedback, String contact){
	
		final String fFeedback = feedback; final String fContact = contact;
		Toast.makeText(NLMainAct.this, getString(R.string.main_feedback_submitting), Toast.LENGTH_SHORT).show();
		
		//Create Handler
		final Handler netHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.arg1 == 0){
					txtFeedback.setText("");
					Toast.makeText(NLMainAct.this, getString(R.string.main_feedback_submitted), Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(NLMainAct.this, getString(R.string.main_feedback_failed), Toast.LENGTH_SHORT).show();
				}
				super.handleMessage(msg);
			}//handleMessage
		};
		//Create thread
		new Thread(){
			public void run(){
				Looper.prepare();
				Message _msg = new Message();
				//Post
				_msg.arg1 = postFeedback(fFeedback, fContact);
				netHandler.sendMessage(_msg);
			}
		}.start();
	
	}
	
	private int postFeedback(String feedback, String contact){
		
		//Set URL
		//Network
		if (mNetUtils == null){
			mNetUtils = new NANetworkUtils();
		}else{
			mNetUtils.closeConnection();
		}
		
		String _r;
		try{
		_r = mNetUtils.fetchString(
				"http://fatlyz.com/notelib/mobile/feedback.php?key=c4C864bk5FxMqHIv" +
				"&feedback=" + URLEncoder.encode(feedback, "UTF-8") +
				"&contact=" + URLEncoder.encode(contact, "UTF-8") +
				"&clientver=" + URLEncoder.encode(getString(R.string.app_ver), "UTF-8") +
				"&phonemodel=" + URLEncoder.encode(android.os.Build.BRAND + " - " + android.os.Build.MODEL, "UTF-8")
				, new Handler()
				);
		mNetUtils.finalize();
		}catch (UnsupportedEncodingException e) {return -1;}
		if (_r == null){return -1;}
		
		//success
		return 0;
	}
}



