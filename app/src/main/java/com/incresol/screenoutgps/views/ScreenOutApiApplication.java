package com.incresol.screenoutgps.views;

import android.app.Application;

import com.urbanairship.UAirship;

/**
 * Created by Incresol-27 on 16-11-2016.
 */

public class ScreenOutApiApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        UAirship.takeOff(this, new UAirship.OnReadyCallback() {
            @Override
            public void onAirshipReady(UAirship uAirship) {
                uAirship.getPushManager().setUserNotificationsEnabled(true);
            }
        });
    }
}
