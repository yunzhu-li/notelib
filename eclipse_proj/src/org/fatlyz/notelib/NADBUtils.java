package org.fatlyz.notelib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class NADBUtils {

	//DB
	SQLiteDatabase mDB;
	String TABLE_NAME = "thumbs_data";
	
	
	public NADBUtils(Context context){
		//Open or Create DB
		try{
			mDB = context.openOrCreateDatabase("/sdcard/.NoteLib/.thumbnails.db", Context.MODE_PRIVATE, null);
		}
		catch(Throwable e){
			Log.w("Notlib-Database", "Can't access DB file on sdcard, access DB file on App's data path");
			mDB = context.openOrCreateDatabase(".thumbnails.db", Context.MODE_PRIVATE, null);
		}
		try{
			mDB.execSQL("CREATE TABLE `thumbs_data` (`id` INTEGER PRIMARY KEY, `data` BLOB)");
		}
		catch(SQLException e){}
	}
	
	public void InsertBinaryData(String id, byte[] data){
		ContentValues cv = new ContentValues();
		cv.put("id", id);
		cv.put("data", data);
		try{
			mDB.insertOrThrow("thumbs_data", null, cv);
		}catch(SQLException e){}
	}

	public void InsertString(String id, String data){
		ContentValues cv = new ContentValues();
		cv.put("id", id);
		cv.put("data", data);
		
		try{
			mDB.insertOrThrow("thumbs_data", null, cv);
		}catch(SQLException e){}
	}
	
	public byte[] GetBinaryData(String id){
		try{
			Cursor c = mDB.rawQuery("SELECT `data` FROM `thumbs_data` WHERE `id` = ?", new String[]{id});
			if (c.getCount() != 0)
			{
				c.moveToFirst();
				byte[] data = c.getBlob(0);
				c.close();
				return data;
			}
					
		}catch(SQLException e){}
		return null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		mDB.close();
		super.finalize();
	}
	
	
}







