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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.namaztime.CityName.CityName;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.byteshaft.namaztime.R.id.relativeLayout;


public class ChangeCityActivity extends AppCompatActivity implements ListView.OnItemClickListener,
        View.OnClickListener {

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
    private ArrayList<CityName> cityList;
    private ArrayList<CityName> search;
    private CityAdapter cityAdapter;
    private ListView list;
    private Button requestCity;
    private EditText toolbarSearchView;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changecitylayout);
        getActivityRequests();
        requestCity = (Button) findViewById(R.id.add_my_city);
        requestCity.setOnClickListener(this);
        cityList = new ArrayList<>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final LinearLayout searchContainer = new LinearLayout(this);
        Toolbar.LayoutParams containerParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;
        searchContainer.setLayoutParams(containerParams);

        // Setup search view
        toolbarSearchView = new EditText(this);
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
        toolbarSearchView.setHint("Search by City Name(without spaces) / province");
        toolbarSearchView.setHintTextColor(Color.parseColor("#b3ffffff"));
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(toolbarSearchView, R.drawable.cursor_color);
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
                    cityAdapter = new CityAdapter(ChangeCityActivity.this, search);
                    list.setAdapter(cityAdapter);
                    for (int i = 0; i < cityList.size(); i++) {
                        if (cityList.get(i).getName().toLowerCase().contains(s.toString())) {
                            search.add(cityList.get(i));
                            cityAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    list.setAdapter(null);
                    cityAdapter = new CityAdapter(ChangeCityActivity.this, cityList);
                    list.setAdapter(cityAdapter);
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
//        saveCities(cityNameArrayList);
        cityAdapter = new CityAdapter(this, cityList);
        list.setAdapter(cityAdapter);
        list.setItemChecked(mPreviousCity, true);
        mRelativeLayout.addView(list);
        return list;
    }

    private void getActivityRequests() {
        ref = FirebaseDatabase.getInstance().
                getReference()
                .child("Database").child("cities");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Log.i("TAG", "request" + ds.getKey());
                    Log.i("TAG", "value " + ds.getValue(CityName.class).getName());
                    cityList.add(ds.getValue(CityName.class));
                    cityAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", databaseError.getMessage());

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        Intent intent = new Intent(this, MainActivity.class);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_my_city:
                if (!toolbarSearchView.getText().toString().trim().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, "byteshaft@gmail.com");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "New City Request");
                    intent.putExtra(Intent.EXTRA_TEXT, String.format("Please Add City : %s to your database. thank you", toolbarSearchView.getText().toString()));
                    startActivity(Intent.createChooser(intent, "Send Email"));
                } else {
                    Toast.makeText(ChangeCityActivity.this, "Please enter your city name at the top with province before pressing this button", Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    private void saveCities(final ArrayList<CityName> cityNames) {
        ref = FirebaseDatabase.getInstance().
                getReference();
        for (CityName cityName1 : cityNames) {
            ref.child("Database").child("cities").push()
                    .setValue(cityName1).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                    }
                }

            });
        }
    }

    private class CityAdapter extends ArrayAdapter<CityName> {

        private ViewHolder viewHolder;
        private ArrayList<CityName> cityNames;

        public CityAdapter(Context context, ArrayList<CityName> arrayList) {
            super(context, R.layout.list_item);
            cityNames = arrayList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
                viewHolder.cityName = (TextView) convertView.findViewById(R.id.tv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            CityName cityName = cityNames.get(position);
            viewHolder.cityName.setText(cityName.getName());
            return convertView;
        }

        @Override
        public int getCount() {
            return cityNames.size();
        }
    }

    private class ViewHolder {
        TextView cityName;
    }
}

