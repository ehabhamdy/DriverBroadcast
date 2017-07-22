package com.ehab.driverbroadcast;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;

/**
 * Created by ehabhamdy on 7/14/17.
 */

public class RDriverApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        new Instabug.Builder(this, BuildConfig.INSTABUG_APP_TOKEN)
                .setInvocationEvent(InstabugInvocationEvent.FLOATING_BUTTON)
                .build();
    }
}
