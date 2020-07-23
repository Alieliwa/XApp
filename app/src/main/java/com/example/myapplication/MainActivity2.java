package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class MainActivity2 extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_PIC_REQUEST = 2;
    ProgressBar progress;
    ImageView profile;
    EditText name,email,phone,address;
    Button add;
    private StorageReference mStorageRef;
    private DatabaseReference mdatabaseref;
    Uri mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        progress=findViewById(R.id.progress);
        profile = findViewById(R.id.profile);
        name =findViewById(R.id.name);
        email=findViewById(R.id.email);
        phone=findViewById(R.id.phone);
        address=findViewById(R.id.adress);
        add=findViewById(R.id.add);
        mStorageRef = FirebaseStorage.getInstance().getReference("profile image");
        mdatabaseref = FirebaseDatabase.getInstance().getReference("profile image");
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder pic=new AlertDialog.Builder(MainActivity2.this);
                pic.setMessage("Choose Method to get picture ");
                pic.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
                    }
                }).setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST);
                    }
                });
                pic.create();
                pic.show();

                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadfile();

                    }
                });
            }
        });
        }



    private String getfileExtention(Uri uri){
        ContentResolver cr= getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadfile() {
        if (profile !=null){
            StorageReference filerefrence = mStorageRef.child("profile image/"+System.currentTimeMillis()
            +"."  +getfileExtention(profile));
            filerefrence.putFile(profile)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(MainActivity2.this, "Upload succesful", Toast.LENGTH_SHORT).show();
                            String uploadid=mdatabaseref.push().getKey();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double mprogress=(100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progress.setProgress((int)mprogress);
                        }
                    });
        }else {
            Toast.makeText(this, "NO image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getfileExtention(ImageView profile) {
    }

    public void onActivityResult ( int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (CAMERA_PIC_REQUEST):
                if (resultCode == Activity.RESULT_OK) {
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    profile.setImageBitmap(image);
                }
                break;
            case PICK_IMAGE_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Uri filepath = data.getData();
                    Bitmap ss = null;
                    try {
                        ss = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), filepath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    profile.setImageBitmap(ss);
                }
                break;
        }
    }
       }