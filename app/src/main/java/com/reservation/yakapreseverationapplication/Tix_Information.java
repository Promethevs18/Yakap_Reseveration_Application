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
    ImageView upuan, pinalaki;
    ConstraintLayout likod;
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
        pinalaki = findViewById(R.id.pinalaki);
        likod = findViewById(R.id.likod);

        vip = findViewById(R.id.vip_num);
        lb = findViewById(R.id.lb_num);
        upb = findViewById(R.id.upb_num);


        //Initialize Firestore elements
        seats_ref = FirebaseFirestore.getInstance().collection("Seat Variations").document("Seat Classes");
        upuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Start with the thumbnail's bounds.
                Rect startBounds = new Rect();
                upuan.getGlobalVisibleRect(startBounds);

                // Set up the enlarged image's bounds.
                Rect finalBounds = new Rect();
                finalBounds.right = pinalaki.getWidth();
                finalBounds.bottom = pinalaki.getHeight();

                // Set the start and final scale factors for the animation.
                float startScaleX = (float) startBounds.width() / finalBounds.width();
                float startScaleY = (float) startBounds.height() / finalBounds.height();

                // Create and start the zoom animation.
                AnimationSet set = new AnimationSet(true);
                set.addAnimation(new ScaleAnimation(startScaleX, 1, startScaleY, 1,
                        startBounds.exactCenterX(), startBounds.exactCenterY()));
                set.setDuration(200); // Adjust the duration as needed.
                pinalaki.startAnimation(set);

                // Make the enlarged image visible and remove the thumbnail.
                likod.setVisibility(View.VISIBLE);
                pinalaki.setVisibility(View.VISIBLE);
            }
        });

        pinalaki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start with the thumbnail's bounds.
                Rect startBounds = new Rect();
                upuan.getGlobalVisibleRect(startBounds);

                // Set up the enlarged image's bounds.
                Rect finalBounds = new Rect();
                finalBounds.right = pinalaki.getWidth();
                finalBounds.bottom = pinalaki.getHeight();

                // Set the start and final scale factors for the animation.
                float startScaleX = (float) startBounds.width() / finalBounds.width();
                float startScaleY = (float) startBounds.height() / finalBounds.height();

                // Create and start the reverse zoom animation.
                AnimationSet set = new AnimationSet(true);
                set.addAnimation(new ScaleAnimation(1, 1 / startScaleX, 1, 1 / startScaleY,
                        pinalaki.getWidth() >> 1, pinalaki.getHeight() >> 2));
                set.setDuration(200); // Adjust the duration as needed.
                pinalaki.startAnimation(set);

                // Make the thumbnail visible and remove the enlarged image.
                likod.setVisibility(View.GONE);
                pinalaki.setVisibility(View.GONE);
            }
        });

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