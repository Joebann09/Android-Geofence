package com.example.android.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, restartPendingIntents.class);
        context.startService(startServiceIntent);
        Log.v("TEST", "Service loaded at start");
    }
}
