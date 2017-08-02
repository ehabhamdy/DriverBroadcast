package com.ehab.driverbroadcast.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ehab.driverbroadcast.R;
import com.ehab.driverbroadcast.utils.NavigationDrawerUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LineSubscriptionActivity extends AppCompatActivity {

    Toolbar mToolbar;
    NavigationDrawerUtil drawerUtil;

    @BindView(R.id.radioManshiaAsfra)
    RadioButton r1;

    @BindView(R.id.radioManshiaVictoria)
    RadioButton r2;

    @BindView(R.id.radioManshiaMandra)
    RadioButton r3;

    @BindView(R.id.radioAsfraMahta)
    RadioButton r4;

    @BindView(R.id.radioMandraMahta)
    RadioButton r5;

    @BindView(R.id.radioMandraAboker)
    RadioButton r6;

    @BindView(R.id.radioLinesGroup)
    RadioGroup linesGroup;

    @BindView(R.id.btnConfirm)
    Button confirmButton;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_subscription);
        ButterKnife.bind(this);
        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tv = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        tv.setText(R.string.line_subscription_activity_title);

        Intent intent = getIntent();
        String line = intent.getStringExtra(NavigationDrawerUtil.SUB_LINE_EXTRA);

        setCurrentLine(line);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = linesGroup.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                RadioButton selectedLineRadio = (RadioButton) findViewById(selectedId);

                mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("line").setValue(selectedLineRadio.getText());

                Toast.makeText(LineSubscriptionActivity.this, R.string.line_subscription_confirmation_message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setCurrentLine(String line) {
        switch(line){
            case "Manshia : Asafra":
                r1.setChecked(true);
                break;
            case "Manshia : Victoria":
                r2.setChecked(true);
                break;
            case "Manshia : Mandra":
                r3.setChecked(true);
                break;
            case "Asfra : Mahta":
                r4.setChecked(true);
                break;
            case "Mandra : Mahta":
                r5.setChecked(true);
                break;
            case "Mandra : Aboker":
                r6.setChecked(true);
                break;
        }
    }
}
