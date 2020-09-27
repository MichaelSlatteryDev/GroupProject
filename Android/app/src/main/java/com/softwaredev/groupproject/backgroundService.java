package com.softwaredev.groupproject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class backgroundService extends Service {

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    ArrayList<String> savedDates;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        savedDates = (ArrayList<String>) intent.getExtras().get("SAVED_DATES");
        Timer timer = new Timer ();
        TimerTask hourlyTask = new TimerTask () {
            @Override
            public void run() {

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd / MM / yyyy");
                String formattedDate = df.format(c.getTime());

                String tempArray[] = formattedDate.split(" / ");
                int tomorrow = Integer.parseInt(tempArray[0]);
                for (int i = 0; i < savedDates.size(); i++) {

                    String currentDate[] = savedDates.get(i).split(" / ");
                    String splitStart[] = currentDate[0].split(": ");
                    int dateDay = Integer.parseInt(splitStart[1]);

                    System.out.println("Tomorrow: " + tomorrow + "\nToday: " + dateDay);

                    if (dateDay == tomorrow+1) {
                        mBuilder = new NotificationCompat.Builder(getApplicationContext());
                        mBuilder.setSmallIcon(R.drawable.calendar);
                        mBuilder.setContentTitle("You have plans tomorrow!");
                        mBuilder.setContentText("Tap here to look");
                        Intent resultIntent = new Intent(getApplicationContext(),
                                CalendarActivity.class);

                        PendingIntent resultPendingIntent = PendingIntent.getActivity
                                (getApplicationContext(), 0,
                                        resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        mBuilder.setContentIntent(resultPendingIntent);

                        mNotificationManager =
                                (NotificationManager) getSystemService
                                        (getApplicationContext().NOTIFICATION_SERVICE);
                        mNotificationManager.notify(0, mBuilder.build());
                        break;
                    }else if (dateDay == tomorrow) {
                        mBuilder = new NotificationCompat.Builder(getApplicationContext());
                        mBuilder.setSmallIcon(R.drawable.calendar);
                        mBuilder.setContentTitle("You have plans today!");
                        mBuilder.setContentText("Tap here to look");
                        Intent resultIntent = new Intent(getApplicationContext(),
                                CalendarActivity.class);

                        PendingIntent resultPendingIntent = PendingIntent.getActivity
                                (getApplicationContext(), 0,
                                        resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        mBuilder.setContentIntent(resultPendingIntent);

                        mNotificationManager =
                                (NotificationManager) getSystemService
                                        (getApplicationContext().NOTIFICATION_SERVICE);
                        mNotificationManager.notify(0, mBuilder.build());
                        break;
                    }

                }
            }
        };

        timer.schedule (hourlyTask, 0l, 1000*60*60);

        return Service.START_STICKY;
    }
}
