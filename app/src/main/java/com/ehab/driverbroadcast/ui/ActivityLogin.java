package com.ehab.driverbroadcast.ui;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.ehab.driverbroadcast.R;
import com.ehab.driverbroadcast.model.User;


public class ActivityLogin extends ActivityBase {

    private static final String TAG = "Message";
    protected TextView mSignUpTextView;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference muser;
    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView tv = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        tv.setText("Login");
        //Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/VarelaRound-Regular.ttf");
        //tv.setTypeface(custom_font);


        mSignUpTextView = (TextView) findViewById(R.id.sigupText);
        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLogin.this, ActivitySignUp.class);
                startActivity(intent);
            }
        });


        mEmailField = (EditText) findViewById(R.id.editText);
        mPasswordField = (EditText) findViewById(R.id.editText2);

        btn = (Button) findViewById(R.id.button);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
                    String e = mEmailField.getText().toString();
                    String p = mPasswordField.getText().toString();
                    login(e, p);
                } else {
                    Toast.makeText(ActivityLogin.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private void login(String email, String password) {

        //Check if the email or password fields are empty
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        hideProgressDialog();

                        if (task.isSuccessful() && mAuth.getCurrentUser().isEmailVerified()) {

                            Intent intent = new Intent(ActivityLogin.this, LocationBroadcastActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {
                            Log.w(TAG, "signInWithEmail", task.getException());
                            if (mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isEmailVerified()) {
                                Toast.makeText(ActivityLogin.this, "Account is not verified \n   Authentication Failed!", Toast.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut();

                            } else {
                                Toast.makeText(ActivityLogin.this, "Email or Password is wrong \n    Authentication Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }


                });
    }


    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        } else {
            mPasswordField.setError(null);
        }

        return result;
    }
}
