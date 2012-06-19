package org.fatlyz.notelib;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NLNoteListAct extends Activity {
	
	//network
	NANetworkUtils mNetUtils = null;
	
	//Variables
	private String mLoadMode = null;
	private String mSearchKeyword = null;
	
	private String mCountryName = null;
	private String mCountryNameCN = null;
	private String mCountryNumNotes = null;
	private int mDispatchedNotesSum = 0; 	//Counter
	private int mLastVisitedNotePos = -1;
	private String mLastVisitedNoteID = null;
	
	//Data
	private String mDataStr = null;
	
	//Widgets
	private ListView mLstNotes = null;
	
	//List variables
	NBSimpleAdapter mLstNotesAdapter = null;
	List<HashMap<String, Object>> mAListNotes = null;
	
	//Thumbs
	ImageView mBtnLoadThumbs = null;
	private int mBmpCount = 0;
	private boolean mThreadRunning = false;
	private boolean mLoadThumbsOn = false;
	private List<Bitmap> mBmpThumbs = new ArrayList<Bitmap>();
	
	@Override
	public void onBackPressed() {
		//
		if(mAListNotes != null && mLstNotesAdapter != null){
			mAListNotes.clear();
			mLstNotesAdapter.notifyDataSetChanged();
		}
		
		//Recycle thumbs
		mBmpCount = 0;
		for (int _ix = 0; _ix < mBmpThumbs.size(); _ix++){
			try{mBmpThumbs.get(_ix).recycle();}
			catch(NullPointerException e){}
		}
		
		//Close connections
		if (mNetUtils != null){mNetUtils.finalize();}
		
		//Switch activities
		super.onBackPressed();
		NLNoteListAct.this.finish();
		overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_notelist);

		//Get info.
		Bundle _ext = getIntent().getExtras();

		mLoadMode = _ext.getString("LoadMode");
		mSearchKeyword = _ext.getString("SearchKeyword");
		mCountryName = _ext.getString("CountryName");
		mCountryNameCN = _ext.getString("CountryNameCN");
		mCountryNumNotes = _ext.getString("CountryNumNotes");
		
		//Get last visited ID
		mLastVisitedNoteID = getSharedPreferences("NoteLibPref", MODE_PRIVATE).getString("LastVisitedNoteID", "-1");
		
		//Get widgets
		mLstNotes = (ListView) findViewById(R.id.NLNoteListAct_lstNotes);
		
		//Back button
		Button btnBack = (Button)findViewById(R.id.NLNoteListAct_btnBack);
		
		btnBack.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				onBackPressed();
			}//onClick
		});
		
		//
		mLstNotes.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				 	startActivity(new Intent(NLNoteListAct.this, NLNoteInfoAct.class)
				 		//ID
				 		.putExtra("NoteID",(String) ((TextView) arg1.findViewById(R.id.lstNotesItemID)).getText())
				 		//ThumbURL
						.putExtra("NoteThumbURL",(String) mAListNotes.get(arg2).get("ItemThumbURL"))
						);
				 	
					overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
					
					//Clear last
					if(mLastVisitedNotePos != -1){mAListNotes.get(mLastVisitedNotePos).put("ItemLastVisited", R.color.c_transparent);}
					//Set new
					mAListNotes.get(arg2).put("ItemLastVisited", R.color.c_andorange);
					mLastVisitedNotePos = arg2;
					mLstNotesAdapter.notifyDataSetChanged();
					//Save ID
					mLastVisitedNoteID = (String) mAListNotes.get(arg2).get("ItemID");
					getSharedPreferences("NoteLibPref", MODE_PRIVATE).edit().putString("LastVisitedNoteID", mLastVisitedNoteID).commit();
					//Log.d("a", arg3 + "");
			}
		});
		
		//-------------------------------------------------------------------------------------------------
		//LoadThumbs button
		mBtnLoadThumbs = (ImageView)findViewById(R.id.NLNoteListAct_btnLoadThumbs);
		
		mBtnLoadThumbs.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				if(mLoadThumbsOn == true){
					//Set flags
					mLoadThumbsOn = false;					
					//Disconnect
					if(mNetUtils != null){mNetUtils.finalize();}
					//Restore icon
					mBtnLoadThumbs.setImageResource(R.drawable.ic_topbar_pic);
					return;
				}
				mBtnLoadThumbs.setImageResource(R.drawable.ic_topbar_pic_on);
				mLoadThumbsOn = true;
				
				//Handler
				final Handler loadThumbHandler = new Handler(){
					@Override
					public void dispatchMessage(Message msg) {
						super.dispatchMessage(msg);
						/*
						try{
							ImageView _img = ((ImageView) mLstNotes.getChildAt(msg.what - mLstNotes.getFirstVisiblePosition()).findViewById(R.id.lstNotesItemIcon));
							_img.setVisibility(ImageView.INVISIBLE);
							_img.startAnimation(AnimationUtils.loadAnimation(NLNoteListAct.this, R.anim.lists_fade_in));
							_img.setVisibility(ImageView.VISIBLE);
						}catch(Throwable e){}
						*/				
						mLstNotesAdapter.notifyDataSetChanged();
						
					}
				};
				
				if(mThreadRunning == false){
					mLstNotesAdapter.clearPassedThumbItemList();
					mLstNotesAdapter.notifyDataSetChanged();
					//Thread
					new Thread(){
						public void run(){
							Looper.prepare();
							fetchNoteThumbs(loadThumbHandler);
							//Looper.loop();
						}
					}.start();
				}
			}//onClick
		});		
		
		//Load list
		if (mLoadMode.equals("Bookmarks")){
			loadBookmarkList();
		}else{
			loadNoteList();
		}
		
	}//onCreate
	
	private void loadBookmarkList(){
		
		//SharedPreferences
		SharedPreferences _sp = getSharedPreferences("NoteLibBookmarks", MODE_PRIVATE);
		
		//All data
		HashMap<String, ?> spMap = new HashMap<String, Object>(); 
		spMap = (HashMap<String, ?>) _sp.getAll();
		
		//Data string to generate
		String _tmpData = new String();
		_tmpData = "#BEGIN#";
		
		//Traverse keys
		Iterator<?> _iter = spMap.keySet().iterator();
		while(_iter.hasNext())
		{
			String _key = (String) _iter.next();
			if (!_key.contains("_")){
				//Note found
				
				/**
				 * Format:
				 * #BEGIN#
				 * !note!2932!中国1975版2 Yuan纸钞!Pick NL!1975!http://ybnotes.com/spic/d/file/banknotes/asia/China/200911415224239057.jpg!
				 * #END#
				 */
				_tmpData += "\n!note!" + _key + "!"  
						+ spMap.get(_key + "_name") + "!"
						+ spMap.get(_key + "_catanum") + "!"
						+ spMap.get(_key + "_edition") + "!"
						+ spMap.get(_key + "_thumb_url") + "!";
				
			}//if
		}//while
		_tmpData += "\n#END#";
		
		dispatchNoteListData(_tmpData);

	}
	
	
	
	private void loadNoteList()
	{
		
		//ProgressDialog
		final NBProgressDlg progDlg = new NBProgressDlg(NLNoteListAct.this, R.string.common_network_connecting, new OnCancelListener(){
			public void onCancel(DialogInterface arg0) {
				if (mNetUtils != null){mNetUtils.finalize();}
				onBackPressed();
			}
		});
		
		//Handler
		final Handler thdHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.what == 0){ //success
					//kill the object
					mNetUtils.finalize();
					
					//Animations
					mLstNotes.startAnimation(AnimationUtils.loadAnimation(NLNoteListAct.this, R.anim.lists_fade_in));
					
					//Dispatch data
					dispatchNoteListData(mDataStr);
					
					//Dismiss progress dialog
					progDlg.dismiss();
					
				}else
				if(msg.what == -1){ //failed
					progDlg.dismiss();
					Toast.makeText(NLNoteListAct.this, R.string.common_network_network_err, Toast.LENGTH_SHORT).show();
					mNetUtils.finalize();
					onBackPressed();
				}
			}//handleMessage()
		};//Handler
		
		//Thread
		new Thread(){
			public void run(){
					Looper.prepare();
					try {
						mNetUtils = new NANetworkUtils();
						if(mLoadMode.equals("Country")){
							mDataStr = mNetUtils.fetchString("http://fatlyz.com/notelib/mobile/notelist.php?key=gEWQ41lf5z9sLx2m&country="
									+ URLEncoder.encode(mCountryName, "UTF-8")
									, new Handler());
							
						} else if(mLoadMode.equals("New")){ 
							mDataStr = mNetUtils.fetchString("http://fatlyz.com/notelib/mobile/notelist_newnotes.php?key=LbpzdRZwotFm2Mnc"
									, new Handler());
									
						} else if(mLoadMode.equals("Search")){ 
							mDataStr = mNetUtils.fetchString("http://fatlyz.com/notelib/mobile/notelist_search.php?key=B3Ho1dT521lsfVg7" +
									"&keyword=" + URLEncoder.encode(mSearchKeyword, "UTF-8") + 
									"&clientver=" + URLEncoder.encode(getString(R.string.app_ver), "UTF-8") +
									"&phonemodel=" + URLEncoder.encode(android.os.Build.BRAND + " - " + android.os.Build.MODEL, "UTF-8")
									, new Handler());
						}
						if (mDataStr != null){
							thdHandler.sendEmptyMessage(0);
						}else{
							thdHandler.sendEmptyMessage(-1);
						}
					}catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
			}//run()
		}.start(); //Thread
				
		
	}//loadNoteList
	
	private void dispatchNoteListData(String dataStr){
		
		//Create List
		mAListNotes = new ArrayList<HashMap<String, Object>>();
		
		//Create Adapter
		mLstNotesAdapter = new NBSimpleAdapter(
				this,
				mAListNotes,				//Data Source   
	            R.layout.lstitem_note,		//ListItem Layout XML
	            new String[] {				//SubItems
						"ItemID",
						"ItemIcon",
						"ItemTitle",
						"ItemPickNum",
						"ItemEdition",
						"ItemLastVisited",
						"ItemThumbURL"
						},
	            new int[] {
						R.id.lstNotesItemID,
						R.id.lstNotesItemIcon,
						R.id.lstNotesItemTitle,
						R.id.lstNotesItemSubtitle1,
						R.id.lstNotesItemSubtitle2,
						R.id.lstNotesItem_imgLastVisited, 
						0
						}
	    );
		//Set ViewBinder
		mLstNotesAdapter.setViewBinder(null);

		//Dispatch Lines
		String _curLine = null;
		int _newLnStartPos = 0;
		int _strlen = dataStr.length();
		char [] dataChrArray = dataStr.toCharArray(); 
		
		for(int _ix = 0; _ix < _strlen; _ix++){
			if(dataChrArray[_ix] == '\n'){
				if(_ix > _newLnStartPos + 3){
					_curLine = new String(dataChrArray, _newLnStartPos, _ix - _newLnStartPos);
					dispatchNoteListDataLine(_curLine.toCharArray());
					
				}//if
				_newLnStartPos = _ix + 1;
			}//if '\n'
		}//for
		//Done.
		
		//Set adapter
		mLstNotes.setAdapter(mLstNotesAdapter);
		
		//Add "header"
		TextView txtTitle = (TextView) findViewById(R.id.NLNoteListAct_txtTitle);
		if (mLoadMode.equals("Country")){
			txtTitle.setText(mCountryNameCN + mCountryNumNotes);
			
		} else if (mLoadMode.equals("New")){
			txtTitle.setText(getString(R.string.notelist_newnotes) + " [" + mDispatchedNotesSum + "]");
			
		} else if (mLoadMode.equals("Bookmarks")){
			txtTitle.setText(getString(R.string.bookmarks_title) + " [" + mDispatchedNotesSum + "]");
			if(mDispatchedNotesSum == 0){
				Toast.makeText(NLNoteListAct.this, getString(R.string.bookmarks_nobookmark), Toast.LENGTH_SHORT).show();
			}
		} else if (mLoadMode.equals("Search")){
			txtTitle.setText(getString(R.string.common_search_result) + " [" + mDispatchedNotesSum + "]");
			
			if(mDispatchedNotesSum < 300){
				if(mDispatchedNotesSum == 0){
					Toast.makeText(NLNoteListAct.this, getString(R.string.common_search_noresult), Toast.LENGTH_LONG).show();
					onBackPressed();
				}else{
				Toast.makeText(NLNoteListAct.this, 
						getString(R.string.common_search_result_0) + mDispatchedNotesSum + getString(R.string.common_search_result_1),
						Toast.LENGTH_SHORT).show();
				}
			}else{
				Toast.makeText(NLNoteListAct.this, getString(R.string.common_search_result_200), Toast.LENGTH_SHORT).show();
			}
		}
		
		//If automatic thumbnails loading is enabled
		if(getSharedPreferences("NoteLibPref", MODE_PRIVATE).getBoolean("AutoLoadThumbs", false) == true){
			mBtnLoadThumbs.performClick();
		}
		
	}//dispatchNoteListData(..);
	
	private void dispatchNoteListDataLine(char[] line){
		
		//Variables
		String _curField = null;
		int _lineLength = line.length; //Log.d("Line Length:", "" + _lineLength);
		int _newFieldStartPos = 1;
		byte _fieldCounter = 0;
		
		HashMap<String, Object> _hMapTmpItem = null;
		
		for(int _ix = 1; _ix < _lineLength; _ix++){ //_ix = 1 because 
			if(line[_ix] == '!'){
				_curField = new String(line, _newFieldStartPos, _ix - _newFieldStartPos);
				//Log.d("DispatchLine(Field):", _curField);
				if(_curField.equals("note")){
					_hMapTmpItem = new HashMap<String, Object>();
					_hMapTmpItem.put("ItemIcon", R.drawable.ic_banknote);
					_fieldCounter = 0;
					//Calc sum
					mDispatchedNotesSum++;
				}else
				if(_hMapTmpItem != null){
					switch(_fieldCounter){
					case 0:
						_hMapTmpItem.put("ItemID", _curField.toString());
						//If lastVisited
						if(_curField.toString().equals(mLastVisitedNoteID.toString())){
							_hMapTmpItem.put("ItemLastVisited", R.color.c_andorange);
							mLastVisitedNotePos = mDispatchedNotesSum - 1;
						}
						_fieldCounter++; break;
					case 1:
						_hMapTmpItem.put("ItemTitle", _curField.toString());
						_fieldCounter++; break;
					case 2:
						_hMapTmpItem.put("ItemPickNum", _curField.toString());
						_fieldCounter++; break;
					case 3:
						_hMapTmpItem.put("ItemEdition", _curField.toString());
						_fieldCounter++; break;
					case 4:
						_hMapTmpItem.put("ItemThumbURL", _curField.toString());
						mAListNotes.add(_hMapTmpItem);
						_fieldCounter++; break;
					}//switch
				}//if
				_newFieldStartPos = _ix + 1;
			}//if (new field)
		}//For
	}//dispatchNoteListDataLine
	
	
	
	private void fetchNoteThumbs(Handler handler)
	{		
		mThreadRunning = true;
		//Open database
		NADBUtils nDBUtils = new NADBUtils(NLNoteListAct.this);
		//Network
		mNetUtils = new NANetworkUtils();
		
		try{
			while(true){
				//if needed
				if (mLoadThumbsOn == false){mThreadRunning = false; nDBUtils.finalize(); return;}
				//thumb need to load
				if (mLstNotesAdapter.mToLoadThumbItemList.size() > 0){
					//Check if already loaded
					if(mAListNotes.get(mLstNotesAdapter.mToLoadThumbItemList.get(0)).get("ItemIcon") instanceof Bitmap)
					{
						//Log.d("Loaded", "Loaded");
						//remove from list
						mLstNotesAdapter.mToLoadThumbItemList.remove(0);
						try {Thread.sleep(250);} catch (InterruptedException e) {e.printStackTrace();}
						continue;
					}
					
					//Check if cached in DB
					byte _tmpImgData[] = nDBUtils.GetBinaryData((String) mAListNotes.get(mLstNotesAdapter.mToLoadThumbItemList.get(0)).get("ItemID"));
					
					if(_tmpImgData != null)
					{
						//Log.d("_tmpImgData.length", _tmpImgData.length + "");
						//Decode data
						mBmpThumbs.add(mBmpCount, BitmapFactory.decodeByteArray(_tmpImgData, 0, _tmpImgData.length));
					}else{
						//Fetch from Internet ---
						
						//get URL
						String tmpURL = (String) mAListNotes.get(mLstNotesAdapter.mToLoadThumbItemList.get(0)).get("ItemThumbURL");
						//Log.d("FetchURL", tmpURL);
						//fetch bitmap**
						mBmpThumbs.add(mBmpCount, mNetUtils.fetchBitmap(tmpURL, new Handler(), null));
						mNetUtils.closeConnection();
						
						//if failed
						if(mBmpThumbs.get(mBmpCount) == null){
							//if failed
							mBmpThumbs.set(mBmpCount, null);
						}else{
							//if successful
							//Save it to DB
							//new OutputStream
							ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
							//Compress image
							mBmpThumbs.get(mBmpCount).compress(Bitmap.CompressFormat.JPEG, 80, byteOutStream);
							//Write DB
							nDBUtils.InsertBinaryData((String) mAListNotes.get(mLstNotesAdapter.mToLoadThumbItemList.get(0)).get("ItemID"), byteOutStream.toByteArray());
						}
					}
					
					//put bitmap into hashmap
					if(mBmpThumbs.get(mBmpCount) == null)
					{
						mAListNotes.get(mLstNotesAdapter.mToLoadThumbItemList.get(0)).put("ItemIcon",R.drawable.ic_banknote);
					}else{
						mAListNotes.get(mLstNotesAdapter.mToLoadThumbItemList.get(0)).put("ItemIcon", mBmpThumbs.get(mBmpCount));
					}
					
					//send message
					handler.sendEmptyMessage(mLstNotesAdapter.mToLoadThumbItemList.get(0));
					//remove from list
					mLstNotesAdapter.mToLoadThumbItemList.remove(0);
					//count
					mBmpCount++;
					//Log.d("FetchURL", "Done");
				}else{
					try {Thread.sleep(250);} catch (InterruptedException e) {e.printStackTrace();}
				}
				//
			}//while
		}
		catch (IndexOutOfBoundsException e){e.printStackTrace();} 
		catch (IllegalStateException e) {e.printStackTrace();}
		catch (Throwable e) {e.printStackTrace();}
		
		//Close database
		try{nDBUtils.finalize();}
		catch (Throwable e) {e.printStackTrace();}
		
		mThreadRunning = false;
		
	}//function

	
} //class


