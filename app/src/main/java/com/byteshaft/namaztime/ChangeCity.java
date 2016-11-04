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

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.byteshaft.namaztime.R.id.relativeLayout;


public class ChangeCity extends AppCompatActivity implements ListView.OnItemClickListener {

    private RelativeLayout mRelativeLayout;
    private Helpers mHelpers;
    private AlarmHelpers mAlarmHelpers;
    private File mFile;
    private ChangeCityHelpers mChangeCityHelpers;
    static ProgressBar sProgressBar;
    private Notifications notifications;
    static boolean sCityChanged = false;
    static boolean sActivityPaused = false;
    private MenuItem refresh;
    private ArrayList<String> cityList;
    private ArrayList<String> search;
    private ArrayAdapter<String> modeAdapter;
    private ListView list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changecitylayout);
        cityList = new ArrayList<>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final LinearLayout searchContainer = new LinearLayout(this);
        Toolbar.LayoutParams containerParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;
        searchContainer.setLayoutParams(containerParams);

        // Setup search view
        EditText toolbarSearchView = new EditText(this);
        // Set width / height / gravity
        int[] textSizeAttr = new int[]{android.R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(new TypedValue().data, textSizeAttr);
        int actionBarHeight = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, actionBarHeight);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.weight = 1;
        toolbarSearchView.setLayoutParams(params);
        // Setup display
        toolbarSearchView.setBackgroundColor(Color.TRANSPARENT);
        toolbarSearchView.setPadding(2, 0, 0, 0);
        toolbarSearchView.setTextColor(Color.WHITE);
        toolbarSearchView.setGravity(Gravity.CENTER_VERTICAL);
        toolbarSearchView.setSingleLine(true);
        toolbarSearchView.setImeActionLabel("Search", EditorInfo.IME_ACTION_UNSPECIFIED);
        toolbarSearchView.setHint("Type your city without space");
        toolbarSearchView.setHintTextColor(Color.parseColor("#b3ffffff"));
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
        } catch (Exception ignored) {

        }

        // Search text changed listener
        toolbarSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    search = new ArrayList<>();
                    list.setAdapter(null);
                    modeAdapter = new ArrayAdapter<String>(ChangeCity.this, android.R.layout.simple_list_item_1, search);
                    list.setAdapter(modeAdapter);
                    for (int i = 0; i < cityList.size(); i++) {
                        if (cityList.contains(s.toString())) {
                            search.add(cityList.get(i));
                        }
                    }
                } else {
                    list.setAdapter(null);
                    modeAdapter = new ArrayAdapter<String>(ChangeCity.this, android.R.layout.simple_list_item_1, cityList);
                    list.setAdapter(modeAdapter);
                }



            }

            @Override
            public void afterTextChanged(Editable s) {
                // http://stackoverflow.com/a/6438918/1692770
            }
        });
        ((LinearLayout) searchContainer).addView(toolbarSearchView);

        // Setup the clear button
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clearParams.gravity = Gravity.CENTER;
        // Add search view to toolbar and hide it
        toolbar.addView(searchContainer);
        sProgressBar = (ProgressBar) findViewById(R.id.mprogressBar);
        sProgressBar.setVisibility(View.INVISIBLE);
        mHelpers = new Helpers(this);
        mAlarmHelpers = new AlarmHelpers(this);
        mChangeCityHelpers = new ChangeCityHelpers(this);
        notifications = new Notifications(this);
        int mPreviousCity = mHelpers.getPreviouslySelectedCityIndex();
        mRelativeLayout = (RelativeLayout) findViewById(relativeLayout);
        ListView list = getListView(mPreviousCity);
        list.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_name_search, menu);
        refresh = menu.findItem(R.id.action_refresh);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAlarmHelpers.removePreviousAlarams();
        sCityChanged = true;
        String city = parent.getItemAtPosition(position).toString();
        String location = getFilesDir().getAbsoluteFile().getAbsolutePath() + "/" + city;
        mFile = new File(location);
        if (mFile.exists()) {
            mChangeCityHelpers.fileExists(parent, position);
        } else {
            if (mHelpers.isNetworkAvailable()) {
                mChangeCityHelpers.fileNotExists(parent, position);
            } else {
                Toast.makeText(this, "Network isn't available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    ListView getListView(int mPreviousCity) {
        list = new ListView(this);
        // punjab
        cityList.add("Lahore Punjab");
        cityList.add("Multan Punjab");
        cityList.add("Vehari Punjab");
        cityList.add("Mailsi Punjab");
        cityList.add("Deraghazikhan Punjab");
        cityList.add("Shorkot Punjab");
        cityList.add("Sadiqabad Punjab");
        cityList.add("Khapur Punjab");
        cityList.add("Muzaffargarh Punjab");
        cityList.add("Gujrawala Punjab");
        cityList.add("Gujrat Punjab");
        cityList.add("Sialkot Punjab");
        cityList.add("Chichawatni Punjab");
        cityList.add("Sahiwal Punjab");
        cityList.add("Okara Punjab");
        cityList.add("Rahimyarkhan Punjab");
        cityList.add("Pattoki Punjab");
        cityList.add("Faisalabad Punjab");
        cityList.add("Bahawalpur Punjab");
        cityList.add("Rawalpindi Punjab");
        cityList.add("Sargodha Punjab");
        cityList.add("Sheikhupura Punjab");
        cityList.add("Jhang Punjab");
        cityList.add("MianChannu Punjab");
        cityList.add("Chiniot Punjab");
        cityList.add("Gojra Punjab");
        // sindh
        cityList.add("Sukkur Sindh");
        cityList.add("Karachi Sindh");
        cityList.add("Hyderabad Sindh");
        cityList.add("Larkana Sindh");
        cityList.add("Nawabshah Sindh");
        cityList.add("MirpurKhas Sindh");
        cityList.add("Jacobabad Sindh");
        cityList.add("Shikarpur Sindh");
        cityList.add("Khairpur Sindh");
        cityList.add("Dadu Sindh");
        // KPK
        cityList.add("Peshawar Khyber Pakhtunkhwa");
        cityList.add("Kohat Khyber Pakhtunkhwa");
        cityList.add("Sawat Khyber Pakhtunkhwa");
        cityList.add("Mallamjubba Khyber Pakhtunkhwa");
        cityList.add("Abbottabad Khyber Pakhtunkhwa");
        cityList.add("Mingora Khyber Pakhtunkhwa");
        cityList.add("Bannu Khyber Pakhtunkhwa");
        cityList.add("Swabi Khyber Pakhtunkhwa");
        cityList.add("Deraismailkhan Khyber Pakhtunkhwa");
        cityList.add("Charsadda Khyber Pakhtunkhwa");
        cityList.add("Nowshera Khyber Pakhtunkhwa");
        cityList.add("Mardan Khyber Pakhtunkhwa");
        // Balochistan
        cityList.add("Quetta Balochistan");
        cityList.add("Turbat Balochistan");
        cityList.add("Sibi Balochistan");
        cityList.add("Lasbela Balochistan");
        cityList.add("Zhob Balochistan");
        cityList.add("Nasirabad Balochistan");
        cityList.add("Jaffarabad Balochistan");
        cityList.add("Hub Balochistan");
        cityList.add("DeraMuradJamali Balochistan");
        cityList.add("DeraAllahYar Balochistan");
        // gitgit baltistan
        cityList.add("Gilgit Gilgit Baltistan");
        cityList.add("Skardu Gilgit Baltistan");
        cityList.add("Ghangche Gilgit Baltistan");
        cityList.add("Makhanpura Gilgit Baltistan");
        cityList.add("Asqurdas Gilgit Baltistan");
        cityList.add("Sumo Gilgit Baltistan");
        cityList.add("Nagar Gilgit Baltistan");
        cityList.add("Gupi Gilgit Baltistan");
        cityList.add("Gultari Gilgit Baltistan");
        // jammu and kashmir
        cityList.add("Muzaffarabad Jammu and Kashmir");
        cityList.add("Mirpur Jammu and Kashmir");
        cityList.add("Bhimber Jammu and Kashmir");
        cityList.add("Kotli Jammu and Kashmir");
        cityList.add("Rawlakot Jammu and Kashmir");
        cityList.add("Bagh Jammu and Kashmir");
        cityList.add("Jatlan Jammu and Kashmir");
        cityList.add("azadkashmir");
        //ICT
        cityList.add("Islamabad Capital Territory");
        cityList.add("Parachinar");
        cityList.add("Razmak");
        cityList.add("Sadda");
        cityList.add("Wana");
        cityList.add("Khaar");
        cityList.add("Alizai");
        cityList.add("DarraAdamKhel");
        cityList.add("LandiKotal");
        cityList.add("Miranshah");
        modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cityList);
        list.setAdapter(modeAdapter);
        list.setItemChecked(mPreviousCity, true);
        mRelativeLayout.addView(list);
        return list;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        Intent intent = new Intent(this , MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sActivityPaused = true;
        if (sProgressBar.isShown()) {
            this.finish();
        }
        finish();
    }

}
