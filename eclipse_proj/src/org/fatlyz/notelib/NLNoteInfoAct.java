package org.fatlyz.notelib;

import java.io.File;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NLNoteInfoAct extends Activity {

	//Network
	NANetworkUtils mNetUtils = null;
	private String mImage0URL = null;
	private String mImage1URL = null;	
	
	private String mDataStr = null;	
	
	//Members
	private String mNoteID = null;
	private String mNoteThumbURL = null;
	private int mCurImage = 0;
	private Bitmap mBitmap0 = null;
	private Bitmap mBitmap1 = null;
	
	//Thread
	private Thread mCurThread = null;
	
	//Widgets
	Button btnLoadImages = null;
	ImageView btnBookmark;
	ImageView btnShare;
	ImageView imgImage0;
	ImageView imgImage1;
	
	@Override
	public void onBackPressed() {
		imgImage0.setVisibility(ImageView.INVISIBLE);
		imgImage1.setVisibility(ImageView.INVISIBLE);
		imgImage0.setImageResource(R.color.c_transparent);
		imgImage1.setImageResource(R.color.c_transparent);
		
		//Close Connection  
		if (mNetUtils != null){mNetUtils.finalize();}
		
		if(mBitmap0 != null){mBitmap0.recycle();}
		if(mBitmap1 != null){mBitmap1.recycle();}
		
		super.onBackPressed();
		NLNoteInfoAct.this.finish();
		overridePendingTransition(R.anim.act_enter, R.anim.act_exit);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_noteinfo);
		
		//Get ID
		mNoteID = (String) this.getIntent().getExtras().get("NoteID");
		mNoteThumbURL = (String) this.getIntent().getExtras().get("NoteThumbURL");

		// --------------------------------------------------------------------------------------
		//Back button
		Button nBtnBack = (Button) findViewById(R.id.NLNoteInfoAct_btnBack);
		nBtnBack.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		//Bookmark button
		btnBookmark = (ImageView)findViewById(R.id.NLNoteInfoAct_btnBookmark);
		if(getSharedPreferences("NoteLibBookmarks", MODE_PRIVATE).contains((String) mNoteID) == true){
			btnBookmark.setImageResource(R.drawable.ic_star_soild);
		}
		
		btnBookmark.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if(getSharedPreferences("NoteLibBookmarks", MODE_PRIVATE).contains((String) mNoteID) == false){
					//Add to bookmarks
					Editor _e = getSharedPreferences("NoteLibBookmarks", MODE_PRIVATE).edit();
					_e.putBoolean((String) mNoteID, true);
					_e.putString(
							mNoteID + "_name", 
							((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtNoteName)).getText().toString());
					
					_e.putString(
							mNoteID + "_catanum", 
							((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtCataNum)).getText().toString());
					
					_e.putString(
							mNoteID + "_edition", 
							((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtEdition)).getText().toString());
					
					_e.putString(
							mNoteID + "_thumb_url", 
							mNoteThumbURL);
					
					_e.commit();
					
					btnBookmark.setImageResource(R.drawable.ic_star_soild);
					Toast.makeText(NLNoteInfoAct.this, R.string.bookmarks_added, Toast.LENGTH_SHORT).show();
					
				}else{
					//Remove from bookmarks
					Editor _e = getSharedPreferences("NoteLibBookmarks", MODE_PRIVATE).edit();
					_e.remove((String) mNoteID);
					_e.remove(mNoteID + "_name");
					_e.remove(mNoteID + "_catanum");
					_e.remove(mNoteID + "_edition");
					_e.commit();
					
					btnBookmark.setImageResource(R.drawable.ic_star_hollow);

				}
			}//onClick
		});
		
		//Share button
		btnShare = ((ImageView)findViewById(R.id.NLNoteInfoAct_btnShare));
		btnShare.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				//Check if pictures cached -------------------
				if(!new File("/sdcard/.NoteLib/" + mNoteID + "_0.jpg.n").exists()){
					Toast.makeText(NLNoteInfoAct.this, R.string.noteinfo_share_needimages, Toast.LENGTH_SHORT).show();
					return;
				}
				//Check if pictures cached -------------------
				
				//Read File
				File _tmpImageFile = new File("/sdcard/.NoteLib/" + mNoteID + "_0.jpg.n");
				File _tmpTempFileToSend = new File("/sdcard/.NoteLib/_TmpNoteImg.jpg.n");
				NAFileUtils.CopyFile(_tmpImageFile, _tmpTempFileToSend);
				
				//Create Intent and put data
				Intent _tmpIntent = new Intent();
				_tmpIntent.setAction("android.intent.action.SEND");
				_tmpIntent.putExtra(Intent.EXTRA_TEXT, 
						getString(R.string.noteinfo_share_text) + 
						((TextView)findViewById(R.id.NLNoteInfoAct_txtNoteName)).getText() + 
						getString(R.string.noteinfo_share_text2)
						);
				_tmpIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(_tmpTempFileToSend));
				_tmpIntent.setType("image/*");  
				startActivity(Intent.createChooser(_tmpIntent, getString(R.string.noteinfo_share_chooser)));
				overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
			}
		});
				
		//LoadImages Button
		btnLoadImages = (Button)findViewById(R.id.NLNoteInfoAct_btnLoadImages);
		btnLoadImages.setVisibility(Button.INVISIBLE);
		btnLoadImages.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				LoadNoteImages();
			}//OnClick
		});//SetOnClickListener
		
		//---------------------------------------------------------------------------------------		
		imgImage0 = ((ImageView)findViewById(R.id.NLNoteInfoAct_imgImage0));
		imgImage1 = ((ImageView)findViewById(R.id.NLNoteInfoAct_imgImage1));

		imgImage0.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent _tmpIntent = new Intent();
				_tmpIntent.setAction("android.intent.action.VIEW");
				_tmpIntent.setDataAndType(Uri.parse("file:///sdcard/.NoteLib/" + mNoteID + "_0.jpg.n"), "image/*");  
				startActivity(_tmpIntent);
				overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
			}
		});
		imgImage1.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent _tmpIntent = new Intent();
				_tmpIntent.setAction("android.intent.action.VIEW");
				_tmpIntent.setDataAndType(Uri.parse("file:///sdcard/.NoteLib/" + mNoteID + "_1.jpg.n"), "image/*");  
				startActivity(_tmpIntent);
				overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
			}
		});
		
		//Load
		loadNoteInfo();
		
	}
	
	private void loadNoteInfo()
	{
		//ProgressDialog
		final NBProgressDlg progDlg = new NBProgressDlg(NLNoteInfoAct.this, R.string.common_network_connecting, new OnCancelListener(){
			public void onCancel(DialogInterface arg0) {
				if (mNetUtils != null){mNetUtils.finalize();}
				onBackPressed();
			}
		});

		final Handler thdHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.what == 0){ //success
					//dispatch data
					dispatchNoteInfoData(mDataStr);
					
					//Load image button visiable
					btnLoadImages.setVisibility(Button.VISIBLE);
					//Close popup window
					progDlg.dismiss();
					
					//Check if pictures cached -------------------
					if(new File("/sdcard/.NoteLib/" + mNoteID + "_0.jpg.n").exists()){
						if(new File("/sdcard/.NoteLib/" + mNoteID + "_1.jpg.n").exists()){
							LoadNoteImages();
						}
					}
					//Check if pictures cached -------------------
					//If automatic loading is enabled
					if(getSharedPreferences("NoteLibPref", MODE_PRIVATE).getBoolean("AutoLoadImages", false) == true){
						LoadNoteImages();
					}
					
				}else
				if(msg.what == -1){ //failed
					progDlg.dismiss();
					Toast.makeText(NLNoteInfoAct.this, R.string.common_network_network_err, Toast.LENGTH_SHORT).show();
					onBackPressed();
				}
				
			}//handleMessage
		};
		
		mCurThread = new Thread(){
			public void run(){
				Looper.prepare();
				//
				mNetUtils = new NANetworkUtils();
				//Fetch data string
				mDataStr = mNetUtils.fetchString("http://fatlyz.com/notelib/mobile/noteinfo.php?key=QrB1g6fbhV9n4Oid&id=" + mNoteID
						, new Handler());

				if (mDataStr != null){
					thdHandler.sendEmptyMessage(0);
				}else{
					thdHandler.sendEmptyMessage(-1);
				}
				//Close connections
				if (mNetUtils != null){mNetUtils.finalize();}

			}
		};
		mCurThread.start();
		
	}

	private void dispatchNoteInfoData(String dataStr){

		//Dispatch Lines
		String _curLine = null;
		int _newLnStartPos = 0;
		int _strlen = dataStr.length();
		char [] dataChrArray = dataStr.toCharArray(); 
		
		for(int _ix = 0; _ix < _strlen; _ix++){
			if(dataChrArray[_ix] == '\n'){
				if(_ix > _newLnStartPos + 3){
					_curLine = new String(dataChrArray, _newLnStartPos, _ix - _newLnStartPos);
					dispatchNoteInfoDataLine(_curLine);
					
				}//if
				_newLnStartPos = _ix + 1;
			}//if '\n'
		}//for
		//Done.
		
	}//fetchNoteInfo
	
	
	
	private void dispatchNoteInfoDataLine(String lineStr){
		
		// ----------------------------------
		//Dispatch Line
		int _newFieldStartPos = 1;
		byte _fieldCounter = 0;
		
		char[] _tmpLineCA = lineStr.toCharArray();
		int _lineLength = lineStr.length();
		//dissociate fields
		for(int _ix = 1; _ix < _lineLength; _ix++){ //_ix = 1 because continents [1,5]
			if(_tmpLineCA[_ix] == '!'){
				//Copy field
				String _curField = new String(_tmpLineCA, _newFieldStartPos, _ix - _newFieldStartPos);

				//Switch field
				switch(_fieldCounter){
				case 0:
					break;
				case 1:
					break;
				case 2:
					((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtNoteName)).setText(_curField);
					break;
				case 3:
					((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtCountry)).setText(_curField);
					break;
				case 4:
					((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtCountryCN)).setText(_curField);
					break;
				case 5:	//Catalog Num
					((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtCataNum)).setText(_curField);
					break;
				case 6:	//Denomination
					((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtDenomination)).setText(_curField);
					break;
				case 7:	//edition
					((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtEdition)).setText(_curField);
					break;
				case 8:	//Publish date
					((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtPublishDate)).setText(_curField);
					break;
				case 9://Printer
					((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtPrinter)).setText(_curField);
					break;
				case 10://Specs
					((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtSpecs)).setText(_curField);
					break;
				case 11://Description
					((TextView)NLNoteInfoAct.this.findViewById(R.id.NLNoteInfoAct_txtDescription)).setText(_curField.replace('^', '\n'));
					break;
				case 12://Image0
					mImage0URL = _curField;
					break;
				case 13://Image1
					mImage1URL = _curField;
					break;
				}
				
				_fieldCounter++;
				_newFieldStartPos = _ix + 1;
			}//if !
		}//for
	}
	
	
	private void LoadNoteImages(){
		//Reset
		imgImage0.setVisibility(ImageView.GONE);
		imgImage1.setVisibility(ImageView.GONE);
		mBitmap0 = null;
		mBitmap1 = null;
		
		//Find progress bar
		final NBProgImgView nbImgFetchProgress = (NBProgImgView)findViewById((R.id.nbImgFetchProgress));
		
		//Disable Button
		btnLoadImages.setText(R.string.noteinfo_loadingpicture);
		btnLoadImages.setEnabled(false);
		nbImgFetchProgress.setVisibility(0);
		
		//Create Message Handler
		final Handler thdHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.arg1 == 5){
					
					//Image OK
					//Set height
					ImageView _tmpImgView;
					if (msg.arg2 == 0){//Image0 OK
						_tmpImgView = ((ImageView)findViewById(R.id.NLNoteInfoAct_imgImage0));
						//Set Image
						_tmpImgView.setImageBitmap(mBitmap0);
						
					}else{//Image1 OK
						_tmpImgView = ((ImageView)findViewById(R.id.NLNoteInfoAct_imgImage1));
						_tmpImgView.setImageBitmap(mBitmap1);
						//Hide button
						btnLoadImages.setVisibility(8); //Gone
						//Hide progress bar
						nbImgFetchProgress.setVisibility(8); //Gone
					}
					//resize
					LayoutParams _tmpLP = _tmpImgView.getLayoutParams();
					_tmpLP.height = getWindowManager().getDefaultDisplay().getWidth() / 2 - 8;
					_tmpImgView.setLayoutParams(_tmpLP);

					//Show
					_tmpImgView.startAnimation(AnimationUtils.loadAnimation(NLNoteInfoAct.this, R.anim.noteinfo_img_fade_in));
					_tmpImgView.setVisibility(ImageView.VISIBLE);

				}else
				if(msg.arg1 == 1){
					if(mCurImage == 0){
						//Update Progress0
						nbImgFetchProgress.SetProgress(msg.arg2 / 2);
					}else{
						//Update Progress1
						nbImgFetchProgress.SetProgress(50 + msg.arg2 / 2);
					}
				}else							
				if(msg.arg1 == -1){
					//Failed, Enable Button
					if (mNetUtils != null){mNetUtils.finalize();}
					Toast.makeText(NLNoteInfoAct.this, R.string.noteinfo_loadpicturefailed, Toast.LENGTH_SHORT).show();
					btnLoadImages.setText(R.string.noteinfo_loadpicture);
					btnLoadImages.setEnabled(true);
				}
			}//handleMessage
		};
		//Create thread
		mCurThread = new Thread(){
			public void run(){
				Looper.prepare();
				
				Message _msg;
				
				//File operations
				File _noteImageFile;
				//Make dir
				_noteImageFile = new File("/sdcard/.NoteLib/");
				_noteImageFile.mkdirs();	
				//Create File objects
				
				
				//Image0
				mCurImage = 0;				
				
				_noteImageFile = new File("/sdcard/.NoteLib/" + mNoteID + "_0.jpg.n");
				//File already exists
				if(_noteImageFile.exists()){
					try{mBitmap0 = BitmapFactory.decodeFile(_noteImageFile.getAbsolutePath());}
					catch(OutOfMemoryError e){}
					if(mBitmap0 != null){
						_msg = new Message();
						_msg.arg1 = 5;
						_msg.arg2 = 0;
						thdHandler.sendMessage(_msg);
					}
				}
				
				if(mBitmap0 == null){
					//Fetch Image0
					mNetUtils = new NANetworkUtils();
					mBitmap0 = mNetUtils.fetchBitmap(mImage0URL, thdHandler, _noteImageFile.getAbsolutePath());
					//Send message
					_msg = new Message();
					if (mBitmap0 != null){
						_msg.arg1 = 5;
						_msg.arg2 = 0;
						thdHandler.sendMessage(_msg);
					}else{
						_msg.arg1 = -1;
						thdHandler.sendMessage(_msg);
						return;
					}
				}

				//Image1
				mCurImage = 1;
				_noteImageFile = new File("/sdcard/.NoteLib/" + mNoteID + "_1.jpg.n");
				if(_noteImageFile.exists()){
					try{mBitmap1 = BitmapFactory.decodeFile(_noteImageFile.getAbsolutePath());}
					catch(OutOfMemoryError e){}
					if(mBitmap1 != null){
						mImage1URL = null;
						_msg = new Message();
						_msg.arg1 = 5;
						_msg.arg2 = 1;
						thdHandler.sendMessage(_msg);
					}
				}
				
				if(mBitmap1 == null){
					//Fetch Image1
					mNetUtils = new NANetworkUtils();
					mBitmap1 = mNetUtils.fetchBitmap(mImage1URL, thdHandler, _noteImageFile.getAbsolutePath());
					//Send Message
					_msg = new Message();
					if (mBitmap1 != null){
						_msg.arg1 = 5;
						_msg.arg2 = 1;
						thdHandler.sendMessage(_msg);
					}else{
						_msg.arg1 = -1;
						thdHandler.sendMessage(_msg);
						return;
					}
				}
			
			}//run
		};
		mCurThread.start();
	}
	
	


}//class





