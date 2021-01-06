package com.example.temperator;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    Button temperature;
    TextView temperatureSelected;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        temperature = findViewById(R.id.temperatureButton);
        temperatureSelected = findViewById(R.id.textViewTemp);
        db = FirebaseFirestore.getInstance();

        db.collection("temperature")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getData().containsValue("°C")){
                                    temperature.setText("°F");
                                    temperatureSelected.setText("Température seléctionnée en °C");
                                }
                                else{
                                    temperature.setText("°C");
                                    temperatureSelected.setText("Température seléctionnée en °F");
                                }
                                Log.d("app", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w("app", "Error getting documents.", task.getException());
                        }
                    }
                });

        findViewById(R.id.backSettings).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        temperature.setOnClickListener(v -> {
            Map<String, Object> temp = new HashMap<>();
            temp.put("temperature", temperature.getText().toString());
            db.collection("temperature")
                    .document("temperature")
                    .set(temp);
            if(temperature.getText().toString() == "°C"){
                temperature.setText("°F");
                temperatureSelected.setText("Température seléctionnée en °C");
            }
            else{
                temperature.setText("°C");
                temperatureSelected.setText("Température seléctionnée en °F");
            }
        });


    }
}