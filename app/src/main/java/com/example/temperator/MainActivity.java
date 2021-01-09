package com.example.temperator;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
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
    EditText editText;
    String url;
    String variableTemp;
    Context context;
    Double latitude, longitude;
    Location gps_loc = null, network_loc = null, final_loc = null;
    FirebaseFirestore db;
    double temperature;
    String finalCity;
    int index;

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

        findViewById(R.id.settings).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.addCity).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TownActivity.class);
            startActivity(intent);
        });

        boolean getData = getDataGPS();
        if(!getData){
            if (intentFromTown != null) {
                setTemp();
                if (intentFromTown.hasExtra("cityClicked")) {
                    Log.d("azreazrazreazr", "onCreate: " + variableTemp);
                    String city = intentFromTown.getStringExtra("cityClicked");
                    textViewNameTown.setText(city);
                    url = "https://www.prevision-meteo.ch/services/json/" + city;
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            response -> {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    // current_condition
                                    JSONObject current_condition = jsonObject.getJSONObject("current_condition");
                                    String icone = current_condition.getString("icon_big");
                                    Integer tmp = current_condition.getInt("tmp");
                                    String condition = current_condition.getString("condition");

                                    if(variableTemp == "°F"){
                                        temperature = (tmp*1.8)+32;
                                    }
                                    else{
                                        temperature = tmp;
                                    }

                                    Picasso.get().load(icone).into(imageViewIcone);
                                    textViewTemperature.setText("Temperature : " + temperature + " " + variableTemp);
                                    textViewCondition.setText(condition);

                                    String city_widget = city.substring(0, 1).toUpperCase() + city.substring(1);

                                    AppWidgetManager appwidgetManager = AppWidgetManager.getInstance(context);
                                    RemoteViews remoteViews = new RemoteViews( context. getPackageName(), R.layout.temperator);
                                    ComponentName thisWidget = new ComponentName(context, Temperator.class);
                                    remoteViews.setTextViewText (R.id.appwidgetTemperature, city_widget + ": " + tmp + "°C");
                                    appwidgetManager.updateAppWidget(thisWidget, remoteViews);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, "Erreur, La ville n'a pas été trouvé", Toast.LENGTH_SHORT).show();
                                }
                            },

                            error -> Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show());
                    queue.add(stringRequest);
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

                                                setCity(finalCity);

                                                String city_widget = finalCity.substring(0, 1).toUpperCase() + finalCity.substring(1);

                                                AppWidgetManager appwidgetManager = AppWidgetManager.getInstance(context);
                                                RemoteViews remoteViews = new RemoteViews( context. getPackageName(), R.layout.temperator);
                                                ComponentName thisWidget = new ComponentName(context, Temperator.class);
                                                remoteViews.setTextViewText (R.id.appwidgetTemperature, city_widget + ": " + tmp + "°C");
                                                appwidgetManager.updateAppWidget(thisWidget, remoteViews);
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

    private void setCity(String finalCity) {
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

                        if(variableTemp == "°F"){
                            temperature = (tmp*1.8)+32;
                        }
                        else{
                            temperature = tmp;
                        }

                        Picasso.get().load(icone).into(imageViewIcone);
                        textViewTemperature.setText("Temperature : " + temperature + " " + variableTemp);
                        textViewCondition.setText(condition);


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur, La ville n'a pas été trouvé", Toast.LENGTH_SHORT).show();
                    }
                },

                error -> Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show());
        queue.add(stringRequest);
    }

    String setTemp(){
        String valueReturned = "";
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
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getData().containsValue("°F")){
                                    variableTemp = "°F";
                                }
                                else if (document.getData().containsValue("°C")){
                                    variableTemp = "°C";
                                }
                            }
                        } else {
                            Log.w("app", "Error getting documents.", task.getException());
                        }
                    }
                });
        valueReturned = variableTemp;
        return valueReturned;
    }

    boolean getDataGPS(){
        RequestQueue queue = Volley.newRequestQueue(this);
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
                Log.d("Nop", "onCreate: " + addresses.size());
                String city = addresses.get(0).getLocality();
                url = "https://www.prevision-meteo.ch/services/json/" + city;
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        response -> {
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                // current_condition
                                JSONObject current_condition = jsonObject.getJSONObject("current_condition");
                                String icone = current_condition.getString("icon_big");
                                Integer tmp = current_condition.getInt("tmp");
                                String condition = current_condition.getString("condition");

                                if(variableTemp == "°F"){
                                    temperature = (tmp*1.8)+32;
                                }
                                else{
                                    temperature = tmp;
                                }

                                Picasso.get().load(icone).into(imageViewIcone);
                                textViewTemperature.setText("Temperature : " + temperature + " " + variableTemp);
                                textViewCondition.setText(condition);


                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Erreur, La ville n'a pas été trouvé", Toast.LENGTH_SHORT).show();
                            }
                        },

                        error -> Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show());
                queue.add(stringRequest);

                textViewNameTown.setText(city);

                return true;
            }
            else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}