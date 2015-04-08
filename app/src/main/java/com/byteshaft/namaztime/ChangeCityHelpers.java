package com.byteshaft.namaztime;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.widget.AdapterView;

public class ChangeCityHelpers extends ContextWrapper {
    Helpers mHelpers = new Helpers(this);

    public ChangeCityHelpers(Context base) {
        super(base);
    }

    void fileNotExsist(AdapterView<?> parent, int position) {
        new NamazTimesDownloadTask(this).execute();
        parent.getItemAtPosition(position);
        parent.setSelection(position);
        String cityName = parent.getItemAtPosition(position).toString();
        mHelpers.saveSelectedCity(cityName, position);

    }

    void fileExsist(AdapterView<?> parent, int position) {
        mHelpers.setTimesFromDatabase(true, MainActivity.sFileName);
        parent.setSelection(position);
        String cityName = parent.getItemAtPosition(position).toString();
        mHelpers.saveSelectedCity(cityName, position);
        Intent alarmIntent = new Intent("com.byteshaft.setalarm");
        sendBroadcast(alarmIntent);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
