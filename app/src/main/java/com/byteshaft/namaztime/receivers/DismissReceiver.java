package com.byteshaft.namaztime.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by s9iper1 on 11/4/16.
 */

public class DismissReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("DismissReceiver", "Dismiss Receiver");
    }
}
