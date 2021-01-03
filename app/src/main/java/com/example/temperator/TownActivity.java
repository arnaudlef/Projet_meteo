package com.example.temperator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class TownActivity extends AppCompatActivity {

    Button addTown;
    EditText nameTown;
    FirebaseFirestore db;

    private int nbCities = 0;
    private Boolean present = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_town);

        addTown = findViewById(R.id.addTown);
        nameTown = findViewById(R.id.textEditCity);
        db = FirebaseFirestore.getInstance();

        db.collection("cities")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                nbCities += 1;
                                Log.d("app", document.getId() + " => " + document.getData());
                                Log.d("TAG", "" + nbCities);
                            }
                        } else {
                            Log.w("app", "Error getting documents.", task.getException());
                        }
                    }
                });

        findViewById(R.id.backTown).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        addTown.setOnClickListener(v -> {
            String town = nameTown.getText().toString().toLowerCase();
            present = false;
            db.collection("cities")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if(document.getData().containsValue(town)){
                                        present = true;
                                    }
                                }
                                if(present == false){
                                    Map<String, Object> city = new HashMap<>();
                                    city.put("city", town);
                                    db.collection("cities")
                                            .document("city" + nbCities)
                                            .set(city);
                                    nbCities += 1;
                                }
                            }
                            else {
                                Log.w("app", "Error getting documents.", task.getException());
                            }
                        }
                    });
        });
    }
}