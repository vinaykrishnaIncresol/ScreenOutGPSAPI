package com.incresol.screenoutgps.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.incresol.screenoutgps.services.MyService;
import com.incresol.screenoutgps.services.SendDataServiceApi;

/**
 * Created by Incresol on 16-Jan-17.
 */

public class ApplicationBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent receiverServiceIntent = new Intent(context, MyService.class);
        context.startService(receiverServiceIntent);
        Intent sendDataApiServiceIntent = new Intent(context, SendDataServiceApi.class);
        context.startService(sendDataApiServiceIntent);
    }
}
