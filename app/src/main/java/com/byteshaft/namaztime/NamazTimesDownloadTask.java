package com.byteshaft.namaztime;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.util.MalformedJsonException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

public class NamazTimesDownloadTask extends AsyncTask<String, Void, String> {

    static boolean taskRunning = false;
    static ProgressDialog mProgressDialog = null;
    private Context mContext = null;
    private Helpers mHelpers = null;
    static boolean dialogShowing = false;

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
        dialogShowing = true;
        if (isCancelled())  {
            mProgressDialog.dismiss();
            return;
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String city = mHelpers.getPreviouslySelectedCityName();
        String timeSpan = "monthly";
        String month = mHelpers.getDate();
        String siteLink = String.format("http://muslimsalat.com/%s/%s/%s.json?key=",
                timeSpan, month, city);
        String apiKey = "0aa4ecbf66c02cf5330688a105dbdc3c";
        String API = siteLink.concat(apiKey);
        JsonElement rootJsonElement;

        try {
            URL url = new URL(API);
            JsonParser jsonParser = new JsonParser();
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.connect();
            JsonReader jsonReader = new JsonReader(new InputStreamReader((InputStream)httpConnection.getContent()));
            jsonReader.setLenient(true);
            rootJsonElement = jsonParser.parse(jsonReader);
            JsonObject jSonObject = rootJsonElement.getAsJsonObject();
            JsonArray mNamazTimesArray = jSonObject.get("items").getAsJsonArray();
            String data = mNamazTimesArray.toString();
            mHelpers.writeDataToFile(MainActivity.sFileName, data);
        } catch (MalformedJsonException | UnknownHostException e) {
            e.printStackTrace();
            showInternetNotAvailableDialog();
        } catch (IOException | JsonSyntaxException | NullPointerException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String work) {
        super.onPostExecute(work);
        taskRunning = true;
        try {
            if (dialogShowing) {
                mProgressDialog.dismiss();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            MainActivity.closeApp();
            taskRunning = false;
        }
        mHelpers.setTimesFromDatabase(true, MainActivity.sFileName);
        Intent alarmIntent = new Intent("com.byteshaft.setalarm");
        mContext.sendBroadcast(alarmIntent);
        dialogShowing = false;
        if (ChangeCity.downloadRun && taskRunning) {
            Intent intent = new Intent(mContext, MainActivity.class);
            mContext.startActivity(intent);
        }

    }
    private void showInternetNotAvailableDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle("Error");
        alert.setMessage("Please Select your City again");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                MainActivity.closeApp();

            }
        });
        alert.show();
    }
}
