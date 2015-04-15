package ua.pp.lab101.synthesizercontrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceAutostartReceiver extends BroadcastReceiver {

    final String LOG_TAG = "ServiceAutostart";

    public ServiceAutostartReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive" + intent.getAction());
        context.startService(new Intent(context, BoardManagerService.class));
    }
}
