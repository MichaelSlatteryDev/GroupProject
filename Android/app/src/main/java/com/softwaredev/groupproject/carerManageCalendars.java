package com.softwaredev.groupproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class carerManageCalendars extends AppCompatActivity {

    private String carerId;
    private ArrayList<String> patients;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Button button;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carer_manage_calendars);

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
                                index = tableLayout.indexOfChild(row);
                                DatabaseReference patientRef = database.getReference("users/Patients/" + patients.get(index));

                                patientRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                                        intent.putExtra("CARER_ID", carerId);
                                        intent.putExtra("PATIENT_ID", patients.get(index));
                                        intent.putExtra("MANAGING", true);
                                        startActivity(intent);
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
                Intent intent = new Intent(getApplicationContext(), security.class);
                intent.putExtra("CARER_ID", carerId);
                startActivity(intent);
            }
        });

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
