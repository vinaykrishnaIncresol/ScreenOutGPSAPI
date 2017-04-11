package com.incresol.screenoutgps.views;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.incresol.screenoutgps.R;

import java.util.ArrayList;
import java.util.List;

public class OutgoingCallsActivity extends Activity {

    SharedPreferences sharedPreferences;
    List<String> outboundList;
    ListView outgoing_calls_list;
    TextView txt_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_calls);
        outboundList = new ArrayList<>();
        outgoing_calls_list = (ListView) findViewById(R.id.outgoing_calls_list);
        sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        int size = sharedPreferences.getInt("outboundCallList_Count", -1);
        if (size != -1) {
            for (int i = 0; i < size; i++) {
                String number = sharedPreferences.getString("outbound_" + i, null);
                outboundList.add(number);
            }
        }
        outgoing_calls_list.setAdapter(new ArrayAdapter<String>(this, R.layout.outgoing_call_list_item, outboundList));
        outgoing_calls_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
//                        Toast.LENGTH_SHORT).show();
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+((TextView) view).getText()));
                if (ActivityCompat.checkSelfPermission(OutgoingCallsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);
            }
        });


    }
}
