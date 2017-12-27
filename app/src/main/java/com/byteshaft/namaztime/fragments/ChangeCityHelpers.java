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

package com.byteshaft.namaztime.fragments;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.byteshaft.namaztime.helpers.Helpers;
import com.byteshaft.namaztime.MainActivity;
import com.byteshaft.namaztime.NamazTimesDownloadTask;

public class ChangeCityHelpers extends ContextWrapper {
    Helpers mHelpers = new Helpers(this);

    public ChangeCityHelpers(Context base) {
        super(base);
    }

    void fileNotExists(String cityName, int position) {
        mHelpers.saveSelectedCity(cityName.trim(), position);
        NamazTimesDownloadTask namazTimesDownloadTask = new NamazTimesDownloadTask(this);
        ChangeCity.sProgressBar.setVisibility(View.VISIBLE);
        namazTimesDownloadTask.downloadNamazTime();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    void fileExists(AdapterView<?> parent, int position) {
        parent.getItemAtPosition(position);
        parent.setSelection(position);
        String cityName = parent.getItemAtPosition(position).toString();
        mHelpers.saveSelectedCity(cityName, position);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
