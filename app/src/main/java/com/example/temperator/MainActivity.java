package com.example.temperator;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    ImageView imageViewIcone;
    TextView textViewTemperature, textViewCondition, textViewNameTown;
    String url;
    String variableTemp;
    Context context;
    Double latitude, longitude;
    Location gps_loc = null, network_loc = null, final_loc = null;
    FirebaseFirestore db;
    double temperature;
    String finalCity;
    int index;
    Button settings, addCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        Intent intentFromTown = getIntent();
        RequestQueue queue = Volley.newRequestQueue(this);
        imageViewIcone = findViewById(R.id.imageViewIcone);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewCondition = findViewById(R.id.textViewCondition);
        textViewNameTown = findViewById(R.id.textViewNameTown);
        db = FirebaseFirestore.getInstance();
        settings = findViewById(R.id.settings);
        addCity = findViewById(R.id.addCity);

        settings.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        });

        addCity.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TownActivity.class);
            startActivity(intent);
        });

        try {
            variableTemp = "°C";

            try {
                if(intentFromTown.hasExtra("cityClicked")){
                    String city = intentFromTown.getStringExtra("cityClicked");
                    Log.d("eeeeeeeeeeee", "onCreate: " + city);
                    textViewNameTown.setText(city);
                    setTemp(city);
                }
                else{
                    String cityLocated = getDataGPS();
                    if (cityLocated != null){
                        setTemp(cityLocated);
                    }
                    else{
                        finalCity = "";
                        index = 0;
                        db.collection("cities")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                if (index == 0){
                                                    finalCity = document.getString("city");
                                                    Log.d("zzzzzzzzzzzz", "onCreate: " + finalCity);
                                                    setTemp(finalCity);
                                                }
                                                index += 1;
                                            }
                                        } else {
                                            Log.w("app", "Error getting documents.", task.getException());
                                        }
                                    }
                                });
                    }

                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Voulez vous vraiment quitter ?")
                .setTitle("Attention !")
                .setPositiveButton("Continuer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                        dialog.dismiss();
                    }
                }).setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void setTemp(String finalCity){
        db.collection("temperature")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().size() == 0){
                                Map<String, Object> temp = new HashMap<>();
                                temp.put("temperature", "°C");
                                db.collection("temperature")
                                        .document("temperature")
                                        .set(temp);
                                setCity(finalCity, "°C");
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getData().containsValue("°F")){
                                    setCity(finalCity, "°F");
                                }
                                else if (document.getData().containsValue("°C")){
                                    setCity(finalCity, "°C");
                                }
                            }
                        } else {
                            Log.w("app", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void setCity(String finalCity, String temp) {
        RequestQueue queue = Volley.newRequestQueue(this);
        textViewNameTown.setText(finalCity);
        url = "https://www.prevision-meteo.ch/services/json/" + finalCity;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        // current_condition
                        JSONObject current_condition = jsonObject.getJSONObject("current_condition");
                        String icone = current_condition.getString("icon_big");
                        Integer tmp = current_condition.getInt("tmp");
                        String condition = current_condition.getString("condition");

                        if(temp == "°F"){
                            temperature = (tmp*1.8)+32;
                        }
                        else{
                            temperature = tmp;
                        }

                        if(condition.toLowerCase().contains("nuit")){
                            if(condition.toLowerCase().contains("nuit nuageuse")) {
                                findViewById(R.id.mainActivity).setBackgroundResource(R.drawable.nuit_nuageuse);
                                setColorLight();
                            }
                            else if(condition.toLowerCase().contains("nuit claire")){
                                findViewById(R.id.mainActivity).setBackgroundResource(R.drawable.nuit_claire);
                                setColorLight();
                            }
                            else if(condition.toLowerCase().contains("orage")){
                                findViewById(R.id.mainActivity).setBackgroundResource(R.drawable.orage);
                                setColorLight();
                            }
                        }
                        else if(condition.toLowerCase().contains("orage")){
                            findViewById(R.id.mainActivity).setBackgroundResource(R.drawable.orage);
                            setColorLight();
                        }
                        else if(condition.toLowerCase().contains("nuage")){
                            findViewById(R.id.mainActivity).setBackgroundResource(R.drawable.quelques_nuages);
                            setColorDark();
                        }
                        else if(condition.toLowerCase().contains("soleil")){
                            findViewById(R.id.mainActivity).setBackgroundResource(R.drawable.soleil);
                            setColorDark();
                        }
                        else if(condition.toLowerCase().contains("neige")){
                            findViewById(R.id.mainActivity).setBackgroundResource(R.drawable.neige);
                            setColorDark();
                        }
                        else if(condition.toLowerCase().contains("pluie")){
                            findViewById(R.id.mainActivity).setBackgroundResource(R.drawable.pluie);
                            setColorDark();
                        }

                        Picasso.get().load(icone).into(imageViewIcone);
                        textViewTemperature.setText("Temperature : " + temperature + " " + temp);
                        textViewCondition.setText(condition);

                        String city_widget = finalCity.substring(0, 1).toUpperCase() + finalCity.substring(1);

                        AppWidgetManager appwidgetManager = AppWidgetManager.getInstance(context);
                        RemoteViews remoteViews = new RemoteViews( context. getPackageName(), R.layout.temperator);
                        ComponentName thisWidget = new ComponentName(context, Temperator.class);
                        remoteViews.setTextViewText (R.id.appwidgetTemperature, city_widget + ": " + temperature + temp);
                        appwidgetManager.updateAppWidget(thisWidget, remoteViews);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur, La ville n'a pas été trouvé", Toast.LENGTH_SHORT).show();
                    }
                },

                error -> Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show());
        queue.add(stringRequest);
    }

    public String getDataGPS(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "La localisation a été bloqué", Toast.LENGTH_SHORT).show();
        try {
            assert locationManager != null;
            gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (gps_loc != null) {
            final_loc = gps_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        } else if (network_loc != null) {
            final_loc = network_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        } else {
            latitude = 0.0;
            longitude = 0.0;
        }

        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() != 0) {
                String city = addresses.get(0).getLocality();

                return city;
            }
            else{
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setColorLight(){
        textViewNameTown.setTextColor(Color.parseColor("#ffffff"));
        textViewTemperature.setTextColor(Color.parseColor("#ffffff"));
        textViewCondition.setTextColor(Color.parseColor("#ffffff"));
        settings.setBackgroundResource(R.drawable.ic_baseline_settings_24);
        addCity.setBackgroundResource(R.drawable.ic_baseline_format_list_bulleted_24);
    }

    public void setColorDark(){
        textViewNameTown.setTextColor(Color.parseColor("#000000"));
        textViewTemperature.setTextColor(Color.parseColor("#000000"));
        textViewCondition.setTextColor(Color.parseColor("#000000"));
        settings.setBackgroundResource(R.drawable.ic_baseline_settings_24_2);
        addCity.setBackgroundResource(R.drawable.ic_baseline_format_list_bulleted_24_2);
    }
}