package com.example.bstorm1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends AppCompatActivity {

    private TextView textView, textView1, textView2;
    private LinearLayout background;
    private LocationManager locationManager;

    private FusedLocationProviderClient client;

    private String cityName = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        textView = findViewById(R.id.textView);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        background = findViewById(R.id.background);

        setBackground();

        client = LocationServices.getFusedLocationProviderClient(this);

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION},1);

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                double lat = location.getLatitude();
                double lon = location.getLongitude();

                //cityName = "";

                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses;

                try {
                    addresses = geocoder.getFromLocation(lat,lon,10);

                    if(addresses.size()>0){
                        for(Address adr: addresses){
                            if(adr.getLocality() != null && adr.getLocality().length() > 0){
                                cityName = adr.getLocality();

                                OkHttpClient client = new OkHttpClient();
                                String url = "http://api.openweathermap.org/data/2.5/weather?q="+ cityName +"&appid=3c56244ba438d5c399de025e55c39d41&units=metric";
                                Request request = new Request.Builder()
                                        .url(url)
                                        .build();
                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        if (response.isSuccessful()) {
                                            final String myResponse = response.body().string();
                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        jsonParse(myResponse);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                                break;
                            }
                        }
                    }

                    textView2.setText(cityName);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    private void setBackground() {
        int currentTime = Calendar.getInstance().getTime().getHours();

        if(currentTime <= 6 || currentTime >= 21){
            background.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.background2));
        }else{
            background.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.background));
        }

    }


    private void jsonParse(String stringData) throws JSONException {
        JSONObject mainObject = new JSONObject(stringData);

        JSONArray weather = mainObject.getJSONArray("weather");
        String description = weather.getJSONObject(0).getString("description");

        JSONObject main = mainObject.getJSONObject("main");
        Double  temp = main.getDouble("temp");

        String city = mainObject.getString("name");

        textView.setText(String.format("%.0f", temp) + "\u00B0");
        textView1.setText(description);
        textView2.setText(city);

    }

}