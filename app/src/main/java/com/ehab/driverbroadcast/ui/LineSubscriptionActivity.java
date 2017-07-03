package com.ehab.driverbroadcast.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.ehab.driverbroadcast.R;
import com.ehab.driverbroadcast.utils.NavigationDrawerUtil;

public class LineSubscriptionActivity extends AppCompatActivity {

    Toolbar mToolbar;
    NavigationDrawerUtil drawerUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_subscription);
        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tv = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        tv.setText("Subscribe to line");

        drawerUtil = new NavigationDrawerUtil();
        //drawerUtil.SetupNavigationDrawer(mToolbar, LineSubscriptionActivity.this ,username, email);
    }
}
