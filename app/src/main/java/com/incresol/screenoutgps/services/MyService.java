package com.incresol.screenoutgps.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.incresol.screenoutgps.utils.UtilsClass;
import com.incresol.screenoutgps.views.MainActivity;


/**
 * Created by Incresol on 12/16/2016.
 */

public class MyService extends Service {
    public static final String BROADCAST_ACTION = "MyService_GPS";
    private static final int ONE_SECOND = 1000;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    Context context;
    Intent intent;
    int counter = 0;
    public static Location toFetchDetails;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        System.out.println("** MyService ** onStartCommand()");
        context = this;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return START_STICKY;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ONE_SECOND, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, ONE_SECOND, 0, listener);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        System.out.println("** MyService ** onTaskRemoved()");
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
        super.onCreate();
        System.out.println("** MyService ** onCreate()");
        intent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("** MyService ** onBind()");

        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > ONE_SECOND;
        boolean isSignificantlyOlder = timeDelta < -ONE_SECOND;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        System.out.println("** MyService ** onDestroy()");
        Log.v("STOP_SERVICE", "DONE");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(listener);
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }


    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {
            Log.i("***************", "Location changed");
            if (isBetterLocation(loc, previousBestLocation)) {
                intent.putExtra("Latitude", loc.getLatitude());
                intent.putExtra("Longitude", loc.getLongitude());
                intent.putExtra("Provider", loc.getProvider());
                sendBroadcast(intent);
                toFetchDetails = loc;
                if (MainActivity.txt_lat != null) {
                    MainActivity.txt_lat.setText("" + loc.getLatitude());
                    MainActivity.txt_long.setText("" + loc.getLongitude());
                    MainActivity.txt_speed.setText("" + (loc.getSpeed() * UtilsClass.metpersecTomilperhou));
                }
            }
        }

        public void onProviderDisabled(String provider) {
//            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
            System.out.println("Gps Disabled");
            try {
//                stopService(new Intent(getApplicationContext(), MyService.class));
//                stopService(new Intent(getApplicationContext(), SendDataServiceApi.class));
//                stopService(new Intent(getApplicationContext(), KioskService.class));
                SendDataServiceApi.interrupted = true;
                if (MainActivity.txt_lat != null) {
                    MainActivity.txt_lat.setText("" + 0.0);
                    MainActivity.txt_long.setText("" + 0.0);
                    MainActivity.txt_speed.setText("" + 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void onProviderEnabled(String provider) {
//            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
            System.out.println("Gps Enabled");
            try {
                startService(new Intent(getApplicationContext(), MyService.class));
                startService(new Intent(getApplicationContext(), SendDataServiceApi.class));
                startService(new Intent(getApplicationContext(), KioskService.class));
                if(SendDataServiceApi.interrupted){
                    SendDataServiceApi.interrupted = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("***** provider -> "+provider+"; status -> "+status+"; extras -> "+extras);
        }

    }
}
