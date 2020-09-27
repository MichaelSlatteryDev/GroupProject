package com.softwaredev.groupproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class security extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("users/Patients");
    private DatabaseReference patientRef;
    private DatabaseReference prevCarerRef;
    private DatabaseReference currentCarerRef;
    private DatabaseReference currentCarerNameRef;
    private ArrayList<String> prevCarerList;
    private TextView patientFound;
    private EditText enterPatientId;
    private Button search;
    private Button takePatient;
    private Button addNewPatient;
    private String carerId;
    private String check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        ImageView home = (ImageView)findViewById(R.id.home);
        ImageView security = (ImageView)findViewById(R.id.homeAuto);
        ImageView calendar = (ImageView)findViewById(R.id.calendar);

        Intent fromCreate = getIntent();
        carerId = fromCreate.getStringExtra("CARER_ID");

        patientFound = (TextView) findViewById(R.id.patientFound);
        enterPatientId = (EditText) findViewById(R.id.enterPatientID);
        search = (Button) findViewById(R.id.search);
        takePatient = (Button) findViewById(R.id.takePatient);
        addNewPatient = (Button) findViewById(R.id.addNewPatient);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        check = enterPatientId.getText().toString();
                        if(!check.isEmpty()) {
                            for (DataSnapshot objSnapshot : dataSnapshot.getChildren()) {
                                String tempPatient = objSnapshot.getKey();
                                if (check.equals(tempPatient)) {
                                    patientFound.setVisibility(View.VISIBLE);
                                    takePatient.setEnabled(true);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        takePatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                patientRef = database.getReference("users/Patients/" + check);
                patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String prevCarer = dataSnapshot.child("CarerID").getValue().toString();

                        prevCarerRef = database.getReference("users/Carers/" + prevCarer + "/Patients");

                        prevCarerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                prevCarerList = (ArrayList<String>) dataSnapshot.getValue();

                                for(int i = 0; i < prevCarerList.size(); i++){
                                    if(prevCarerList.get(i).equals(check)){
                                        prevCarerList.remove(i);

                                        currentCarerRef = database.getReference("users/Carers/" + carerId + "/Patients");

                                        currentCarerRef.addListenerForSingleValueEvent(new ValueEventListener() {

                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                ArrayList<String> currentCarerList = (ArrayList<String>) dataSnapshot.getValue();
                                                currentCarerList.add(check);

                                                prevCarerRef.setValue(prevCarerList);
                                                currentCarerRef.setValue(currentCarerList);
                                                patientRef.child("CarerID").setValue(carerId);

                                                currentCarerNameRef = database.getReference("users/Carers/" + carerId);

                                                currentCarerNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        patientRef.child("Carer Name").setValue(dataSnapshot.child("Name").getValue().toString());
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        addNewPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainActivity = new Intent(getApplicationContext(), CreateAccount.class);
                mainActivity.putExtra("CARER_ID", carerId);
                getApplicationContext().startActivity(mainActivity);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CarerHomeScreen.class);
                intent.putExtra("CARER_ID", carerId);
                startActivity(intent);
            }
        });

        security.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), carerManageCalendars.class);
                intent.putExtra("CARER_ID", carerId);
                startActivity(intent);
            }
        });
    }
}
