package com.byteshaft.namaztime;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;


public class ChangeCity extends ActionBarActivity {
    public static boolean downloadRun = false;
    LinearLayout linearLayout;
    Helpers mHelpers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        mHelpers = new Helpers(this);
        int mPreviousCity = mHelpers.getPreviouslySelectedCityIndex();
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        mHelpers = new Helpers(this);
        ListView list = new ListView(this);
        String[] cityList = new String[]{"Karachi", "Lahore", "Multan"
                , "Islamabad", "Peshawar", "Azad Kashmir", "Faisalabad", "Bahawalpur", "Rawalpindi", "Hyderabad", "Quetta"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cityList);
        list.setAdapter(modeAdapter);
        list.setItemChecked(mPreviousCity, true);
        linearLayout.addView(list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new NamazTimesDownloadTask(ChangeCity.this).execute();
                parent.getItemAtPosition(position);
                parent.setSelection(position);
                String city = parent.getItemAtPosition(position).toString().toLowerCase();
                mHelpers.saveSelectedCity(city, position);
                downloadRun = true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }
}
