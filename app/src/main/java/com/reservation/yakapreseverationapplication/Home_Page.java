package com.reservation.yakapreseverationapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Home_Page extends AppCompatActivity {

    //UI ELEMENTS HERE
    ImageView imahe, expanded;
    Button buy;
    ConstraintLayout out;
    ProgressDialog build;
    TextView yt, fb, ig;

    //variables
    String ytLink = "https://www.youtube.com/@MygzMolinovlogs";
    String igLink = "https://www.instagram.com/mygz.molino/";
    String fbLink = "https://www.facebook.com/mygz.molinoii";
    int [] imageResources = { R.drawable.mygz, R.drawable.boobsie, R.drawable.ethel,R.drawable.gay, R.drawable.keanna, R.drawable.gabriela,
            R.drawable.jon,  R.drawable.rowel};
    int currentIndex = 0;
    Handler hawak = new Handler();

    //API ELEMENTS HERE
    //Google related
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    GoogleSignInAccount account;

    //Firebase Related
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);


        //Initialize your UI elements here
        imahe = findViewById(R.id.poster);
        buy = findViewById(R.id.bili);
        out = findViewById(R.id.out_back);
        expanded = findViewById(R.id.zoomed_out);
        build  = new ProgressDialog(this);
        yt = findViewById(R.id.mygz_yt);
        fb = findViewById(R.id.mygz_fb);
        ig = findViewById(R.id.mygz_ig);

        //Initialize your API elements
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestIdToken("966287741955-eh20lshb893btpu0u8o7nkm7ctatj6qs.apps.googleusercontent.com").build();
        gsc = GoogleSignIn.getClient(Home_Page.this,gso);

        //Firebase-related
        user = FirebaseAuth.getInstance().getCurrentUser();

        //BELOW IS WHERE THE MAGIC HAPPENS

        //the slideshow master
        hawak.postDelayed(new Runnable() {
            private boolean fadeIn = true;
            private static final long TRANSITION_DURATION = 100;

            @Override
            public void run() {
                if (fadeIn) {
                    imahe.setImageResource(imageResources[currentIndex]);
                    imahe.animate().alpha(1).setDuration(TRANSITION_DURATION).start();
                } else {
                    imahe.animate().alpha(0).setDuration(TRANSITION_DURATION).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            currentIndex = (currentIndex + 1) % imageResources.length;
                            imahe.setImageResource(imageResources[currentIndex]);
                            imahe.animate().alpha(1).setDuration(TRANSITION_DURATION).start();
                        }
                    });
                }

                fadeIn = !fadeIn;
                hawak.postDelayed(this, TRANSITION_DURATION + 1500);
            }
        }, 0);

        //When the user clicks on the buy tickets button
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signIn = gsc.getSignInIntent();
                startActivityForResult(signIn, 100);
            }
        });

        //when the user clicks on the Youtube ni Mygz
        yt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goYoutube = new Intent(Intent.ACTION_VIEW, Uri.parse(ytLink));
                startActivity(goYoutube);
            }
        });

        //when the user clicks on the Facebook ni Mygz
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goFacebook = new Intent(Intent.ACTION_VIEW, Uri.parse(fbLink));
                startActivity(goFacebook);
            }
        });

        //when the user clicks on the Instagram of Mygz
        ig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goInsta = new Intent(Intent.ACTION_VIEW, Uri.parse(igLink));
                startActivity(goInsta);
            }
        });
    }

    //below is responsible for google sign in
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100){
            Task<GoogleSignInAccount> taskAccount = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInInfo(taskAccount);
        }
    }

    //taking the creds from Google
    private void handleSignInInfo(Task<GoogleSignInAccount> taskAccount) {
        try{
            GoogleSignInAccount taken_account = taskAccount.getResult(ApiException.class);
            if(taken_account != null){
                AuthCredential creds = GoogleAuthProvider.getCredential(taken_account.getIdToken(),null);
                FirebaseUsingGoogle(creds);
            }
        }
        catch (ApiException e){
            Toast.makeText(this, "Exception occurred due to: \n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //a method to use the credential borrowed from Google to put in our firebase auth
    private void FirebaseUsingGoogle(AuthCredential creds) {
        build.setTitle("Signing in to Firebase");
        build.setMessage("Using your Google Authentication to sign in to Firebase");
        build.show();
        FirebaseAuth.getInstance().signInWithCredential(creds).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Intent goNext = new Intent(Home_Page.this, Tix_Information.class);
                startActivity(goNext);
                build.dismiss();
                Home_Page.this.finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                build.dismiss();
                Toast.makeText(Home_Page.this, "Your request cannot be completed due to: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(user != null){
            SharedPreferences getShared = getSharedPreferences("User Data", MODE_PRIVATE);
            if(getShared.getBoolean("Uploaded", false)){
                Intent goNext = new Intent(Home_Page.this, Tix_Information.class);
                startActivity(goNext);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hawak.removeCallbacksAndMessages(null);
    }
}