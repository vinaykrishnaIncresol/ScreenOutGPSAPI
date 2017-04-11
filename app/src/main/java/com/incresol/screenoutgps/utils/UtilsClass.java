package com.incresol.screenoutgps.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.urbanairship.UAirship;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import cz.msebera.android.httpclient.Header;

import static com.incresol.screenoutgps.RegistrationActivity.prgDialog;

public class UtilsClass {
    public static AlertDialog.Builder builder;
    public static AlertDialog alert;
    public static int notificationsCount = 0;
    public static String hostedAddress = "https://api.webtestlabs.com/screenout/";
    public static String generateApiKey = hostedAddress+"verify-user-code/data?code=";
    public static String updateorAddPushnotificationToken1 = hostedAddress+"push-notification-key/";
    public static String updateorAddPushnotificationToken2 = "/data?&type=android&token=";
    public static String homebaseAddorUpdate1 = hostedAddress+"homebase/";
    public static String homebaseAddorUpdate2 = "/data?latitude=";
    public static String homebaseAddorUpdate3 = "&longitude=";
    public static String sendMessage1 = hostedAddress+"comments/";
    public static String sendMessage2 = "/data?email=";
    public static String sendMessage3 = "&comments=";
    public static String pushNotificationsList1 = hostedAddress+"view-notifications/";
    public static String pushNotificationsList2 = "/data?push=";
    public static String markPushNotification1 = hostedAddress+"mark-notification-read/";
    public static String markPushNotification2 = "/data?messageId=1&isRead=yes";
    public static String sendDataApi1 = hostedAddress+"push/";
    public static String sendDataApi2 = "/data?deviceName=";
    public static String sendDataApi3 = "&deviceid=";
    public static String sendDataApi4 = "&deviceType=";
    public static String sendDataApi5 = "&action=Speed%20Change&speed=";
    public static String sendDataApi6 = "&maxspeed=";
    public static String sendDataApi7 = "&latitude=";
    public static String sendDataApi8 = "&longitude=";
    public static String sendDataApi9 = "&islocked=";
    public static String sendDataApi10 = "&disconnectedStatus=";
    public static String sendDataApi11 = "&maxSpeedChangeCount=";
    public static String defsendDataAction = "Speed Change";
    public static String inboundWhitelist = hostedAddress+"inbound_calls/";
    public static String outboundWhitelist = hostedAddress+"outbound_calls/";
    public static int defsendDataSpeed = 32;
    public static int defsendDataMaxSpeed = 55;
    public static boolean defsendDataIsLocked = true;
    public static boolean defsendDataDisconnectedStatus = true;
    public static int defsendDataMaxSpeedChangeCount = 3;
    public static int defaultDelaySeconds = 10;
    public static int defaultThreshold = 3;
    public static double metpersecTomilperhou = 2.23694;
    public static String markPush1 = hostedAddress+"mark-notification-read/";
    public static String markPush2 = "/data?messageId=";
    public static String markPush3 = "&isRead=yes";

    public static boolean isInternetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void showSnackBar(Context context, CoordinatorLayout coordinatorLayout, String content) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, content, Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    public static void buildAlertMessageNoGps(final Context context) {
        builder = new AlertDialog.Builder(context);
        builder.setMessage("Your GPS seems to be disabled, Please Enable it.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.dismiss();
                        context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.dismiss();
                        ((Activity) context).finish();
                    }
                });
        alert = builder.create();
        if (alert != null && !alert.isShowing()) {
            alert.show();
        }
    }

    public static void sendChannelId(final Context context, final CoordinatorLayout coordinatorLayout) {
        SharedPreferences sharedPreferences;
        sharedPreferences = context.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        String apik = sharedPreferences.getString("API_KEY", null);
        if (apik != null) {
            String channelId = UAirship.shared().getPushManager().getChannelId();
            System.out.println("channel Id -> ************* " + channelId);
            if (channelId != null) {
                AsyncHttpClient client = new AsyncHttpClient();
                String url = UtilsClass.updateorAddPushnotificationToken1 + apik +
                        UtilsClass.updateorAddPushnotificationToken2 + channelId;
                client.get(url, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                            super.onSuccess(statusCode, headers, response);
                        System.out.println(response);
                        try {
                            JSONObject dataObject = response.getJSONObject("data");
                            String code = dataObject.getString("code");
                            String message = dataObject.getString("message");
                            String status = dataObject.getString("status");
                            if (201 == Integer.parseInt(code)) {
                                showSnackBar(context, coordinatorLayout, message);
                            } else {
                                showSnackBar(context, coordinatorLayout, message);
                            }
                        } catch (JSONException e) {
                            if (prgDialog.isShowing()) {
                                prgDialog.dismiss();
                            }
                            e.printStackTrace();
                            showSnackBar(context, coordinatorLayout, "Exception on Success");
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                            super.onFailure(statusCode, headers, throwable, errorResponse);
                        System.out.println(errorResponse);
                        try {
                            if (errorResponse != null) {
                                JSONObject dataObject = errorResponse.getJSONObject("data");
                                String code = dataObject.getString("code");
                                String message = dataObject.getString("message");
                                String status = dataObject.getString("status");
                                showSnackBar(context, coordinatorLayout, message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showSnackBar(context, coordinatorLayout, "Exception on Error");
                        }
                    }
                });
            }
        }
    }

    public static boolean isInBackground(Context context) {
        System.out.println("IsinBackground kiosk Called !!!!!!!!!!!");

        String currentApp = "NULL";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }
        Log.e(context.getClass().getSimpleName(), "Current App kiosk in foreground is: " + currentApp);


        // || currentApp.equalsIgnoreCase("com.google.android.music")
        // || currentApp.contains("com.google.android.music")//
        if (currentApp.equalsIgnoreCase(context.getPackageName())
                || currentApp.equalsIgnoreCase("com.google.android.apps.maps")
                || currentApp.contains("com.android.incallui")
                || currentApp.equalsIgnoreCase("com.google.android.music")
                || currentApp.contains("com.google.android.music")
                || currentApp.contains("com.android.dialer")) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                System.out.println("Services -> " + service.service.getClassName());
                return true;
            }
        }
        return false;
    }

    public static int readNotificationsCount(final Context context) {
        SharedPreferences sharedPreferences;
        sharedPreferences = context.getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        String apik = sharedPreferences.getString("API_KEY", null);
        AsyncHttpClient client = new SyncHttpClient();
        String url = UtilsClass.pushNotificationsList1 + apik + UtilsClass.pushNotificationsList2;
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
                try {
                    JSONObject dataObject = response.getJSONObject("data");
                    String result = dataObject.getString("result");
                    if (result.equalsIgnoreCase("succes")) {
                        String unreadCount = dataObject.getString("unread");
                        notificationsCount = Integer.parseInt(unreadCount);
                    } else {
                        System.out.println("Notification Count got error response in onSuccess()");
                        notificationsCount = 0;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
                System.out.println("Notification Count got into onFailure()");
                notificationsCount = 0;
            }
        });
        System.out.println("readNotificationsCount --> " + notificationsCount);
        return notificationsCount;
    }
}
