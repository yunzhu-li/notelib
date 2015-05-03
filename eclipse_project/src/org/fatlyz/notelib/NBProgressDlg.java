//
//  This file is part of NoteLib.
//
//  Copyright (c) 2012-2015 Yunzhu Li.
//
//  NoteLib is free software: you can redistribute it
//  and/or modify it under the terms of the GNU General
//  Public License version 3 as published by the Free
//  Software Foundation.
//
//  NoteLib is distributed in the hope that it will be
//  useful, but WITHOUT ANY WARRANTY; without even the
//  implied warranty of MERCHANTABILITY or FITNESS FOR A
//  PARTICULAR PURPOSE. See the GNU General Public License
//  for more details.
//
//  You should have received a copy of the GNU General Public
//  License along with NoteLib.
//  If not, see http://www.gnu.org/licenses/.
//

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

    public NBProgressDlg(Context context, int textResId, OnCancelListener listener) {
        this(context, context.getString(textResId), listener);
    }

    public NBProgressDlg(Context context, String text, OnCancelListener listener) {
        // ProgressDialog
        progDlg = new ProgressDialog.Builder(context).setOnCancelListener(listener).show();

        // GetWindow
        final Window popWindow = progDlg.getWindow();
        popWindow.setContentView(R.layout.pop_msg);

        // SetIcon Animation
        ((ImageView) popWindow.findViewById(R.id.popmsg_imgIcon)).startAnimation(AnimationUtils.loadAnimation(popWindow.getContext(), R.anim.popmsg_icon));
        // Set text
        ((TextView) popWindow.findViewById(R.id.popmsg_txtMsg)).setText(text);

    }

    public void dismiss() {
        progDlg.dismiss();
    }
}
