package com.byteshaft.namaztime;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity /* implements View.OnClickListener */ {

    TextView textView, textViewTwo;
    final String fileName = "namazTime";
    String DbData;
    Calendar c = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-M-dd");
    String dDate = df.format(c.getTime());
    String matchedObj;
    String fajr;
    String dhuhr;
    String asar;
    String maghrib;
    String isha;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        textViewTwo = (TextView) findViewById(R.id.textViewTwo);

        try {
            gettingDataFromDb();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (matchedObj == null) {

            SystemManagement systemManagement = new SystemManagement(this);
            systemManagement.execute();
        }

    }

    private void gettingDataFromDb() throws IOException , JSONException{

        FileInputStream readFile = openFileInput(fileName);
        BufferedInputStream bis = new BufferedInputStream(readFile);
        StringBuilder stringBuilder = new StringBuilder();
        while (bis.available() != 0) {
            char characters = (char) bis.read();
            stringBuilder.append(characters);
        }
        bis.close();
        readFile.close();
        // deserializing the content...
        JSONArray readingData = new JSONArray(stringBuilder.toString());
        StringBuilder combineNew =new StringBuilder();

        for (int i = 0; i < readingData.length();i++){
            String loop = readingData.getJSONObject(i).get("date_for").toString();
            if (loop.matches(dDate)){
                matchedObj = readingData.getJSONObject(i).toString().trim();
                if (matchedObj.contains(dDate)){
                    fajr = readingData.getJSONObject(i).get("fajr").toString();
                    dhuhr = readingData.getJSONObject(i).get("dhuhr").toString();
                    asar = readingData.getJSONObject(i).get("asr").toString();
                    maghrib = readingData.getJSONObject(i).get("maghrib").toString();
                    isha = readingData.getJSONObject(i).get("isha").toString();

                }

            }
            else{
                buildErrorDialog(this,"No Namaz time Available", "Please Connect to internet" , "Ok" );

            }

            DbData = combineNew.append(loop + "\n").toString();


            textView.setText("Fajr :" + fajr+"\n" +"Dhuhr" + dhuhr +"\n" +"Asar"+asar + "\n"+"Maghrib"
                    + maghrib +"\n"+ "Isha" +isha);


        }

    }
    private static AlertDialog buildErrorDialog(final Activity context, String title,
                                                String description, String buttonText) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(description);
        builder.setCancelable(false);
        builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                context.finish();
            }
        });

        return builder.create();
    }
}
