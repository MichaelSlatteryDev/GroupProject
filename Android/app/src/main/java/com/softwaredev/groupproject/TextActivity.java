package com.softwaredev.groupproject;

import android.content.Intent;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Calendar;

public class TextActivity extends AppCompatActivity {

    static String SAVED_MESSAGE = "com.example.michael.MESSAGE";
    static long SAVED_DATE_LONG = 0;
    static String SAVED_DATE_STRING = "com.example.michael.SAVED_DATE";
    static int OVERWRITE = 0;
    private GestureDetectorCompat gestureDetector;
    private GestureActivity gestureActivity = new GestureActivity();
    private EditText savedText;
    private TextView savedDate;
    private Button confirmChanges;
    private ImageView profile;
    private ImageView calendar;
    private ImageView home;
    private String patientId;
    private String carerId;
    private boolean managing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        savedText = (EditText) findViewById(R.id.textSaved);
        home = (ImageView)findViewById(R.id.home);
        profile = (ImageView)findViewById(R.id.profile);
        calendar = (ImageView)findViewById(R.id.calendar);

        Intent dateIntent = getIntent();
        String date = dateIntent.getStringExtra(CalendarActivity.SAVED_DATE);
        SAVED_DATE_LONG = dateIntent.getLongExtra("SAVED_DATE_LONG",
                CalendarActivity.SAVED_DATE_LONG);
        savedDate = (TextView) findViewById(R.id.date_display);
        savedDate.setText(date);

        confirmChanges = (Button) findViewById(R.id.confirm);
        confirmChanges.setText("Confirm");
        confirmChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!managing) {
                    Intent intent = new Intent(TextActivity.this, CalendarActivity.class);
                    intent.putExtra(SAVED_MESSAGE, savedText.getText().toString());
                    intent.putExtra(SAVED_DATE_STRING, savedDate.getText().toString());
                    intent.putExtra("SAVED_DATE_LONG", SAVED_DATE_LONG);
                    intent.putExtra("OVERWRITE", OVERWRITE);
                    intent.putExtra("PATIENT_ID", patientId);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(TextActivity.this, CalendarActivity.class);
                    intent.putExtra(SAVED_MESSAGE, savedText.getText().toString());
                    intent.putExtra(SAVED_DATE_STRING, savedDate.getText().toString());
                    intent.putExtra("SAVED_DATE_LONG", SAVED_DATE_LONG);
                    intent.putExtra("OVERWRITE", OVERWRITE);
                    intent.putExtra("PATIENT_ID", patientId);
                    intent.putExtra("CARER_ID", carerId);
                    intent.putExtra("MANAGING", managing);
                    startActivity(intent);
                }
            }
        });

        patientId = dateIntent.getStringExtra("PATIENT_ID");

        carerId = dateIntent.getStringExtra("CARER_ID");

        managing = dateIntent.getBooleanExtra("MANAGING", false);

        gestureDetector = new GestureDetectorCompat(this, gestureActivity);

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
                    Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                    intent.putExtra("PATIENT_ID", patientId);
                    startActivity(intent);
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

    public void onRadioButtonClicked(View view){
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.overwriteText:
                if (checked) {
                    OVERWRITE = 1;
                    break;
                }
            case R.id.appendText:
                if (checked) {
                    OVERWRITE = 2;
                    break;
                }
            case R.id.deleteText:
                if (checked) {
                    OVERWRITE = 3;
                    break;
                }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return false;
    }

    public class GestureActivity extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY){
            if(event2.getX() > event1.getX()){
                if(!managing) {
                    Intent intent = new Intent(TextActivity.this, CalendarActivity.class);
                    intent.putExtra(SAVED_MESSAGE, "No entry added");
                    intent.putExtra(SAVED_DATE_STRING, savedDate.getText().toString());
                    intent.putExtra("SAVED_DATE_LONG", SAVED_DATE_LONG);
                    intent.putExtra("PATIENT_ID", patientId);
                    startActivity(intent);
                } else{
                    Intent intent = new Intent(TextActivity.this, CalendarActivity.class);
                    intent.putExtra(SAVED_MESSAGE, "No entry added");
                    intent.putExtra(SAVED_DATE_STRING, savedDate.getText().toString());
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
}
