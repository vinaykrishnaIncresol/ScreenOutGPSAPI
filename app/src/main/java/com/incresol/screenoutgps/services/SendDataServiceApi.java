package com.incresol.screenoutgps.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.incresol.screenoutgps.utils.UtilsClass;

import java.util.Date;

/**
 * Created by Incresol on 11-Jan-17.
 */

public class SendDataServiceApi extends Service {

    static SharedPreferences sharedPreferences;
    public static boolean isScreenLocked = false;
    public static Date sendDataDate;
    static Thread sendDataThread;
    static boolean interrupted = false;
    Context contextSendDataServiceApi;
    Handler handler;

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        System.out.println("** SendDataServiceApi ** onTaskRemoved()");
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePI);

    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public void onCreate() {
        handler = new Handler();
        super.onCreate();
        System.out.println("** SendDataServiceApi ** onCreate()");
        contextSendDataServiceApi = this;
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        sendDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("sendDataThread service is started !!!!");
                do {
                    if (!interrupted) {
                        sendDataAPI(contextSendDataServiceApi);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("sendDataThread Exception!!!!!");
                    }
                } while (true);
            }
        });
        if (!sendDataThread.isAlive()) {
            sendDataThread.start();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("SendDataServiceAPI -> Destroy ()");
        sendDataThread.interrupt();
        interrupted = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("SendDataServiceApi -> ", " ** onStartCommand **");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("** SendDataServiceApi ** onBind()");

        return null;
    }

    public static int getMaxSpeed(Context context) {
        if (UtilsClass.isInternetConnected(context)) {
            sharedPreferences = context.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
            int dup_val = sharedPreferences.getInt("api_maxspeed", -1);
            if (dup_val == -1) {
                dup_val = UtilsClass.defsendDataMaxSpeed;
                System.out.println("delay Sending getMaxspeed sharedpreference -> " + dup_val);
                return dup_val;
            } else {
                System.out.println("delay Sending getMaxspeed  -> " + dup_val);
                return dup_val;
            }
        } else {
            return UtilsClass.defsendDataMaxSpeed;
        }
    }

    public static int getThreshold(Context context) {
        if (UtilsClass.isInternetConnected(context)) {
            sharedPreferences = context.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
            int dup_val = sharedPreferences.getInt("api_threshold", -1);
            if (dup_val == -1) {
                dup_val = UtilsClass.defaultThreshold;
                System.out.println("delay Sending getThreshold sharedpreference -> " + dup_val);
                return dup_val;
            } else {
                System.out.println("delay Sending getThreshold  -> " + dup_val);
                return dup_val;
            }
        } else {
            return UtilsClass.defaultThreshold;
        }
    }


    public static int getDelay(Context context) {
        if (UtilsClass.isInternetConnected(context)) {
            sharedPreferences = context.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
            int dup_val = sharedPreferences.getInt("api_delay", -1);
            if (dup_val == -1) {
                dup_val = UtilsClass.defaultDelaySeconds;
                System.out.println("delay Sending getDelay sharedpreference -> " + dup_val);
                return dup_val;
            } else {
                System.out.println("delay Sending getDelay -> " + dup_val);
                return dup_val;
            }
        } else {
            return UtilsClass.defaultDelaySeconds;
        }
    }

    public static boolean getScreenLock(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        boolean screenlock = sharedPreferences.getBoolean("api_screenlock", true);
        return screenlock;
    }

    public static int getAppsCOunt(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        int screenlock = sharedPreferences.getInt("api_appscount", -1);
        return screenlock;
    }

    public static boolean getIsLocked() {
        return isScreenLocked;
    }

    public static boolean getDisconnectedStatus(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isGPSEnabled;
    }


    public static int getMaxSpeedChangeCount(Context context) {
        sharedPreferences = context.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        int dup_val = sharedPreferences.getInt("api_maxspeedchangecount", -1);
        if (dup_val == -1) {
            dup_val = UtilsClass.defsendDataMaxSpeedChangeCount;
            return dup_val;
        } else {
            return dup_val;
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String getDeviceId(Context context) {
        String device_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return device_id;
    }

    public static String getDeviceIMEI(Context context) {
        TelephonyManager mngr = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        return mngr.getDeviceId();
    }

    public void sendDataAPI(final Context context) {
        if (UtilsClass.isInternetConnected(context)) {
            Intent intent = new Intent("com.incresol.screenoutgps.GET_STATUS_INTENT");
            intent.putExtra("sendData", 1);
            sendBroadcast(intent);

        } else {
            System.out.println("No Internet to SEND DATA API !!!!");
        }
    }
}
