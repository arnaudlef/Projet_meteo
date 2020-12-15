package com.example.temperator;

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

public class MainActivity extends AppCompatActivity {

    ImageView imageViewIcone;
    TextView textViewTemperature, textViewCondition;
    EditText editText;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewIcone = findViewById(R.id.imageViewIcone);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewCondition = findViewById(R.id.textViewCondition);
        editText = findViewById(R.id.editText);

        findViewById(R.id.button).setOnClickListener(v -> {
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
}