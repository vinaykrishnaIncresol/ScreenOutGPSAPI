package com.incresol.screenoutgps.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;
import android.widget.Toast;

import com.incresol.screenoutgps.services.MyService;
import com.incresol.screenoutgps.services.SendDataServiceApi;
import com.incresol.screenoutgps.utils.UtilsClass;
import com.incresol.screenoutgps.views.MainActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.LOCATION_SERVICE;

public class MyReceiver extends BroadcastReceiver {

    static Date sendDataDate = null;
    static boolean sendingDataAPI = false;

    @Override
    public void onReceive(Context context, Intent intentfrom) {

        switch (intentfrom.getIntExtra("sendData", -1)) {
            case 1:
//                if(UtilsClass.isMyServiceRunning(SendDataServiceApi.class, context)) {
                    System.out.println("Called Sending Data @@@@@@@");
                    LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                    boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    Location location;
                    int speed;
                    double lat, longi;
                    if (isGPSEnabled) {
                        if (MyService.toFetchDetails != null) {
                            System.out.println("location -> " + MyService.toFetchDetails);
                            System.out.println("lat -> " + MyService.toFetchDetails.getLatitude());
                            System.out.println("longi -> " + MyService.toFetchDetails.getLongitude());
                            System.out.println("speed -> " + MyService.toFetchDetails.getSpeed());

                            speed = (int) (MyService.toFetchDetails.getSpeed() * UtilsClass.metpersecTomilperhou);
                            lat = MyService.toFetchDetails.getLatitude();
                            longi = MyService.toFetchDetails.getLongitude();
                        } else {
                            speed = 0;
                            lat = 0;
                            longi = 0;
                        }
                    } else {
                        speed = 0;
                        lat = 0;
                        longi = 0;
                    }

                    if (speed > SendDataServiceApi.getThreshold(context)) {
                        System.out.println("Current Speed = " + speed + " > Threshold Speed = " + SendDataServiceApi.getThreshold(context));
                        boolean screenlock = SendDataServiceApi.getScreenLock(context);
                        int appscount = SendDataServiceApi.getAppsCOunt(context);
                        if (screenlock) {
                            SendDataServiceApi.isScreenLocked = true;
                            PhoneStateReceiver.kioskmode = true;
                            MainActivity.enableScreenOut();
                            if (appscount != -1 && appscount > 0) {
//                                MainActivity.enableScreenOut();
                                for (int i = 0; i < appscount; i++) {
                                    System.out.println("#### appscount -> " + appscount + " i -> " + i);
                                    if (i == 0) {
                                        if(MainActivity.txt_maps != null) {
                                            MainActivity.txt_maps.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    if (i == 1) {
                                        if(MainActivity.txt_music != null) {
                                            MainActivity.txt_music.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    if (i == 2) {
                                        if(MainActivity.txt_calls != null) {
                                            MainActivity.txt_calls.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    if (i == 3) {
                                        if(MainActivity.txt_test != null) {
                                            MainActivity.txt_test.setVisibility(View.VISIBLE);
                                        }
                                        SharedPreferences sharedPreferences = context.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
                                        String pkgname = sharedPreferences.getString("package_4", null);
                                        if (pkgname != null) {
                                            String[] appname = pkgname.split(".");
                                            String applicationame = appname[appname.length - 1].trim();
                                            MainActivity.txt_test.setText(applicationame);
                                            PackageManager pkgmanager = context.getPackageManager();
                                            try {
                                                Drawable d = context.getPackageManager().getApplicationIcon(pkgname);
                                                MainActivity.txt_test.setBackgroundDrawable(d);
                                            } catch (PackageManager.NameNotFoundException e) {
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("Current Speed = " + speed + " < Threshold Speed = " + SendDataServiceApi.getThreshold(context));
                        SendDataServiceApi.isScreenLocked = false;
                        PhoneStateReceiver.kioskmode = false;
                        if (MainActivity.drawer != null) {
                            MainActivity.disableScreenOut();
                        }
                    }
                    int delaytime = SendDataServiceApi.getDelay(context);
                    System.out.println("delay Sending time 123 -> " + delaytime);
                    if (sendDataDate == null) {
                        System.out.println("delay Sending time 456-> " + delaytime);
                        if (UtilsClass.isInternetConnected(context)) {
                            sendingData(context, speed, lat, longi);
                        }
                        return;
                    } else {
                        Date currentDate = new Date();
                        long diffInMS = currentDate.getTime() - sendDataDate.getTime();
                        long diffInSEC = TimeUnit.MILLISECONDS.toSeconds(diffInMS);
                        long t = delaytime * 1000;
                        System.out.println("delay Sending timeing long -> " + t + "\t diffsec -> " + diffInSEC);
                        if (diffInMS >= t) {
                            System.out.println("delay Sending time 789-> " + delaytime);
                            if (UtilsClass.isInternetConnected(context)) {
                                sendingData(context, speed, lat, longi);
                            }
                        }
                    }
//                }
                break;
            default:
                System.out.println("Myreceiver is calling default");
                break;
        }
    }

    public static void sendingData(final Context context, int speed, double latitude, double longitude) {
        sendingDataAPI = true;
        System.out.println("Called Sending Data 1234");
        final SharedPreferences sharedPreferences;
        sharedPreferences = context.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        String apik = sharedPreferences.getString("API_KEY", null);
        AsyncHttpClient client = new AsyncHttpClient();
        String url = UtilsClass.sendDataApi1 + apik
                + UtilsClass.sendDataApi2 + SendDataServiceApi.getDeviceName() + UtilsClass.sendDataApi3 + SendDataServiceApi.getDeviceIMEI(context) + UtilsClass.sendDataApi4 + SendDataServiceApi.getDeviceName() +
                UtilsClass.sendDataApi5 + speed + UtilsClass.sendDataApi6 + SendDataServiceApi.getMaxSpeed(context) + UtilsClass.sendDataApi7 + latitude + UtilsClass.sendDataApi8 + longitude
                + UtilsClass.sendDataApi9 + SendDataServiceApi.getIsLocked() + UtilsClass.sendDataApi10 + SendDataServiceApi.getDisconnectedStatus(context) + UtilsClass.sendDataApi11 + SendDataServiceApi.getMaxSpeedChangeCount(context);
        System.out.println("delay Sending Url -> " + url);
        sendDataDate = new Date();
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject dataObject = response.getJSONObject("data");
                    String responseCode = dataObject.getString("code");
                    if (201 == Integer.parseInt(responseCode)) {
                        String delaytime = dataObject.getString("delay");
                        String maxSpeed = dataObject.getString("maxSpeed");
                        String threshold = dataObject.getString("Threshold");
                        String screenlock = dataObject.getString("screenlock");
                        String appscount = dataObject.getString("appscount");
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("api_delay", Integer.parseInt(delaytime));
                        editor.putInt("api_maxspeed", Integer.parseInt(maxSpeed));
                        editor.putInt("api_threshold", Integer.parseInt(threshold));
                        editor.putBoolean("api_screenlock", screenlock.equalsIgnoreCase("true") ? true : false);
                        editor.putInt("api_appscount", Integer.parseInt(appscount));
                        JSONArray dataObjectArray_Apps = null;
                        try {
                            dataObjectArray_Apps = response.getJSONArray("apps");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (dataObjectArray_Apps != null && dataObjectArray_Apps.length() > 0) {
                            for (int i = 0; i < dataObjectArray_Apps.length(); i++) {
                                JSONObject arrayObject = dataObjectArray_Apps.getJSONObject(i);
                                System.out.println("##### package -> " + arrayObject.getString("package"));
                                editor.putString("package_" + i, arrayObject.getString("package"));
                            }
                        } else {
                            System.out.println("There are no User Contacts yet!");
                        }
                        editor.commit();
                        sendingDataAPI = false;
//                        Toast.makeText(context, "Callback-> delaytime -> " + delaytime+"\tmaxSpeed -> "+maxSpeed+"\tthreshold -> "+threshold, Toast.LENGTH_SHORT).show();
                    } else {
                        sendingDataAPI = false;
                        System.out.println("Send Data Request Sendin Response was not 201 .... ");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    sendingDataAPI = false;
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                System.out.println("Send Data Request Sendin Entered Into Failure Case ....");
                sendingDataAPI = false;
            }
        });
    }
}
