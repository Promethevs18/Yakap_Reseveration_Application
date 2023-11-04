package com.reservation.yakapreseverationapplication;

import static android.os.Build.VERSION.SDK_INT;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;

public class Final_Page extends AppCompatActivity {


    //UI Instantiation
    Button goHome, saveSS;
    ImageView qr;

    //Local variables
    SharedPreferences getShared;
    Intent get;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_page);

        //UI Initialization
        qr = findViewById(R.id.qrCode);
        goHome = findViewById(R.id.goBack);
        saveSS = findViewById(R.id.screenShot);

        //Local variables initialization
        getShared = getSharedPreferences("User Data", MODE_PRIVATE);
        get = getIntent();



        //BELOW IS WHERE THE MAGIC BEGINS

        //display the qr from SharedPreference

        if(getShared != null ){
            try{
                //call on the MultiFormatWriter for the conversion of text into a specific code
                MultiFormatWriter write = new MultiFormatWriter();

                //call the matrix to arrange the format of your custom code
                BitMatrix matrix = write.encode("Yakap Admin app can only decode the values of - "+ getShared.getString("Firestore Path", "Undefined"), BarcodeFormat.QR_CODE, 300,300);

                //Contrary to the name, the BarcodeEncoder allows you to create multiple code formats. Call it first
                BarcodeEncoder encoder = new BarcodeEncoder();

                //Use the Bitmap since we are going to get the image equivalent and place it in our imageView.
                // Pass the matrix to the BarcodeEncoder createBitmap() method
                Bitmap bitmap = encoder.createBitmap(matrix);

                //Use the bitmap as the image source of the imageView
                qr.setImageBitmap(bitmap);

            } catch (WriterException e) {
                Toast.makeText(this, "Error occurred due to: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            qr.setImageResource(R.drawable.yakap_1);
        }

        //When the user clicks on the return to main menu button
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goHome = new Intent(Final_Page.this, Tix_Information.class);
                startActivity(goHome);
                Final_Page.this.finish();
            }
        });

        //When the user saves a screenshot of the entire frame
        saveSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        try{
                            View view = getWindow().getDecorView().getRootView();
                            String path = Environment.getExternalStorageDirectory() + "";
                            File file = new File(path);

                            if(!file.exists()){
                                boolean mkDir = file.mkdir();
                            }

                            String filePath = path + "/" + get.getStringExtra("name")+ "-ticket screenshot" + ".png";
                            view.setDrawingCacheEnabled(true);
                            Bitmap mapa = Bitmap.createBitmap(view.getDrawingCache());
                            view.setDrawingCacheEnabled(false);

                            File imageUrl = new File(filePath);
                            FileOutputStream outputStream = new FileOutputStream(imageUrl);
                            mapa.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
                            outputStream.flush();
                            outputStream.close();

                            AlertDialog.Builder fileGood = new AlertDialog.Builder(Final_Page.this);
                            fileGood.setTitle("Screenshot Saved!");
                            fileGood.setMessage("A screenshot of this window has been saved to your device. That will serve as your copy");
                            fileGood.setCancelable(true);
                            fileGood.show();
                        }
                        catch (Exception e){
                            Toast.makeText(Final_Page.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                } else {

                    //asking for permission to write to storage
                    ActivityCompat.requestPermissions(Final_Page.this, new String[]{
                            android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, PackageManager.PERMISSION_GRANTED);
                }


            }
        });
    }
}