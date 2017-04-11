package com.incresol.screenoutgps.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.incresol.screenoutgps.utils.UtilsClass;
import com.incresol.screenoutgps.views.MainActivity;

/**
 * Created by Incresol on 11-Jan-17.
 */

public class KioskService extends Service {

    private static final String TAG = KioskService.class.getSimpleName();

    private Thread t = null;
    private Context ctx = null;
    private boolean running = false;

    @Override
    public void onCreate() {
        running = true;
        ctx = this;

        // start a thread that periodically checks if your app is in the
        // foreground
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    handleKioskMode();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "Thread interrupted: 'KioskService'");
                    }
                } while (running);
                stopSelf();
            }
        });

        t.start();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping service 'KioskService'");
        System.out.println("KioskService -> onDestroy()");
        running = false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service 'KioskService'");
        System.out.println("KioskService -> onStartCommand()");
        return START_STICKY;
    }

    private void handleKioskMode() {
        // is Kiosk Mode active?
        System.out.println("Kiosk mode Handlekioskmode");
        if (isKioskModeActive()) {
            // is App in background?
            if (isInBackground()) {
                System.out.println("Kiosk mode restoreapp");
                restoreApp(); // restore!
            }
        }
    }

    private boolean isInBackground() {
        System.out.println("Kiosk mode isinbackground -> "+UtilsClass.isInBackground(ctx));
        return UtilsClass.isInBackground(ctx);
    }

    private void restoreApp() {
        // Restart activity
        Intent i;
        i = new Intent(ctx, MainActivity.class);
        i.putExtra("showfullscreen", true);
        i.putExtra("mode", true);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        ctx.startActivity(i);
    }

    public boolean isKioskModeActive() {
        System.out.println("Kiosk mode iskioskmodeactive -> "+SendDataServiceApi.isScreenLocked );
        return SendDataServiceApi.isScreenLocked;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        System.out.println("** KioskService ** onTaskRemoved()");
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePI);

    }
}
