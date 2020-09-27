package com.softwaredev.groupproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import android.content.Context;

import java.util.ArrayList;

public class CreateAccount extends AppCompatActivity {

    private EditText text;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("users/Patients");
    private DatabaseReference carers = database.getReference("users/Carers");
    private int max = 0;
    private String name;
    private String address;
    private String dateOfBirth;
    private String phoneNo;
    private String eircode;
    private String patientId;
    private String carerId;
    private String carerName;
    private String carerCheck;
    private ArrayList<String> startList;

    Context myContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_account);

        Intent checkCarer = getIntent();
        carerCheck = checkCarer.getStringExtra("CARER_ID");


        Button createPatientButton = (Button) findViewById(R.id.AddPatientButton);
        createPatientButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                text = (EditText) findViewById(R.id.PatientName);
                name = text.getText().toString();

                text = (EditText) findViewById(R.id.PatientAddress);
                address = text.getText().toString();
                text = (EditText) findViewById(R.id.PatientAddress2);
                if(!text.getText().toString().isEmpty()) address += ", " + text.getText().toString();
                text = (EditText) findViewById(R.id.PatientAddressCity);
                if(!text.getText().toString().isEmpty()) address += ", " + text.getText().toString();

                text = (EditText) findViewById(R.id.PatientDOB);
                dateOfBirth = text.getText().toString();

                text = (EditText) findViewById(R.id.PatientPhoneNo);
                phoneNo = text.getText().toString();

                text = (EditText) findViewById(R.id.PatientEircode);
                eircode = text.getText().toString();

                text = (EditText) findViewById(R.id.enterCarerID);
                carerId = text.getText().toString();

                if(!name.isEmpty() && !address.isEmpty() && !phoneNo.isEmpty() && !carerId.isEmpty())

                    carers.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot objSnapshot: dataSnapshot.getChildren()) {
                                if(carerId.equals(objSnapshot.getKey())){
                                    carerName = objSnapshot.child("Name").getValue().toString();
                                    final DatabaseReference carerAddPatient = database.getReference("users/Carers/" + carerId + "/Patients");
                                    System.out.println("Entered");
                                    carerAddPatient.addListenerForSingleValueEvent(new ValueEventListener() {
                                          @Override
                                          public void onDataChange(final DataSnapshot carerSnapshot) {
                                              System.out.println("Entered2");
                                              startList = (ArrayList<String>) carerSnapshot.getValue();
                                              if(startList.get(0).equals("Placeholder")){
                                                  startList.remove(0);
                                              }

                                              myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                  @Override
                                                  public void onDataChange(DataSnapshot snapshot) {
                                                      System.out.println("Entered3");
                                                      for (DataSnapshot objSnapshot: snapshot.getChildren()) {
                                                          String current = objSnapshot.getKey();
                                                          String[] split = current.split("Patient");

                                                          if(max < Integer.parseInt(split[1])){
                                                              max = Integer.parseInt(split[1]);
                                                          }
                                                      }
                                                      max++;
                                                      DatabaseReference patients = myRef.child("Patient" + max);
                                                      patientId = "Patient" + max;
                                                      max = 0;
                                                      patients.child("Name").setValue(name);
                                                      patients.child("Address").setValue(address);
                                                      patients.child("Phone Number").setValue(phoneNo);
                                                      patients.child("Date of Birth").setValue(dateOfBirth);
                                                      patients.child("Eircode").setValue(eircode);
                                                      patients.child("PatientID").setValue(patientId);
                                                      patients.child("Left Geo").setValue("false");
                                                      patients.child("CarerID").setValue(carerId);
                                                      patients.child("Carer Name").setValue(carerName);
                                                      startList.add(patientId);
                                                      carerAddPatient.setValue(startList);
                                                      if(carerCheck == null) {
                                                          Intent mainActivity = new Intent(myContext, patientHomeScreenActivity.class);
                                                          mainActivity.putExtra("PATIENT_ID", patientId);
                                                          myContext.startActivity(mainActivity);
                                                      } else{
                                                          Intent mainActivity = new Intent(myContext, CarerHomeScreen.class);
                                                          mainActivity.putExtra("CARER_ID", carerCheck);
                                                          myContext.startActivity(mainActivity);
                                                      }
                                                  }

                                                  public void onCancelled(DatabaseError error) {
                                                      // Failed to read value

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
        });


    }

    public String getPatientId(){
        return patientId;
    }
}
