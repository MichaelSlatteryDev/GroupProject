package com.softwaredev.groupproject;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    static String SAVED_DATE="com.example.michael.DATE";
    static long SAVED_DATE_LONG = 0;
    private CalendarView calendarView;
    private TextView dateDisplay;
    private TextView savedMessage;
    private boolean deleted = false;
    private String message = "";
    private int month;
    private int year;
    private GestureDetectorCompat gestureDetector;
    private GestureActivity gestureActivity = new GestureActivity();
    private ArrayList<String> savedDates = new ArrayList<>();
    private ArrayList<String> savedMessages = new ArrayList<>();
    private ArrayList<Long> orderDates = new ArrayList<>();
    private ArrayList<String> orderString = new ArrayList<>();
    private ArrayList<Long> contextOrderDate = new ArrayList<>();
    private ArrayList<String> contextOrderString = new ArrayList<>();
    private ImageView profile;
    private ImageView calendar;
    private ImageView home;
    private String patientId;
    private String carerId;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference writeDateRef;
    private DatabaseReference writeMessageRef;
    private boolean managing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Intent getPatient = getIntent();
        patientId = getPatient.getStringExtra("PATIENT_ID");

        carerId = getPatient.getStringExtra("CARER_ID");

        managing = getPatient.getBooleanExtra("MANAGING", false);

        writeDateRef = database.getReference("users/Patients/" + patientId
                + "/Dates");
        writeMessageRef = database.getReference("users/Patients/" + patientId
                + "/Messages");

        //Gesture detector being created for the fling
        gestureDetector = new GestureDetectorCompat(this, gestureActivity);

        calendarView = (CalendarView) findViewById(R.id.calendarView);
        dateDisplay = (TextView) findViewById(R.id.date_display);
        savedMessage = (TextView) findViewById(R.id.displayText);
        savedMessage.setTextColor(ContextCompat.getColor(this, R.color.black));
        home = (ImageView)findViewById(R.id.home);
        profile = (ImageView)findViewById(R.id.profile);
        calendar = (ImageView)findViewById(R.id.calendar);

        //Spinner being created.
        Spinner spinner = (Spinner) findViewById(R.id.months_spinner);
        //Have the ArrayAdapter be populated with the values in the string-array months_array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        //Make it a simple spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Set the spinners adapter
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //Get the current date and set dateDisplays to todays date
        Date date = new Date(calendarView.getDate());
        SimpleDateFormat d = new SimpleDateFormat("dd / MM / yyyy");
        String a = d.format(date);
        dateDisplay.setText("Date: " + a);

        //Register dateDisplay for the ContextMenu
        registerForContextMenu(dateDisplay);

        //Reads First to check if previous entries exist
        writeDateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("Entered1");
                if(dataSnapshot.getValue() != null){
                    System.out.println("Entered2");
                    savedDates = (ArrayList<String>) dataSnapshot.getValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        writeMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Get the Intent from TextActivity
                Intent messageIntent = getIntent();

                //Makes the date the same as the one where the message was
                // entered when returning from TextActivity
                SAVED_DATE_LONG = messageIntent.getLongExtra("SAVED_DATE_LONG",
                        TextActivity.SAVED_DATE_LONG);
                System.out.println("Long: " + SAVED_DATE_LONG);
                if(SAVED_DATE_LONG != 0) {
                    System.out.println(SAVED_DATE_LONG);
                    calendarView.setDate(SAVED_DATE_LONG);
                    dateDisplay.setText(messageIntent.getStringExtra(TextActivity.SAVED_DATE_STRING));
                }

                if(dataSnapshot.getValue() != null) {
                    savedMessages = (ArrayList<String>) dataSnapshot.getValue();
                }

                //Makes the message the message sent from TextActivity
                message = messageIntent.getStringExtra(TextActivity.SAVED_MESSAGE);
                if (message != null) {
                    if (!message.equals("No entry added") && message != null) {
                        //Add message and dates to array and displays message
                        if (savedDates.size() > 0) {
                            for (int i = 0; i < savedDates.size(); i++) {
                                //If user chose to add
                                if (messageIntent.getIntExtra("OVERWRITE", TextActivity.OVERWRITE) == 2 &&
                                        dateDisplay.getText().toString().equals(savedDates.get(i))) {
                                    String tempString;
                                    tempString = savedMessages.get(i);
                                    tempString += ("\n" + message);
                                    savedMessages.remove(i);
                                    savedMessages.add(i, tempString);
                                    savedMessage.setText(savedMessages.get(i));
                                    deleted = true;
                                    break;
                                }
                            }
                            for (int i = 0; i < savedDates.size(); i++) {
                                //If user chose to delete
                                if (messageIntent.getIntExtra("OVERWRITE", TextActivity.OVERWRITE) == 3 &&
                                        dateDisplay.getText().toString().equals(savedDates.get(i))) {
                                    savedMessages.remove(i);
                                    savedDates.remove(i);
                                    deleted = true;
                                    break;
                                }
                            }
                            //If user chose to overwrite old message
                            if (messageIntent.getIntExtra("OVERWRITE", TextActivity.OVERWRITE) == 1) {
                                System.out.println("Overwrite Message");
                                for (int i = 0; i < savedDates.size(); i++) {
                                    if (dateDisplay.getText().toString().equals(savedDates.get(i))) {
                                        savedMessages.remove(i);
                                        savedMessages.add(i, message);
                                        savedDates.remove(i);
                                        savedDates.add(i, dateDisplay.getText().toString());
                                        savedMessage.setText(message);
                                        break;
                                    } else if (i == savedDates.size() - 1) {
                                        savedMessages.add(message);
                                        savedDates.add(dateDisplay.getText().toString());
                                        savedMessage.setText(message);
                                        break;
                                    }
                                }
                            } else if (!deleted) {
                                //Error message
                                System.out.println("Warning1");
                                savedMessage.setText("ERROR: Nothing to change. Choose New Message if you " +
                                        "want to add a new message.");
                                savedMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                            }
                            //If user wants to make a new message
                        } else if (messageIntent.getIntExtra("OVERWRITE", TextActivity.OVERWRITE) == 1) {
                            System.out.println("New Message");
                            savedMessages.add(message);
                            savedDates.add(dateDisplay.getText().toString());
                            savedMessage.setText(message);
                        } else {
                            System.out.println("Warning2");
                            savedMessage.setText("ERROR: Nothing to change. Choose New Message if you " +
                                    "want to add a new message.");
                            savedMessage.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                        }
                    }
                }

                //Start the background service
                Intent svc=new Intent(getApplicationContext(), backgroundService.class);
                svc.putExtra("SAVED_DATES", savedDates);
                startService(svc);

                //Get the current year and month
                long timestamp = calendarView.getDate();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(timestamp);
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH) + 1;

                //What happens when a new date is chosen on the CalendarView
                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(CalendarView calendarView, int i, int i1, int i2) {
                        //Change dateDisplay
                        dateDisplay.setText("Date: " + i2 + " / " + (i1+1) + " / " + i);

                        //Change year and month
                        year = i;
                        month = i1+1;

                        savedMessage.setTextColor(ContextCompat.getColor(getApplicationContext(),
                                R.color.black));

                        System.out.println("Cal Date: " + calendarView.getDate());

                        Calendar c = Calendar.getInstance();
                        c.set(i, i1, i2);

                        calendarView.setDate(c.getTimeInMillis());

                        SAVED_DATE_LONG = calendarView.getDate();

                        for (int date = 0; date < savedDates.size(); date++) {
                            System.out.println("Date: " + savedDates.get(date));
                        }

                        //If the date has anything written on it set the text
                        for (int date = 0; date < savedDates.size(); date++) {
                            if (dateDisplay.getText().toString().equals(savedDates.get(date))) {
                                savedMessage.setText(savedMessages.get(date));
                                break;
                            } else {
                                savedMessage.setText("");
                            }
                        }
                    }
                });

                sortDates();

                for(int i = 0; i < orderString.size();  i++){
                    String[] split = orderString.get(i).split(": ");
                    savedDates.set(i, split[0] + ": " +  split[1]);
                    savedMessages.set(i, split[2]);
                }

                //Saves arrays to firebase
                writeDateRef.setValue(savedDates);
                writeMessageRef.setValue(savedMessages);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if(!managing) {
            home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), patientHomeScreenActivity.class);
                    intent.putExtra("PATIENT_ID", patientId);
                    startActivity(intent);
                }
            });

            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    intent.putExtra("PATIENT_ID", patientId);
                    startActivity(intent);
                }
            });

            calendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        } else {
            Button manageCalendar = (Button) findViewById(R.id.manageCalendar);
            manageCalendar.setVisibility(View.VISIBLE);

            manageCalendar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), carerManageCalendars.class);
                    intent.putExtra("CARER_ID", carerId);
                    startActivity(intent);
                }
            });
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        //Code for the ContextMenu
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Events this month");

        for(int i = 0; i < savedDates.size(); i++){
            //Split the dates string into an array
            String DMY[] = savedDates.get(i).split(" / ");
            String tempMonth = DMY[1];
            String tempYear = DMY[2];

            //Split first item to get rid of Date:
            String splitDay[] = DMY[0].split(": ");

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(splitDay[1]));

            //If the month and year are the same as the current add the message and day to the arrays
            if(Integer.parseInt(tempMonth) == month && Integer.parseInt(tempYear) == year){
                contextOrderDate.add(cal.getTimeInMillis());
                contextOrderString.add(savedDates.get(i) + ": " + savedMessages.get(i));
            }
        }

        //Use insertion sort to sort the arrays
        for(int i = 1; i < contextOrderDate.size(); i++){
            String currentString = contextOrderString.get(i);
            long currentDate = contextOrderDate.get(i);
            int j = i-1;

            while(j >= 0 && contextOrderDate.get(j) > currentDate){
                contextOrderDate.set(j+1, contextOrderDate.get(j));
                contextOrderString.set(j+1, contextOrderString.get(j));
                j = j-1;
            }
            contextOrderDate.set(j+1, currentDate);
            contextOrderString.set(j+1, currentString);
        }

        //Add the dates in order to the context mane
        for(int i = 0; i < contextOrderString.size(); i++){
            menu.add(0, v.getId(), 0, contextOrderString.get(i));
        }

        contextOrderDate.clear();
        contextOrderString.clear();
    }

    private void sortDates(){

        for(int i = 0; i < savedDates.size(); i++){
            //Split the dates string into an array
            String DMY[] = savedDates.get(i).split(" / ");
            String tempMonth = DMY[1];
            String tempYear = DMY[2];

            //Split first item to get rid of Date:
            String splitDay[] = DMY[0].split(": ");
            String splitMonth = DMY[1];
            String splitYear = DMY[2];

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, Integer.parseInt(splitYear));
            cal.set(Calendar.MONTH, Integer.parseInt(splitMonth)-1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(splitDay[1]));

            //If the month and year are the same as the current add the message and day to the arrays
            //if(Integer.parseInt(tempMonth) == month && Integer.parseInt(tempYear) == year){
                orderDates.add(cal.getTimeInMillis());
                orderString.add(savedDates.get(i) + ": " + savedMessages.get(i));
            //}
        }

        //Use insertion sort to sort the arrays
        for(int i = 1; i < orderDates.size(); i++){
            String currentString = orderString.get(i);
            long currentDate = orderDates.get(i);
            int j = i-1;

            while(j >= 0 && orderDates.get(j) > currentDate){
                orderDates.set(j+1, orderDates.get(j));
                orderString.set(j+1, orderString.get(j));
                j = j-1;
            }
            orderDates.set(j+1, currentDate);
            orderString.set(j+1, currentString);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Makes touch events be used by gesture detector
        gestureDetector.onTouchEvent(event);
        return false;

    }

    public class GestureActivity extends GestureDetector.SimpleOnGestureListener {

        @Override
        //If the user flings from right to left bring them to TextActivity
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY){
            if(event2.getX() < event1.getX()) {
                if(!managing) {
                    Intent intent = new Intent(CalendarActivity.this, TextActivity.class);
                    intent.putExtra(SAVED_DATE, dateDisplay.getText());
                    System.out.println("Going Date: " + SAVED_DATE_LONG);
                    intent.putExtra("SAVED_DATE_LONG", SAVED_DATE_LONG);
                    intent.putExtra("PATIENT_ID", patientId);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(CalendarActivity.this, TextActivity.class);
                    intent.putExtra(SAVED_DATE, dateDisplay.getText());
                    System.out.println("Going Date: " + SAVED_DATE_LONG);
                    intent.putExtra("SAVED_DATE_LONG", SAVED_DATE_LONG);
                    intent.putExtra("PATIENT_ID", patientId);
                    intent.putExtra("CARER_ID", carerId);
                    intent.putExtra("MANAGING", managing);
                    startActivity(intent);
                }
            }
            return true;
        }
    }

    //If you choose an item on the Spinner
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        int day;
        int month;

        //Check which month was selected
        for(int i = 1; i <= 12; i++) {
            if (pos == i) {
                day = 1;
                month = i-1;
                //Uses calendarDate method to get date in long format
                long milliDate = calendarDate(month, day);

                //Set the date and dateDisplay
                calendarView.setDate(milliDate);
                dateDisplay.setText("Date: 01 / " + i + " / " + year);
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    //Get the date in long format
    private long calendarDate(int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        //Returns the date in long format
        return calendar.getTimeInMillis();
    }
}
