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
import android.util.Log;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NamazTimesDownloadTask {
    static boolean taskRunning = false;
    private Context mContext = null;
    private Helpers mHelpers = null;
    String data;

    public NamazTimesDownloadTask(Context context) {
        this.mContext = context;
        mHelpers = new Helpers(mContext);
    }

    void downloadNamazTime() {
        String[] cityState = mHelpers.getPreviouslySelectedCityName().split(" ");
        String city = cityState[0];
        String state = null;
        if (cityState.length > 1) {
            state = cityState[1];
        }
        String country = "Pakistan";
        String API = String.format("http://api.aladhan.com/calendarByCity?city=%s" +
                        "&country=%s&school=1&method=4",
                city,country);
        if (state != null) {
            API = API + "&state="+ state;
        }
        Log.i("TAG", API);

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest request = new JsonObjectRequest(API, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("TAG", String.valueOf(response));
                try {
                    if (response.getInt("code") == 200) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");
                            data = jsonArray.toString();
                            mHelpers.writeDataToFile(mHelpers.getPreviouslySelectedCityName(), data);
                                mHelpers.setTimesFromDatabase(true, mHelpers.getPreviouslySelectedCityName());
                                if (MainActivity.sProgressBar.isShown()) {
                                    MainActivity.sProgressBar.setVisibility(View.INVISIBLE);
                                } else if (ChangeCityActivity.sCityChanged) {
                                    if (ChangeCityActivity.sProgressBar.isShown()) {
                                        ChangeCityActivity.sProgressBar.setVisibility(View.INVISIBLE);
                                    }
                                    if (!ChangeCityActivity.sActivityPaused) {
                                        Intent intent = new Intent(mContext, MainActivity.class);
                                        mContext.startActivity(intent);
                                    }
                                } else {
                                    return;
                                }
                            taskRunning = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (MainActivity.sProgressBar.isShown()) {
                    MainActivity.sProgressBar.setVisibility(View.INVISIBLE);
                } else if (ChangeCityActivity.sCityChanged) {
                    if (ChangeCityActivity.sProgressBar.isShown()) {
                        ChangeCityActivity.sProgressBar.setVisibility(View.INVISIBLE);
                    }
                    if (!ChangeCityActivity.sActivityPaused) {
                        Intent intent = new Intent(mContext, MainActivity.class);
                        mContext.startActivity(intent);
                    }
                } else {
                    return;
                }
                error.printStackTrace();
                Log.i("NetworkTime", String.valueOf(error.getNetworkTimeMs()));
                Log.i("NetworkTime", String.valueOf(error.getCause()));
            }
        });
        requestQueue.add(request);
    }
}