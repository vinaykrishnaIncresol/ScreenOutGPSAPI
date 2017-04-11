package com.incresol.screenoutgps.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.incresol.screenoutgps.services.GPSTracker;
import com.incresol.screenoutgps.R;
import com.incresol.screenoutgps.utils.UtilsClass;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class HomebaseActivity extends AppCompatActivity {

    GPSTracker gpsTracker;
    EditText edtLat, edtLong;
    Button btnLocationSend;
    CoordinatorLayout coordinate_activity_homebase;
    TextView txt_title;
    ImageView img_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homebase);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_back_title_layout);
        txt_title = (TextView) findViewById(R.id.myTitle);
        txt_title.setText("Homebase");
        gpsTracker = new GPSTracker(HomebaseActivity.this);
        img_back = (ImageView)findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.navigationView.getMenu().getItem(0).setChecked(true);
                finish();
            }
        });
        edtLat = (EditText) findViewById(R.id.edt_lat);
        edtLong = (EditText) findViewById(R.id.edt_long);
        coordinate_activity_homebase = (CoordinatorLayout)findViewById(R.id.coordinate_activity_homebase);
        btnLocationSend = (Button) findViewById(R.id.btn_send_location);
        edtLat.setText("" + gpsTracker.getLatitude());
        edtLong.setText("" + gpsTracker.getLongitude());
        btnLocationSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(UtilsClass.isInternetConnected(HomebaseActivity.this)){
                SharedPreferences sharedPreferences;
                sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
                String apik = sharedPreferences.getString("API_KEY", null);
                AsyncHttpClient client = new AsyncHttpClient();
                String url = UtilsClass.homebaseAddorUpdate1 + apik +
                        UtilsClass.homebaseAddorUpdate2 + gpsTracker.getLatitude() + UtilsClass.homebaseAddorUpdate3 + gpsTracker.getLongitude();
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
                                UtilsClass.showSnackBar(HomebaseActivity.this, coordinate_activity_homebase, "message:- " + message);
                            } else {
                                UtilsClass.showSnackBar(HomebaseActivity.this, coordinate_activity_homebase, "message:- " + message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        UtilsClass.showSnackBar(HomebaseActivity.this,coordinate_activity_homebase,"LocationUpdate Failed");
                    }
                });
            }else{
                UtilsClass.showSnackBar(HomebaseActivity.this,coordinate_activity_homebase,"No Internet Access");
            }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainActivity.navigationView.getMenu().getItem(0).setChecked(true);
    }
}
