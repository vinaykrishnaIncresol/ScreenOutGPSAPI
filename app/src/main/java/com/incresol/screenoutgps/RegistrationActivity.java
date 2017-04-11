package com.incresol.screenoutgps;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.incresol.screenoutgps.utils.UtilsClass;
import com.incresol.screenoutgps.views.MainActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class RegistrationActivity extends Activity {
    EditText edtRegistrationCode;
    Button btnRegister;
    public static ProgressDialog prgDialog;
    public static CoordinatorLayout coordinatorLayout;
    public static SharedPreferences sharedPreferences;
    static Context context;
    AlertDialog usageStatAlert;
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
    private static int PERMISSIONS_REQUEST = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        String apik = sharedPreferences.getString("API_KEY", null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean usageStat = checkUsagePermission();
            if (!usageStat) {
                buildAlertUsageStatPermission();
            } else {
                requestPermission();
            }
        }
        if (apik == null) {
            setContentView(R.layout.activity_registration);
            coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
            edtRegistrationCode = (EditText) findViewById(R.id.edtRegistrationCode);
            btnRegister = (Button) findViewById(R.id.btnRegister);

            prgDialog = new ProgressDialog(this);
            prgDialog.setMessage("Please wait...");
            prgDialog.setCancelable(false);
            context = getApplicationContext();

            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (UtilsClass.isInternetConnected(RegistrationActivity.this)) {
                        if (edtRegistrationCode.getText().toString().trim().length() == 8) {
                            edtRegistrationCode.setError(null);
                            prgDialog.show();
                            AsyncHttpClient client = new AsyncHttpClient();
                            String url = UtilsClass.generateApiKey + edtRegistrationCode.getText().toString();
                            client.get(url, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                            super.onSuccess(statusCode, headers, response);
                                    System.out.println(response);
                                    try {
                                        JSONObject dataObject = response.getJSONObject("data");
                                        String responseCode = dataObject.getString("responseCode");
                                        String message = dataObject.getString("message");
                                        String description = dataObject.getString("description");
                                        if (201 == Integer.parseInt(responseCode)) {
                                            if (prgDialog.isShowing()) {
                                                prgDialog.dismiss();
                                            }
                                            String apikey = dataObject.getString("apikey");
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("API_KEY", apikey);
                                            editor.commit();
                                            UtilsClass.showSnackBar(context, coordinatorLayout, "API key:- " + apikey);
                                            UtilsClass.sendChannelId(context, coordinatorLayout);
                                            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(mainIntent);
                                            finish();
                                        } else {
                                            if (prgDialog.isShowing()) {
                                                prgDialog.dismiss();
                                            }
                                            UtilsClass.showSnackBar(context, coordinatorLayout, description);
                                        }
                                    } catch (JSONException e) {
                                        if (prgDialog.isShowing()) {
                                            prgDialog.dismiss();
                                        }
                                        e.printStackTrace();
                                        UtilsClass.showSnackBar(context, coordinatorLayout, "Exception on Success");
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                            super.onFailure(statusCode, headers, throwable, errorResponse);
                                    System.out.println(errorResponse);
                                    try {
                                        if (errorResponse != null) {
                                            JSONObject dataObject = errorResponse.getJSONObject("data");
                                            String responseCode = dataObject.getString("responseCode");
                                            String message = dataObject.getString("message");
                                            String description = dataObject.getString("description");
                                            if (prgDialog.isShowing()) {
                                                prgDialog.dismiss();
                                            }
                                            UtilsClass.showSnackBar(context, coordinatorLayout, description);
                                        } else {
                                            if (prgDialog.isShowing()) {
                                                prgDialog.dismiss();
                                            }
                                            UtilsClass.showSnackBar(context, coordinatorLayout, "No Internet Connection");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        if (prgDialog.isShowing()) {
                                            prgDialog.dismiss();
                                        }
                                        UtilsClass.showSnackBar(context, coordinatorLayout, "Exception on Error");
                                    }
                                }
                            });
                        } else {
                            edtRegistrationCode.setError("Enter Valid Code!");
                        }
                    } else {
                        UtilsClass.showSnackBar(context, coordinatorLayout, "No Internet");
                    }
                }
            });

        } else {
            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            System.out.println("checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.INTERNET) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.INTERNET)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.WAKE_LOCK) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.WAKE_LOCK)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.EXPAND_STATUS_BAR) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.EXPAND_STATUS_BAR)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.WRITE_SETTINGS) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.WRITE_SETTINGS)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.CALL_PHONE) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.CALL_PHONE)
                    + "\ncheckCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) -> "
                    + checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS));
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
                requestPermissions(Permissions, PERMISSIONS_REQUEST);
            } else {
                System.out.println("All the required permissions are granted!!!");
            }
        } else {
            System.out.println("requestPermissions are not required, as this device is < Marshmallow");
        }
    }

    public boolean checkUsagePermission() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid,
                    applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void buildAlertUsageStatPermission() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Enable permission for Screen Out Gps API Application").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog,
                                        @SuppressWarnings("unused") final int id) {
                        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 1234);
                    }
                }).setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                finish();
            }
        });
        usageStatAlert = builder.create();
        usageStatAlert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234) {
            boolean usageStats = checkUsagePermission();
            if (!usageStats) {
                buildAlertUsageStatPermission();
            } else {
                requestPermission();
            }
        }
    }
}
