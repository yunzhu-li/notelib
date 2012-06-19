package org.fatlyz.notelib;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class NLCatalogAct extends Activity {

	//Member variables
	private NANetworkUtils mNetUtils = null;
	
	//Data
	private String mDataStr = null;
	private String mLastVisitedCountryID = null;
	
	//Flags
	private byte mCurContinent = 0;
	private int[] mLastVisitedPosCountry = {-1, -1};
	
	private int[] mCurDispatchPos = {-1, -1};
	
	//List
	private NBExpandableListView mELstCatalog;
	
	//Adapters
	private NBExpandableListAdapter mLstCatalogAdapter = null;
	private ArrayList<HashMap<String, Object>> mAListGroups = null;
	private ArrayList<List<Map<String,Object>>> mAListChildLists = null;
	private ArrayList<Map<String,Object>> mAListCurChildListInGroup = null;
	
	//Top bar
	ImageView mBtnSearch;
	
	//Functions ----------------------------------------------------------------------
	@Override
	public void onBackPressed() {
		if(mNetUtils != null){mNetUtils.finalize();}
		super.onBackPressed();
		NLCatalogAct.this.finish();
		overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_catalog);
		
		//Get last visited ID
		mLastVisitedCountryID = getSharedPreferences("NoteLibPref", MODE_PRIVATE).getString("LastVisitedCountryID", "-1");
		
		//Back button
		Button nBtnBack = (Button)findViewById(R.id.NLCatalogAct_btnBack);
		
		nBtnBack.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				onBackPressed();
			}//onClick
		});
		
		// --------------------------- Search bar--------------------------------------------------------
		//Search bar
		final LinearLayout llSearchBar = (LinearLayout) findViewById(R.id.NLMainAct_incSearchbar);
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
		btnSNew.setVisibility(Button.GONE);
		
		//Search button in search bar
		ImageView btnSearch = (ImageView)findViewById(R.id.common_searchbar_btnSearch);
		btnSearch.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(llSearchBar.getVisibility() == LinearLayout.VISIBLE)
				{
					if(txtKeyword.getText().length() != 0){
						if(llSearchBar.getVisibility() == LinearLayout.VISIBLE)
						{
							if(txtKeyword.getText().length() != 0){
								txtKeyword.setEnabled(false);llSearchBar.setVisibility(LinearLayout.GONE);
								//Start NLCatalogAct
								startActivity(new Intent(NLCatalogAct.this, NLNoteListAct.class)
								.putExtra("LoadMode", "Search")
								.putExtra("SearchKeyword", txtKeyword.getText().toString()));			
								overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
							}else{
								Toast.makeText(NLCatalogAct.this, getString(R.string.common_search_nokeywords), Toast.LENGTH_SHORT).show();
							}
						}
					}else{
						Toast.makeText(NLCatalogAct.this, getString(R.string.common_search_nokeywords), Toast.LENGTH_SHORT).show();
					}
				}
			}//onClick
		});
		// --------------------------- Search bar--------------------------------------------------------
		
		//Search button in topbar
		mBtnSearch = (ImageView)findViewById(R.id.NLCatalogAct_btnSearch);
		
		mBtnSearch.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(llSearchBar.getVisibility() == LinearLayout.VISIBLE)
				{
					txtKeyword.setEnabled(false);llSearchBar.setVisibility(LinearLayout.GONE);
				}else{
					llSearchBar.startAnimation(AnimationUtils.loadAnimation(NLCatalogAct.this, R.anim.bar_in));
					txtKeyword.setEnabled(true);llSearchBar.setVisibility(LinearLayout.VISIBLE);
					txtKeyword.setText("");
					txtKeyword.requestFocus();
					txtKeyword.requestFocusFromTouch();
				}
			}//onClick
		});
		

		//List thing
		mELstCatalog = (NBExpandableListView) findViewById(R.id.eLstCatalog);

		mELstCatalog.setChildDivider(getResources().getDrawable(R.color.c_elist_divider));
		mELstCatalog.setGroupIndicator(getResources().getDrawable(R.drawable.s_lst_elist_catalog_group_indicator));
		
		//
	    //Click Listener
		mELstCatalog.setOnChildClickListener(new OnChildClickListener(){
			@SuppressWarnings("unchecked")
			public boolean onChildClick(ExpandableListView arg0, View arg1,
					int arg2, int arg3, long arg4) {
				
				startActivity(
						new Intent(NLCatalogAct.this, NLNoteListAct.class)
						.putExtra("LoadMode", "Country")
						.putExtra("CountryName", (String) ((TextView) arg1.findViewById(R.id.lstCatalogItemCSubtitle1)).getText())
						.putExtra("CountryNameCN", (String) ((TextView) arg1.findViewById(R.id.lstCatalogItemCTitle)).getText())
						.putExtra("CountryNumNotes", (String) ((TextView) arg1.findViewById(R.id.lstCatalogItemCTitledata)).getText())
				);
				overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
				
				//Delete old indicator
				if(mLastVisitedPosCountry[0] >= 0 && mLastVisitedPosCountry[1] >= 0){
					((HashMap<String, Object>) mLstCatalogAdapter.getChild(mLastVisitedPosCountry[0], mLastVisitedPosCountry[1]))
					.put("ItemLastVisitedCountry", null);
				}
				//Set new indicator
				((HashMap<String, Object>) mLstCatalogAdapter.getChild(arg2, arg3)).put("ItemLastVisitedCountry", R.color.c_andorange);
				mLstCatalogAdapter.notifyDataSetChanged();
				
				mLastVisitedPosCountry[0] = arg2; mLastVisitedPosCountry[1] = arg3;
				
				//Save ID
				mLastVisitedCountryID = (String) ((HashMap<String, Object>) mLstCatalogAdapter.getChild(arg2, arg3)).get("ItemID");
				getSharedPreferences("NoteLibPref", MODE_PRIVATE).edit().putString("LastVisitedCountryID", mLastVisitedCountryID).commit();
				
				return true;
			}
		});
		
		//Load the list
		loadCountryList();
	}
	
	
	/* ******************************************************************************************************** */
	/* ******************************************************************************************************** */
	
	
	private void loadCountryList(){
		
		//ProgressDialog
		final NBProgressDlg progDlg = new NBProgressDlg(NLCatalogAct.this, R.string.common_network_connecting, new OnCancelListener(){
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
					//kill the object
					mNetUtils.finalize();
					
					//Animations
					mELstCatalog.startAnimation(AnimationUtils.loadAnimation(NLCatalogAct.this, R.anim.lists_fade_in));
					
					//Dispatch data
					dispatchCountryListData(mDataStr);
					
					//Dismiss progress dialog
					progDlg.dismiss();
					
				}else
				if(msg.what == -1){ //failed
					progDlg.dismiss();
					Toast.makeText(NLCatalogAct.this, R.string.common_network_network_err, Toast.LENGTH_SHORT).show();
					mNetUtils.finalize();
					onBackPressed();
				}
			}//handleMessage
		};
		
		new Thread(){
			public void run(){
					Looper.prepare();

					try {
						mNetUtils = new NANetworkUtils();
						mDataStr = mNetUtils.fetchString("http://fatlyz.com/notelib/mobile/countrylist.php?key=wqU4pvyuq5aSjQqR" + 
											"&clientver=" + URLEncoder.encode(getString(R.string.app_ver), "UTF-8") +
											"&phonemodel=" + URLEncoder.encode(android.os.Build.BRAND + " - " + android.os.Build.MODEL, "UTF-8")
											, new Handler()
									);
						
						if (mDataStr != null){
							thdHandler.sendEmptyMessage(0);
						}else{
							thdHandler.sendEmptyMessage(-1);
						}
						/*
						}else
						if(mMode == 1){
								downloadData(llHandler, "http://fatlyz.com/notelib/mobile/notelist.php?key=gEWQ41lf5z9sLx2m&country=" + URLEncoder.encode(mListInCountryModeCurCountry, "UTF-8"));
						}else
						if(mMode == 2){
								downloadData(llHandler, "http://fatlyz.com/notelib/mobile/notelist_newnotes.php?key=LbpzdRZwotFm2Mnc");
						}else
						if(mMode == 3){
								downloadData(llHandler, "http://fatlyz.com/notelib/mobile/notelist_search.php?key=B3Ho1dT521lsfVg7&keyword=" + URLEncoder.encode(mSearchKeyword, "UTF-8"));
						}
						*/
					} catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
					//Looper.loop();
				}
		}.start();

	}//LoadList
	
	private void dispatchCountryListData(String dataStr)
	{
		//List thing
		mAListGroups = new ArrayList<HashMap<String, Object>>();  
		mAListChildLists = new ArrayList<List<Map<String,Object>>>();
		
		//Add Groups (5 continents)
		HashMap<String, Object> _hMapTmpGrp = new HashMap<String, Object>();
		_hMapTmpGrp.put("ItemIcon", R.drawable.elist_groupindicator_bg);
		_hMapTmpGrp.put("ItemTitle", getString(R.string.catalog_continent_asia));
		_hMapTmpGrp.put("ItemTitleData", " [#]");
		_hMapTmpGrp.put("ItemSubTitle1", getString(R.string.catalog_continent_asia_sub));
		mAListGroups.add(0, _hMapTmpGrp);
		
		_hMapTmpGrp = new HashMap<String, Object>();
		_hMapTmpGrp.put("ItemIcon", R.drawable.elist_groupindicator_bg);
		_hMapTmpGrp.put("ItemTitle", getString(R.string.catalog_continent_europe));
		_hMapTmpGrp.put("ItemTitleData", " [#]");
		_hMapTmpGrp.put("ItemSubTitle1", getString(R.string.catalog_continent_europe_sub));
		mAListGroups.add(1, _hMapTmpGrp);
		
		_hMapTmpGrp = new HashMap<String, Object>();
		_hMapTmpGrp.put("ItemIcon", R.drawable.elist_groupindicator_bg);
		_hMapTmpGrp.put("ItemTitle", getString(R.string.catalog_continent_africa));
		_hMapTmpGrp.put("ItemTitleData", " [#]");
		_hMapTmpGrp.put("ItemSubTitle1", getString(R.string.catalog_continent_africa_sub));
		mAListGroups.add(2, _hMapTmpGrp);		
		
		_hMapTmpGrp = new HashMap<String, Object>();
		_hMapTmpGrp.put("ItemIcon", R.drawable.elist_groupindicator_bg);
		_hMapTmpGrp.put("ItemTitle", getString(R.string.catalog_continent_americas));
		_hMapTmpGrp.put("ItemTitleData", " [#]");
		_hMapTmpGrp.put("ItemSubTitle1", getString(R.string.catalog_continent_americas_sub));
		mAListGroups.add(3, _hMapTmpGrp);
		
		_hMapTmpGrp = new HashMap<String, Object>();
		_hMapTmpGrp.put("ItemIcon", R.drawable.elist_groupindicator_bg);
		_hMapTmpGrp.put("ItemTitle", getString(R.string.catalog_continent_oceania));
		_hMapTmpGrp.put("ItemTitleData", " [#]");
		_hMapTmpGrp.put("ItemSubTitle1", getString(R.string.catalog_continent_oceania_sub));
		mAListGroups.add(4, _hMapTmpGrp);
		
		//Set Adapter
		mLstCatalogAdapter = new NBExpandableListAdapter(
				this,
				mAListGroups,				//Data Source   
	            R.layout.lstitem_catalog_group,	//ListItem Layout XML
	            new String[] {"ItemIcon", "ItemTitle", "ItemTitleData", "ItemSubTitle1"},   		//SubItems
	            new int[] {R.id.lstCatalogItemGIcon, R.id.lstCatalogItemGTitle, R.id.lstCatalogItemGTitledata, R.id.lstCatalogItemGSubtitle1},
	            mAListChildLists,			//Child Data Source   
	            R.layout.lstitem_catalog_country,	//Child ListItem Layout XML
	            new String[] {"ItemID", "ItemIcon", "ItemTitle", "ItemTitleData", "ItemSubTitle1", "ItemLastVisitedCountry"},   		//SubItems
	            new int[] {R.id.lstCatalogItemCID, R.id.lstCatalogItemCIcon, R.id.lstCatalogItemCTitle, R.id.lstCatalogItemCTitledata, R.id.lstCatalogItemCSubtitle1, R.id.lstCatalogItem_imgLastVisitedCountry}
	    );
			

		//Dispatch Lines
		String _curLine = null;
		int _newLnStartPos = 0;
		int _strlen = dataStr.length();
		char [] dataChrArray = dataStr.toCharArray(); 
		
		for(int _ix = 0; _ix < _strlen; _ix++){
			if(dataChrArray[_ix] == '\n'){
				if(_ix > _newLnStartPos + 3){
					_curLine = new String(dataChrArray, _newLnStartPos, _ix - _newLnStartPos);
					dispatchCountryListDataLine(_curLine.toCharArray());
					
				}//if
				_newLnStartPos = _ix + 1;
			}//if '\n'
		}//for
		//Done.
		
		//List thing
		mAListChildLists.add(mAListCurChildListInGroup);
		mLstCatalogAdapter.notifyDataSetChanged();
		mELstCatalog.setAdapter(mLstCatalogAdapter);

	}//dispatchListData
	
	private void dispatchCountryListDataLine(char[] line)
	{
		String _curLine = new String(line);
		String _curField = null;
		int _lineLength = line.length; //Log.d("Line Length:", "" + _lineLength);
		int _newFieldStartPos = 1;
		byte _fieldCounter = 0;
		
		//List thing
		HashMap<String, Object> _hMapTmpChild = null;

			//Init Child list
			if(_curLine.toString().equals("%continent = 1%")){
				mCurContinent = 1; 
				mCurDispatchPos[0] = 0; mCurDispatchPos[1] = 0;
				mAListCurChildListInGroup = new ArrayList<Map<String,Object>>();
				return;
				
			}else if(_curLine.toString().equals("%continent = 2%")){
				mCurContinent = 2;
				mCurDispatchPos[0] = 1; mCurDispatchPos[1] = 0;
				mAListChildLists.add(mAListCurChildListInGroup); 
				mAListCurChildListInGroup = new ArrayList<Map<String,Object>>();
				return;
			}else if(_curLine.toString().equals("%continent = 3%")){
				mCurContinent = 3;
				mCurDispatchPos[0] = 2; mCurDispatchPos[1] = 0;
				mAListChildLists.add(mAListCurChildListInGroup); 
				mAListCurChildListInGroup = new ArrayList<Map<String,Object>>();
				return;
			}else if(_curLine.toString().equals("%continent = 4%")){
				mCurContinent = 4;
				mCurDispatchPos[0] = 3; mCurDispatchPos[1] = 0;
				mAListChildLists.add(mAListCurChildListInGroup); 
				mAListCurChildListInGroup = new ArrayList<Map<String,Object>>();
				return;
			}else if(_curLine.toString().equals("%continent = 5%")){
				mCurContinent = 5;
				mCurDispatchPos[0] = 4; mCurDispatchPos[1] = 0;
				mAListChildLists.add(mAListCurChildListInGroup); 
				mAListCurChildListInGroup = new ArrayList<Map<String,Object>>();
				return;
			}

			//dissociate fields
			for(int _ix = 1; _ix < _lineLength; _ix++){ //_ix = 1 because continents [1,5]
				if(line[_ix] == '!'){
					//Copy field
					_curField = new String(line, _newFieldStartPos, _ix - _newFieldStartPos);
					if(_curField.equals("country")){
						_hMapTmpChild = new HashMap<String, Object>();
						
						_fieldCounter = 0;
					}else
					//Number of notes of continent
					if(_curField.equals("continent_sum")){
						mAListGroups.get(mCurContinent - 1).put("ItemTitleData",
								" [" + new String(line, _ix + 1, _lineLength - _ix - 2) + "]");
					}else
					//Fill data
					if(_hMapTmpChild != null){
						switch(_fieldCounter){
						case 0: //id
							int _tmpCountryID = Integer.parseInt(_curField.toString());
							_hMapTmpChild.put("ItemIcon", NAGetNFlagResId.GetNFlagResId(_tmpCountryID));
							//Set ID
							_hMapTmpChild.put("ItemID", _curField.toString());
							//If lastVisited
							if(_curField.toString().equals(mLastVisitedCountryID.toString())){
								_hMapTmpChild.put("ItemLastVisitedCountry", R.color.c_andorange);
								mLastVisitedPosCountry[0] = mCurDispatchPos[0];
								mLastVisitedPosCountry[1] = mCurDispatchPos[1];
							}
							mCurDispatchPos[1]++;
							_fieldCounter++; break;
						case 1: //name
							_hMapTmpChild.put("ItemSubTitle1", _curField.toString());
							_fieldCounter++; break;
						case 2: //name_cn
							_hMapTmpChild.put("ItemTitle", _curField.toString());
							_fieldCounter++; break;
						case 3: //continent
							_hMapTmpChild.put("ItemTitleData", " [" + _curField.toString() + "]");
							mAListCurChildListInGroup.add(_hMapTmpChild);
							_fieldCounter++; break;
						}//switch

					}//if
					_newFieldStartPos = _ix + 1;
				}//if (new field)
			}//For
	}//dispatchListDataLine

	
}//class

















