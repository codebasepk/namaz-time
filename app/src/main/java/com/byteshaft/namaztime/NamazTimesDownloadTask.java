package com.byteshaft.namaztime;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NamazTimesDownloadTask extends AsyncTask<String, Void, JsonElement> {

    private ProgressDialog mProgressDialog = null;
    private Context mContext = null;

    public NamazTimesDownloadTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage("Updating Namaz Time");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected JsonElement doInBackground(String... params) {
        String siteLink = "http://muslimsalat.com/monthly.json/" + MainActivity.CITY_NAME + "?key=";
        String apiKey = "0aa4ecbf66c02cf5330688a105dbdc3c";
        String API = siteLink + apiKey;
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
        Helpers helpers = new Helpers(mContext);
        writeDataToFile(data);
        helpers.setTimesFromDatabase();
    }

    private void writeDataToFile(String input) {
        String fileName = MainActivity.sFileName;
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(input.getBytes());
            fileOutputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
