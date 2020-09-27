package com.softwaredev.groupproject;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CarerHomeScreen extends AppCompatActivity {

    private TextView dateAndTime;
    private String carerId;
    private ArrayList<String> patients;
    private Button button;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carer_home_screen);

        ImageView home = (ImageView)findViewById(R.id.home);
        ImageView security = (ImageView)findViewById(R.id.homeAuto);
        ImageView calendar = (ImageView)findViewById(R.id.calendar);

        Intent fromCreate = getIntent();
        carerId = fromCreate.getStringExtra("CARER_ID");

        DatabaseReference patientsRef = database.getReference("users/Carers/" + carerId + "/Patients");

        patientsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    patients = (ArrayList<String>) dataSnapshot.getValue();

                    final TableLayout tableLayout = (TableLayout) findViewById(R.id.patientTable);

                    for(int i = 0; i < patients.size(); i++){
                        TableRow tableRow = new TableRow(getApplicationContext());
                        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                        tableRow.setTag(patients);
                        button = new Button(getApplicationContext());
                        button.setText(i+1 + ": " + patients.get(i));

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                TableRow row = (TableRow) view.getParent();
                                int index = tableLayout.indexOfChild(row);
                                DatabaseReference patientRef = database.getReference("users/Patients/" + patients.get(index));

                                patientRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        TextView patientInfo = (TextView) findViewById(R.id.patientDesc);

                                        patientInfo.setText(getString(R.string.patientInfo, dataSnapshot.child("Name").getValue(String.class),
                                                dataSnapshot.child("Phone Number").getValue(String.class), dataSnapshot.child("Address").getValue(String.class),
                                                dataSnapshot.child("Date of Birth").getValue(String.class), dataSnapshot.child("Eircode").getValue(String.class),
                                                dataSnapshot.child("Left Geo").getValue(String.class)));
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                        button.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                        tableRow.addView(button);
                        tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        dateAndTime = (TextView) findViewById(R.id.dateAndTime);

        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dateAndTime.setText(new SimpleDateFormat("dd/MMM/yyyy kk:mm",
                        Locale.UK).format(new Date()));
                someHandler.postDelayed(this, 1000);
            }
        }, 10);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        security.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), security.class);
                intent.putExtra("CARER_ID", carerId);
                startActivity(intent);
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
