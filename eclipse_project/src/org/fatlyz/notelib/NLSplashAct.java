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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class NLSplashAct extends Activity {

    boolean canSwitch = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.act_splash);

        // App entry point

        // Check if first start
        SharedPreferences sPref = getSharedPreferences("NoteLibPref", MODE_PRIVATE);
        if (sPref.getString("FirstStartedVer", "").equals(getString(R.string.app_ver)) == false) {

            sPref.edit().putString("FirstStartedVer", getString(R.string.app_ver)).commit();

            canSwitch = false;
            // startActivity(new Intent(NLSplashAct.this, NLMainAct.class));
            startActivity(new Intent(NLSplashAct.this, NLWelcomeAct.class));
            overridePendingTransition(R.anim.act_enter_noscale, R.anim.act_exit);
            try {
                NLSplashAct.this.finish();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return;
        }

        // Thread
        final Thread thdSwitch = new Thread() {
            public void run() {
                Looper.prepare();
                try {
                    Thread.sleep(1150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (canSwitch == true) {
                    startActivity(new Intent(NLSplashAct.this, NLMainAct.class));
                    overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
                }
                try {
                    NLSplashAct.this.finish();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                // Looper.loop();
            }
        };
        thdSwitch.start();

        // Image Click
        ImageView imgSplash = (ImageView) findViewById(R.id.NLSplashAct_imgLogo);
        imgSplash.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                canSwitch = false;
                // startActivity(new Intent(NLSplashAct.this, NLMainAct.class));
                startActivity(new Intent(NLSplashAct.this, NLMainAct.class));
                overridePendingTransition(R.anim.act_enter, R.anim.act_exit);
                try {
                    NLSplashAct.this.finish();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        // Image2 Click
        ImageView imgSplash2 = (ImageView) findViewById(R.id.NLSplashAct_imgLogo2);
        imgSplash2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                canSwitch = false;
                // startActivity(new Intent(NLSplashAct.this, NLMainAct.class));
                startActivity(new Intent(NLSplashAct.this, NLWelcomeAct.class));
                overridePendingTransition(R.anim.act_enter_noscale, R.anim.act_exit);
                try {
                    NLSplashAct.this.finish();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        NLSplashAct.this.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onBackPressed();
    }
}
