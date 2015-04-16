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

public class NamazTimesDownloadTask  {
    static boolean taskRunning = false;
    private Context mContext = null;
    private Helpers mHelpers = null;
    String data;

    public NamazTimesDownloadTask(Context context) {
        this.mContext = context;
        mHelpers = new Helpers(mContext);
    }

    void downloadNamazTime() {
        String city = mHelpers.getPreviouslySelectedCityName();
        String timeSpan = "monthly";
        String month = mHelpers.getDate();
        String siteLink = String.format("http://muslimsalat.com/%s/%s/%s.json?key=",
                timeSpan, month, city);
        String apiKey = "0aa4ecbf66c02cf5330688a105dbdc3c";
        String API = siteLink.concat(apiKey);

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest request = new JsonObjectRequest(API , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("items");
                    data = jsonArray.toString();
                    System.out.println(jsonArray);
                    mHelpers.writeDataToFile(MainActivity.sFileName, data);
                    if(response.length() != 0) {
                        mHelpers.setTimesFromDatabase(true, MainActivity.sFileName);
                        if (MainActivity.progressBar.isShown()){
                            MainActivity.progressBar.setVisibility(View.INVISIBLE);
                        } else if (ChangeCity.mProgressBar.isShown()) {
                            ChangeCity.mProgressBar.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(mContext , MainActivity.class);
                            mContext.startActivity(intent);
                        }
                    }
                    taskRunning = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.i("NetworkTime", String.valueOf(error.getNetworkTimeMs()));
                Log.i("NetworkTime", String.valueOf(error.getCause()));
            }
        });
        requestQueue.add(request);
    }
}