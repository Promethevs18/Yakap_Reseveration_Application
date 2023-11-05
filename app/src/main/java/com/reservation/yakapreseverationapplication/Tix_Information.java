package com.reservation.yakapreseverationapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

public class Tix_Information extends AppCompatActivity {

    //UI elements
    Button purchase;
    ImageView upuan;
    TextView vip, lb, upb;

    //Firebase Cloud Firestore
    DocumentReference seats_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tix_information);
        //Initialize UI elements
        purchase = findViewById(R.id.bili);
        upuan = findViewById(R.id.seats);

        vip = findViewById(R.id.vip_num);
        lb = findViewById(R.id.lb_num);
        upb = findViewById(R.id.upb_num);


        //Initialize Firestore elements
        seats_ref = FirebaseFirestore.getInstance().collection("Seat Variations").document("Seat Classes");

        purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goFill = new Intent(Tix_Information.this, Fill_Out.class);
                startActivity(goFill);
                Tix_Information.this.finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        seats_ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){
                    vip.setText(String.format("%s seats left", Objects.requireNonNull(value.get("VIP")).toString()));
                    lb.setText(String.format("%s seats left", Objects.requireNonNull(value.get("LB")).toString()));
                    upb.setText(String.format("%s seats left", Objects.requireNonNull(value.get("UPB")).toString()));
                }
            }
        });
    }
}