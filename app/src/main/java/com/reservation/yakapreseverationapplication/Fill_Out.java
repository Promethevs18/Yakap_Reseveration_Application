package com.reservation.yakapreseverationapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Fill_Out extends AppCompatActivity {

    //UI ELEMENTS INSTANTIATION
    TextInputLayout nameLayout, addressLayout, phoneLayout, emailLayout, transactionLayout, othersLayout;
    TextInputEditText fullName, address, phoneNum, email, transactionNum, methods;
    EditText ticketsNum;
    Button submit, attach, pwdID, totalPrice;
    RadioGroup pwd, paymentMethod;
    Spinner seatCat;


    //Firebase Related Stuff
    StorageReference store;
    FirebaseUser user;
    DocumentReference seats, seatPrice;
    CollectionReference info;

    //Data containers
    HashMap<String, Object> mapa;
    List<String> seatClasses;

    //Local Variables
    String pwdOption = "", sectionSelected = "", receiptLink = "", pwdIDLink = "", which = "", seatCount = "";
    double seat_price, totalCost, pwdDiscount, discountedCost;
    Uri filePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_out);
        try{
        //SET UI ELEMENTS
        //TextInputLayouts
        nameLayout = findViewById(R.id.nameInputLayout);
        addressLayout = findViewById(R.id.addressInputLayout);
        phoneLayout = findViewById(R.id.phoneInputLayout);
        emailLayout = findViewById(R.id.emailInputLayout);
        transactionLayout = findViewById(R.id.transactInputLayout);
        othersLayout = findViewById(R.id.othersInputLayout);
        totalPrice = findViewById(R.id.total_price);

        //TextInputEditTexts
        fullName = findViewById(R.id.full_name);
        address = findViewById(R.id.address);
        phoneNum = findViewById(R.id.phone_num);
        email = findViewById(R.id.email);
        transactionNum = findViewById(R.id.transactionNum);
        methods = findViewById(R.id.other_options);

        //Button
        submit = findViewById(R.id.submit);
        attach = findViewById(R.id.attachResibo);
        pwdID = findViewById(R.id.upload_pwd_id);

        //RadioGroups
        pwd = findViewById(R.id.pwd);
        paymentMethod = findViewById(R.id.payments);

        //EditText
        ticketsNum = findViewById(R.id.tickets);

        //Spinner
        seatCat = findViewById(R.id.sections);

        //Firebase Related
        seats = FirebaseFirestore.getInstance().collection("Seat Variations").document("Seat Classes");
        seatPrice = FirebaseFirestore.getInstance().collection("Seat Variations").document("Seat Prices");
        store = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();


        //BELOW IS WHERE THE MAGIC HAPPENS

        //Radio Button for the pwd
        pwd.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.Yes){
                    pwdOption = "Yes";
                    pwdID.setVisibility(View.VISIBLE);
                }
                else {
                    pwdOption = "No";
                    pwdID.setVisibility(View.GONE);
                }
            }
        });
        seats.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //using the array to populate
                seatClasses = new ArrayList<>();
                //add all data taken from the document into the array
                seatClasses.addAll(Objects.requireNonNull(documentSnapshot.getData()).keySet());
                //setting an adapter for the spinner
                ArrayAdapter<String> saksakan = new ArrayAdapter<>(Fill_Out.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, seatClasses);
                //assign the dropdown style for the adapter
                saksakan.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
                //set the spinner's adapter to the custom one
                seatCat.setAdapter(saksakan);
            }
        });

        //This is for the spinner selection
        seatCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sectionSelected = parent.getItemAtPosition(position).toString();
                seats.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            seatCount = documentSnapshot.get(sectionSelected).toString();
                        }
                    }
                });

                seatPrice.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            seat_price = Double.parseDouble(documentSnapshot.get(sectionSelected).toString());
                        }
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sectionSelected = "";
            }
        });

        //For the payment buttons
        paymentMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.Gcash){
                    othersLayout.setVisibility(View.GONE);
                    methods.setText("Gcash");
                } else if (checkedId == R.id.Maya) {
                    othersLayout.setVisibility(View.GONE);
                    methods.setText("Maya");
                } else if (checkedId == R.id.Others) {
                    othersLayout.setVisibility(View.VISIBLE);
                    methods.setText("");
                }
            }
        });

        //When the user clicks on the attach receipt
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goPick = new Intent();
                goPick.setType("image/*");
                goPick.setAction(Intent.ACTION_GET_CONTENT);
                which = "receipt";
                startActivityForResult(Intent.createChooser(goPick, "Select receipt"), 22);
            }
        });

        //When the user clicks on the pwd id
        pwdID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goPick = new Intent();
                goPick.setType("image/*");
                goPick.setAction(Intent.ACTION_GET_CONTENT);
                which = "pwdID";
                startActivityForResult(Intent.createChooser(goPick, "Select receipt"), 22);
            }
        });

        //When the user clicks on the submit button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Objects.requireNonNull(fullName.getText()).toString().isEmpty() && Objects.requireNonNull(address.getText()).toString().isEmpty() && 
                        Objects.requireNonNull(phoneNum.getText()).toString().isEmpty() && Objects.requireNonNull(email.getText()).toString().isEmpty() && 
                        ticketsNum.getText().toString().isEmpty() && Objects.requireNonNull(transactionNum.getText()).toString().isEmpty() && TextUtils.isEmpty(ticketsNum.getText().toString())
                        && Objects.equals(seatCount, ""))
                {
                    nameLayout.setError("Name is required");
                    addressLayout.setError("Address is required");
                    phoneLayout.setError("Phone number is required");
                    emailLayout.setError("Email is required");
                    ticketsNum.setError("Number of tickets is required");
                    transactionLayout.setError("Transaction number is required");
                    ticketsNum.setError("Ticket number is required and select a seating");
                    Toast.makeText(Fill_Out.this, "Some information is lacking. Kindly review your form", Toast.LENGTH_SHORT).show();
                   
                }
                else{
                    if (TextUtils.isEmpty(ticketsNum.getText().toString())) {
                        ticketsNum.setError("Ticket number is required and select a seating");
                        Toast.makeText(Fill_Out.this, "Number of tickets is required", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        int numTickets = Integer.parseInt(ticketsNum.getText().toString());
                        if (numTickets > Integer.parseInt(seatCount)) {
                            AlertDialog.Builder tooMuch = new AlertDialog.Builder(Fill_Out.this);
                            tooMuch.setTitle("Number of tickets exceeds");
                            tooMuch.setMessage("Your number of tickets exceeds the seats left for this section. Try to lower your purchase or try a different section");
                            tooMuch.setIcon(R.drawable.warning);
                            tooMuch.setCancelable(true);
                            tooMuch.show();
                        }
                        else{
                            ProgressDialog pd = new ProgressDialog(Fill_Out.this);
                            pd.setTitle("Uploading information");
                            pd.setMessage("Sending your secret information to our secret database...");
                            pd.show();

                            int ticketsLeft =  Integer.parseInt(seatCount) - Integer.parseInt( ticketsNum.getText().toString());
                            seats.update(sectionSelected, ticketsLeft);

                            HashMap<String, Object> infoMap = new HashMap<>();

                            infoMap.put("fullName", fullName.getText().toString());
                            infoMap.put("address", address.getText().toString());
                            infoMap.put("phoneNum", phoneNum.getText().toString());
                            infoMap.put("email", email.getText().toString());
                            infoMap.put("pwd", pwdOption);
                            infoMap.put("section", sectionSelected);
                            infoMap.put("ticketsBought", ticketsNum.getText().toString());
                            infoMap.put("paymentMode", Objects.requireNonNull(methods.getText()).toString());
                            infoMap.put("transactNo", transactionNum.getText().toString());
                            infoMap.put("receiptLink", receiptLink);
                            infoMap.put("pwdIdLink", pwdIDLink);
                            infoMap.put("totalPrice", totalCost);


                            info = FirebaseFirestore.getInstance().collection(sectionSelected);
                            info.document(fullName.getText().toString()).set(infoMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    writeToSharedPreferences(fullName.getText().toString(), sectionSelected);
                                    pd.dismiss();
                                    AlertDialog.Builder notif = new AlertDialog.Builder(Fill_Out.this);
                                    notif.setTitle("Information Saved!");
                                    notif.setMessage("Your information has been saved to our database. Our team will look into your data submitted for verification\nClick on the button to close this message");
                                    notif.setIcon(R.drawable.approved);
                                    notif.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent goFinal = new Intent(Fill_Out.this, Final_Page.class);
                                            goFinal.putExtra("name", fullName.getText().toString());
                                            startActivity(goFinal);
                                            Fill_Out.this.finish();
                                            dialog.dismiss();
                                        }
                                    });
                                    notif.show();
                                }
                            });
                        }
                    }

                }

            }
        });

        //When the user clicks on the show total cost button
        totalPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder showPrice = new AlertDialog.Builder(Fill_Out.this);
                if(pwdIDLink.isEmpty() || pwdIDLink.contentEquals("")){
                    if(TextUtils.isEmpty(ticketsNum.getText().toString())){
                        Toast.makeText(Fill_Out.this, "Ticket quantity invalid", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        totalCost = seat_price * Double.parseDouble(ticketsNum.getText().toString());
                        showPrice.setTitle("Total cost of tickets");
                        showPrice.setMessage("Your ticket breakdown is:\n\t"
                                + sectionSelected + " = " + seat_price + " pesos"
                                + "\n\tNumber of tickets acquired: "+ ticketsNum.getText().toString()
                                + "\n\tTotal cost = " + totalCost +"\n\n"
                                + "Click anywhere to close this window");
                        showPrice.setCancelable(true);
                        showPrice.show();
                    }
                }
                else {
                    if (TextUtils.isEmpty(ticketsNum.getText().toString())) {
                        Toast.makeText(Fill_Out.this, "Ticket quantity invalid", Toast.LENGTH_SHORT).show();
                    } else {
                        discountedCost = (seat_price * Double.parseDouble(ticketsNum.getText().toString())) * pwdDiscount;
                        totalCost = (seat_price * Double.parseDouble(ticketsNum.getText().toString())) - discountedCost;
                        showPrice.setTitle("Total cost of tickets");
                        showPrice.setMessage("Your ticket breakdown is:\n\t"
                                + sectionSelected + " = " + seat_price + " pesos"
                                + "\n\tNumber of tickets acquired: " + ticketsNum.getText().toString()
                                + "\n\tPWD Discount cost = " + discountedCost
                                + "\n\tTotal cost = " + totalCost
                                + "\n\n"
                                + "Click anywhere to close this window");
                        showPrice.setCancelable(true);
                        showPrice.show();
                    }
                }
            }
        });
        }
        catch (Exception e){
            Toast.makeText(this, "Exception nangyari", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeToSharedPreferences(String name, String sectionSelected) {
        DocumentReference dataPath = FirebaseFirestore.getInstance().collection(sectionSelected).document(name);
        SharedPreferences saveToSys = getSharedPreferences("User Data", MODE_PRIVATE);
        SharedPreferences.Editor editLaman = saveToSys.edit();
        editLaman.putString("Firestore Path", dataPath.getPath());
        editLaman.putBoolean("Uploaded", true);
        editLaman.apply();

    }

    //An override method to let the user pick an image, and the system will determine which pic belongs to a category
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 22 && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
            try {
                Bitmap bitty = MediaStore.Images.Media.getBitmap(Fill_Out.this.getContentResolver(),filePath);
                ProgressDialog pd = new ProgressDialog(Fill_Out.this);
                pd.setTitle("Uploading image...");
                pd.show();

                store.child( Objects.requireNonNull(fullName.getText()).toString() +"/" + which + "/" + UUID.randomUUID().toString()).putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                pd.dismiss();
                                if(Objects.equals(which, "receipt")){
                                    receiptLink = task.getResult().toString();
                                }
                                else
                                {
                                    pwdIDLink = task.getResult().toString();
                                    pwdDiscount = 0.20;
                                }

                                Toast.makeText(Fill_Out.this, "Image Uploaded!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Fill_Out.this, "Exception occurred due to: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        pd.setMessage("Uploaded " + progress +"%");
                    }
                });
            }
            catch (IOException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}