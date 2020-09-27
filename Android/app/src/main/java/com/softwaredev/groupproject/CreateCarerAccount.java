package com.softwaredev.groupproject;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CreateCarerAccount extends AppCompatActivity {

    private EditText text;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("users/Carers");
    private int max = 0;
    private String fName;
    private String lName;
    private String name;
    private String address;
    private String password;
    private String phoneNo;
    private String email;
    private String carerId;
    private ArrayList<String> emptyList = new ArrayList<>();

    Context myContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_carer_account);

        Button createCarerButton = (Button) findViewById(R.id.CreateCarer);
        createCarerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                text = (EditText) findViewById(R.id.EnterCarerFName);
                fName = text.getText().toString();

                text = (EditText) findViewById(R.id.EnterCarerLName);
                lName = " " + text.getText().toString();
                name = fName + lName;

                text = (EditText) findViewById(R.id.line1);
                address = text.getText().toString();
                text = (EditText) findViewById(R.id.line2);
                if(!text.getText().toString().isEmpty()) address += ", " + text.getText().toString();
                text = (EditText) findViewById(R.id.lineCity);
                if(!text.getText().toString().isEmpty()) address += ", " + text.getText().toString();

                text = (EditText) findViewById(R.id.enterPassword);
                password = text.getText().toString();

                text = (EditText) findViewById(R.id.enterPhone);
                phoneNo = text.getText().toString();

                text = (EditText) findViewById(R.id.enterEmail);
                email = text.getText().toString();

                if(!name.isEmpty() && !address.isEmpty() && !phoneNo.isEmpty() &&
                        !email.isEmpty() && !password.isEmpty())
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for (DataSnapshot objSnapshot: snapshot.getChildren()) {
                                String current = objSnapshot.getKey();
                                String[] split = current.split("Carer");

                                if(max < Integer.parseInt(split[1])){
                                    max = Integer.parseInt(split[1]);
                                }
                            }
                            max++;
                            DatabaseReference newCarer = myRef.child("Carer" + max);
                            carerId = "Carer" + max;
                            max = 0;
                            newCarer.child("Name").setValue(name);
                            newCarer.child("Address").setValue(address);
                            newCarer.child("PhoneNo").setValue(phoneNo);
                            newCarer.child("Password").setValue(password);
                            newCarer.child("Email").setValue(email);
                            newCarer.child("ID").setValue(carerId);
                            newCarer.child("Geo").setValue("yes");
                            emptyList.add("Placeholder");
                            newCarer.child("Patients").setValue(emptyList);
                            Intent mainActivity = new Intent(myContext, CreateAccount.class);
                            mainActivity.putExtra("CARER_ID", carerId);
                            myContext.startActivity(mainActivity);
                        }

                        public void onCancelled(DatabaseError error) {
                            // Failed to read value

                        }
                    });

            }
        });
    }
}
