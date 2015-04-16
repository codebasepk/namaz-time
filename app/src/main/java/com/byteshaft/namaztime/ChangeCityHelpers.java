package com.byteshaft.namaztime;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

public class ChangeCityHelpers extends ContextWrapper {
    Helpers mHelpers = new Helpers(this);

    public ChangeCityHelpers(Context base) {
        super(base);
    }

    void fileNotExists(AdapterView<?> parent, int position) {
        parent.getItemAtPosition(position);
        parent.setSelection(position);
        String cityName = parent.getItemAtPosition(position).toString();
        mHelpers.saveSelectedCity(cityName, position);
        NamazTimesDownloadTask namazTimesDownloadTask = new NamazTimesDownloadTask(this);
        ChangeCity.mProgressBar.setVisibility(View.VISIBLE);
        namazTimesDownloadTask.downloadNamazTime();
    }

    void fileExists(AdapterView<?> parent, int position) {
        mHelpers.setTimesFromDatabase(true, MainActivity.sFileName);
        parent.setSelection(position);
        String cityName = parent.getItemAtPosition(position).toString();
        mHelpers.saveSelectedCity(cityName, position);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
