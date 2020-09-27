package com.softwaredev.groupproject;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.integreight.onesheeld.sdk.OneSheeldConnectionCallback;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.sdk.OneSheeldManager;
import com.integreight.onesheeld.sdk.OneSheeldScanningCallback;
import com.integreight.onesheeld.sdk.OneSheeldSdk;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class patientHomeScreenActivity extends AppCompatActivity implements OnCompleteListener<Void>{

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private TextView textView;
    private String patientId;
    private ArrayList<String> savedDates = new ArrayList<>();
    private ArrayList<String> savedMessages = new ArrayList<>();
    private static final String TAG = patientHomeScreenActivity.class.getSimpleName();
    private Switch lights;
    private Switch heating;
    private Double latitude, longitude;
    private boolean light = false;
    private boolean heat = false;
    private OneSheeldDevice realDevice;
    private Button callCarer;

    /**
     * Tracks whether the user requested to add or remove geofences, or to do neither.
     */
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    /**
     * Provides access to the Geofencing API.
     */
    private GeofencingClient mGeofencingClient;

    /**
     * The list of geofences used in this sample.
     */
    private ArrayList<Geofence> mGeofenceList;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Virtual Care Assistant");
        setContentView(R.layout.activity_patient_home_screen);
        ImageView home = (ImageView)findViewById(R.id.home);
        ImageView profile = (ImageView)findViewById(R.id.profile);
        ImageView calendar = (ImageView)findViewById(R.id.calendar);
        lights = (Switch)findViewById(R.id.LightSwitch);
        heating = (Switch)findViewById(R.id.HeatSwitch);
        callCarer = (Button)findViewById(R.id.callCarer);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        //populateGeofenceList();
        populateGeofenceList();

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        Intent fromCreate = getIntent();
        patientId = fromCreate.getStringExtra("PATIENT_ID");

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference writeDateRef = database.getReference("users/Patients/" + patientId
                + "/Dates");
        DatabaseReference writeMessageRef = database.getReference("users/Patients/" + patientId
                + "/Messages");

        writeDateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    savedDates = (ArrayList<String>) dataSnapshot.getValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        writeMessageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    savedMessages = (ArrayList<String>) dataSnapshot.getValue();

                    TextView calendarDate1 = (TextView)findViewById(R.id.calendarDate1);
                    TextView calendarDate2 = (TextView)findViewById(R.id.calendarDate2);
                    TextView calendarDate3 = (TextView)findViewById(R.id.calendarDate3);
                    TextView calendarMessage1 = (TextView)findViewById(R.id.calendarMessage1);
                    TextView calendarMessage2 = (TextView)findViewById(R.id.calendarMessage2);
                    TextView calendarMessage3 = (TextView)findViewById(R.id.calendarMessage3);

                    int check = 0;

                    for(int i = 0; i < savedMessages.size(); i++){

                        String DMY[] = savedDates.get(i).split(" / ");
                        String splitDay[] = DMY[0].split(": ");
                        String splitMonth = DMY[1];
                        String splitYear = DMY[2];

                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.YEAR, Integer.parseInt(splitYear));
                        cal.set(Calendar.MONTH, Integer.parseInt(splitMonth)-1);
                        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(splitDay[1]));

                        final Calendar yesterday = Calendar.getInstance();
                        yesterday.add(Calendar.DATE, -1);

                        System.out.println("Current: " + cal.getTimeInMillis());
                        System.out.println("Yesterday: " + yesterday.getTimeInMillis());

                        if(cal.getTimeInMillis() > yesterday.getTimeInMillis()) {
                            if (check == 0 && check < savedDates.size()) {
                                calendarDate1.setText(savedDates.get(i));
                                calendarMessage1.setText(savedMessages.get(i));
                            } else if (check == 1 && check < savedDates.size()) {
                                calendarDate2.setText(savedDates.get(i));
                                calendarMessage2.setText(savedMessages.get(i));
                            } else if (check == 2 && check < savedDates.size()) {
                                calendarDate3.setText(savedDates.get(i));
                                calendarMessage3.setText(savedMessages.get(i));
                            }
                            check++;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Used to show the date and time on home screen
        textView = (TextView) findViewById(R.id.dateAndTime);

        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setText(new SimpleDateFormat("dd/MMM/yyyy kk:mm",
                        Locale.UK).format(new Date()));
                someHandler.postDelayed(this, 1000);
            }
        }, 10);


        lights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!light) {
                    light = true;
                } else {
                    light = false;
                }
                if(realDevice != null) {
                    realDevice.digitalWrite(13, light);
                }
            }
        });

        heating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!heat) {
                    heat = true;
                } else {
                    heat = false;
                }

                if(realDevice != null) {
                    realDevice.digitalWrite(9, false);
                    realDevice.digitalWrite(8, heat);
                }
            }
        });

        callCarer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:0873845770"));
                startActivity(callIntent);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
    }

    private void loadData(DatabaseReference ref, final TextView row){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                row.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void connectOneSheeld(){
        OneSheeldSdk.init(this);
        OneSheeldSdk.setDebugging(true);

        OneSheeldManager manager = OneSheeldSdk.getManager();
        manager.setConnectionRetryCount(1);
        manager.setAutomaticConnectingRetriesForClassicConnections(true);

        OneSheeldScanningCallback scanningCallback = new OneSheeldScanningCallback() {
            @Override
            public void onDeviceFind(OneSheeldDevice device) {
                OneSheeldSdk.getManager().cancelScanning();
                device.connect();
            }
        };

        OneSheeldConnectionCallback connectionCallback = new OneSheeldConnectionCallback() {
            @Override
            public void onConnect(OneSheeldDevice device) {
                realDevice = device;
            }
        };
        manager.addScanningCallback(scanningCallback);
        manager.addConnectionCallback(connectionCallback);

        manager.scan();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            performPendingGeofenceTask();
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }



    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */

    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if (!checkPermissions()) {
            //ADD log to say insufficient permissions
            return;
        }

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }


    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        if (!checkPermissions()) {
            //ADD log to say insufficient permissions
            return;
        }

        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener( this);
    }

    /**
     * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
     * is available.
     * @param task the resulting Task, containing either a result or error.
     */

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE;
        if (task.isSuccessful()) {
            updateGeofencesAdded(!getGeofencesAdded());
            int messageId = getGeofencesAdded() ? R.string.geofences_added : R.string.geofences_removed;
            Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */

    private void populateGeofenceList() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref1 = database.getReference("GPS");
        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() != null) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    latitude = (Double) dataSnapshot.child("latitude").getValue();
                    longitude = (Double) dataSnapshot.child("longitude").getValue();
                    System.out.println(longitude + " TEST" + latitude);

                    System.out.println(longitude + " "  + latitude);
                    mGeofenceList.add(new Geofence.Builder()
                            // Set the request ID of the geofence. This is a string to identify this
                            // geofence.
                            .setRequestId("1")

                            // Set the circular region of this geofence.
                            .setCircularRegion(
                                    latitude,
                                    longitude,
                                    Constants.GEOFENCE_RADIUS_IN_METERS
                            )

                            // Set the expiration duration of the geofence. This geofence gets automatically
                            // removed after this period of time.
                            .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                            // Set the transition types of interest. Alerts are only generated for these
                            // transition. We track entry and exit transitions in this sample.
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                    Geofence.GEOFENCE_TRANSITION_EXIT)

                            // Create the geofence.
                            .build());
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }


    /**
     * Returns true if geofences were added, otherwise false.
     */
    private boolean getGeofencesAdded() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Constants.GEOFENCES_ADDED_KEY, false);
    }


    private void updateGeofencesAdded(boolean added) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
                .apply();
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    private void performPendingGeofenceTask() {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            addGeofences();
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            removeGeofences();
        }
    }

    /**
     * Return the current state of the permissions needed.
     */

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            // Request permission
            ActivityCompat.requestPermissions(patientHomeScreenActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(patientHomeScreenActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.");
                performPendingGeofenceTask();
            } else {
                // Permission denied.

                mPendingGeofenceTask = PendingGeofenceTask.NONE;
            }
        }
    }
    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofencesButtonHandler(View view) {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD;
            requestPermissions();
            return;
        }
        addGeofences();
    }
}
