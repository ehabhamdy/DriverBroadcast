package com.ehab.driverbroadcast.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.ehab.driverbroadcast.R;


public class ActivitySplash extends ActivityBase {

    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {


                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {

                    if (user.isEmailVerified()) {
                        Intent intent = new Intent(ActivitySplash.this, LocationBroadcastActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {
                        // User signed out or No Network Connection
                        Intent intent = new Intent(ActivitySplash.this, ActivityLogin.class);

                        //Removing HomeActivity from the back stack
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }


                }else{
                    Intent intent = new Intent(ActivitySplash.this, ActivityLogin.class);
                    //Removing HomeActivity from the back stack
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        }, SPLASH_TIME_OUT);


    }
}


