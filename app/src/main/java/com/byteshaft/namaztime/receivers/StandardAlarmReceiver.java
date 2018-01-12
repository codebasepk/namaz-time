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

package com.byteshaft.namaztime.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.byteshaft.namaztime.helpers.AlarmHelpers;
import com.byteshaft.namaztime.helpers.Helpers;

public class StandardAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Helpers helpers = new Helpers(context);
        AlarmHelpers alarmHelpers = new AlarmHelpers(context);
        String sFileName = helpers.getPreviouslySelectedCityName();
        helpers.setTimesFromDatabase(false, sFileName);
        alarmHelpers.setAlarmForNextNamaz();
    }
}
