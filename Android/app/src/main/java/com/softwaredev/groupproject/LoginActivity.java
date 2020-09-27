package com.softwaredev.groupproject;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    Context myContext = this;
    String TAG = LoginActivity.class.getSimpleName();
    String value = " ";
    boolean correctUser = false;
    boolean correctPass = true;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String test1, test2, userName;
    TwitterLoginButton loginButton;
    EditText aEdit;
    EditText bEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
       /* loadPrefs();
        if (userName != "default"){
            Intent profileIntent = new Intent(myContext, patientHomeScreenActivity.class);
            profileIntent.putExtra("PATIENT_ID", "Patient1");
            myContext.startActivity(profileIntent);
        }*/

        Twitter.initialize(this);

        Button button = (Button)findViewById(R.id.loginButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                aEdit = (EditText) findViewById(R.id.nameEntry);
                test1 = aEdit.getText().toString();
                bEdit = (EditText) findViewById(R.id.passwordEntry);
                test2 = bEdit.getText().toString();
                DatabaseReference patientRef = myRef.child("users/Patients");
                DatabaseReference carerRef = myRef.child("users/Carers");

                patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot objSnapshot : snapshot.getChildren()) {
                            String z = objSnapshot.getKey();
                            System.out.println("Key: " + z);
                            if (test1.equals(z)) {
                                correctUser = true;
                                userName = "users/Patients/" + test1;
                                //savePrefs("userData", userName);
                                DatabaseReference tempCarerRef = myRef.child("users/Carers/" + objSnapshot.child("CarerID").getValue(String.class));
                                tempCarerRef.addListenerForSingleValueEvent(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(test2.equals(dataSnapshot.child("Password").getValue(String.class))) {
                                            Intent profileIntent = new Intent(myContext, patientHomeScreenActivity.class);
                                            profileIntent.putExtra("PATIENT_ID", test1);
                                            myContext.startActivity(profileIntent);
                                        } else {
                                            aEdit.setText("");
                                            bEdit.setText("");
                                            aEdit.setHintTextColor(getResources().getColor(R.color.red));
                                            aEdit.setHint("Please try again");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                break;
                            }
                        }
                    }

                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                    }
                });

                carerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot objSnapshot : dataSnapshot.getChildren()) {
                            String z = objSnapshot.getKey();
                            if (test1.equals(z)) {
                                correctUser = true;
                                userName = "users/Carers/" + test1;
                                //savePrefs("userData", userName);
                                if(test2.equals(objSnapshot.child("Password").getValue(String.class))) {
                                    Intent profileIntent = new Intent(myContext, CarerHomeScreen.class);
                                    profileIntent.putExtra("CARER_ID", test1);
                                    myContext.startActivity(profileIntent);
                                } else {
                                    aEdit.setText("");
                                    bEdit.setText("");
                                    aEdit.setHintTextColor(getResources().getColor(R.color.red));
                                    aEdit.setHint("Please try again");
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                /*if (correctUser != true) {
                    patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.child(test2).exists()) {
                                correctUser = true;
                            } else {

                            }
                        }

                        public void onCancelled(DatabaseError error) {
                            // Failed to read value

                        }
                    });
                    //TESTING TESTING

                    //TESTING TESTING

*/
            }
        });

        Button createPatient = (Button) findViewById(R.id.patientButton);
        createPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createPatientIntent = new Intent(myContext, CreateAccount.class);
                myContext.startActivity(createPatientIntent);
            }
        });

        Button createCarer = (Button) findViewById(R.id.carerButton);
        createCarer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createCarerIntent = new Intent(myContext, CreateCarerAccount.class);
                myContext.startActivity(createCarerIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    /*private void savePrefs(String key, String value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }
    private void loadPrefs(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String stuff = sp.getString("userData", "default");
        userName = stuff;
    }*/
}







