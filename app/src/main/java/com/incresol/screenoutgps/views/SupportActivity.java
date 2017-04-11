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

import com.incresol.screenoutgps.R;
import com.incresol.screenoutgps.utils.UtilsClass;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SupportActivity extends AppCompatActivity {

    TextView txt_title;
    ImageView img_cancel;
    EditText edt_name, edt_email, edt_feedback;
    Button btn_submit;
    public static SharedPreferences sharedPreferences;
    CoordinatorLayout coordinatorLayoutSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_title_layout);
        txt_title = (TextView) findViewById(R.id.myTitle);
        txt_title.setText("Support");
        coordinatorLayoutSupport = (CoordinatorLayout)findViewById(R.id.coordinate_activity_support);
        img_cancel = (ImageView) findViewById(R.id.img_cancel);
        edt_name = (EditText) findViewById(R.id.edt_support_name);
        edt_email = (EditText) findViewById(R.id.edt_support_email);
        edt_feedback = (EditText) findViewById(R.id.edt_support_feedback);
        btn_submit = (Button) findViewById(R.id.btn_support_send);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_name.getText().toString().length() != 0) {
                    edt_name.setError(null);
                    if (edt_email.getText().toString().length() != 0) {
                        edt_email.setError(null);
                        if (edt_feedback.getText().toString().length() != 0) {
                            edt_feedback.setError(null);
                            if(UtilsClass.isInternetConnected(SupportActivity.this)) {
                                submitFeedback();
                            }else{
                                UtilsClass.showSnackBar(SupportActivity.this,coordinatorLayoutSupport,"No Internet Connectivity");
                            }
                        } else {
                            edt_feedback.setError("Feedback Required");
                        }
                    } else {
                        edt_email.setError("Email Required");
                    }
                } else {
                    edt_name.setError("Name Required");
                }
            }
        });
        img_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.navigationView.getMenu().getItem(0).setChecked(true);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainActivity.navigationView.getMenu().getItem(0).setChecked(true);
    }

    public void submitFeedback() {
        sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        String apik = sharedPreferences.getString("API_KEY", null);
        AsyncHttpClient client = new AsyncHttpClient();
        String url = UtilsClass.sendMessage1 + apik + UtilsClass.sendMessage2 + edt_email.getText().toString() + UtilsClass.sendMessage3 + edt_feedback.getText().toString();
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
                try {
                    JSONObject dataObject = response.getJSONObject("data");
                    String responseCode = dataObject.getString("code");
                    if (201 == Integer.parseInt(responseCode)) {
                        UtilsClass.showSnackBar(SupportActivity.this, coordinatorLayoutSupport, "Message was successfully sent.");
                    } else {
                        UtilsClass.showSnackBar(SupportActivity.this, coordinatorLayoutSupport, "Message was unsuccessful.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
                UtilsClass.showSnackBar(SupportActivity.this,coordinatorLayoutSupport,"Message Sending  Failed");
            }
        });
    }
}
