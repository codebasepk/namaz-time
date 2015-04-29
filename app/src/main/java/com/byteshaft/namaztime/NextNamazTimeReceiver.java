/*
 *
 *  * (C) Copyright 2015 byteShaft Inc.
 *  *
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser General Public License
 *  * (LGPL) version 2.1 which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl-2.1.html
 *  
 */

package com.byteshaft.namaztime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NextNamazTimeReceiver extends BroadcastReceiver {
    Notifications notifications;

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmHelpers alarmHelpers = new AlarmHelpers(context);
        notifications = new Notifications(context);
        notifications.removeNotification();
        alarmHelpers.setAlarmForNextNamaz();
    }
}
