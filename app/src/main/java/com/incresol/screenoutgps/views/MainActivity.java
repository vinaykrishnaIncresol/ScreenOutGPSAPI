package com.incresol.screenoutgps.views;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.incresol.screenoutgps.R;
import com.incresol.screenoutgps.services.GPSService;
import com.incresol.screenoutgps.services.KioskService;
import com.incresol.screenoutgps.services.MyService;
import com.incresol.screenoutgps.services.SendDataServiceApi;
import com.incresol.screenoutgps.utils.UtilsClass;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static DrawerLayout drawer;
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;
    public static NavigationView navigationView;
    boolean doubleBackTOExitPressedOnce = false;
    public static CoordinatorLayout mCoordinatorLayout;
    public static ImageView openMenuImage;
    BroadcastReceiver statusReceiverData;
    LocationManager locationManager;
    public static LinearLayout appsLayout;
    public static TextView txt_lat, txt_long, txt_speed, badger, txt_maps, txt_calls, txt_music, txt_test;
    Button sendLocation;
    //    GPSTracker gpsTracker;
    private static String[] Permissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.GET_TASKS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.WAKE_LOCK};
    private static int LOCATION_REQUEST = 123;
    public static Timer timerTask;
    public static ImageView img_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("************ ON CREATE ***********");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        txt_lat = (TextView) findViewById(R.id.txt_latitude);
        txt_long = (TextView) findViewById(R.id.txt_longitude);
        txt_speed = (TextView) findViewById(R.id.txt_speed);
        img_logo = (ImageView) findViewById(R.id.img_logo);
        appsLayout = (LinearLayout) findViewById(R.id.hidingthe_apps);
        sendLocation = (Button) findViewById(R.id.btn_send_location);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        sendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(Gravity.LEFT);
                if (MyService.toFetchDetails != null) {
                    if (UtilsClass.isInternetConnected(MainActivity.this)) {
                        SharedPreferences sharedPreferences;
                        sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
                        String apik = sharedPreferences.getString("API_KEY", null);
                        AsyncHttpClient client = new AsyncHttpClient();
                        String url = UtilsClass.homebaseAddorUpdate1 + apik +
                                UtilsClass.homebaseAddorUpdate2 + MyService.toFetchDetails.getLatitude() + UtilsClass.homebaseAddorUpdate3 + MyService.toFetchDetails.getLongitude();
                        client.get(url, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                        super.onSuccess(statusCode, headers, response);
                                System.out.println(response);
                                try {
                                    JSONObject dataObject = response.getJSONObject("data");
                                    String responseCode = dataObject.getString("code");
                                    String message = dataObject.getString("message");
//                            String type = dataObject.getString("type");
                                    if (201 == Integer.parseInt(responseCode)) {
                                        UtilsClass.showSnackBar(MainActivity.this, mCoordinatorLayout, "message:- " + message);
                                    } else {
                                        UtilsClass.showSnackBar(MainActivity.this, mCoordinatorLayout, "message:- " + message);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                        super.onFailure(statusCode, headers, throwable, errorResponse);
                                UtilsClass.showSnackBar(MainActivity.this, mCoordinatorLayout, "LocationUpdate Failed");
                            }
                        });
                    } else {
                        UtilsClass.showSnackBar(MainActivity.this, mCoordinatorLayout, "No Internet Access");
                    }
                } else {
                    UtilsClass.showSnackBar(MainActivity.this, mCoordinatorLayout, "Coordinates not yet found!");
                }
            }
        });

        toggle = new ActionBarDrawerToggle(
                MainActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        openMenuImage = (ImageView) toolbar.findViewById(R.id.openMenu);
        openMenuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("MENU CLICKED **************************************");
                drawer.openDrawer(Gravity.LEFT);
            }
        });
        badger = (TextView) toolbar.findViewById(R.id.badger);
        toggle.setDrawerIndicatorEnabled(false);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        txt_calls = (TextView) findViewById(R.id.gps_txt_outboundicon);
        txt_calls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homebaseIntent = new Intent(getApplicationContext(), OutgoingCallsActivity.class);
                startActivity(homebaseIntent);
            }
        });
        txt_maps = (TextView) findViewById(R.id.gps_txt_maps);
        txt_maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent maps_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"));
                maps_intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(maps_intent);
            }
        });
        txt_music = (TextView) findViewById(R.id.gps_txt_music);
        txt_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pkgname = "com.google.android.music";
                PackageManager pkgmanager = getPackageManager();
                try {
                    Intent intent = pkgmanager.getLaunchIntentForPackage(pkgname);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast toast = Toast.makeText(MainActivity.this, "Google Play Music application is not installed", Toast.LENGTH_SHORT);
                    TextView toastTextview = (TextView) toast.getView().findViewById(android.R.id.message);
                    toastTextview.setTextColor(Color.RED);
                    toast.show();
                }
            }
        });
        txt_test = (TextView) findViewById(R.id.gps_txt_test);
        txt_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
                String pkgname = sharedPreferences.getString("package_4", null);
                if (pkgname != null) {
                    String[] appname = pkgname.split(".");
                    String applicationame = appname[appname.length-1].trim();
                    txt_test.setText(applicationame);
                    PackageManager pkgmanager = getPackageManager();
                    try {
                        Intent intent = pkgmanager.getLaunchIntentForPackage(pkgname);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, applicationame+" application is not installed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        requestPermission();

//        if (!UtilsClass.isMyServiceRunning(MyService.class, MainActivity.this)) {
        startService(new Intent(MainActivity.this, MyService.class));
//        }
//        if (!UtilsClass.isMyServiceRunning(SendDataServiceApi.class, MainActivity.this)) {
        startService(new Intent(MainActivity.this, SendDataServiceApi.class));
//        }
//        if (!UtilsClass.isMyServiceRunning(KioskService.class, MainActivity.this)) {
        startService(new Intent(MainActivity.this, KioskService.class));
//        }
        startService(new Intent(MainActivity.this, GPSService.class));

        UtilsClass.sendChannelId(MainActivity.this, mCoordinatorLayout);
        timerTask = new Timer();
        timerTask.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (UtilsClass.isInternetConnected(MainActivity.this)) {
                    final int notificationCount = UtilsClass.readNotificationsCount(MainActivity.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout view = (LinearLayout) navigationView.getMenu().findItem(R.id.nav_notifications).getActionView();
                            final TextView txtView = (TextView) view.findViewById(R.id.txtNotificationCounter);
                            if (notificationCount != 0) {
                                txtView.setVisibility(View.VISIBLE);
                                txtView.setText("" + notificationCount);
                                badger.setVisibility(View.VISIBLE);
                                badger.setText("" + notificationCount);
                            } else {
                                txtView.setVisibility(View.INVISIBLE);
                                badger.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                } else {
                    System.out.println("There is no internet to fetch unread notifications count!");
                }
            }
        }, 0, 10000);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            UtilsClass.buildAlertMessageNoGps(MainActivity.this);
        }
    }


    public void requestPermission() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            System.out.println("checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    + "\ncheckCallingOrSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    + "\ncheckCallingOrSelfPermission(android.Manifest.permission.INTERNET) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.INTERNET)
                    + "\ncheckCallingOrSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE));

            if (checkCallingOrSelfPermission(
                    Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(
                    Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(
                    Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(
                    Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(
                    Manifest.permission.GET_TASKS) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(
                    Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(
                    Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(
                    Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(
                    Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(Permissions, 123);
            }
//            if (!UtilsClass.isMyServiceRunning(MyService.class, MainActivity.this)) {
            startService(new Intent(MainActivity.this, MyService.class));
//            }
//            if (!UtilsClass.isMyServiceRunning(SendDataServiceApi.class, MainActivity.this)) {
            startService(new Intent(MainActivity.this, SendDataServiceApi.class));
//            }
//            if (!UtilsClass.isMyServiceRunning(KioskService.class, MainActivity.this)) {
            startService(new Intent(MainActivity.this, KioskService.class));
//            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions,grantResults);
        switch (requestCode) {
            case 123:
                for (int i = 0; i < grantResults.length; i++) {
                    System.out.println("Permission -> " + permissions[i] + "was -> " + grantResults[i]);
                    if (permissions[i].equalsIgnoreCase("android.permission.ACCESS_COARSE_LOCATION")
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                        if (!UtilsClass.isMyServiceRunning(MyService.class, MainActivity.this)) {
                        startService(new Intent(MainActivity.this, MyService.class));
//                        }
//                        if (!UtilsClass.isMyServiceRunning(SendDataServiceApi.class, MainActivity.this)) {
                        startService(new Intent(MainActivity.this, SendDataServiceApi.class));
//                        }
//                        if (!UtilsClass.isMyServiceRunning(KioskService.class, MainActivity.this)) {
                        startService(new Intent(MainActivity.this, KioskService.class));
//                        }
                    } else if (permissions[i].equalsIgnoreCase("android.permission.ACCESS_COARSE_LOCATION")
                            && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(Permissions, LOCATION_REQUEST);
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    public static void enableScreenOut() {
        if (drawer != null) {
            MainActivity.openMenuImage.setVisibility(View.INVISIBLE);
            MainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            MainActivity.img_logo.setImageResource(R.drawable.logo_fade);
            badger.setVisibility(View.INVISIBLE);
            appsLayout.setVisibility(View.VISIBLE);
        }
    }

    public static void disableScreenOut() {
        if (drawer != null) {
            MainActivity.openMenuImage.setVisibility(View.VISIBLE);
            MainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            MainActivity.img_logo.setImageResource(R.drawable.logo);
            appsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("************ ON RESUME ***********");
        if (UtilsClass.alert != null && UtilsClass.alert.isShowing()) {
            UtilsClass.alert.dismiss();
        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            UtilsClass.buildAlertMessageNoGps(MainActivity.this);
        }
//        if (!UtilsClass.isMyServiceRunning(MyService.class, MainActivity.this)) {
        startService(new Intent(MainActivity.this, MyService.class));
//        }
//        if (!UtilsClass.isMyServiceRunning(SendDataServiceApi.class, MainActivity.this)) {
        startService(new Intent(MainActivity.this, SendDataServiceApi.class));
//        }
//        if (!UtilsClass.isMyServiceRunning(KioskService.class, MainActivity.this)) {
        startService(new Intent(MainActivity.this, KioskService.class));
//        }
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackTOExitPressedOnce) {
                super.onBackPressed();
            } else {
                doubleBackTOExitPressedOnce = true;
                Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "PRESS AGAIN TO EXIT", Snackbar.LENGTH_SHORT);
                View view = snackbar.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.YELLOW);
                snackbar.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackTOExitPressedOnce = false;
                    }
                }, 2000);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
//            Intent notificationsIntent = new Intent(getApplicationContext(),MainActivity.class);
//            startActivity(notificationsIntent);
        } else if (id == R.id.nav_notifications) {
            Intent notificationsIntent = new Intent(getApplicationContext(), NotificationsActivity.class);
            startActivity(notificationsIntent);
        } else if (id == R.id.nav_support) {
            Intent supportIntent = new Intent(getApplicationContext(), SupportActivity.class);
            startActivity(supportIntent);
        } else if (id == R.id.nav_whitelistInboundsettings) {
            Intent whiteListInboundIntent = new Intent(getApplicationContext(), InboundWhiteListSyncActivity.class);
            startActivity(whiteListInboundIntent);
        } else if (id == R.id.nav_whitelistOutboundsettings) {
            Intent homebaseIntent = new Intent(getApplicationContext(), OutboundWhiteListSyncActivity.class);
            startActivity(homebaseIntent);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
