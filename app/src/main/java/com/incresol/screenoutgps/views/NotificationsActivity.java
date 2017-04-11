package com.incresol.screenoutgps.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.incresol.screenoutgps.R;
import com.incresol.screenoutgps.adapter.DataAdapter;
import com.incresol.screenoutgps.adapter.RecyclerItemClickListener;
import com.incresol.screenoutgps.modal.Notifications;
import com.incresol.screenoutgps.utils.UtilsClass;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class NotificationsActivity extends AppCompatActivity {

    TextView txt_title;
    ImageView img_back;
    SharedPreferences sharedPreferences;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;
    CoordinatorLayout coordinate_activity_notifications;
    ArrayList<Notifications> notificationsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_back_title_layout);
        txt_title = (TextView) findViewById(R.id.myTitle);
        txt_title.setText("Notifications");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.navigationView.getMenu().getItem(0).setChecked(true);
                finish();
            }
        });
        coordinate_activity_notifications = (CoordinatorLayout) findViewById(R.id.coordinate_activity_notifications);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_notifications);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        notificationsArrayList = new ArrayList<>();
        adapter = new DataAdapter(notificationsArrayList, NotificationsActivity.this);
        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (UtilsClass.isInternetConnected(NotificationsActivity.this)) {
            new FetchNotifications().execute();
        } else {
            UtilsClass.showSnackBar(NotificationsActivity.this, coordinate_activity_notifications, "No Internet!");
        }

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(NotificationsActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                System.out.println("Clicked Position -> " + position + "\n dateread -> " + notificationsArrayList.get(position).getDateRead());
                final int id = notificationsArrayList.get(position).getId();
                if (UtilsClass.isInternetConnected(NotificationsActivity.this) && !notificationsArrayList.get(position).isRead()) {
                    SharedPreferences sharedPreferences;
                    sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
                    String apik = sharedPreferences.getString("API_KEY", null);
                    AsyncHttpClient client = new SyncHttpClient();
                    String url = UtilsClass.markPush1 + apik + UtilsClass.markPush2 + id + UtilsClass.markPush3;
                    client.get(url, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                        super.onSuccess(statusCode, headers, response);
                            try {
                                JSONObject dataObject = response.getJSONObject("data");
                                String result = dataObject.getString("result");
                                if (result.equalsIgnoreCase("success")) {
                                    System.out.println("id -> " + id + " mark as read -> true ");
                                    fetchNotifications();
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                        super.onFailure(statusCode, headers, responseString, throwable);
                        }
                    });
                }
            }
        }));
    }

    public void fetchNotifications(){
        if (UtilsClass.isInternetConnected(NotificationsActivity.this)) {
            new FetchNotifications().execute();
        } else {
            UtilsClass.showSnackBar(NotificationsActivity.this, coordinate_activity_notifications, "No Internet!");
        }
    }

    private class FetchNotifications extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(NotificationsActivity.this);
            progressDialog.setMessage("Loading ....");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences sharedPreferences;
            sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
            String apik = sharedPreferences.getString("API_KEY", null);
            AsyncHttpClient client = new SyncHttpClient();
            String url = UtilsClass.pushNotificationsList1 + apik + UtilsClass.pushNotificationsList2;

//            String url = "https://api.managesync.com/screenout/view-notifications/f01e119946e3cec8e08dcfbe11cf89d4/data?push=";
            client.get(url, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if(notificationsArrayList != null){
                                    notificationsArrayList.clear();
                                }
                                JSONObject dataObject = response.getJSONObject("data");
                                String result = dataObject.getString("result");
                                if (result.equalsIgnoreCase("succes")) {
                                    String unreadCount = dataObject.getString("unread");
                                    JSONArray dataObjectArray = dataObject.getJSONArray("message");
                                    if (dataObjectArray.length() > 0) {
                                        for (int i = 0; i < dataObjectArray.length(); i++) {
                                            JSONObject arrayObject = dataObjectArray.getJSONObject(i);
                                            final Notifications notifications = new Notifications();
                                            notifications.setId(arrayObject.getInt("id"));
                                            notifications.setDateRead(arrayObject.getString("dateRead"));
                                            notifications.setNotification(arrayObject.getString("notification"));
                                            notifications.setSentondate(arrayObject.getString("sentondate"));
                                            if (arrayObject.getString("isRead").equalsIgnoreCase("yes")) {
                                                notifications.setRead(true);
                                            } else {
                                                notifications.setRead(false);
                                            }
                                            notificationsArrayList.add(notifications);
                                            adapter.notifyDataSetChanged();

                                        }
                                        recyclerView.setAdapter(adapter);
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        UtilsClass.showSnackBar(NotificationsActivity.this, coordinate_activity_notifications, "There are no Notifications yet!");
                                        System.out.println("There are no Notifications yet!");
                                    }
                                } else {
                                    UtilsClass.showSnackBar(NotificationsActivity.this, coordinate_activity_notifications, "Error on Fetching Notifications!");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                UtilsClass.showSnackBar(NotificationsActivity.this, coordinate_activity_notifications, "Results object was not found!!");
                            }
                        }
                    });
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                super.onFailure(statusCode, headers, responseString, throwable);
                    System.out.println("Notification Count got error response in onSuccess()");
                }
            });
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainActivity.navigationView.getMenu().getItem(0).setChecked(true);
    }
}
