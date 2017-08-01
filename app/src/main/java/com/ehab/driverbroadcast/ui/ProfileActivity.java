package com.ehab.driverbroadcast.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ehab.driverbroadcast.R;
import com.ehab.driverbroadcast.utils.NavigationDrawerUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {
    @BindView(R.id.profile_username_textview)
    TextView usernameTextview;

    @BindView(R.id.profile_email_textview)
    TextView emailTextview;

    @BindView(R.id.line_textview)
    TextView lineTextView;

    @BindView(R.id.back_button)
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        Intent intent = getIntent();

        String username = intent.getStringExtra(NavigationDrawerUtil.USERNAME_EXTRA);
        String email = intent.getStringExtra(NavigationDrawerUtil.EMAIL_EXTRA);
        String line =  intent.getStringExtra(NavigationDrawerUtil.SUB_LINE_EXTRA);

        usernameTextview.setText(username);
        emailTextview.setText(email);
        lineTextView.setText(line);

    }

    public void backButtonPressed(View view){
        this.finish();
    }
}
