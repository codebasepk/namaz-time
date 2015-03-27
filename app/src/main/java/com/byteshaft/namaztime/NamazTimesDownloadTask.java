package com.byteshaft.namaztime;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NamazTimesDownloadTask extends AsyncTask<String, Void, JsonElement> {

    private ProgressDialog mProgressDialog = null;
    private Context mContext = null;
    private Helpers mHelpers = null;


    public NamazTimesDownloadTask(Context context) {
        this.mContext = context;
        mHelpers = new Helpers(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage("Downloading Namaz Time");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected JsonElement doInBackground(String... params) {
        String city = mHelpers.getPreviouslySelectedCityName();
        String timeSpan = "monthly";
        String month = mHelpers.getDate();
        String siteLink = String.format("http://muslimsalat.com/%s/%s/%s.json?key=",
                timeSpan, month, city);
        String apiKey = "0aa4ecbf66c02cf5330688a105dbdc3c";
        String API = siteLink.concat(apiKey);
        JsonElement rootJsonElement = null;
        try {
            URL url = new URL(API);
            JsonParser jsonParser = new JsonParser();
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.connect();
            rootJsonElement = jsonParser.parse(
                    new InputStreamReader((InputStream) httpConnection.getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootJsonElement;
    }

    @Override
    protected void onPostExecute(JsonElement jsonElement) {
        super.onPostExecute(jsonElement);
        mProgressDialog.dismiss();
        JsonObject mRootJsonObject = jsonElement.getAsJsonObject();
        JsonArray mNamazTimesArray = mRootJsonObject.get("items").getAsJsonArray();
        String data = mNamazTimesArray.toString();
        mHelpers.writeDataToFile(MainActivity.sFileName, data);
        if (ChangeCity.downloadRun) {
            Intent intent = new Intent(mContext, MainActivity.class);
            mContext.startActivity(intent);
        }
        mHelpers.setTimesFromDatabase(true);
        Intent alarmIntent = new Intent("com.byteshaft.Setalarm");
        mContext.sendBroadcast(alarmIntent);
    }
}
