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

public class SystemManagement extends AsyncTask<String, Void, JsonElement> {

    ProgressDialog mProgressDialog;
    Context mContext;

    public SystemManagement(Context context) {
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
        String siteLink = "http://muslimsalat.com/weekly.json?key=";
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
        String data =  mNamazTimesArray.toString();
        writeDataToFile(data);
    }

    private void writeDataToFile(String input) {
        String fileName = "namazTime";
        FileOutputStream fileOutputStream;

        try {
            fileOutputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(input.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
