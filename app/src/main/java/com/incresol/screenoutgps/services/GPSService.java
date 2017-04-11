package com.incresol.screenoutgps.services;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.incresol.screenoutgps.utils.UtilsClass;
import com.incresol.screenoutgps.views.MainActivity;

public class GPSService extends Service {
    private static final String TAG = "GPSService";
    Context context;
    public static boolean isGPSAvailable;
    static Thread checkGPSAvailability;
    LocationManager locationManager;


    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("** GPSService ** onBind()");

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePI);

    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        context = this;
        checkGPSAvailability = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("sendDataThread service is started !!!!");
                do {
//                    if (!checkGPSAvailability.isInterrupted()) {
                        checkGPS();
//                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("sendDataThread Exception!!!!!");
                    }
                } while (true);
            }
        });
        if (!checkGPSAvailability.isAlive()) {
            checkGPSAvailability.start();
        }
    }

    public void checkGPS() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isGPSAvailable = false;
        } else {
            isGPSAvailable = true;
            if (!UtilsClass.isMyServiceRunning(MyService.class, context)) {
                startService(new Intent(context, MyService.class));
            }
            if (!UtilsClass.isMyServiceRunning(SendDataServiceApi.class, context)) {
                startService(new Intent(context, SendDataServiceApi.class));
            }
            if (!UtilsClass.isMyServiceRunning(KioskService.class, context)) {
                startService(new Intent(context, KioskService.class));
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }
}
