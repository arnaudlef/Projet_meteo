package com.example.temperator;

import android.content.Intent;
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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    ImageView imageViewIcone;
    TextView textViewTemperature, textViewCondition;
    EditText editText;
    String url;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        imageViewIcone = findViewById(R.id.imageViewIcone);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewCondition = findViewById(R.id.textViewCondition);
        editText = findViewById(R.id.editText);

        findViewById(R.id.settings).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.addCity).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TownActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.backTown).setOnClickListener(v -> {
            RequestQueue queue = Volley.newRequestQueue(this);

            url = "https://www.prevision-meteo.ch/services/json/" + editText.getText().toString();
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
        });
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