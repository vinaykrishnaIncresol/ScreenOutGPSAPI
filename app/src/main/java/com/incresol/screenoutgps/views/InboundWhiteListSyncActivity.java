package com.incresol.screenoutgps.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.incresol.screenoutgps.R;
import com.incresol.screenoutgps.adapter.BoundCallsAdapter;
import com.incresol.screenoutgps.adapter.RecyclerItemClickListener;
import com.incresol.screenoutgps.modal.BoundCalls;
import com.incresol.screenoutgps.utils.UtilsClass;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class InboundWhiteListSyncActivity extends AppCompatActivity {

    private RecyclerView recyclerView_Users, recyclerView_Company, recyclerView_Location, recyclerView_Group;
    private RecyclerView.LayoutManager layoutManager_User, layoutManager_Company, layoutManager_Location, layoutManager_Group;
    private RecyclerView.Adapter adapter_Users, adapter_Company, adapter_Location, adapter_Group;
    ArrayList<BoundCalls> boundcallsArrayList_Users;
    ArrayList<BoundCalls> boundcallsArrayList_Company;
    ArrayList<BoundCalls> boundcallsArrayList_Location;
    ArrayList<BoundCalls> boundcallsArrayList_Group;
    TextView txt_title;
    ImageView img_back;
    Button refresh_Button;
    CoordinatorLayout coordinate_activity_notifications;
    public static List<String> inboundCallList_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbound_white_list_sync);
        inboundCallList_ALL = new ArrayList<>();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_back_title_layout);
        txt_title = (TextView) findViewById(R.id.myTitle);
        txt_title.setText("Inbound Whitelisted Calls");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.navigationView.getMenu().getItem(0).setChecked(true);
                finish();
            }
        });
        coordinate_activity_notifications = (CoordinatorLayout) findViewById(R.id.coordinate_activity_inboundwhitelist);
        recyclerView_Users = (RecyclerView) findViewById(R.id.recycler_inbound_usercalls);
        recyclerView_Users.setHasFixedSize(true);
        recyclerView_Company = (RecyclerView) findViewById(R.id.recycler_inbound_companycalls);
        recyclerView_Company.setHasFixedSize(true);
        recyclerView_Location = (RecyclerView) findViewById(R.id.recycler_inbound_locationcalls);
        recyclerView_Location.setHasFixedSize(true);
        recyclerView_Group = (RecyclerView) findViewById(R.id.recycler_inbound_groupcalls);
        recyclerView_Group.setHasFixedSize(true);
        layoutManager_User = new LinearLayoutManager(this);
        layoutManager_Location = new LinearLayoutManager(this);
        layoutManager_Company = new LinearLayoutManager(this);
        layoutManager_Group = new LinearLayoutManager(this);
        recyclerView_Company.setLayoutManager(layoutManager_Company);
        recyclerView_Users.setLayoutManager(layoutManager_User);
        recyclerView_Location.setLayoutManager(layoutManager_Location);
        recyclerView_Group.setLayoutManager(layoutManager_Group);
        boundcallsArrayList_Users = new ArrayList<>();
        boundcallsArrayList_Company = new ArrayList<>();
        boundcallsArrayList_Location = new ArrayList<>();
        boundcallsArrayList_Group = new ArrayList<>();
        adapter_Users = new BoundCallsAdapter(boundcallsArrayList_Users, InboundWhiteListSyncActivity.this);
        adapter_Company = new BoundCallsAdapter(boundcallsArrayList_Company, InboundWhiteListSyncActivity.this);
        adapter_Location = new BoundCallsAdapter(boundcallsArrayList_Location, InboundWhiteListSyncActivity.this);
        adapter_Group = new BoundCallsAdapter(boundcallsArrayList_Group, InboundWhiteListSyncActivity.this);

        recyclerView_Users.addOnItemTouchListener(new RecyclerItemClickListener(InboundWhiteListSyncActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                System.out.println("Clicked Position -> " + position + "\n dateread -> " + boundcallsArrayList_Users.get(position).getName());
                Toast.makeText(getApplicationContext(), "" + boundcallsArrayList_Users.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        }));
        recyclerView_Company.addOnItemTouchListener(new RecyclerItemClickListener(InboundWhiteListSyncActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                System.out.println("Clicked Position -> " + position + "\n dateread -> " + boundcallsArrayList_Company.get(position).getName());
                Toast.makeText(getApplicationContext(), "" + boundcallsArrayList_Company.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        }));
        recyclerView_Location.addOnItemTouchListener(new RecyclerItemClickListener(InboundWhiteListSyncActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                System.out.println("Clicked Position -> " + position + "\n dateread -> " + boundcallsArrayList_Location.get(position).getName());
                Toast.makeText(getApplicationContext(), "" + boundcallsArrayList_Location.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        }));
        recyclerView_Group.addOnItemTouchListener(new RecyclerItemClickListener(InboundWhiteListSyncActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                System.out.println("Clicked Position -> " + position + "\n dateread -> " + boundcallsArrayList_Group.get(position).getName());
                Toast.makeText(getApplicationContext(), "" + boundcallsArrayList_Group.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        }));
        if(UtilsClass.isInternetConnected(InboundWhiteListSyncActivity.this)) {
            new FetchInboundCalls().execute();
        }else{
            Toast.makeText(InboundWhiteListSyncActivity.this,"No Internet Connectivity to Update the List of Contacts",Toast.LENGTH_SHORT).show();
        }

    }

    private class FetchInboundCalls extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(InboundWhiteListSyncActivity.this);
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
            try {
                final SharedPreferences sharedPreferences;
                sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
                String apik = sharedPreferences.getString("API_KEY", null);
                final String inboundUrl = UtilsClass.inboundWhitelist + apik;
                AsyncHttpClient client = new SyncHttpClient();
                client.get(inboundUrl, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
//                            super.onSuccess(statusCode, headers, response);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject dataObject = response.getJSONObject("data");
                                    String result = dataObject.getString("result");
                                    if (result.equalsIgnoreCase("success")) {
// Users Calls Parsing
                                        JSONArray dataObjectArray_Users = null;
                                        try {
                                            dataObjectArray_Users = dataObject.getJSONArray("inbound_user_phone");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (dataObjectArray_Users != null && dataObjectArray_Users.length() > 0) {
                                            for (int i = 0; i < dataObjectArray_Users.length(); i++) {
                                                JSONObject arrayObject = dataObjectArray_Users.getJSONObject(i);
                                                final BoundCalls boundCalls = new BoundCalls();
                                                boundCalls.setName(arrayObject.getString("name"));
                                                boundCalls.setNumber(arrayObject.getString("number"));
                                                boundcallsArrayList_Users.add(boundCalls);
                                                adapter_Users.notifyDataSetChanged();
                                            }
                                            recyclerView_Users.setAdapter(adapter_Users);
                                            adapter_Users.notifyDataSetChanged();
                                        } else {
                                            System.out.println("There are no User Contacts yet!");
                                        }
// Comapany Calls Parsing
                                        JSONArray dataObjectArray_Company = null;
                                        try {
                                            dataObjectArray_Company = dataObject.getJSONArray("inbound_company_phone");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (dataObjectArray_Company != null && dataObjectArray_Company.length() > 0) {
                                            for (int i = 0; i < dataObjectArray_Company.length(); i++) {
                                                JSONObject arrayObject = dataObjectArray_Company.getJSONObject(i);
                                                final BoundCalls boundCalls = new BoundCalls();
                                                boundCalls.setName(arrayObject.getString("name"));
                                                boundCalls.setNumber(arrayObject.getString("number"));
                                                boundcallsArrayList_Company.add(boundCalls);
                                                adapter_Company.notifyDataSetChanged();
                                            }
                                            recyclerView_Company.setAdapter(adapter_Company);
                                            adapter_Company.notifyDataSetChanged();
                                        } else {
                                            System.out.println("There are no Company Contacts yet!");
                                        }
// Location Calls Parsing
                                        JSONArray dataObjectArray_Location = null;
                                        try {
                                            dataObjectArray_Location = dataObject.getJSONArray("inbound_location_phone");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (dataObjectArray_Location != null && dataObjectArray_Location.length() > 0) {
                                            for (int i = 0; i < dataObjectArray_Location.length(); i++) {
                                                JSONObject arrayObject = dataObjectArray_Location.getJSONObject(i);
                                                final BoundCalls boundCalls = new BoundCalls();
                                                boundCalls.setName(arrayObject.getString("name"));
                                                boundCalls.setNumber(arrayObject.getString("number"));
                                                boundcallsArrayList_Location.add(boundCalls);
                                                adapter_Location.notifyDataSetChanged();
                                            }
                                            recyclerView_Location.setAdapter(adapter_Location);
                                            adapter_Location.notifyDataSetChanged();
                                        } else {
                                            System.out.println("There are no Location Contacts yet!");
                                        }
// Group Calls Parsing
                                        JSONArray dataObjectArray_Group = null;
                                        try {
                                            dataObjectArray_Group = dataObject.getJSONArray("inbound_group_phone");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (dataObjectArray_Group != null && dataObjectArray_Group.length() > 0) {
                                            for (int i = 0; i < dataObjectArray_Group.length(); i++) {
                                                JSONObject arrayObject = dataObjectArray_Group.getJSONObject(i);
                                                final BoundCalls boundCalls = new BoundCalls();
                                                boundCalls.setName(arrayObject.getString("name"));
                                                boundCalls.setNumber(arrayObject.getString("number"));
                                                boundcallsArrayList_Group.add(boundCalls);
                                                adapter_Group.notifyDataSetChanged();
                                            }
                                            recyclerView_Location.setAdapter(adapter_Group);
                                            adapter_Group.notifyDataSetChanged();
                                        } else {
                                            System.out.println("There are no Group Contacts yet!");
                                        }


                                        if (inboundCallList_ALL.size() > 0) {
                                            inboundCallList_ALL.clear();
                                        }

                                        if (boundcallsArrayList_Users.size() > 0) {
                                            for (int i = 0; i < boundcallsArrayList_Users.size(); i++) {
                                                BoundCalls bndCall = boundcallsArrayList_Users.get(i);
                                                System.out.println("*****name:-" + bndCall.getName() + " number:-" + bndCall.getNumber());
                                                inboundCallList_ALL.add(bndCall.getNumber());
                                            }
                                        }
                                        if (boundcallsArrayList_Company.size() > 0) {
                                            for (int i = 0; i < boundcallsArrayList_Company.size(); i++) {
                                                BoundCalls bndCall = boundcallsArrayList_Company.get(i);
                                                System.out.println("*****name:-" + bndCall.getName() + " number:-" + bndCall.getNumber());
                                                inboundCallList_ALL.add(bndCall.getNumber());
                                            }
                                        }
                                        if (boundcallsArrayList_Location.size() > 0) {
                                            for (int i = 0; i < boundcallsArrayList_Location.size(); i++) {
                                                BoundCalls bndCall = boundcallsArrayList_Location.get(i);
                                                System.out.println("*****name:-" + bndCall.getName() + " number:-" + bndCall.getNumber());
                                                inboundCallList_ALL.add(bndCall.getNumber());
                                            }
                                        }
                                        if (boundcallsArrayList_Group.size() > 0) {
                                            for (int i = 0; i < boundcallsArrayList_Group.size(); i++) {
                                                BoundCalls bndCall = boundcallsArrayList_Group.get(i);
                                                System.out.println("*****name:-" + bndCall.getName() + " number:-" + bndCall.getNumber());
                                                inboundCallList_ALL.add(bndCall.getNumber());
                                            }
                                        }
                                        if (inboundCallList_ALL != null) {
                                            SharedPreferences sharedPreferences1 = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences1.edit();
                                            editor.putInt("inboundCallList_Count", inboundCallList_ALL.size());
                                            for (int i = 0; i < inboundCallList_ALL.size(); i++) {
                                                System.out.println("$$$$ size -> " + inboundCallList_ALL.size() + " of " + i + "number -> " + inboundCallList_ALL.get(i));
                                                editor.putString("inbound_name_" + i, inboundCallList_ALL.get(i));
                                                editor.putString("inbound_number_" + i, inboundCallList_ALL.get(i));
                                            }
                                            editor.commit();
                                        }
                                    } else {
                                        System.out.println("Error on Fetching InboundContactsList!");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    System.out.println("Results object was not found!!");
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                            super.onFailure(statusCode, headers, responseString, throwable);
                        System.out.println("Fetching InboundCalls List got Failure !!!!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainActivity.navigationView.getMenu().getItem(0).setChecked(true);
    }
}
