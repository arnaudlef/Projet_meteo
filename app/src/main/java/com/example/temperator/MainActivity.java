package com.example.temperator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView imageViewIcone;
    TextView textViewTemperature, textViewCondition, textViewNameTown;
    EditText editText;
    String url;
    Context context;
    Double latitude, longitude;
    Location gps_loc = null, network_loc = null, final_loc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        Intent intentFromTown = getIntent();
        imageViewIcone = findViewById(R.id.imageViewIcone);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewCondition = findViewById(R.id.textViewCondition);
        textViewNameTown = findViewById(R.id.textViewNameTown);
        RequestQueue queue = Volley.newRequestQueue(this);

        findViewById(R.id.settings).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.addCity).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TownActivity.class);
            startActivity(intent);
        });

        if (intentFromTown != null) {
            if (intentFromTown.hasExtra("cityClicked")) {
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
                                String tmp = current_condition.getString("tmp");
                                String condition = current_condition.getString("condition");

                                Picasso.get().load(icone).into(imageViewIcone);
                                textViewTemperature.setText("Temperature : " + tmp + " °C");
                                textViewCondition.setText(condition);


                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Erreur, La ville n'a pas été trouvé", Toast.LENGTH_SHORT).show();
                            }
                        },

                        error -> Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show());
                queue.add(stringRequest);
            }
        }

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
            if (addresses != null) {
                String city = addresses.get(0).getLocality();

                url = "https://www.prevision-meteo.ch/services/json/" + city;
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        response -> {
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                // current_condition
                                JSONObject current_condition = jsonObject.getJSONObject("current_condition");
                                String icone = current_condition.getString("icon_big");
                                String tmp = current_condition.getString("tmp");
                                String condition = current_condition.getString("condition");

                                Picasso.get().load(icone).into(imageViewIcone);
                                textViewTemperature.setText("Temperature : " + tmp);
                                textViewCondition.setText(condition);


                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Erreur, La ville n'a pas été trouvé", Toast.LENGTH_SHORT).show();
                            }
                        },

                        error -> Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show());
                queue.add(stringRequest);

                textViewNameTown.setText(city);
            }
        } catch (Exception e) {
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
}