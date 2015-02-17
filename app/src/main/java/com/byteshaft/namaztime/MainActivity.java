package com.byteshaft.namaztime;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import android.app.Activity;
import android.os.Bundle;
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
import java.util.Date;

public class MainActivity extends Activity /* implements View.OnClickListener */ {

    private final String apiKey = "0aa4ecbf66c02cf5330688a105dbdc3c";
    private final String siteLink = "http://muslimsalat.com/daily.json?key=";
    private final String mApiLink = siteLink + apiKey;
    String fajr;
    String duhr;
    String asar;
    String magrib;
    String esha;
    String date;
    JsonObject namazTimes;
    TextView textViewTwo;
    TextView textView;
    String getdate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        textViewTwo = (TextView) findViewById(R.id.textViewTwo);
        Toast.makeText(this,getdate,Toast.LENGTH_LONG).show();

        try {
            HttpURLConnection request = gettingReadyTogetDataByUrlFromServer();
            ParsingDataUsingJsonParser(request);
            writeDataToFile();
            ReadingFileFromDatabase();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HttpURLConnection gettingReadyTogetDataByUrlFromServer() throws IOException {
        URL url = new URL(mApiLink);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
        return request;
    }

    private void ParsingDataUsingJsonParser(HttpURLConnection request) throws IOException {
        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject rootobj = root.getAsJsonObject();
        JsonArray array = rootobj.get("items").getAsJsonArray();
        namazTimes = array.get(0).getAsJsonObject();
       // textView.setText(namazTimes.get("date_for").toString());
        textView.setText(namazTimes.get("fajr").toString());
    }

    private void writeDataToFile() throws IOException, JSONException {

        JSONArray array  = new JSONArray();
        JSONObject info = new JSONObject();
//        Toast.makeText(this,"working" , Toast.LENGTH_SHORT).show();
// getting ready to send data to DB
        info.put("date" , namazTimes.get("date_for").toString().trim());
        info.put("fajr" , namazTimes.get("fajr").toString().trim());
        info.put("dhuhr" , namazTimes.get("dhuhr").toString().trim());
        info.put("asar" , namazTimes.get("asr").toString().trim());
        info.put("magrib", namazTimes.get("maghrib").toString().trim());
        info.put("esha" , namazTimes.get("isha").toString().trim());
        array.put(info);
//saving data to database now
        String data = array.toString();
        FileOutputStream createFile = openFileOutput("namazTime", MODE_PRIVATE);
        createFile.write(data.getBytes());
        createFile.close();
        //textViewTwo.setText(data);
        }

    private void ReadingFileFromDatabase() throws IOException, JSONException {
        FileInputStream readFile = openFileInput("namazTime");
        BufferedInputStream bis = new BufferedInputStream(readFile);
        StringBuilder stringBuilder = new StringBuilder();
        while (bis.available() != 0) {
            char characters = (char) bis.read();
            stringBuilder.append(characters);
        }
        bis.close();
        readFile.close();
        // deserializing the content...
        StringBuffer combineNew = loppingThroughtData(stringBuilder);
        //textViewTwo.setText(combineNew.toString());
    }

    private StringBuffer loppingThroughtData(StringBuilder stringBuilder) throws JSONException {
        JSONArray readingData = new JSONArray(stringBuilder.toString());
        StringBuffer combineNew =new StringBuffer();

        for (int i = 0; i < readingData.length();i++) {
            String date = readingData.getJSONObject(i).getString("date").replace("\"", "");
            if (getdate.equals(date)) {

                String fajr = readingData.getJSONObject(i).getString("fajr").replace("\"", "");
                String dhuhr = readingData.getJSONObject(i).getString("duhr").replace("\"", "");
                String asar = readingData.getJSONObject(i).getString("asr").replace("\"", "");
                String maghrib = readingData.getJSONObject(i).getString("magrib").replace("\"", "");
                String esha = readingData.getJSONObject(i).getString("esha").replace("\"", "");

                textViewTwo.setText(" Date is " + date + "\n" + "Namaz Timings" + fajr + "\n"
                        + dhuhr + "\n" + asar + "\n" + maghrib + "\n" + esha + "\n");
            }else {
                try {
                    HttpURLConnection request = gettingReadyTogetDataByUrlFromServer();
                    ParsingDataUsingJsonParser(request);
                    writeDataToFile();
                    Toast.makeText(this,"working" , Toast.LENGTH_SHORT).show();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return combineNew;
    }
}
