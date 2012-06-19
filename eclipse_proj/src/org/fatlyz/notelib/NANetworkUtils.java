package org.fatlyz.notelib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class NANetworkUtils {

	public HttpURLConnection mCurUrlConn;
	
	//Handler return :
	//-1 Failed
	// 0 Success
	// 1 Progress
	
	public void closeConnection()
	{
		if(mCurUrlConn != null){
			mCurUrlConn.disconnect();
		}
	}
	
	@Override
	protected void finalize(){
		closeConnection();
		Log.i("Notelib-Network", "finalizing..");
		try {super.finalize();
		} catch (Throwable e) {	e.printStackTrace();}
	}

	public String fetchString(String Url, Handler handler){
		
		Log.d("Notelib-Network", "Fetching-URL: " + Url);
		
		try{
			//Set URL
			URL url = null;
			url = new URL(Url);

			//Open Connection
			mCurUrlConn = (HttpURLConnection)url.openConnection();
			
			//Create InputStream
			InputStreamReader inStream = null;
			inStream = new InputStreamReader(mCurUrlConn.getInputStream());

			//Create Buffer
			char[] buf = new char[2048];
			char[] data = new char[256000];

			//Variables
			int _bytesRead = 0;
			int _bytesReadSum = 0;
			
			//Read
			while (true) {;
			
				//Read
				_bytesRead = inStream.read(buf);
				if (_bytesRead == -1) { break; }
				
				//Copy to jpgData
				System.arraycopy(buf, 0, data, _bytesReadSum, _bytesRead);
				
				//Get bytes read Sum
				_bytesReadSum += _bytesRead;
			
			}
			
			//toString
			String str = new String(data); 
			//TODO OutOfMemoryError
			
			//Close Connection  
			mCurUrlConn.disconnect();
			
			//Send Result
			handler.sendEmptyMessage(0);
			
			return str;
		}
		
		catch (IOException e) {
			Log.w("Notelib-Network", "IOException");
			mCurUrlConn.disconnect();
			handler.sendEmptyMessage(-1);
			return null;
		}	
		
				
	}//fetchString
	
	
	public Bitmap fetchBitmap(String Url, Handler handler, String writeFileName){

		Log.d("Notelib-Network:", "Fetching-URL(Bitmap): " + Url);

		try{
		
			//Set URL
			URL url = null;
			url = new URL(Url);
			
			//Open Connection
			mCurUrlConn = (HttpURLConnection)url.openConnection();
			
			//Referrer
			mCurUrlConn.addRequestProperty("Referer", "http://www.ybnotes.com");
			
			//Create InputStream
			InputStream inStream = null;
			inStream = mCurUrlConn.getInputStream();
	
			//Get file length
			int _fileLen = mCurUrlConn.getContentLength();
			//Variables
			int _bytesRead = 0;
			int _bytesReadSum = 0;
			//Block
			byte[] buf = new byte[2048];
			byte[] jpgData = new byte[_fileLen];
			
			while(true){
				//Read data
				_bytesRead = inStream.read(buf);
	
				//If end reached
				if(_bytesRead == -1){break;}
	
				//Copy to jpgData
				System.arraycopy(buf, 0, jpgData, _bytesReadSum, _bytesRead);
				//Get bytes read Sum
				_bytesReadSum += _bytesRead;
				
				//Set Message(Progress) & Send
				Message _msg = new Message();
				_msg.what = -2;
				_msg.arg1 = 1;
				_msg.arg2 = 100 * _bytesReadSum / _fileLen;
				handler.sendMessage(_msg);
				
			}
			
			//Close connection
			mCurUrlConn.disconnect();
			
			//Send Message
			handler.sendEmptyMessage(0);
			
			
			//Write File
			if(writeFileName != null){
				FileOutputStream _jpgOptStream;
				try {
					_jpgOptStream = new FileOutputStream(new File(writeFileName));
					_jpgOptStream.write(jpgData);
					_jpgOptStream.close();
				} catch (FileNotFoundException e) {e.printStackTrace();}
				catch (IOException e) {e.printStackTrace();}
			}
			
			//Decode & return
			try{
				Bitmap bmp = BitmapFactory.decodeByteArray(jpgData, 0, _fileLen);
				return bmp;
			}catch(OutOfMemoryError e){
				return null;
			}
		}
		
		catch (IOException e){
			Log.w("Notelib-Network(Bitmap)", "IOException");
			handler.sendEmptyMessage(-1);
			mCurUrlConn.disconnect();
			return null;
		}

	}//FetchBitmap
	
	
	
}



