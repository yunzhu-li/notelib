package org.fatlyz.notelib;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class NBProgressDlg {
	
	AlertDialog progDlg = null;
	
	public NBProgressDlg(Context context, int textResId, OnCancelListener listener){
		this(context, context.getString(textResId), listener);
	}
	
	public NBProgressDlg(Context context, String text, OnCancelListener listener)
	{
		//ProgressDialog
		progDlg = new ProgressDialog.Builder(context)
		.setOnCancelListener(listener).show();
		
		//GetWindow
		final Window popWindow = progDlg.getWindow();
		popWindow.setContentView(R.layout.pop_msg);
		
		//SetIcon Animation
		((ImageView)popWindow.findViewById(R.id.popmsg_imgIcon)).startAnimation(
				AnimationUtils.loadAnimation(popWindow.getContext(), R.anim.popmsg_icon));
		//Set text
		((TextView)popWindow.findViewById(R.id.popmsg_txtMsg)).setText(text);
		
	}
	

	

	
	public void dismiss(){
		progDlg.dismiss();
	}
	
	
	
}
