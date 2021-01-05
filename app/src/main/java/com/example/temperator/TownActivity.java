package com.example.temperator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import android.view.View;
import android.widget.Toast;

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
        ListView list = (ListView)findViewById(R.id.listview);
        ArrayAdapter<String> tableau = new ArrayAdapter<>(list.getContext(), R.layout.city, R.id.textView);

        db.collection("cities")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                nbCities += 1;
                                tableau.add(document.getString("city"));
                            }
                            list.setAdapter(tableau);
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
                                    if (document.getData().containsValue(town)) {
                                        present = true;
                                    }
                                }
                                if (present == false) {
                                    Map<String, Object> city = new HashMap<>();
                                    city.put("city", town);
                                    db.collection("cities")
                                            .document("city" + nbCities)
                                            .set(city);
                                    nbCities += 1;
                                    tableau.add(town);
                                }
                                list.setAdapter(tableau);
                            } else {
                                Log.w("app", "Error getting documents.", task.getException());
                            }
                        }
                    });
        });

        list.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("TAG", "" + position);
        });
    }
}