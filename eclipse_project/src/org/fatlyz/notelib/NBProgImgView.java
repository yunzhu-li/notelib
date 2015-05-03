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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class NBProgImgView extends ImageView {

    public NBProgImgView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NBProgImgView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NBProgImgView(Context context) {
        super(context);
    }

    float mProgress = 0;

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        // this.setImageDrawable(null);
        Paint _tmpPaint = new Paint();
        _tmpPaint.setStrokeWidth(20);
        // Draw background
        _tmpPaint.setColor(getResources().getColor(R.color.c_nextlightgray));
        canvas.drawLine(0, 0, getWidth(), getHeight(), _tmpPaint);

        // Draw progress
        _tmpPaint.setColor(getResources().getColor(R.color.c_andblue));
        canvas.drawLine(0, 0, getWidth() * (mProgress / 100.0f), 0, _tmpPaint);

    }

    public void SetProgress(int porgress) {
        mProgress = porgress;
        this.invalidate();
    }
}
