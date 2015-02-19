package com.byteshaft.namaztime;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bilal on 2/18/15.
 */


public class SystemManagement extends AsyncTask<String , Void , JsonArray> {
    public SystemManagement(Context context) {
        this.context = context;
    }
// Initialization //

    private final String apiKey = "0aa4ecbf66c02cf5330688a105dbdc3c";
    private final String siteLink = "http://muslimsalat.com/weekly.json?key=";
    final String mApiLink = siteLink + apiKey;
    JsonObject rootobj;
    JsonArray array;
    ProgressDialog pDialog;
    Context context;
    String namaztime;
    String result;
    String data;
    final String fileName = "namazTime";
    FileOutputStream createFile;

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Updating Namaz Time");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
        pDialog.show();
    }

    @Override
    protected JsonArray doInBackground(String... params) {
        try{
        URL url = new URL(mApiLink);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            rootobj = root.getAsJsonObject();
            array = rootobj.get("items").getAsJsonArray();
       }
        catch(IOException e){
            e.printStackTrace();
        }
        return array;
    }

    @Override
    protected void onPostExecute(JsonArray jsonArray) {
        super.onPostExecute(jsonArray);
        pDialog.dismiss();

        int i = 0;
        while(i < array.size()) {
            namaztime = array.get(0).getAsJsonObject().toString();
            result += namaztime;
            i++;
        }
        JSONArray jArr = new JSONArray();
        jArr.put(result);

        data = array.toString();
        try {
            createFile = context.openFileOutput(fileName , Context.MODE_PRIVATE);
            createFile.write(data.getBytes());
            createFile.close();

        }catch (IOException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

    }
}
